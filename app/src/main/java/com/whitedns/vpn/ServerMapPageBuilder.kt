package com.whitedns.vpn

import android.app.Activity
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
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
 * Экран «Карта»: тёмная неоновая карта, пин юзера (🇷🇺 / GeoIP), пин выхода
 * (флаг канала или GeoIP-страна выхода при включённом Щите), дуга маршрута,
 * карточка «Откуда → Куда» с городами.
 */
object ServerMapPageBuilder {

    private fun log(activity: Activity, step: String, detail: String = "") {
        runCatching {
            java.io.File(activity.filesDir, "map_debug.txt")
                .appendText("${System.currentTimeMillis()} $step $detail\n")
        }
    }

    fun buildScreen(activity: Activity): View {
        log(activity, "buildScreen:start")
        val bg = pal(activity).background
        val tp = pal(activity).textPrimary
        val ts = pal(activity).textSecondary
        val teal = pal(activity).teal

        val prefs = activity.getSharedPreferences("white_dns_runtime_state", Activity.MODE_PRIVATE)
        val stateStarted = prefs.getString("state", "") == "Started"
        val activeFlag = prefs.getString("connection_country_flag", "").orEmpty()
        val activeTag = prefs.getString("active_connection_tag", "").orEmpty()
        log(activity, "state", "started=$stateStarted flag='$activeFlag' tag='$activeTag'")

        val root = FrameLayout(activity).apply { setBackgroundColor(bg) }

        // ===== карта =====
        val mapView = WorldMapView(activity).apply {
            colorBg = bg
            colorLandFill = pal(activity).surfaceElevated2
            colorNeon = teal
            colorAccent = 0xFF9FE8DB.toInt()
            colorText = tp
            seaLabels = listOf(
                WorldMapView.SeaLabel(51.8f, 26.8f, activity.getString(R.string.map_sea_persian_gulf), 6.5f),
                WorldMapView.SeaLabel(50.9f, 41.6f, activity.getString(R.string.map_sea_caspian), 4.0f),
            )
        }
        root.addView(mapView, FrameLayout.LayoutParams(-1, -1))

        val countries = WorldMapAsset.countries(activity)
        log(activity, "countries", "n=${countries.size}")

        // пин юзера
        val ru = WorldMapAsset.anchorOf("RU", countries)
        if (ru != null) mapView.myPin = WorldMapView.Pin(ru[0], ru[1], "🇷🇺", activity.getString(R.string.map_russia))

        // пин выхода: по флагу канала, иначе — GeoIP подскажет
        var exitIso: String? = WorldMapAsset.flagToCode(activeFlag)
        if (stateStarted && exitIso != null) {
            val a = WorldMapAsset.anchorOf(exitIso, countries)
            if (a != null) mapView.serverPin = WorldMapView.Pin(a[0], a[1], activeFlag, countryName(activeFlag), accent = true)
        }
        mapView.loadMap(activity)
        mapView.pinsUpdated()

        // ===== заголовок =====
        val header = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(activity, 20), dp(activity, 12), dp(activity, 20), 0)
        }
        header.addView(TextView(activity).apply {
            text = activity.getString(R.string.map_page_title)
            textSize = 26f
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            setTextColor(tp)
            setShadowLayer(10f, 0f, 0f, bg)
        })
        val subtitle = TextView(activity).apply {
            text = activity.getString(R.string.map_subtitle_orig)
            textSize = 13f; setTextColor(ts)
            setShadowLayer(8f, 0f, 0f, bg)
        }
        header.addView(subtitle, LinearLayout.LayoutParams(-2, -2).apply { topMargin = dp(activity, 2) })
        root.addView(header, FrameLayout.LayoutParams(-2, -2, Gravity.TOP or Gravity.START))

        // ===== карточка Откуда → Куда =====
        val bottomWrap = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(activity, 16), 0, dp(activity, 16), dp(activity, 12))
        }
        val card = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            background = EsDs.glassBackground(
                activity,
                pal(activity),
                radiusDp = EsDs.Radius.card,
                tintAlpha = if (pal(activity).isDark) 214 else 182,
            )
            elevation = dp(activity, 6).toFloat()
            setPadding(dp(activity, 18), dp(activity, 14), dp(activity, 18), dp(activity, 14))
        }

        fun endpointRow(icon: String, title: String, valueView: TextView): LinearLayout {
            val row = LinearLayout(activity).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
            row.addView(TextView(activity).apply {
                text = icon; textSize = 15f
                setPadding(0, 0, dp(activity, 10), 0)
            })
            val col = LinearLayout(activity).apply { orientation = LinearLayout.VERTICAL }
            col.addView(TextView(activity).apply { text = title; textSize = 11f; setTextColor(ts) })
            valueView.textSize = 15f
            valueView.typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            col.addView(valueView, LinearLayout.LayoutParams(-2, -2).apply { topMargin = dp(activity, 1) })
            row.addView(col, LinearLayout.LayoutParams(0, -2, 1f))
            return row
        }

        val youValue = TextView(activity).apply {
            setTextColor(tp)
            text = "🇷🇺 ${activity.getString(R.string.map_russia)}"
        }
        val exitValue = TextView(activity).apply {
            setTextColor(if (stateStarted) teal else ts)
            text = if (stateStarted) {
                if (activeFlag.isNotEmpty()) "$activeFlag ${countryName(activeFlag)}"
                else activeTag.ifEmpty { activity.getString(R.string.monitor_via_tunnel) }
            } else activity.getString(R.string.map_no_active)
        }
        card.addView(endpointRow("📍", activity.getString(R.string.map_you), youValue))

        // стрелка-разделитель
        val arrowRow = LinearLayout(activity).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        arrowRow.addView(View(activity).apply {
            background = GradientDrawable().apply {
                cornerRadius = dp(activity, 2).toFloat()
                setColor((teal.toLong() and 0x40FFFFFFL).toInt())
            }
        }, LinearLayout.LayoutParams(0, dp(activity, 2), 1f).apply { marginEnd = dp(activity, 8) })
        arrowRow.addView(TextView(activity).apply {
            text = "↓"; textSize = 13f; setTextColor(teal); typeface = Typeface.DEFAULT_BOLD
        })
        arrowRow.addView(View(activity).apply {
            background = GradientDrawable().apply {
                cornerRadius = dp(activity, 2).toFloat()
                setColor((teal.toLong() and 0x40FFFFFFL).toInt())
            }
        }, LinearLayout.LayoutParams(0, dp(activity, 2), 1f).apply { marginStart = dp(activity, 8) })
        card.addView(arrowRow, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(activity, 10); bottomMargin = dp(activity, 10) })

        card.addView(endpointRow("🌐", activity.getString(R.string.map_exit), exitValue))
        bottomWrap.addView(card, LinearLayout.LayoutParams(-1, -2))
        root.addView(bottomWrap, FrameLayout.LayoutParams(-1, -2, Gravity.BOTTOM))

        // ===== GeoIP: уточняет города и (если нет флага) страну выхода =====
        GlobalScope.launch(Dispatchers.IO) {
            val geo = runCatching {
                val proxy = if (stateStarted) Proxy(Proxy.Type.SOCKS, InetSocketAddress("127.0.0.1", MihomoRuntimeDefaults.MIXED_PORT)) else null
                val conn = (URL("https://api.ip.sb/geoip?t=" + System.currentTimeMillis()).let { u ->
                    proxy?.let { p -> u.openConnection(p) } ?: u.openConnection()
                }) as HttpURLConnection
                conn.connectTimeout = 6000; conn.readTimeout = 8000
                conn.setRequestProperty("User-Agent", "EnterpriseShield/1.0 (Android)")
                conn.setRequestProperty("Connection", "close")
                JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
            }.getOrNull()
            if (geo == null) return@launch
            val cc = geo.optString("country_code", "")
            val city = geo.optString("city", "")
            val country = geo.optString("country", "")
            launch(Dispatchers.Main) {
                if (!stateStarted && cc.length == 2) {
                    youValue.text = "${flagOf(cc)} $country${if (city.isNotEmpty()) " · $city" else ""}"
                    val a = WorldMapAsset.anchorOf(cc, countries)
                    if (a != null) {
                        mapView.myPin = WorldMapView.Pin(a[0], a[1], flagOf(cc), city.ifEmpty { country })
                        mapView.pinsUpdated()
                    }
                } else if (stateStarted) {
                    // под Щитом geoip = точка выхода
                    if (city.isNotEmpty() || country.isNotEmpty()) {
                        exitValue.text = "${if (cc.length == 2) flagOf(cc) else "🌐"} " +
                                (if (city.isNotEmpty()) "$city · " else "") + country +
                                (if (activeTag.isNotEmpty()) " · $activeTag" else "")
                    }
                    if (exitIso == null && cc.length == 2) {
                        val a = WorldMapAsset.anchorOf(cc, countries)
                        if (a != null) {
                            mapView.serverPin = WorldMapView.Pin(a[0], a[1], flagOf(cc), city.ifEmpty { country }, accent = true)
                            mapView.pinsUpdated()
                        }
                    }
                }
            }
        }
        log(activity, "buildScreen:done")
        return root
    }

    private fun flagOf(cc: String): String {
        val sb = StringBuilder()
        for (c in cc.uppercase()) sb.append(Character.toChars(0x1F1E6 + (c - 'A')))
        return sb.toString()
    }

    private fun countryName(flag: String): String {
        val code = WorldMapAsset.flagToCode(flag) ?: return ""
        return mapOf(
            "RU" to "Россия", "NL" to "Нидерланды", "DE" to "Германия", "US" to "США",
            "FI" to "Финляндия", "PL" to "Польша", "FR" to "Франция", "GB" to "Британия",
            "CH" to "Швейцария", "TR" to "Турция", "KZ" to "Казахстан", "SE" to "Швеция",
            "EE" to "Эстония", "AT" to "Австрия", "UA" to "Украина", "CA" to "Канада",
            "SG" to "Сингапур", "JP" to "Япония", "HK" to "Гонконг", "AM" to "Армения",
        )[code] ?: code
    }

    private fun pal(activity: Activity): com.whitedns.vpn.WhiteDnsPalette =
        com.whitedns.vpn.WhiteDnsDesignTokens.forContext(activity)

    private fun dp(activity: Activity, v: Int): Int = (v * activity.resources.displayMetrics.density).toInt()
}
