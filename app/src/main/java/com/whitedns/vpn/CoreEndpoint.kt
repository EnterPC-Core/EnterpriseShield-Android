package com.whitedns.vpn

import android.content.Context
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * Поиск реального REST-порта ядра FlClash:
 * сохранённый порт → 9090 (fallback) → все LISTEN-порты на 127.0.0.1 из /proc/net/tcp.
 * Проверка: GET /version с Bearer secret (секрет один на сессию).
 */
object CoreEndpoint {

    @Volatile private var cachedPort: Int = 0
    @Volatile private var cachedAt: Long = 0

    fun resolve(context: Context, timeoutMs: Int = 4000): Pair<Int, String>? {
        val prefs = context.getSharedPreferences("white_dns_runtime_state", Context.MODE_PRIVATE)
        val secret = prefs.getString("control_secret", "").orEmpty()
        if (secret.isEmpty()) return null
        if (cachedPort > 0 && System.currentTimeMillis() - cachedAt < 60_000) {
            return cachedPort to secret
        }
        val candidates = mutableListOf(prefs.getInt("control_port", 0), 9090)
        // все слушающие 127.0.0.1-порты из /proc/net/tcp
        runCatching {
            File("/proc/net/tcp").readLines().forEach { line ->
                val parts = line.trim().split("\\s+".toRegex())
                if (parts.size >= 4 && parts[3] == "0A") {   // LISTEN
                    val local = parts[1]                     // 0100007F:XXXX (LE)
                    if (local.startsWith("0100007F:")) {
                        val port = local.substringAfter(":").toIntOrNull(16) ?: return@forEach
                        if (port in 1024..65535) candidates.add(port)
                    }
                }
            }
        }
        for (port in candidates.distinct()) {
            if (port <= 0) continue
            if (probe(port, secret, timeoutMs)) {
                cachedPort = port
                cachedAt = System.currentTimeMillis()
                // обновить сохранённый порт для остальных компонентов
                prefs.edit().putInt("control_port", port).apply()
                return port to secret
            }
        }
        return null
    }

    private fun probe(port: Int, secret: String, timeoutMs: Int): Boolean = runCatching {
        val conn = (URL("http://127.0.0.1:$port/version").openConnection() as HttpURLConnection).apply {
            setRequestProperty("Authorization", "Bearer $secret")
            connectTimeout = timeoutMs
            readTimeout = timeoutMs
        }
        val ok = conn.responseCode == 200
        conn.disconnect()
        ok
    }.getOrDefault(false)

    fun api(port: Int, secret: String, path: String, timeoutMs: Int = 8000): JSONObject? = runCatching {
        val conn = (URL("http://127.0.0.1:$port$path").openConnection() as HttpURLConnection).apply {
            setRequestProperty("Authorization", "Bearer $secret")
            connectTimeout = timeoutMs
            readTimeout = timeoutMs
        }
        val text = conn.inputStream.bufferedReader().use { it.readText() }
        conn.disconnect()
        if (text.isBlank()) null else runCatching { JSONObject(text) }.getOrNull()
    }.getOrNull()

    fun put(port: Int, secret: String, path: String, body: String): Boolean = runCatching {
        val conn = (URL("http://127.0.0.1:$port$path").openConnection() as HttpURLConnection).apply {
            requestMethod = "PUT"
            setRequestProperty("Authorization", "Bearer $secret")
            setRequestProperty("Content-Type", "application/json")
            doOutput = true
            connectTimeout = 5000
            readTimeout = 8000
        }
        conn.outputStream.use { it.write(body.toByteArray()) }
        val code = conn.responseCode
        conn.disconnect()
        code in 200..299
    }.getOrDefault(false)
}
