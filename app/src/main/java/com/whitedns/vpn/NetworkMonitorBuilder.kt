package com.whitedns.vpn

import android.app.Activity
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URL

/**
 * Мониторинг сети: карточка на главном экране + страница.
 * Стиль Щита: SURFACE alpha 200 + обводка OUTLINE + разделители, скругление 16.
 * GeoIP с фолбэками (api.ip.sb → ipapi.co → ip-api.com), живое обновление карточки.
 */
object NetworkMonitorBuilder {

    private val TEST_URLS = listOf(
        "https://cp.cloudflare.com/generate_204",
        "https://www.gstatic.com/generate_204",
        "https://api.telegram.org",
        "https://web.telegram.org",
        "https://www.instagram.com",
        "https://www.facebook.com",
        "https://www.linkedin.com",
        "https://x.com",
        "https://discord.com",
        "https://chatgpt.com",
        "https://gemini.google.com",
        "https://www.youtube.com/generate_204",
        "https://api.github.com/",
        "https://rutracker.org",
        "https://dns.google/resolve?name=example.com",
    )

    class GeoInfo(val ip: String, val country: String, val cc: String, val city: String, val asn: String)

    // ===== утилиты стиля Щита =====
    private fun alpha(c: Int, a: Int): Int = (c and 0x00FFFFFF) or (a.coerceIn(0, 255) shl 24)

    private fun cardBg(surface: Int, outline: Int): GradientDrawable = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = -1f // задаётся снаружи через cornerRadius(dp)
    }

    private fun surfaceC(activity: Activity): Int = pal(activity).surface
    private fun outlineC(activity: Activity): Int = pal(activity).outline

    private fun styledCard(activity: Activity, cornerDp: Int = 16): LinearLayout {
        val surface = surfaceC(activity); val outline = outlineC(activity)
        return LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(activity, cornerDp).toFloat()
                setColor(alpha(surface, 200))
                setStroke(dp(activity, 1), alpha(outline, 180))
            }
            clipToOutline = true
        }
    }

    private fun divider(activity: Activity): View =
        View(activity).apply { setBackgroundColor(alpha(outlineC(activity), 150)) }

        // ===== inline-ячейка для панели «загрузка/отдача» =====
    fun buildInlineStat(activity: Activity, openPage: () -> Unit): View {
        val tp = pal(activity).textPrimary
        val ts = pal(activity).textSecondary

        val cell = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutDirection = View.LAYOUT_DIRECTION_LOCALE
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(activity, 4), dp(activity, 6), dp(activity, 2), dp(activity, 6))
            isClickable = true
            setOnClickListener { openPage() }
        }
        val icon = TextView(activity).apply {
            text = "📡"
            textSize = 10f
            includeFontPadding = false
        }
        val status = TextView(activity).apply {
            text = activity.getString(R.string.monitor_checking)
            textSize = 9f
            typeface = WhiteDnsBodyTypeface
            setTextColor(ts)
            includeFontPadding = false
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
        }
        cell.addView(icon, LinearLayout.LayoutParams(-2, -2).apply { marginEnd = dp(activity, 4) })
        cell.addView(status, LinearLayout.LayoutParams(0, -2, 1f))

        fun refresh() {
            GlobalScope.launch(Dispatchers.IO) {
                val started = isShieldStarted(activity)
                val geo = fetchGeo(started)
                launch(Dispatchers.Main) {
                    if (geo != null) {
                        val flag = if (geo.cc.length == 2) codeToFlag(geo.cc) else "🌐"
                        status.text = "$flag ${geo.ip}"
                        status.setTextColor(if (started) pal(activity).teal else ts)
                    } else {
                        status.text = activity.getString(R.string.monitor_offline)
                        status.setTextColor(ts)
                    }
                }
            }
        }
        val tick = object : Runnable {
            override fun run() {
                if (cell.isAttachedToWindow) {
                    refresh()
                    cell.postDelayed(this, 10_000)
                }
            }
        }
        cell.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) { v.post(tick) }
            override fun onViewDetachedFromWindow(v: View) { v.removeCallbacks(tick) }
        })
        return cell
    }

    // ===== карточка на главный экран =====
    fun buildCard(activity: Activity, openPage: () -> Unit): View {
        val tp = pal(activity).textPrimary
        val ts = pal(activity).textSecondary
        val teal = pal(activity).teal

        val card = styledCard(activity).apply {
            setPadding(dp(activity, 16), dp(activity, 12), dp(activity, 16), dp(activity, 12))
            isClickable = true
            setOnClickListener { openPage() }
        }
        val row = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val title = TextView(activity).apply {
            text = activity.getString(R.string.monitor_title)
            textSize = 14f
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            setTextColor(tp)
        }
        val status = TextView(activity).apply {
            text = activity.getString(R.string.monitor_checking)
            textSize = 13f; setTextColor(ts)
            maxLines = 1
        }
        row.addView(title)
        row.addView(LinearLayout(activity), LinearLayout.LayoutParams(0, 1, 1f))
        row.addView(status, LinearLayout.LayoutParams(-2, -2).apply { marginStart = dp(activity, 8) })
        card.addView(row)

        fun refresh() {
            status.text = activity.getString(R.string.monitor_checking)
            status.setTextColor(ts)
            GlobalScope.launch(Dispatchers.IO) {
                val started = isShieldStarted(activity)
                val geo = fetchGeo(started)
                launch(Dispatchers.Main) {
                    if (geo != null) {
                        val flag = if (geo.cc.length == 2) codeToFlag(geo.cc) else "🌐"
                        status.text = "$flag ${geo.ip}"
                        status.setTextColor(if (started) teal else ts)
                    } else {
                        status.text = activity.getString(R.string.monitor_offline)
                        status.setTextColor(ts)
                    }
                }
            }
        }

        // живое обновление: сразу + каждые 12с, пока карточка на экране
        val tick = object : Runnable {
            override fun run() {
                if (card.isAttachedToWindow) {
                    refresh()
                    card.postDelayed(this, 12_000)
                }
            }
        }
        card.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) { v.post(tick) }
            override fun onViewDetachedFromWindow(v: View) { v.removeCallbacks(tick) }
        })
        return card
    }

    // ===== страница мониторинга =====
    fun buildScreen(activity: Activity): View {
        val bg = pal(activity).background
        val tp = pal(activity).textPrimary
        val ts = pal(activity).textSecondary
        val teal = pal(activity).teal
        val red = pal(activity).red
        val amber = pal(activity).amber

        val root = FrameLayout(activity).apply { setBackgroundColor(bg) }
        val scroll = ScrollView(activity).apply { setBackgroundColor(bg); isFillViewport = true }
        val content = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(activity, 20), dp(activity, 18), dp(activity, 20), dp(activity, 40))
        }
        scroll.addView(content)
        root.addView(scroll, FrameLayout.LayoutParams(-1, -1))

        content.addView(TextView(activity).apply {
            text = activity.getString(R.string.monitor_title)
            textSize = 22f
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            setTextColor(tp)
        })
        content.addView(TextView(activity).apply {
            text = activity.getString(R.string.monitor_subtitle)
            textSize = 13f; setTextColor(ts)
        }, LinearLayout.LayoutParams(-2, -2).apply { topMargin = dp(activity, 2) })

        // ===== карточка сети =====
        val netCard = styledCard(activity).apply { setPadding(dp(activity, 4), dp(activity, 4), dp(activity, 4), dp(activity, 8)) }
        fun netRow(label: String, value: TextView): LinearLayout {
            val r = LinearLayout(activity).apply {
                orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(activity, 16), dp(activity, 10), dp(activity, 16), dp(activity, 10))
            }
            r.addView(TextView(activity).apply { text = label; textSize = 13f; setTextColor(ts) })
            r.addView(LinearLayout(activity), LinearLayout.LayoutParams(0, 1, 1f))
            value.textSize = 14f; value.setTextColor(tp); value.typeface = Typeface.DEFAULT_BOLD
            r.addView(value, LinearLayout.LayoutParams(-2, -2).apply { marginStart = dp(activity, 12) })
            return r
        }
        val ipV = TextView(activity).apply { text = "—" }
        val countryV = TextView(activity).apply { text = "—" }
        val cityV = TextView(activity).apply { text = "—" }
        val viaV = TextView(activity).apply { text = "—"; setTextColor(teal) }
        val asnV = TextView(activity).apply { text = ""; textSize = 12f; setTextColor(ts) }

        fun addSep() = netCard.addView(divider(activity), LinearLayout.LayoutParams(-1, dp(activity, 1)).apply {
            marginStart = dp(activity, 18); marginEnd = dp(activity, 18)
        })

        netCard.addView(netRow(activity.getString(R.string.monitor_ip), ipV))
        addSep()
        netCard.addView(netRow(activity.getString(R.string.monitor_country), countryV))
        addSep()
        netCard.addView(netRow(activity.getString(R.string.monitor_city), cityV))
        addSep()
        netCard.addView(netRow(activity.getString(R.string.monitor_route), viaV))
        netCard.addView(asnV, LinearLayout.LayoutParams(-1, -2).apply {
            marginStart = dp(activity, 16); marginEnd = dp(activity, 16); topMargin = dp(activity, 2); bottomMargin = dp(activity, 10)
        })
        content.addView(netCard, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(activity, 14) })

        // ===== URL-тест =====
        content.addView(TextView(activity).apply {
            text = activity.getString(R.string.monitor_url_test)
            textSize = 15f
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            setTextColor(tp)
        }, LinearLayout.LayoutParams(-2, -2).apply { topMargin = dp(activity, 18) })

        val urlCard = styledCard(activity).apply { setPadding(dp(activity, 16), dp(activity, 12), dp(activity, 16), dp(activity, 12)) }
        content.addView(urlCard, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(activity, 8) })

        fun runUrlTest() {
            urlCard.removeAllViews()
            val started = isShieldStarted(activity)
            val placeHolder = TextView(activity).apply {
                text = activity.getString(R.string.monitor_testing)
                textSize = 13f; setTextColor(ts)
            }
            urlCard.addView(placeHolder)
            val pool = java.util.concurrent.Executors.newFixedThreadPool(4)
            TEST_URLS.forEach { url ->
                pool.submit {
                    val t0 = System.nanoTime()
                    val code = runCatching {
                        val conn = (URL(url).let { u ->
                            val p = if (started) Proxy(Proxy.Type.SOCKS, InetSocketAddress("127.0.0.1", MihomoRuntimeDefaults.MIXED_PORT)) else null
                            p?.let { u.openConnection(it) } ?: u.openConnection()
                        }) as HttpURLConnection
                        conn.connectTimeout = 4000; conn.readTimeout = 6000
                        conn.setRequestProperty("User-Agent", "EnterpriseShield/1.0 (Android)")
                        conn.setRequestProperty("Connection", "close")
                        val c = conn.responseCode
                        conn.disconnect()
                        c
                    }.getOrDefault(-1)
                    val ms = (System.nanoTime() - t0) / 1e6
                    GlobalScope.launch(Dispatchers.Main) {
                        if (placeHolder.isAttachedToWindow && urlCard.childCount == 1 && urlCard.getChildAt(0) === placeHolder) urlCard.removeView(placeHolder)
                        val ok = code in 200..399
                        val color = if (ok) teal else red
                        val host = runCatching { URL(url).host }.getOrDefault(url)
                        val r = LinearLayout(activity).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
                        r.addView(TextView(activity).apply {
                            text = if (ok) "✓" else "✗"; textSize = 15f; setTextColor(color)
                            typeface = Typeface.DEFAULT_BOLD
                        })
                        r.addView(TextView(activity).apply {
                            text = host; textSize = 13f; setTextColor(tp)
                            setPadding(dp(activity, 10), 0, 0, 0)
                            maxLines = 1
                            ellipsize = android.text.TextUtils.TruncateAt.MIDDLE
                        }, LinearLayout.LayoutParams(0, -2, 1f))
                        r.addView(TextView(activity).apply {
                            text = if (ok) "${ms.toInt()} ${activity.getString(R.string.monitor_ms)}" else "HTTP $code"
                            textSize = 12f; setTextColor(if (ok) ts else amber)
                        })
                        urlCard.addView(r, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(activity, 8) })
                    }
                }
            }
            pool.shutdown()
        }

        fun loadGeo() {
            GlobalScope.launch(Dispatchers.IO) {
                val started = isShieldStarted(activity)
                val geo = fetchGeo(started)
                launch(Dispatchers.Main) {
                    if (geo == null) {
                        ipV.text = "—"; countryV.text = activity.getString(R.string.map_unknown)
                        viaV.text = activity.getString(R.string.monitor_offline); viaV.setTextColor(amber)
                        return@launch
                    }
                    ipV.text = geo.ip
                    countryV.text = (if (geo.cc.length == 2) codeToFlag(geo.cc) + " " else "") + geo.country
                    cityV.text = geo.city.ifEmpty { "—" }
                    asnV.text = geo.asn
                    viaV.text = activity.getString(if (started) R.string.monitor_via_tunnel else R.string.monitor_via_direct)
                    viaV.setTextColor(if (started) teal else amber)
                }
            }
        }
        loadGeo()
        runUrlTest()

        val btn = com.google.android.material.button.MaterialButton(activity).apply {
            text = activity.getString(R.string.monitor_refresh)
            setOnClickListener { loadGeo(); runUrlTest() }
        }
        content.addView(btn, LinearLayout.LayoutParams(-1, dp(activity, 48)).apply { topMargin = dp(activity, 16) })

        return root
    }

    // ===== GeoIP с фолбэками =====
    private fun isShieldStarted(activity: Activity): Boolean =
        activity.getSharedPreferences("white_dns_runtime_state", Activity.MODE_PRIVATE)
            .getString("state", "") == "Started"

    private fun httpGet(url: String, socks: Boolean, timeoutC: Int = 7000, timeoutR: Int = 9000): String? =
        runCatching {
            val p = if (socks) Proxy(Proxy.Type.SOCKS, InetSocketAddress("127.0.0.1", MihomoRuntimeDefaults.MIXED_PORT)) else null
            val conn = (URL(url).let { u -> p?.let { u.openConnection(it) } ?: u.openConnection() }) as HttpURLConnection
            conn.connectTimeout = timeoutC; conn.readTimeout = timeoutR
            conn.setRequestProperty("User-Agent", "EnterpriseShield/1.0 (Android)")
            conn.setRequestProperty("Cache-Control", "no-cache")
            conn.setRequestProperty("Connection", "close")
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            conn.disconnect()
            body
        }.getOrNull()

    private fun parseIpSb(body: String): GeoInfo? = runCatching {
        val j = JSONObject(body)
        GeoInfo(j.optString("ip"), j.optString("country"), j.optString("country_code"),
            j.optString("city"), j.optString("asn_organization").ifEmpty { j.optString("isp") })
    }.getOrNull()

    private fun parseIpApiCom(body: String): GeoInfo? = runCatching {
        val j = JSONObject(body)
        GeoInfo(j.optString("query"), j.optString("country"), j.optString("countryCode"),
            j.optString("city"), j.optString("as").ifEmpty { j.optString("isp") })
    }.getOrNull()

    private fun fetchGeo(socks: Boolean): GeoInfo? {
        val bust = "?t=" + System.currentTimeMillis()
        parseIpSb(httpGet("https://api.ip.sb/geoip$bust", socks) ?: return fallbackGeo(socks))?.let { return it }
        return fallbackGeo(socks)
    }

    private fun fallbackGeo(socks: Boolean): GeoInfo? {
        runCatching {
            val j = JSONObject(httpGet("https://ipapi.co/json/?t=" + System.currentTimeMillis(), socks) ?: return@runCatching null)
            return GeoInfo(j.optString("ip"), j.optString("country_name"), j.optString("country_code"),
                j.optString("city"), j.optString("org"))
        }
        parseIpApiCom(httpGet("http://ip-api.com/json", socks) ?: return null)?.let { return it }
        return null
    }

    private fun codeToFlag(cc: String): String {
        val sb = StringBuilder()
        for (c in cc.uppercase()) sb.append(Character.toChars(0x1F1E6 + (c - 'A')))
        return sb.toString()
    }

    private fun pal(activity: Activity): com.whitedns.vpn.WhiteDnsPalette =
        com.whitedns.vpn.WhiteDnsDesignTokens.forContext(activity)

    private fun dp(activity: Activity, v: Int): Int = (v * activity.resources.displayMetrics.density).toInt()
}
