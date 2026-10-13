package com.whitedns.vpn

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.SecureRandom

/**
 * Генерация Cloudflare WARP (WireGuard) конфигов на устройстве.
 * X25519 через BouncyCastle (bcprov-jdk18on).
 */
object WarpGenerator {

    private const val API = "https://api.cloudflareclient.com/v0a4005"
    private const val WARP_FILE = "warp_configs.json"

    data class WarpConfig(
        val name: String,
        val server: String,
        val port: Int,
        val privateKey: String,
        val publicKey: String,
        val ip4: String,
        val ip6: String,
    )

    fun toMihomoYaml(c: WarpConfig): String = buildString {
        appendLine("  - name: \"${c.name}\"")
        appendLine("    type: wireguard")
        appendLine("    server: ${c.server}")
        appendLine("    port: ${c.port}")
        appendLine("    ip: ${c.ip4}")
        appendLine("    ipv6: \"${c.ip6}\"")
        appendLine("    private-key: \"${c.privateKey}\"")
        appendLine("    public-key: \"${c.publicKey}\"")
        appendLine("    udp: true")
        appendLine("    mtu: 1280")
    }

    fun generate(context: Context, label: String, onResult: (WarpConfig?) -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            val result = runCatching { registerDevice(label) }
                .onFailure { e -> android.util.Log.e("WarpGen", "failed", e) }
                .getOrNull()
            if (result != null) saveConfig(context, result)
            launch(Dispatchers.Main) { onResult(result) }
        }
    }

    fun generateBatch(context: Context, count: Int, onProgress: (Int, Int) -> Unit, onDone: (List<WarpConfig>) -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            val all = mutableListOf<WarpConfig>()
            for (i in 1..count) {
                val latch = java.util.concurrent.CountDownLatch(1)
                var cfg: WarpConfig? = null
                generate(context, "🌐 WARP $i") { c -> cfg = c; latch.countDown() }
                latch.await(45, java.util.concurrent.TimeUnit.SECONDS)
                cfg?.let { all.add(it) }
                launch(Dispatchers.Main) { onProgress(i, count) }
                if (i < count) Thread.sleep(2500)
            }
            launch(Dispatchers.Main) { onDone(all.toList()) }
        }
    }

    private fun registerDevice(label: String): WarpConfig {
        val priv = ByteArray(32).also { SecureRandom().nextBytes(it) }
        priv[0] = (priv[0].toInt() and 248).toByte()
        priv[31] = ((priv[31].toInt() and 127) or 64).toByte()
        val pub = ByteArray(32)
        org.bouncycastle.math.ec.rfc7748.X25519.generatePublicKey(priv, 0, pub, 0)
        val pubB64 = b64(pub)
        val privB64 = b64(priv)

        val body = JSONObject().apply {
            put("f", "en en en"); put("install_id", ""); put("key", pubB64)
            put("locale", "en_US"); put("model", "PC")
            put("tos", "2024-01-01T00:00:00.000Z"); put("type", "Android")
            put("serial_number", hex(11))
        }
        val (regStr, _) = http("$API/reg", "POST", body.toString(), null)
        val reg = JSONObject(regStr)
        val token = reg.getString("token")
        val (actStr, _) = http("$API/reg/${reg.getString("id")}", "PATCH",
            JSONObject().put("warp_enabled", true).toString(), token)
        val activated = JSONObject(actStr)
        val peer = activated.getJSONObject("config").getJSONArray("peers").getJSONObject(0)
        val iface = activated.getJSONObject("config").getJSONObject("interface")
        val host = peer.getJSONObject("endpoint").optString("host", "engage.cloudflareclient.com:2408")
        val server = host.substringBefore(":").ifBlank { "engage.cloudflareclient.com" }
        val port = host.substringAfter(":", "2408").toIntOrNull()?.takeIf { it in 1..65535 } ?: 2408
        val addresses = iface.getJSONObject("addresses")
        return WarpConfig(label, server, port, privB64, peer.getString("public_key"),
            addresses.getString("v4"), addresses.getString("v6"))
    }

    private fun http(url: String, method: String, body: String?, token: String?): Pair<String, Int> {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("User-Agent", "okhttp/3.12.1")
            if (token != null) setRequestProperty("Authorization", "Bearer $token")
            if (body != null) doOutput = true
            connectTimeout = 15000; readTimeout = 20000
        }
        if (body != null) conn.outputStream.use { it.write(body.toByteArray()) }
        val code = conn.responseCode
        val text = runCatching { conn.inputStream.bufferedReader().use { it.readText() } }
            .getOrElse { runCatching { conn.errorStream?.bufferedReader()?.use { r -> r.readText() } }.getOrNull() ?: "HTTP $code" }
        conn.disconnect()
        if (code !in 200..299) throw RuntimeException("CF API $code: " + text.take(150))
        return text to code
    }

    fun loadConfigs(context: Context): List<WarpConfig> = runCatching {
        val f = File(context.filesDir, WARP_FILE)
        if (!f.isFile) return emptyList()
        val arr = JSONArray(f.readText())
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            WarpConfig(o.getString("name"), o.getString("server"), o.getInt("port"),
                o.getString("private_key"), o.getString("public_key"),
                o.getString("ip4"), o.getString("ip6"))
        }
    }.getOrDefault(emptyList())

    fun clearConfigs(context: Context) {
        runCatching { File(context.filesDir, WARP_FILE).delete() }
    }

    private fun saveConfig(context: Context, cfg: WarpConfig) {
        runCatching {
            val f = File(context.filesDir, WARP_FILE)
            val arr = if (f.isFile) JSONArray(f.readText()) else JSONArray()
            arr.put(JSONObject().apply {
                put("name", cfg.name); put("server", cfg.server); put("port", cfg.port)
                put("private_key", cfg.privateKey); put("public_key", cfg.publicKey)
                put("ip4", cfg.ip4); put("ip6", cfg.ip6)
            })
            f.writeText(arr.toString())
        }
    }

    private fun b64(b: ByteArray): String = android.util.Base64.encodeToString(b, android.util.Base64.NO_WRAP)
    private fun hex(n: Int): String = ByteArray(n).also { SecureRandom().nextBytes(it) }
        .joinToString("") { "%02x".format(it) }
}
