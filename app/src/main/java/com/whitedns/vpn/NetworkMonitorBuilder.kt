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
 * Мониторинг сети (как в оригинале): карточка на главном экране + страница
 * с GeoIP (IP/страна/город/ASN) и URL-тестом доступности через туннель.
 */
object NetworkMonitorBuilder {

    private val TEST_URLS = listOf(
        "https://cp.cloudflare.com/generate_204",
        "https://www.gstatic.com/generate_204",
        "https://connectivitycheck.gstatic.com/generate_204",
        "https://api.ip.sb/geoip",
        "https://dns.google/resolve?name=example.com",
        "https://www.youtube.com/generate_204",
        "https://github.com/generate_204",
    )

    // ====== карточка на главный экран ======
    fun buildCard(activity: Activity, openPage: () -> Unit): View {
        val surface = paletteColor(activity, "surface", 0xFF1A2028)
        val tp = paletteColor(activity, "textPrimary", 0xFFE8EDF2)
        val ts = paletteColor(activity, "textSecondary", 0xFF8A94A0)
        val teal = paletteColor(activity, "teal", 0xFF4DD0C4)

        val card = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                cornerRadius = dp(activity, 16).toFloat(); setColor(surface)
                setStroke(dp(activity, 1).toInt(), teal and 0x26FFFFFF)
            }
            setPadding(dp(activity, 16), dp(activity, 14), dp(activity, 16), dp(activity, 14))
            isClickable = true
            setOnClickListener { openPage() }
        }
        val title = TextView(activity).apply {
            text = "📡  ${activity.getString(R.string.monitor_title)}"
            textSize = 15f
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            setTextColor(tp)
        }
        val status = TextView(activity).apply {
            text = activity.getString(R.string.monitor_checking)
            textSize = 13f; setTextColor(ts)
            setPadding(0, dp(activity, 4), 0, 0)
        }
        card.addView(title)
        card.addView(status)

        // живое обновление статуса карточки
        fun refresh() {
            GlobalScope.launch(Dispatchers.IO) {
                val started = isShieldStarted(activity)
                val geo = fetchGeo(started)
                launch(Dispatchers.Main) {
                    if (geo != null) {
                        val cc = geo.optString("country_code", "")
                        val ip = geo.optString("ip", "")
                        val flag = if (cc.length == 2) codeToFlag(cc) else "🌐"
                        val via = activity.getString(if (started) R.string.monitor_via_tunnel else R.string.monitor_via_direct)
                        status.text = "$flag $ip · $via"
                        status.setTextColor(if (started) teal else ts)
                    } else {
                        status.text = activity.getString(R.string.monitor_offline)
                    }
                }
            }
        }
        refresh()
        return card
    }

    // ====== страница мониторинга ======
    fun buildScreen(activity: Activity): View {
        val bg = paletteColor(activity, "background", 0xFF101418)
        val surface = paletteColor(activity, "surface", 0xFF1A2028)
        val tp = paletteColor(activity, "textPrimary", 0xFFE8EDF2)
        val ts = paletteColor(activity, "textSecondary", 0xFF8A94A0)
        val teal = paletteColor(activity, "teal", 0xFF4DD0C4)
        val red = paletteColor(activity, "red", 0xFFEF5350)
        val amber = paletteColor(activity, "amber", 0xFFFFB74D)

        val root = FrameLayout(activity).apply { setBackgroundColor(bg) }
        val scroll = ScrollView(activity).apply { setBackgroundColor(bg); isFillViewport = true }
        val content = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(activity, 20), dp(activity, 20), dp(activity, 20), dp(activity, 40))
        }
        scroll.addView(content)
        root.addView(scroll, FrameLayout.LayoutParams(-1, -1))

        content.addView(TextView(activity).apply {
            text = activity.getString(R.string.monitor_title)
            textSize = 24f
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            setTextColor(tp)
        })
        content.addView(TextView(activity).apply {
            text = activity.getString(R.string.monitor_subtitle)
            textSize = 13f; setTextColor(ts)
        }, LinearLayout.LayoutParams(-2, -2).apply { topMargin = dp(activity, 2) })

        // ===== карточка сети =====
        val netCard = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                cornerRadius = dp(activity, 16).toFloat(); setColor(surface)
            }
            setPadding(dp(activity, 16), dp(activity, 14), dp(activity, 16), dp(activity, 14))
        }
        fun row(label: String, value: TextView) {
            val r = LinearLayout(activity).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
            r.addView(TextView(activity).apply { text = label; textSize = 13f; setTextColor(ts) })
            r.addView(LinearLayout(activity), LinearLayout.LayoutParams(0, 1, 1f))
            r.addView(value, LinearLayout.LayoutParams(-2, -2))
            netCard.addView(r, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(activity, 6) })
        }
        val ipV = TextView(activity).apply { text = "—"; textSize = 14f; setTextColor(tp); typeface = Typeface.DEFAULT_BOLD }
        val countryV = TextView(activity).apply { text = "—"; textSize = 14f; setTextColor(tp); typeface = Typeface.DEFAULT_BOLD }
        val cityV = TextView(activity).apply { text = "—"; textSize = 14f; setTextColor(tp); typeface = Typeface.DEFAULT_BOLD }
        val asnV = TextView(activity).apply { text = "—"; textSize = 13f; setTextColor(ts) }
        val viaV = TextView(activity).apply { text = "—"; textSize = 14f; setTextColor(teal); typeface = Typeface.DEFAULT_BOLD }
        row(activity.getString(R.string.monitor_ip), ipV)
        row(activity.getString(R.string.monitor_country), countryV)
        row(activity.getString(R.string.monitor_city), cityV)
        row(activity.getString(R.string.monitor_route), viaV)
        netCard.addView(asnV, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(activity, 6) })
        content.addView(netCard, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(activity, 14) })

        // ===== URL-тест =====
        content.addView(TextView(activity).apply {
            text = activity.getString(R.string.monitor_url_test)
            textSize = 16f
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            setTextColor(tp)
        }, LinearLayout.LayoutParams(-2, -2).apply { topMargin = dp(activity, 20) })
        content.addView(TextView(activity).apply {
            text = activity.getString(R.string.monitor_url_note)
            textSize = 12f; setTextColor(ts)
        }, LinearLayout.LayoutParams(-2, -2).apply { topMargin = dp(activity, 2) })

        val urlList = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                cornerRadius = dp(activity, 16).toFloat(); setColor(surface)
            }
            setPadding(dp(activity, 16), dp(activity, 12), dp(activity, 16), dp(activity, 12))
        }
        content.addView(urlList, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(activity, 10) })

        fun runUrlTest() {
            urlList.removeAllViews()
            urlList.addView(TextView(activity).apply {
                text = activity.getString(R.string.monitor_testing)
                textSize = 13f; setTextColor(ts)
            })
            val started = isShieldStarted(activity)
            GlobalScope.launch(Dispatchers.IO) {
                val results = TEST_URLS.map { url ->
                    val t0 = System.nanoTime()
                    val code = runCatching {
                        val conn = (URL(url).let { u ->
                            if (started) Proxy(Proxy.Type.SOCKS, InetSocketAddress("127.0.0.1", MihomoRuntimeDefaults.MIXED_PORT))
                                .let { p -> u.openConnection(p) } ?: u.openConnection()
                            else u.openConnection()
                        }) as HttpURLConnection
                        conn.connectTimeout = 8000; conn.readTimeout = 10000
                        conn.requestMethod = if (url.contains("dns.google")) "GET" else "GET"
                        val c = conn.responseCode
                        conn.disconnect()
                        c
                    }.getOrDefault(-1)
                    val ms = (System.nanoTime() - t0) / 1e6
                    Triple(url, code, ms)
                }
                launch(Dispatchers.Main) {
                    urlList.removeAllViews()
                    for ((url, code, ms) in results) {
                        val ok = code in 200..399
                        val color = if (ok) teal else red
                        val host = runCatching { URL(url).host }.getOrDefault(url)
                        val r = LinearLayout(activity).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
                        r.addView(TextView(activity).apply {
                            text = if (ok) "✓" else "✗"
                            textSize = 15f; setTextColor(color)
                            typeface = Typeface.DEFAULT_BOLD
                        })
                        r.addView(TextView(activity).apply {
                            text = host
                            textSize = 13f; setTextColor(tp)
                            setPadding(dp(activity, 10), 0, 0, 0)
                        })
                        r.addView(LinearLayout(activity), LinearLayout.LayoutParams(0, 1, 1f))
                        r.addView(TextView(activity).apply {
                            text = if (ok) "${ms.toInt()} ${activity.getString(R.string.monitor_ms)}" else "HTTP $code"
                            textSize = 13f; setTextColor(if (ok) ts else amber)
                        })
                        urlList.addView(r, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(activity, 8) })
                    }
                }
            }
        }

        // ===== GeoIP загрузка =====
        fun loadGeo() {
            val started = isShieldStarted(activity)
            GlobalScope.launch(Dispatchers.IO) {
                val geo = fetchGeo(started)
                launch(Dispatchers.Main) {
                    if (geo == null) {
                        ipV.text = activity.getString(R.string.map_unknown); return@launch
                    }
                    ipV.text = geo.optString("ip", "—")
                    val cc = geo.optString("country_code", "")
                    val country = geo.optString("country", "")
                    countryV.text = (if (cc.length == 2) codeToFlag(cc) + " " else "") + country
                    cityV.text = geo.optString("city", "").ifEmpty { "—" }
                    asnV.text = geo.optString("asn_organization", "").ifEmpty { geo.optString("isp", "") }
                    viaV.text = activity.getString(if (started) R.string.monitor_via_tunnel else R.string.monitor_via_direct)
                    viaV.setTextColor(if (started) teal else amber)
                }
            }
        }
        loadGeo()
        runUrlTest()

        // ===== кнопка обновить =====
        val btn = com.google.android.material.button.MaterialButton(activity).apply {
            text = activity.getString(R.string.monitor_refresh)
            setOnClickListener { loadGeo(); runUrlTest() }
        }
        content.addView(btn, LinearLayout.LayoutParams(-1, dp(activity, 48)).apply { topMargin = dp(activity, 18) })

        return root
    }

    private fun isShieldStarted(activity: Activity): Boolean =
        activity.getSharedPreferences("white_dns_runtime_state", Activity.MODE_PRIVATE)
            .getString("state", "") == "Started"

    private fun fetchGeo(viaSocks: Boolean): JSONObject? = runCatching {
        val proxy = if (viaSocks) Proxy(Proxy.Type.SOCKS, InetSocketAddress("127.0.0.1", MihomoRuntimeDefaults.MIXED_PORT)) else null
        val conn = (URL("https://api.ip.sb/geoip").let { u -> proxy?.let { p -> u.openConnection(p) } ?: u.openConnection() }) as HttpURLConnection
        conn.connectTimeout = 6000; conn.readTimeout = 8000
        JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
    }.getOrNull()

    private fun codeToFlag(cc: String): String {
        val sb = StringBuilder()
        for (c in cc.uppercase()) sb.append(Character.toChars(0x1F1E6 + (c - 'A')))
        return sb.toString()
    }

    private fun paletteColor(activity: Activity, name: String, default: Long): Int {
        return try {
            val field = activity.javaClass.getDeclaredField("palette")
            field.isAccessible = true
            val palette = field.get(activity)
            val getter = palette.javaClass.getMethod("get" + name[0].uppercaseChar() + name.substring(1))
            getter.isAccessible = true
            (getter.invoke(palette) as? Int) ?: default.toInt()
        } catch (_: Exception) {
            default.toInt()
        }
    }

    private fun dp(activity: Activity, v: Int): Int = (v * activity.resources.displayMetrics.density).toInt()
}
