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
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URL

/**
 * Экран «Карта» — упрощённая надёжная версия:
 * карта на весь экран, заголовок, две подписи (юзер/выход) внизу.
 * Каждый шаг открытия логируется в diagnostics.txt (filesDir) для отладки.
 */
object ServerMapPageBuilder {

    private fun log(activity: Activity, step: String, detail: String = "") {
        runCatching {
            val f = java.io.File(activity.filesDir, "map_debug.txt")
            f.appendText("${System.currentTimeMillis()} $step $detail\n")
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
        log(activity, "state", "started=$stateStarted flag=$activeFlag")

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

        // ===== страны из ассета =====
        val countries = WorldMapAsset.countries(activity)
        log(activity, "countries:loaded", "n=${countries.size}")

        // пины
        val ruAnchor = WorldMapAsset.anchorOf("RU", countries)
        if (ruAnchor != null) {
            mapView.myPin = WorldMapView.Pin(ruAnchor[0], ruAnchor[1], "🇷🇺", activity.getString(R.string.map_russia))
        }
        val exitIso = WorldMapAsset.flagToCode(activeFlag)
        val exitAnchor = if (stateStarted && exitIso != null) WorldMapAsset.anchorOf(exitIso, countries) else null
        if (exitAnchor != null) {
            mapView.serverPin = WorldMapView.Pin(exitAnchor[0], exitAnchor[1], activeFlag, countryName(activeFlag), accent = true)
        }
        mapView.loadMap(activity)
        mapView.pinsUpdated()
        log(activity, "map:loaded", "ru=${ruAnchor != null} exit=${exitAnchor != null}")

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
        header.addView(TextView(activity).apply {
            text = activity.getString(R.string.map_subtitle_orig)
            textSize = 13f; setTextColor(ts)
            setShadowLayer(8f, 0f, 0f, bg)
        }, LinearLayout.LayoutParams(-2, -2).apply { topMargin = dp(activity, 2) })
        root.addView(header, FrameLayout.LayoutParams(-2, -2, Gravity.TOP or Gravity.START))

        // ===== нижняя карточка =====
        val bottomWrap = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(activity, 16), 0, dp(activity, 16), dp(activity, 12))
        }
        val card = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(activity, 20).toFloat()
                setColor((pal(activity).surface.toLong() and 0xFFFFFFD1L).toInt())
                setStroke(dp(activity, 1), (teal.toLong() and 0x26FFFFFFL).toInt())
            }
            setPadding(dp(activity, 18), dp(activity, 16), dp(activity, 18), dp(activity, 16))
        }
        val youValue = TextView(activity).apply {
            text = "🇷🇺 ${activity.getString(R.string.map_russia)}"
            textSize = 15f; setTextColor(tp)
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
        }
        val exitValue = TextView(activity).apply {
            text = if (exitAnchor != null) "$activeFlag ${countryName(activeFlag)}"
            else activity.getString(R.string.map_no_active)
            textSize = 15f
            setTextColor(if (stateStarted) teal else ts)
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
        }
        card.addView(TextView(activity).apply {
            text = activity.getString(R.string.map_you); textSize = 12f; setTextColor(ts)
        })
        card.addView(youValue, LinearLayout.LayoutParams(-2, -2).apply { topMargin = dp(activity, 2) })
        card.addView(TextView(activity).apply {
            text = activity.getString(R.string.map_exit); textSize = 12f; setTextColor(ts)
        }, LinearLayout.LayoutParams(-2, -2).apply { topMargin = dp(activity, 12) })
        card.addView(exitValue, LinearLayout.LayoutParams(-2, -2).apply { topMargin = dp(activity, 2) })
        bottomWrap.addView(card, LinearLayout.LayoutParams(-1, -2))
        root.addView(bottomWrap, FrameLayout.LayoutParams(-1, -2, Gravity.BOTTOM))

        // ===== GeoIP (город юзера при выключенном, город выхода при включённом) =====
        GlobalScope.launch(Dispatchers.IO) {
            val geo = runCatching {
                val proxy = if (stateStarted) Proxy(Proxy.Type.SOCKS, InetSocketAddress("127.0.0.1", MihomoRuntimeDefaults.MIXED_PORT)) else null
                val conn = (URL("https://api.ip.sb/geoip?t=" + System.currentTimeMillis()).let { u ->
                    proxy?.let { p -> u.openConnection(p) } ?: u.openConnection()
                }) as java.net.HttpURLConnection
                conn.connectTimeout = 6000; conn.readTimeout = 8000
                conn.setRequestProperty("User-Agent", "EnterpriseShield/1.0 (Android)")
                conn.setRequestProperty("Connection", "close")
                JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
            }.getOrNull()
            if (geo != null) launch(Dispatchers.Main) {
                val cc = geo.optString("country_code", "")
                val city = geo.optString("city", "")
                if (!stateStarted && cc.length == 2) {
                    youValue.text = "${flagOf(cc)} ${geo.optString("country", "")}${if (city.isNotEmpty()) " · $city" else ""}"
                } else if (stateStarted && city.isNotEmpty()) {
                    exitValue.text = "$activeFlag $city · ${geo.optString("country", "")}"
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
