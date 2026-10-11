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
 * Экран «Карта» как в оригинале: чистая неоновая карта мира, два пина (юзер + выход),
 * glow-дуга, внизу карточка «Вы здесь / Выход» с городами и GeoIP.
 */
object ServerMapPageBuilder {

    fun buildScreen(activity: Activity): View {
        val bg = pal(activity).background
        val surface = pal(activity).surface
        val tp = pal(activity).textPrimary
        val ts = pal(activity).textSecondary
        val teal = pal(activity).teal

        val prefs = activity.getSharedPreferences("white_dns_runtime_state", Activity.MODE_PRIVATE)
        val stateStarted = prefs.getString("state", "") == "Started"
        val activeFlag = prefs.getString("connection_country_flag", "").orEmpty()
        val activeTag = prefs.getString("active_connection_tag", "").orEmpty()

        val root = FrameLayout(activity).apply { setBackgroundColor(bg) }

        // ===== карта =====
        val mapView = WorldMapView(activity).apply {
            colorBg = bg
            colorNeon = teal
            colorAccent = 0xFF8FE8D8.toInt()
            colorText = tp
            seaLabels = listOf(
                WorldMapView.SeaLabel(51.8f, 26.8f, activity.getString(R.string.map_sea_persian_gulf), 6.5f),
                WorldMapView.SeaLabel(50.9f, 41.6f, activity.getString(R.string.map_sea_caspian), 4.0f),
            )
        }
        mapView.loadMap(activity)

        // пин юзера: RU по умолчанию (наш юзер — РФ); уточнение GeoIP если Щит выключен
        var myAnchor = anchorOf(activity, "RU")
        mapView.myPin = WorldMapView.Pin(myAnchor.first, myAnchor.second, "🇷🇺", activity.getString(R.string.map_russia))
        // пин выхода: по флагу активного канала
        val exitIso = WorldMapAsset.flagToCode(activeFlag)
        val exitAnchor = if (stateStarted && exitIso != null) anchorOf(activity, exitIso) else null
        if (exitAnchor != null) {
            mapView.serverPin = WorldMapView.Pin(exitAnchor.first, exitAnchor.second, activeFlag, countryNameFromFlag(activeFlag), accent = true)
        }

        // ===== заголовок =====
        val header = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(activity, 20), dp(activity, 10), dp(activity, 20), 0)
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
        val bottom = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(activity, 16), 0, dp(activity, 16), dp(activity, 12))
        }
        val card = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                cornerRadius = dp(activity, 24).toFloat()
                setColor((surface.toLong() and 0xFFFFFFD1L).toInt())
                setStroke(dp(activity, 1).toInt(), teal and 0x26FFFFFF)
            }
            setPadding(dp(activity, 18), dp(activity, 16), dp(activity, 18), dp(activity, 16))
        }
        bottom.addView(card, LinearLayout.LayoutParams(-1, -2))
        root.addView(bottom, FrameLayout.LayoutParams(-1, -2, Gravity.BOTTOM))

        fun endpointRow(icon: String, title: String, value: TextView): LinearLayout {
            val row = LinearLayout(activity).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
            row.addView(TextView(activity).apply {
                text = icon; textSize = 17f
                setPadding(0, 0, dp(activity, 10), 0)
            })
            val col = LinearLayout(activity).apply { orientation = LinearLayout.VERTICAL }
            col.addView(TextView(activity).apply { text = title; textSize = 12f; setTextColor(ts) })
            value.textSize = 15f
            value.typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            value.setTextColor(tp)
            col.addView(value, LinearLayout.LayoutParams(-2, -2).apply { topMargin = dp(activity, 1) })
            row.addView(col, LinearLayout.LayoutParams(0, -2, 1f))
            return row
        }

        val youValue = TextView(activity).apply { text = activity.getString(R.string.map_russia) }
        val exitValue = TextView(activity).apply {
            text = if (stateStarted && exitIso != null) "$activeFlag ${countryNameFromFlag(activeFlag)}"
            else activity.getString(R.string.map_no_active)
            setTextColor(if (stateStarted) teal else ts)
        }
        card.addView(endpointRow("📍", activity.getString(R.string.map_you), youValue))
        card.addView(endpointRow("🌐", activity.getString(R.string.map_exit), exitValue),
            LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(activity, 14) })

        // статус-строка как в оригинале
        val statusLine = TextView(activity).apply {
            textSize = 12f; setTextColor(ts)
            text = if (stateStarted) "" else activity.getString(R.string.map_offline_here)
        }
        card.addView(statusLine, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(activity, 12) })

        // ===== GeoIP =====
        fun fetchGeo(useSocks: Boolean): JSONObject? = runCatching {
            val proxy = if (useSocks) Proxy(Proxy.Type.SOCKS, InetSocketAddress("127.0.0.1", MihomoRuntimeDefaults.MIXED_PORT)) else null
            val conn = (URL("https://api.ip.sb/geoip").let { u -> proxy?.let { p -> u.openConnection(p) } ?: u.openConnection() }) as java.net.HttpURLConnection
            conn.connectTimeout = 6000; conn.readTimeout = 8000
            JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
        }.getOrNull()

        GlobalScope.launch(Dispatchers.IO) {
            if (stateStarted) {
                // под Щитом: geoip покажет точку выхода — уточняем город сервера
                val geo = fetchGeo(useSocks = true)
                val city = geo?.optString("city").orEmpty()
                val country = geo?.optString("country").orEmpty()
                launch(Dispatchers.Main) {
                    if (city.isNotEmpty() || country.isNotEmpty()) {
                        exitValue.text = "$activeFlag $city${if (city.isNotEmpty() && country.isNotEmpty()) ", " else ""}$country"
                        if (exitAnchor != null && mapView.serverPin != null) {
                            mapView.serverPin = WorldMapView.Pin(
                                exitAnchor.first, exitAnchor.second, activeFlag,
                                city.ifEmpty { country }, accent = true
                            )
                            mapView.pinsUpdated()
                        }
                    }
                }
            } else {
                // Щит выключен: определяем реальное местоположение юзера
                val geo = fetchGeo(useSocks = false)
                val cc = geo?.optString("country_code").orEmpty()
                val city = geo?.optString("city").orEmpty()
                launch(Dispatchers.Main) {
                    if (cc.length == 2) {
                        val a = anchorOf(activity, cc) ?: return@launch
                        mapView.myPin = WorldMapView.Pin(a.first, a.second, codeToFlag(cc),
                            city.ifEmpty { geo?.optString("country").orEmpty().ifEmpty { cc } })
                        mapView.pinsUpdated()
                        youValue.text = "${codeToFlag(cc)} $city${if (city.isNotEmpty()) ", " else ""}${geo?.optString("country").orEmpty()}"
                    }
                }
            }
        }

        return root
    }

    private fun anchorOf(activity: Activity, code: String): Pair<Float, Float> {
        val list = WorldMapAsset.countries(activity)
        val a = WorldMapAsset.anchorOf(code, list)
        return (a?.get(0) ?: 32767f) to (a?.get(1) ?: 16383f)
    }

    private fun codeToFlag(cc: String): String {
        val sb = StringBuilder()
        for (c in cc.uppercase()) sb.append(Character.toChars(0x1F1E6 + (c - 'A')))
        return sb.toString()
    }

    private fun countryNameFromFlag(flag: String): String {
        val code = WorldMapAsset.flagToCode(flag) ?: return ""
        return mapOf(
            "RU" to "Россия", "NL" to "Нидерланды", "DE" to "Германия", "US" to "США",
            "FI" to "Финляндия", "PL" to "Польша", "FR" to "Франция", "GB" to "Британия",
            "CH" to "Швейцария", "TR" to "Турция", "KZ" to "Казахстан", "SE" to "Швеция",
            "EE" to "Эстония", "AT" to "Австрия", "BG" to "Болгария", "CZ" to "Чехия",
            "HU" to "Венгрия", "LV" to "Латвия", "LT" to "Литва", "MD" to "Молдова",
            "RO" to "Румыния", "SK" to "Словакия", "IT" to "Италия", "ES" to "Испания",
            "CA" to "Канада", "SG" to "Сингапур", "JP" to "Япония", "AM" to "Армения",
            "AZ" to "Азербайджан", "UA" to "Украина", "BY" to "Беларусь", "BE" to "Бельгия",
            "HK" to "Гонконг", "IL" to "Израиль", "IN" to "Индия", "KG" to "Киргизия",
            "MY" to "Малайзия", "NO" to "Норвегия", "PT" to "Португалия", "RS" to "Сербия",
            "VN" to "Вьетнам", "AR" to "Аргентина",
        )[code] ?: code
    }

    private fun pal(activity: Activity): com.whitedns.vpn.WhiteDnsPalette =
        com.whitedns.vpn.WhiteDnsDesignTokens.forContext(activity)

    private fun dp(activity: Activity, v: Int): Int = (v * activity.resources.displayMetrics.density).toInt()
}
