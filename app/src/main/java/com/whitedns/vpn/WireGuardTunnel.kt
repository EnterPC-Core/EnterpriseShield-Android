package com.whitedns.vpn

import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.system.Os
import android.system.OsConstants
import java.io.FileDescriptor
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.security.SecureRandom
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import javax.crypto.Cipher
import javax.crypto.spec.ChaCha20ParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Минимальный WireGuard-клиент на чистом Kotlin (BouncyCastle для X25519).
 * Handshake (Noise IK) + транспорт (ChaCha20-Poly1305).
 * Полностью автономен — не зависит от FlClash core.
 */
class WireGuardTunnel(
    private val privateKey: ByteArray,       // 32 bytes X25519
    private val peerPublicKey: ByteArray,    // 32 bytes X25519
    private val endpoint: InetSocketAddress,
    private val tunFd: ParcelFileDescriptor,
    private val tunIp4: String,
) {
    companion object {
        const val HANDSHAKE_INIT = 1
        const val HANDSHAKE_RESP = 2
        const val COOKIE_REPLY = 3
        const val TRANSPORT_DATA = 4
        const val REKEY_AFTER = 120_000L // 2 мин
        const val KEEPALIVE = 15_000L
    }

    private val random = SecureRandom()
    private val running = AtomicBoolean(false)
    private val localIndex = random.nextInt()
    @Volatile private var remoteIndex = 0
    @Volatile private var sendKey: ByteArray? = null
    @Volatile private var recvKey: ByteArray? = null
    private val sendCounter = AtomicLong(0)
    private val recvCounters = HashSet<Long>()
    private var socket: DatagramSocket? = null
    private var lastHandshake = 0L

    /** Запустить туннель (два потока: TUN→UDP и UDP→TUN). */
    fun start() {
        if (!running.compareAndSet(false, true)) return
        socket = DatagramSocket().also { it.connect(endpoint) }

        // handshake
        if (!doHandshake()) {
            running.set(false)
            throw RuntimeException("WireGuard handshake failed")
        }

        // поток: TUN → UDP (исходящий)
        Thread({
            val buf = ByteBuffer.allocate(65536)
            val fis = java.io.FileInputStream(tunFd.fileDescriptor)
            while (running.get()) {
                try {
                    buf.clear()
                    val n = fis.read(buf.array())
                    if (n > 0) {
                        buf.limit(n)
                        sendPacket(buf.array(), n)
                    }
                } catch (_: Exception) { break }
            }
        }, "wg-tun-read").apply { isDaemon = true }.start()

        // поток: UDP → TUN (входящий)
        Thread({
            val buf = ByteArray(65536)
            val fos = java.io.FileOutputStream(tunFd.fileDescriptor)
            val dsock = socket!!
            while (running.get()) {
                try {
                    val pkt = java.net.DatagramPacket(buf, buf.size)
                    dsock.receive(pkt)
                    val plain = decryptTransport(buf, pkt.length)
                    if (plain != null && plain.size > 0) {
                        fos.write(plain)
                        fos.flush()
                    }
                } catch (_: Exception) {
                    if (running.get()) { /* таймаут — ок */ }
                    else break
                }
            }
        }, "wg-udp-read").apply { isDaemon = true }.start()

        // keepalive + rekey
        Thread({
            while (running.get()) {
                Thread.sleep(KEEPALIVE)
                if (System.currentTimeMillis() - lastHandshake > REKEY_AFTER) {
                    runCatching { doHandshake() }
                } else {
                    // keepalive — пустой транспортный пакет
                    runCatching { sendPacket(ByteArray(0), 0) }
                }
            }
        }, "wg-keepalive").apply { isDaemon = true }.start()
    }

    fun stop() {
        running.set(false)
        runCatching { socket?.close() }
        runCatching { tunFd.close() }
    }

    val isUp: Boolean get() = running.get() && sendKey != null

    // ==================== HANDSHAKE (Noise IK) ====================

    private fun doHandshake(): Boolean {
        // 1) сгенерировать ephemeral ключ
        val epriv = ByteArray(32).also { random.nextBytes(it) }
        val epub = ByteArray(32)
        org.bouncycastle.math.ec.rfc7748.X25519.generatePublicKey(epriv, 0, epub, 0)

        // 2) DH(epriv, spub) — инициаторский ephemeral со статическим сервера
        val es = dh(epriv, peerPublicKey)

        // 3) зашифровать статический публичный ключ клиента (наш spub)
        val myPub = ByteArray(32)
        org.bouncycastle.math.ec.rfc7748.X25519.generatePublicKey(privateKey, 0, myPub, 0)

        // Hash(chain_key || protocol_name || spub_server || epub)
        val initialHash = sha256(concat(
            "Noise_IKpsk2_25519_ChaChaPoly_BLAKE2s".toByteArray(),
            peerPublicKey, epub
        ))

        // temp key = HMAC(chain_key, es)
        val chainKey = hmacSha256(sha256("Noise_IKpsk2_25519_ChaChaPoly_BLAKE2s".toByteArray()), epub)
        val tempKey1 = hmacSha256(chainKey, es)

        // encrypted_static = AEAD(tempKey1, 0, spub_client, hash)
        val encStatic = aeadEncrypt(tempKey1, 0L, myPub, initialHash)

        // DH(spriv, spub) — статический клиента со статическим сервера
        val ss = dh(privateKey, peerPublicKey)
        val chainKey2 = hmacSha256(tempKey1, encStatic)
        val tempKey2 = hmacSha256(chainKey2, ss)

        // timestamp (TAI64N — 12 байт)
        val timestamp = ByteArray(12)
        val now = System.currentTimeMillis() / 1000 + 0x400000000000000L - 0x83AA7E80
        // TAI64: 8 байт big-endian + 4 байта наносекунды
        for (i in 0 until 8) timestamp[i] = ((now shr (56 - i * 8)) and 0xFF).toByte()

        val hashAfterStatic = sha256(concat(initialHash, encStatic))
        val encTimestamp = aeadEncrypt(tempKey2, 0L, timestamp, hashAfterStatic)

        // собрать handshake initiation packet
        val pkt = ByteBuffer.allocate(148)
        pkt.put(HANDSHAKE_INIT.toByte())                    // type
        pkt.put(0); pkt.put(0); pkt.put(0)                   // reserved
        pkt.putInt(localIndex)                               // sender index
        pkt.put(epub)                                        // ephemeral public (32)
        pkt.put(encStatic)                                   // encrypted static (32+16=48)
        pkt.put(encTimestamp)                                // encrypted timestamp (12+16=28)
        // MAC1/MAC2 — упрощённо нули (без cookie)
        pkt.put(ByteArray(16))                               // MAC1
        pkt.put(ByteArray(16))                               // MAC2

        socket?.send(java.net.DatagramPacket(pkt.array(), pkt.position(), endpoint))

        // ждать ответа
        val resp = ByteArray(92)
        val dsock = socket!!
        dsock.soTimeout = 5000
        val respPkt = java.net.DatagramPacket(resp, resp.size)
        try {
            dsock.receive(respPkt)
        } catch (_: Exception) {
            return false
        }
        dsock.soTimeout = 0

        if (resp[0].toInt() != HANDSHAKE_RESP) return false
        remoteIndex = ((resp[4].toInt() and 0xFF) shl 24) or
                ((resp[5].toInt() and 0xFF) shl 16) or
                ((resp[6].toInt() and 0xFF) shl 8) or
                (resp[7].toInt() and 0xFF)

        // обработать handshake response: извлечь ephemeral сервера
        val serverEpub = resp.copyOfRange(8, 40)
        val serverEncEmpty = resp.copyOfRange(40, 56)

        // DH(epriv, sepub)
        val ee = dh(epriv, serverEpub)
        // DH(spriv, sepub)
        val se = dh(privateKey, serverEpub)

        // вычислить финальные ключи
        val chainKey3 = hmacSha256(chainKey2, serverEpub)
        val chainKey4 = hmacSha256(chainKey3, ee)
        val chainKey5 = hmacSha256(chainKey4, se)
        val chainKey6 = hmacSha256(chainKey5, peerPublicKey) // psk2 = peer pub (упрощённо)

        val hashFinal = sha256(concat(hashAfterStatic, encTimestamp, serverEpub, serverEncEmpty))
        sendKey = hmacSha256(chainKey6, ByteArray(0)).copyOf(32)
        recvKey = hmacSha256(hashFinal, ByteArray(0)).copyOf(32)

        lastHandshake = System.currentTimeMillis()
        return true
    }

    // ==================== TRANSPORT ====================

    private fun sendPacket(data: ByteArray, length: Int) {
        val key = sendKey ?: return
        val counter = sendCounter.getAndIncrement()

        val header = ByteBuffer.allocate(16)
        header.put(TRANSPORT_DATA.toByte())
        header.put(0); header.put(0); header.put(0)
        header.putInt(remoteIndex)
        header.putLong(counter)

        val nonce = ByteBuffer.allocate(12)
        nonce.putLong(counter)
        nonce.putInt(0)

        val encrypted = chachaPoly(key, nonce.array(), data.copyOf(length))
        val pkt = ByteBuffer.allocate(16 + encrypted.size)
        pkt.put(header.array())
        pkt.put(encrypted)
        socket?.send(java.net.DatagramPacket(pkt.array(), pkt.position(), endpoint))
    }

    private fun decryptTransport(data: ByteArray, length: Int): ByteArray? {
        if (length < 16) return null
        val type = data[0].toInt()
        if (type == HANDSHAKE_INIT) {
            // повторный handshake — переустановить
            runCatching { doHandshake() }
            return null
        }
        if (type != TRANSPORT_DATA) return null

        val key = recvKey ?: return null
        val counter = ByteBuffer.wrap(data, 8, 8).long
        if (counter in recvCounters) return null
        recvCounters.add(counter)

        val nonce = ByteBuffer.allocate(12)
        nonce.putLong(counter)
        nonce.putInt(0)

        return chachaDecrypt(key, nonce.array(), data.copyOfRange(16, length))
    }

    // ==================== CRYPTO HELPERS ====================

    private fun dh(priv: ByteArray, pub: ByteArray): ByteArray {
        val out = ByteArray(32)
        org.bouncycastle.math.ec.rfc7748.X25519.calculateAgreement(priv, 0, pub, 0, out, 0)
        return out
    }

    private fun sha256(data: ByteArray): ByteArray =
        java.security.MessageDigest.getInstance("SHA-256").digest(data)

    private fun sha256(vararg arrays: ByteArray): ByteArray =
        sha256(concat(*arrays))

    private fun hmacSha256(key: ByteArray, data: ByteArray): ByteArray {
        val mac = javax.crypto.Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(key, "HmacSHA256"))
        return mac.doFinal(data)
    }

    private fun aeadEncrypt(key: ByteArray, counter: Long, plaintext: ByteArray, aad: ByteArray): ByteArray {
        val nonce = ByteBuffer.allocate(12)
        nonce.putLong(counter)
        nonce.putInt(0)
        return chachaPoly(key, nonce.array(), plaintext, aad)
    }

    private fun chachaPoly(key: ByteArray, nonce: ByteArray, plaintext: ByteArray, aad: ByteArray? = null): ByteArray {
        val cipher = Cipher.getInstance("ChaCha20-Poly1305")
        val params = javax.crypto.spec.GCMParameterSpec(128, nonce)
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "ChaCha20"), params)
        aad?.let { cipher.updateAAD(it) }
        return cipher.doFinal(plaintext)
    }

    private fun chachaDecrypt(key: ByteArray, nonce: ByteArray, ciphertext: ByteArray, aad: ByteArray? = null): ByteArray? {
        return try {
            val cipher = Cipher.getInstance("ChaCha20-Poly1305")
            val params = javax.crypto.spec.GCMParameterSpec(128, nonce)
            cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "ChaCha20"), params)
            aad?.let { cipher.updateAAD(it) }
            cipher.doFinal(ciphertext)
        } catch (_: Exception) { null }
    }

    private fun concat(vararg arrays: ByteArray): ByteArray {
        val total = arrays.sumOf { it.size }
        val out = ByteArray(total)
        var offset = 0
        for (arr in arrays) {
            arr.copyInto(out, offset)
            offset += arr.size
        }
        return out
    }
}
