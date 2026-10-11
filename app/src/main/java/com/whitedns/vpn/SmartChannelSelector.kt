package com.whitedns.vpn

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Умный выбор сервера для «♻️ Авто-выбор»: тестирует кандидатов НЕ по одному URL,
 * а по набору сервисов (YouTube, Telegram, Instagram, Cloudflare) через REST ядра
 * (GET /proxies/{name}/delay?url=...). Выбирает серверы, прошедшие ВСЕ проверки,
 * из них — с минимальной суммарной задержкой, и переключает группу на лучший.
 */
object SmartChannelSelector {

    private const val GROUP = "♻️ Авто-выбор"
    private val CHECK_URLS = listOf(
        "https://www.youtube.com/generate_204",
        "https://api.telegram.org",
        "https://www.instagram.com",
        "https://cp.cloudflare.com/generate_204",
    )

    fun interface Logger { fun log(line: String) }

    private val running = java.util.concurrent.atomic.AtomicBoolean(false)
    private const val RECHECK_MS = 45 * 60 * 1000L

    /**
     * @param controlPort REST-порт ядра
     * @param secret секрет ядра
     * @param candidates сколько лучших по последнему пингу серверов проверять (напр. 25)
     */
    fun selectBest(context: Context, controlPort: Int, secret: String, candidates: Int = 25, log: Logger? = null) {
        if (!running.compareAndSet(false, true)) return
        GlobalScope.launch(Dispatchers.IO) {
            try {
              while (true) {
                delay(20_000)   // дать ядру подняться
                runCatching {
                val base = "http://127.0.0.1:$controlPort"
                fun api(path: String, method: String = "GET", body: String? = null): JSONObject? {
                    val conn: HttpURLConnection = (URL(base + path).openConnection() as HttpURLConnection).apply {
                        requestMethod = method
                        setRequestProperty("Authorization", "Bearer $secret")
                        connectTimeout = 5000
                        readTimeout = 12000
                    }
                    if (body != null) {
                        conn.doOutput = true
                        conn.outputStream.use { it.write(body.toByteArray()) }
                    }
                    val text = conn.inputStream.bufferedReader().use { it.readText() }
                    conn.disconnect()
                    return if (text.isBlank()) null else runCatching { JSONObject(text) }.getOrNull()
                }

                // 1) члены группы + их текущие задержки
                val groupJson = api("/proxies/" + java.net.URLEncoder.encode(GROUP, "UTF-8")) ?: return@runCatching
                val all = groupJson.optJSONArray("all") ?: return@runCatching
                val scored = mutableListOf<Pair<String, Long>>()   // name -> известный delay
                for (i in 0 until all.length()) {
                    val n = all.optString(i)
                    if (n == GROUP || n.startsWith("📶") || n.startsWith("📹") || n.startsWith("✈") ||
                        n.startsWith("🧱") || n.startsWith("🤖")) continue
                    val pj = api("/proxies/" + java.net.URLEncoder.encode(n, "UTF-8")) ?: continue
                    val history = pj.optJSONArray("extra")?.optJSONObject(0)?.optJSONArray("history")
                    var lastDelay = Long.MAX_VALUE
                    if (history != null && history.length() > 0) {
                        lastDelay = history.optJSONObject(history.length() - 1).optLong("delay", 0)
                        if (lastDelay <= 0) lastDelay = Long.MAX_VALUE
                    }
                    scored.add(n to lastDelay)
                }
                val pool = scored.sortedBy { it.second }.take(candidates).map { it.first }
                log?.log("кандидатов: ${pool.size}")
                if (pool.isEmpty()) return@runCatching

                // 2) мульти-проверка: сервер должен пройти ВСЕ URL
                data class Cand(val name: String, val sumDelay: Long)
                val universal = mutableListOf<Cand>()
                for (name in pool) {
                    var sum = 0L
                    var allOk = true
                    for (u in CHECK_URLS) {
                        val d = api("/proxies/" + java.net.URLEncoder.encode(name, "UTF-8") +
                                "/delay?url=" + java.net.URLEncoder.encode(u, "UTF-8") + "&timeout=5000")
                        val delay = d?.optInt("delay", 0) ?: 0
                        if (delay <= 0) { allOk = false; break }
                        sum += delay
                    }
                    if (allOk) universal.add(Cand(name, sum))
                }
                log?.log("универсальных: ${universal.size}")
                if (universal.isEmpty()) return@runCatching

                // 3) переключить группу на лучшего (минимальная сумма задержек)
                val best = universal.minByOrNull { it.sumDelay } ?: return@runCatching
                val enc = java.net.URLEncoder.encode(GROUP, "UTF-8")
                val r = URL("$base/proxies/$enc").openConnection() as HttpURLConnection
                r.requestMethod = "PUT"
                r.setRequestProperty("Authorization", "Bearer $secret")
                r.setRequestProperty("Content-Type", "application/json")
                r.doOutput = true
                r.outputStream.use { it.write("""{"name":"${best.name.replace("\"", "")}"}""".toByteArray()) }
                val code = r.responseCode
                r.disconnect()
                log?.log("выбран: ${best.name} (HTTP $code)")
            }.onFailure { log?.log("ошибка: ${it.message}") }
                delay(RECHECK_MS)
              }
            } finally { running.set(false) }
        }
    }
}
