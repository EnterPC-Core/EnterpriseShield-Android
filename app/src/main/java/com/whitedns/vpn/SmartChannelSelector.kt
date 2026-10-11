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
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URL
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Умный выбор сервера «♻️ Авто-выбор» (select-группа).
 *
 * ЭТАП 1 — скрининг: REST /delay 40 живых кандидатов × 4 сервиса (быстро, параллельно).
 * ЭТАП 2 — ФИНАЛ (живой перебор): топ-5 кандидатов по очереди включаются в группу,
 *          и каждый прогоняется РЕАЛЬНЫМ клиентом через SOCKS-туннель (как мониторинг):
 *          6 сервисов, все должны дать ✓, меряется сумма времени. Побеждает лучший.
 * ЭТАП 3 — контроль: health каждые 90с; провал → Резерв + новый перебор.
 */
object SmartChannelSelector {

    private const val GROUP = "♻️ Авто-выбор"
    private const val MAIN_GROUP = "🚀 Каналы"


    /** Хосты, забракованные пользователем. Серверы с ними навсегда исключены из автоматики. */
    private val BANNED_NAMES = setOf("Enterprise 438", "Enterprise 439")

    private val SCREEN_URLS = listOf(
        "https://www.youtube.com/generate_204",
        "https://api.telegram.org",
        "https://www.instagram.com",
        "https://cp.cloudflare.com/generate_204",
    )
    private val LIVE_URLS = listOf(
        "https://www.youtube.com/generate_204",
        "https://api.telegram.org",
        "https://www.instagram.com",
        "https://x.com",
        "https://discord.com",
        "https://web.whatsapp.com",
        "https://www.tiktok.com",
        "https://cp.cloudflare.com/generate_204",
    )

    private const val FULL_INTERVAL_MS = 15 * 60 * 1000L
    private const val HEALTH_INTERVAL_MS = 90 * 1000L

    private val running = AtomicBoolean(false)
    @Volatile private var stopped = false

    fun stop() {
        stopped = true
    }

    fun start(context: Context, controlPort: Int, secret: String) {
        stopped = false
        if (!running.compareAndSet(false, true)) return
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val base = "http://127.0.0.1:$controlPort"
                val prefs = context.getSharedPreferences("white_dns_smart_selector", Context.MODE_PRIVATE)

                fun switchMainToAuto(): Boolean = runCatching {
                    val r: HttpURLConnection = URL("$base/proxies/${enc(MAIN_GROUP)}").openConnection() as HttpURLConnection
                    r.requestMethod = "PUT"
                    r.setRequestProperty("Authorization", "Bearer $secret")
                    r.setRequestProperty("Content-Type", "application/json")
                    r.doOutput = true
                    r.outputStream.use { it.write("""{"name":"$GROUP"}""".toByteArray()) }
                    val code = r.responseCode
                    r.disconnect()
                    code in 200..299
                }.getOrDefault(false)

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

                fun currentOfGroup(): String? =
                    api("/proxies/" + enc(GROUP))?.optString("now")?.takeIf { it.isNotBlank() }

                /** быстрый скрининг одного сервера через REST */
                suspend fun screen(name: String): Pair<Long, Int> = withContext(Dispatchers.IO) {
                    var sum = 0L; var passed = 0
                    for (u in LIVE_URLS) {
                        val d = api("/proxies/${enc(name)}/delay?url=${enc(u)}&timeout=5000")
                        val delay = d?.optInt("delay", 0) ?: 0
                        if (delay > 0) { passed++; sum += delay }
                    }
                    sum to passed
                }

                /** реальный клиентский трафик через HTTP-порт ядра (mixed 2080):
                 *  DNS резолвит ЯДРО (через туннель), соединение реальное.
                 *  Проверяет фактически включённый сервер группы. */
                fun trafficTest(urls: List<String>): Pair<Long, Int> {
                    var sum = 0L; var passed = 0
                    for (u in urls) {
                        val ok = runCatching {
                            val conn = (URL(u).let { it.openConnection(java.net.Proxy(java.net.Proxy.Type.HTTP, InetSocketAddress("127.0.0.1", MihomoRuntimeDefaults.MIXED_PORT))) }) as HttpURLConnection
                            conn.connectTimeout = 4000; conn.readTimeout = 6000
                            conn.setRequestProperty("User-Agent", "EnterpriseShield/1.0 (Android)")
                            conn.setRequestProperty("Connection", "close")
                            val t0 = System.nanoTime()
                            val code = conn.responseCode
                            val ms = (System.nanoTime() - t0) / 1_000_000
                            conn.disconnect()
                            if (code in 200..399) { sum += ms; true } else false
                        }.getOrDefault(false)
                        if (ok) passed++
                    }
                    return sum to passed
                }

                /** честный тест сервера через ядро: DNS через прокси, реальный TLS, код ответа.
                 *  name=null -> тест активного выбора группы. */
                fun coreTest(name: String?): Pair<Long, Int> {
                    var sum = 0L; var passed = 0
                    for (u in LIVE_URLS) {
                        val path = if (name == null) "/group/${enc(GROUP)}/delay" else "/proxies/${enc(name)}/delay"
                        val d = api("$path?url=${enc(u)}&timeout=6000")
                        val delay = d?.optInt("delay", 0) ?: 0
                        if (delay > 0) { passed++; sum += delay }
                    }
                    return sum to passed
                }

                /** ЭТАП 1+2: полный цикл выбора */
                suspend fun fullRun(): Boolean {
                    runCatching { switchMainToAuto() }
                    val groupJson = api("/proxies/" + enc(GROUP)) ?: return false
                    val all = groupJson.optJSONArray("all") ?: return false
                    val members = (0 until all.length()).mapNotNull { all.optString(it) }
                        .filter { it != GROUP && !it.startsWith("📶") && !it.startsWith("📹") &&
                                  !it.startsWith("✈") && !it.startsWith("🧱") && !it.startsWith("🤖") }
                    if (members.isEmpty()) return false

                    // ЖИВОЙ СКРИН: в select-режиме история пустая (ядро не тестирует само),
                    // поэтому сначала быстрый одиночный тест каждого (лёгкий URL, параллельно),
                    // отбираем живых — только их проверяем глубоко.
                    val candidates = members.filter { n -> BANNED_NAMES.none { b -> n.contains(b) } }
                    val pingUrl = "https://cp.cloudflare.com/generate_204"
                    val aliveList = mutableListOf<Pair<String, Long>>()
                    for (chunk in candidates.chunked(12)) {
                        if (stopped || aliveList.size >= 60) break
                        val res = chunk.map { n ->
                            async(Dispatchers.IO) {
                                val d = api("/proxies/${enc(n)}/delay?url=${enc(pingUrl)}&timeout=4000")
                                n to (d?.optInt("delay", 0) ?: 0).toLong()
                            }
                        }.awaitAll()
                        aliveList += res.filter { it.second > 0 }
                    }
                    if (aliveList.isEmpty()) {
                        switchTo(GROUP)
                        prefs.edit().putString("last_server", "").putInt("last_passed", 0).apply()
                        return false
                    }
                    val pool = aliveList.sortedBy { it.second }.take(40).map { it.first }

                    // ЭТАП 1: параллельный скрининг (чанки по 8)
                    val screened = pool.chunked(8).flatMap { chunk ->
                        chunk.map { name ->
                            async(Dispatchers.IO) {
                                val r = screen(name)
                                Triple(name, r.first, r.second)
                            }
                        }.awaitAll()
                    }

                    val fullPass = screened.filter { it.third == LIVE_URLS.size }
                    if (fullPass.isEmpty()) {
                        switchTo(GROUP)
                        prefs.edit().putString("last_server", "").putInt("last_passed", 0).apply()
                        return false
                    }

                    // ЭТАП 2: повторная проверка топ-8 финалистов через ядро (стабильность)
                    val finalists = fullPass.sortedBy { it.second }.take(8)
                    var bestName: String? = null; var bestSum = Long.MAX_VALUE
                    for ((name, _, _) in finalists) {
                        val (sum, passed) = coreTest(name)
                        prefs.edit().putString("candidate_$name", "$passed/${LIVE_URLS.size}").apply()
                        if (passed == LIVE_URLS.size && sum < bestSum) {
                            bestSum = sum; bestName = name
                        }
                    }
                    if (bestName == null) {
                        switchTo(GROUP)
                        prefs.edit().putString("last_server", "").putInt("last_passed", 0).apply()
                        return false
                    }
                    // ВТОРОЙ ЭШЕЛОН: включаем кандидата и гоняем РЕАЛЬНЫЙ трафик через ядро.
                    // Не прошёл — следующий кандидат; все пали — группа на себя (url-test fallback невозможен, select).
                    var finalOk = false
                    val ordered = (listOf(bestName!!) + finalists.map { it.first }.filter { it != bestName }).distinct()
                    for (cand in ordered) {
                        if (!switchTo(cand)) continue
                        delay(1500)
                        val quick = listOf(
                            "https://www.youtube.com/generate_204",
                            "https://api.telegram.org",
                            "https://cp.cloudflare.com/generate_204",
                        )
                        val (_, tpassed) = trafficTest(quick)
                        prefs.edit().putString("candidate_traffic_$cand", "$tpassed/3").apply()
                        if (tpassed == quick.size) { bestName = cand; finalOk = true; break }
                    }
                    if (!finalOk) {
                        switchTo(GROUP)
                        prefs.edit().putString("last_server", "").putInt("last_passed", 0).apply()
                        return false
                    }
                    if (finalOk) {
                        prefs.edit()
                            .putString("last_server", bestName)
                            .putInt("last_passed", LIVE_URLS.size)
                            .putInt("last_total", LIVE_URLS.size)
                            .putLong("last_at", System.currentTimeMillis())
                            .putBoolean("full_pass", true)
                            .apply()
                    }
                    return finalOk
                }

                // Универсал: замкнуть основной маршрут на управляемую группу
                // (сбрасывает прошлый ручной выбор сервера в «Каналах»)
                runCatching { switchMainToAuto() }
                delay(20_000)
                while (!stopped) {
                    runCatching { fullRun() }
                    val startedAt = System.currentTimeMillis()
                    while (!stopped && System.currentTimeMillis() - startedAt < FULL_INTERVAL_MS) {
                        delay(HEALTH_INTERVAL_MS)
                        val cur = currentOfGroup() ?: break
                        if (cur == GROUP) continue
                        val (_, passed) = coreTest(cur)
                        val (_, tpassed) = trafficTest(listOf("https://cp.cloudflare.com/generate_204", "https://www.youtube.com/generate_204"))
                        if (passed < LIVE_URLS.size || tpassed < 2) {
                            runCatching { switchTo(GROUP) }
                            runCatching { fullRun() }
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
