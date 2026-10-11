package com.whitedns.vpn

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Умный выбор сервера для «♻️ Авто-выбор» (теперь select-группа — ядро её не трогает).
 * Логика: полный прогон кандидатов (мульти-URL: YouTube, Telegram, Instagram, CF),
 * побеждает прошедший ВСЕ с минимальной суммой задержек; если таковых нет — максимум проверок.
 * Между прогонами — быстрая проверка активного сервера (3 мин); упал → мгновенный полный прогон.
 * Решение пишется в prefs (smart_selector_last) для отображения в UI.
 */
object SmartChannelSelector {

    private const val GROUP = "♻️ Авто-выбор"
    private const val RESERVE = "⚡ Резерв"
    private val CHECK_URLS = listOf(
        "https://www.youtube.com/generate_204",
        "https://api.telegram.org",
        "https://www.instagram.com",
        "https://cp.cloudflare.com/generate_204",
    )
    private const val FULL_INTERVAL_MS = 15 * 60 * 1000L
    private const val HEALTH_INTERVAL_MS = 90 * 1000L

    private val running = AtomicBoolean(false)

    fun start(context: Context, controlPort: Int, secret: String, candidates: Int = 60) {
        if (!running.compareAndSet(false, true)) return
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val base = "http://127.0.0.1:$controlPort"
                val prefs = context.getSharedPreferences("white_dns_smart_selector", Context.MODE_PRIVATE)

                fun api(path: String, method: String = "GET", body: String? = null): JSONObject? = runCatching {
                    val conn: HttpURLConnection = (URL(base + path).openConnection() as HttpURLConnection).apply {
                        requestMethod = method
                        setRequestProperty("Authorization", "Bearer $secret")
                        connectTimeout = 4000
                        readTimeout = 9000
                    }
                    if (body != null) {
                        conn.doOutput = true
                        conn.outputStream.use { it.write(body.toByteArray()) }
                    }
                    val text = conn.inputStream.bufferedReader().use { it.readText() }
                    conn.disconnect()
                    if (text.isBlank()) null else runCatching { JSONObject(text) }.getOrNull()
                }.getOrNull()

                fun currentOfGroup(): String? =
                    api("/proxies/" + enc(GROUP))?.optString("now")?.takeIf { it.isNotBlank() }

                fun switchTo(name: String): Boolean = runCatching {
                    val r: HttpURLConnection = URL("$base/proxies/${enc(GROUP)}").openConnection() as HttpURLConnection
                    r.requestMethod = "PUT"
                    r.setRequestProperty("Authorization", "Bearer $secret")
                    r.setRequestProperty("Content-Type", "application/json")
                    r.doOutput = true
                    r.outputStream.use { it.write("""{"name":"$name"}""".toByteArray()) }
                    val code = r.responseCode
                    r.disconnect()
                    code in 200..299
                }.getOrDefault(false)

                /** проверка одного сервера: (сумма задержек, сколько прошёл) */
                suspend fun checkServer(name: String): Pair<Long, Int> = withContext(Dispatchers.IO) {
                    var sum = 0L; var passed = 0
                    for (u in CHECK_URLS) {
                        val d = api("/proxies/${enc(name)}/delay?url=${enc(u)}&timeout=5000")
                        val delay = d?.optInt("delay", 0) ?: 0
                        if (delay > 0) { passed++; sum += delay }
                    }
                    sum to passed
                }

                /** полный прогон и переключение */
                suspend fun fullRun(): Boolean {
                    val groupJson = api("/proxies/" + enc(GROUP)) ?: return false
                    val all = groupJson.optJSONArray("all") ?: return false
                    val members = (0 until all.length()).mapNotNull { all.optString(it) }
                        .filter { it != GROUP && !it.startsWith("📶") && !it.startsWith("📹") &&
                                  !it.startsWith("✈") && !it.startsWith("🧱") && !it.startsWith("🤖") }
                    if (members.isEmpty()) return false

                    // кандидаты: по последней известной задержке
                    val scored = members.map { n ->
                        val hist = api("/proxies/" + enc(n))?.optJSONArray("history")
                        var last = Long.MAX_VALUE
                        if (hist != null) for (h in hist.length() - 1 downTo 0) {
                            val d = hist.optJSONObject(h)?.optLong("delay", 0) ?: 0
                            if (d > 0) { last = d; break }
                        }
                        n to last
                    }
                    val alive = scored.filter { it.second != Long.MAX_VALUE }.sortedBy { it.second }
                    val pool = (if (alive.size >= 15) alive.take(40) else alive + scored.filter { it.second == Long.MAX_VALUE })
                        .take(40).map { it.first }

                    val results = pool.chunked(8).flatMap { chunk ->
                        chunk.map { name ->
                            async(Dispatchers.IO) {
                                val r = checkServer(name)
                                Triple(name, r.first, r.second)
                            }
                        }.awaitAll()
                    }

                    val maxPassed = results.maxOfOrNull { it.third } ?: 0
                    if (maxPassed == 0) {
                        switchTo(RESERVE)
                        return false
                    }
                    val best = results.filter { it.third == maxPassed }.minByOrNull { it.second } ?: return false
                    val ok = switchTo(best.first)
                    if (ok) {
                        prefs.edit()
                            .putString("last_server", best.first)
                            .putInt("last_passed", best.third)
                            .putInt("last_total", CHECK_URLS.size)
                            .putLong("last_at", System.currentTimeMillis())
                            .putBoolean("full_pass", best.third == CHECK_URLS.size)
                            .apply()
                        // повторная валидация через 30с: сервер мог «поплыть» сразу после теста
                        delay(30_000)
                        val recheck = checkServer(best.first)
                        if (recheck.second < CHECK_URLS.size) {
                            runCatching { switchTo(RESERVE) }
                            prefs.edit().putString("last_server", RESERVE).putBoolean("full_pass", false).apply()
                            return false
                        }
                    }
                    return ok
                }

                // сразу страховка: url-test сам найдёт живой сервер
                runCatching { switchTo(RESERVE) }
                delay(20_000)   // дать ядру подняться и Резерву прогреться
                while (true) {
                    runCatching { fullRun() }

                    // быстрый health активного между полными прогонами
                    val startedAt = System.currentTimeMillis()
                    while (System.currentTimeMillis() - startedAt < FULL_INTERVAL_MS) {
                        delay(HEALTH_INTERVAL_MS)
                        val cur = currentOfGroup() ?: break
                        val (sum, passed) = checkServer(cur)
                        if (passed < CHECK_URLS.size) {
                            runCatching { switchTo(RESERVE) }   // мгновенно на живой
                            runCatching { fullRun() }            // затем умный поиск
                            break
                        }
                    }
                }
            } catch (_: Exception) {
            } finally {
                running.set(false)
            }
        }
    }

    private fun enc(s: String): String = java.net.URLEncoder.encode(s, "UTF-8")
}
