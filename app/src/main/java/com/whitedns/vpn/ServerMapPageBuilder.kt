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
 * Экран «Карта каналов» — полноэкранная интерактивная карта в стиле оригинала:
 * заголовок сверху, RouteBadge с городами внизу (GeoIP), pinch-зум, неон, дуга маршрута.
 */
object ServerMapPageBuilder {

    fun buildScreen(activity: Activity): View {
        val bg = paletteColor(activity, "background", 0xFF101418)
        val surface = paletteColor(activity, "surface", 0xFF1A2028)
        val tp = paletteColor(activity, "textPrimary", 0xFFE8EDF2)
        val ts = paletteColor(activity, "textSecondary", 0xFF8A94A0)
        val teal = paletteColor(activity, "teal", 0xFF4DD0C4)

        val prefs = activity.getSharedPreferences("white_dns_runtime_state", Activity.MODE_PRIVATE)
        val stateStarted = prefs.getString("state", "") == "Started"
        val activeFlag = prefs.getString("connection_country_flag", "").orEmpty()
        val activeTag = prefs.getString("active_connection_tag", "").orEmpty()

        val root = FrameLayout(activity).apply { setBackgroundColor(bg) }

        // ===== Карта на весь экран =====
        val mapView = WorldMapView(activity).apply {
            colorBg = bg
            colorLand = blendHex(surface, 0xFF14202B.toInt(), 0.5f)
            colorNeon = teal
            colorAccent = 0xFF9FE8DB.toInt()
            colorDim = ts
            seaLabels = listOf(
                WorldMapView.SeaLabel(51.8f, 26.8f, activity.getString(R.string.map_sea_persian_gulf), 6.5f),
                WorldMapView.SeaLabel(50.9f, 41.6f, activity.getString(R.string.map_sea_caspian), 4.0f),
            )
        }
        root.addView(mapView, FrameLayout.LayoutParams(-1, -1))

        // ===== Данные каналов =====
        val names = mutableListOf<String>()
        runCatching {
            for (line in ShieldChannelsAsset.yaml.lineSequence()) {
                val t = line.trim()
                if (t.startsWith("- name: ") && !t.contains("Каналы") && !t.contains("Авто")) {
                    names.add(t.removePrefix("- name: ").trim().trim('"'))
                }
            }
        }
        val nodes = names.mapNotNull { n ->
            val f = WorldMapAsset.flagFromName(n) ?: return@mapNotNull null
            WorldMapView.Node(n, WorldMapAsset.flagToCode(f), f, stateStarted && f == activeFlag && activeTag.isNotEmpty())
        }
        mapView.nodes = nodes
        mapView.loadMap(activity)

        // точка юзера: RU по умолчанию; уточняется GeoIP ниже
        mapView.myPoint = anchorNorm(activity, "RU")
        val myCityBox = arrayOf<String?>(null)
        val exitBox = arrayOf<String?>(null)   // город/страна выхода (GeoIP)

        // ===== Заголовок (полупрозрачный поверх карты) =====
        val header = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(activity, 20), dp(activity, 12), dp(activity, 20), 0)
        }
        header.addView(TextView(activity).apply {
            text = activity.getString(R.string.map_page_title)
            textSize = 24f
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            setTextColor(tp)
            setShadowLayer(8f, 0f, 0f, bg)
        })
        header.addView(TextView(activity).apply {
            text = activity.getString(R.string.map_subtitle, nodes.size)
            textSize = 13f; setTextColor(ts)
            setShadowLayer(6f, 0f, 0f, bg)
        }, LinearLayout.LayoutParams(-2, -2).apply { topMargin = dp(activity, 2) })
        root.addView(header, FrameLayout.LayoutParams(-2, -2, Gravity.TOP or Gravity.START))

        // ===== RouteBadge внизу =====
        val badge = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = GradientDrawable().apply {
                cornerRadius = dp(activity, 18).toFloat(); setColor(surface)
                setStroke(dp(activity, 1).toInt(), (teal and 0x33FFFFFF))
            }
            setPadding(dp(activity, 16), dp(activity, 12), dp(activity, 16), dp(activity, 12))
        }
        fun badgeCol(titleRes: Int, flag: String, place: String): LinearLayout {
            val col = LinearLayout(activity).apply { orientation = LinearLayout.VERTICAL }
            col.addView(TextView(activity).apply {
                text = activity.getString(titleRes); textSize = 11f; setTextColor(ts)
            })
            col.addView(TextView(activity).apply {
                text = if (place.isEmpty()) activity.getString(R.string.map_unknown) else "$flag $place"
                textSize = 15f
                typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
                setTextColor(tp)
            }, LinearLayout.LayoutParams(-2, -2).apply { topMargin = dp(activity, 2) })
            return col
        }
        val fromFlag = "🇷🇺"
        val fromText = arrayOf(activity.getString(R.string.map_russia))
        val toFlag = arrayOf(if (activeFlag.isNotEmpty()) activeFlag else "🌐")
        val toText = arrayOf(if (stateStarted && activeTag.isNotEmpty()) countryNameFromFlag(activeFlag) else activity.getString(R.string.map_no_active))
        val fromCol = badgeCol(R.string.map_from, fromFlag, fromText[0])
        val toCol = badgeCol(R.string.map_to, toFlag[0], toText[0])

        // линия-коннектор между колонками
        val connector = View(activity).apply {
            background = GradientDrawable().apply {
                cornerRadius = dp(activity, 2).toFloat(); setColor(teal and 0x55FFFFFF)
            }
        }
        badge.addView(fromCol, LinearLayout.LayoutParams(0, -2, 1f))
        badge.addView(connector, LinearLayout.LayoutParams(dp(activity, 46), dp(activity, 2)).apply {
            setMargins(dp(activity, 10), 0, dp(activity, 10), 0)
        })
        badge.addView(toCol, LinearLayout.LayoutParams(0, -2, 1f))

        val badgeWrap = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(activity, 16), 0, dp(activity, 16), dp(activity, 14))
        }
        badgeWrap.addView(badge, LinearLayout.LayoutParams(-1, -2))
        root.addView(badgeWrap, FrameLayout.LayoutParams(-1, -2, Gravity.BOTTOM))

        // ===== GeoIP: страна/город юзера и выхода (как в оригинале) =====
        fun fetchGeo(): JSONObject? = runCatching {
            val proxy = if (stateStarted) Proxy(Proxy.Type.SOCKS, InetSocketAddress("127.0.0.1", MihomoRuntimeDefaults.MIXED_PORT)) else null
            val conn = (URL("https://api.ip.sb/geoip").let { u -> proxy?.let { p -> u.openConnection(p) } ?: u.openConnection() }) as java.net.HttpURLConnection
            conn.connectTimeout = 6000; conn.readTimeout = 8000
            JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
        }.getOrNull()

        GlobalScope.launch(Dispatchers.IO) {
            val geo = fetchGeo()
            if (geo != null) {
                val cc = geo.optString("country_code", "")
                val city = geo.optString("city", "")
                launch(Dispatchers.Main) {
                    if (cc.length == 2) {
                        val anchor = anchorNorm(activity, cc)
                        if (anchor != null) {
                            mapView.myPoint = anchor
                            mapView.myFlag = codeToFlag(cc)
                            mapView.myCity = city
                        }
                    }
                    // точка выхода: если под Щитом — это страна активного канала, geoip подтверждает город
                    val exitCity = city
                    if (exitCity.isNotEmpty() && stateStarted) {
                        toText[0] = countryNameFromFlag(activeFlag).ifEmpty { geo.optString("country", cc) }
                        (toCol.getChildAt(1) as? TextView)?.text = "$toFlag[0] $exitCity · ${toText[0]}"
                    } else if (cc.length == 2 && !stateStarted) {
                        // Щит выключен: показываем реальную страну юзера справа как «нет маршрута»
                    }
                }
            }
        }

        return root
    }

    private fun anchorNorm(activity: Activity, code: String): Pair<Float, Float>? {
        val list = WorldMapAsset.countries(activity)
        val a = WorldMapAsset.anchorOf(code, list) ?: return null
        return a[0] to a[1]
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

    private fun blendHex(a: Int, b: Int, t: Float): Int {
        val ar = (a shr 16 and 0xFF); val ag = (a shr 8 and 0xFF); val ab = (a and 0xFF)
        val br = (b shr 16 and 0xFF); val bg = (b shr 8 and 0xFF); val bb = (b and 0xFF)
        val r = (ar + (br - ar) * t).toInt(); val g = (ag + (bg - ag) * t).toInt(); val bl = (ab + (bb - ab) * t).toInt()
        return (0xFF shl 24) or (r shl 16) or (g shl 8) or bl
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
