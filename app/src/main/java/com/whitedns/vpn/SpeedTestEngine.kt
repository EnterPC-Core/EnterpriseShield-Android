package com.whitedns.vpn

import kotlinx.coroutines.*
import java.io.InputStream
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URL
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.roundToLong

/**
 * Спид-тест Щита через ядро (SOCKS5 127.0.0.1:socksPort).
 * Методика (Cloudflare): адаптивное расписание параллельных загрузок/отдач,
 * замер unloaded/loaded latency -> bufferbloat.
 */
class SpeedTestEngine(private val socksPort: Int?) {

    data class State(
        val phase: Phase = Phase.Idle,
        val downMbps: Double = 0.0,
        val upMbps: Double = 0.0,
        val idlePingMs: Long = 0,
        val loadedPingMs: Long = 0,
        val transferredMb: Double = 0.0
    ) {
        val bufferbloatMs: Long get() = (loadedPingMs - idlePingMs).coerceAtLeast(0)
    }

    enum class Phase { Idle, Warmup, Download, Upload, Done, Failed }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val stopped = AtomicBoolean(false)
    var onState: ((State) -> Unit)? = null
    @Volatile private var state = State()
        set(v) { field = v; onState?.invoke(v) }

    fun socksProxy(): Proxy? = socksPort?.let {
        Proxy(Proxy.Type.SOCKS, InetSocketAddress("127.0.0.1", it))
    }

    private fun push(phase: Phase = state.phase, down: Double = state.downMbps,
                     up: Double = state.upMbps, idle: Long = state.idlePingMs,
                     loaded: Long = state.loadedPingMs, mb: Double = state.transferredMb) {
        state = State(phase, down, up, idle, loaded, mb)
    }

    fun stop() { stopped.set(true) }

    fun start() {
        stopped.set(false)
        scope.launch {
            push(Phase.Warmup)
            // unloaded latency
            var idle = 0L
            repeat(4) { idle = maxOf(idle, ping()) }
            if (stopped.get()) { push(Phase.Idle); return@launch }
            push(Phase.Warmup, idle = idle)

            // === DOWNLOAD ===
            push(Phase.Download, idle = idle)
            val downBytes = arrayOf(100_000L, 1_000_000L, 10_000_000L, 25_000_000L)
            val downThreads = intArrayOf(1, 4, 6, 6)
            var totalDown = 0.0
            var samplesDown = mutableListOf<Double>()
            for (i in downBytes.indices) {
                if (stopped.get()) break
                val res = parallelRound(isDown = true, bytes = downBytes[i], threads = downThreads[i])
                totalDown += res.first
                if (res.second > 0) samplesDown.add(res.second)
                if (samplesDown.isNotEmpty()) {
                    val best = samplesDown.max()
                    push(Phase.Download, down = best, idle = idle, mb = totalDown)
                }
            }

            // === UPLOAD ===
            if (!stopped.get()) {
                push(Phase.Upload, idle = idle, mb = totalDown)
                var totalUp = 0.0
                val samplesUp = mutableListOf<Double>()
                val upBytes = arrayOf(100_000L, 1_000_000L, 5_000_000L)
                val upThreads = intArrayOf(1, 4, 4)
                for (i in upBytes.indices) {
                    if (stopped.get()) break
                    val res = parallelRound(isDown = false, bytes = upBytes[i], threads = upThreads[i])
                    totalUp += res.first
                    if (res.second > 0) samplesUp.add(res.second)
                    if (samplesUp.isNotEmpty()) push(Phase.Upload, down = state.downMbps, up = samplesUp.max(), idle = idle, mb = totalDown + totalUp)
                }
            }

            // === loaded latency during last download was tracked separately ===
            push(if (stopped.get()) Phase.Idle else Phase.Done,
                 down = state.downMbps, up = state.upMbps, idle = idle, loaded = state.loadedPingMs, mb = state.transferredMb)
        }
    }

    /** Одна «волна»: threads параллельных запросов; возвращает (МБ, Мбит/с). */
    private fun parallelRound(isDown: Boolean, bytes: Long, threads: Int): Pair<Double, Double> {
        val start = System.nanoTime()
        var total = 0L
        runBlocking {
            val jobs = (0 until threads).map {
                launch(Dispatchers.IO) {
                    total += if (isDown) downloadChunk(bytes) else uploadChunk(bytes)
                    if (isDown && it == 0) {
                        // loaded latency: меряем на фоне параллельной загрузки
                        val loaded = ping()
                        if (loaded > 0) push(state.phase, loaded = loaded)
                    }
                }
            }
            jobs.joinAll()
        }
        val secs = (System.nanoTime() - start) / 1e9
        val mb = total / 1048576.0
        val mbps = if (secs > 0) (total * 8.0 / 1e6 / secs) else 0.0
        return mb to mbps
    }

    private fun openStream(url: String): InputStream {
        val conn = URL(url).let { u ->
            socksProxy()?.let { p -> u.openConnection(p) } ?: u.openConnection()
        }
        conn.connectTimeout = 8000
        conn.readTimeout = 15000
        return conn.getInputStream()
    }

    private fun downloadChunk(bytes: Long): Long {
        return runCatching {
            openStream("https://speed.cloudflare.com/__down?bytes=$bytes").use { ins ->
                val buf = ByteArray(64 * 1024); var n: Int; var t = 0L
                while (ins.read(buf).also { n = it } >= 0 && !stopped.get()) { t += n }
                t
            }
        }.getOrDefault(0L)
    }

    private fun uploadChunk(bytes: Long): Long {
        return runCatching {
            val conn = (URL("https://speed.cloudflare.com/__up").let { u ->
                socksProxy()?.let { p -> u.openConnection(p) } ?: u.openConnection()
            }) as java.net.HttpURLConnection
            conn.connectTimeout = 8000; conn.readTimeout = 15000
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/octet-stream")
            conn.setFixedLengthStreamingMode(bytes.toInt())
            val payload = ByteArray(64 * 1024) { 'E'.code.toByte() }
            conn.outputStream.use { outs ->
                var sent = 0L
                while (sent < bytes && !stopped.get()) {
                    val chunk = minOf(payload.size.toLong(), bytes - sent).toInt()
                    outs.write(payload, 0, chunk); sent += chunk
                }
                outs.flush()
            }
            conn.inputStream.use { it.read(ByteArray(256)) }
            bytes
        }.getOrDefault(0L)
    }

    private fun ping(): Long {
        return runCatching {
            val t0 = System.nanoTime()
            openStream("https://speed.cloudflare.com/__down?bytes=0").use { it.read(ByteArray(128)) }
            ((System.nanoTime() - t0) / 1e6).roundToLong()
        }.getOrDefault(0L)
    }
}
