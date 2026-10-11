package com.whitedns.vpn

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/**
 * Экран «Карта каналов»: мировая карта с точками серверов Щита,
 * подсветка активного канала, анимированная дуга подключения.
 */
object ServerMapPageBuilder {

    fun buildScreen(activity: Activity): View {
        val bg = paletteColor(activity, "background", 0xFF101418)
        val surface = paletteColor(activity, "surface", 0xFF1A2028)
        val tp = paletteColor(activity, "textPrimary", 0xFFE8EDF2)
        val ts = paletteColor(activity, "textSecondary", 0xFF8A94A0)
        val teal = paletteColor(activity, "teal", 0xFF4DD0C4)

        val root = FrameLayout(activity).apply { setBackgroundColor(bg) }
        val main = LinearLayout(activity).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(bg) }
        root.addView(main, FrameLayout.LayoutParams(-1, -1))

        // ===== Заголовок =====
        val header = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(activity, 20), dp(activity, 18), dp(activity, 20), dp(activity, 6))
        }
        header.addView(TextView(activity).apply {
            text = activity.getString(R.string.map_page_title)
            textSize = 20f
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            setTextColor(tp)
            gravity = Gravity.CENTER_HORIZONTAL
        })
        val subtitle = TextView(activity).apply {
            textSize = 13f; setTextColor(ts); gravity = Gravity.CENTER_HORIZONTAL
        }
        header.addView(subtitle, LinearLayout.LayoutParams(-2, -2).apply { topMargin = dp(activity, 2) })
        main.addView(header)

        // ===== Карта =====
        val mapView = WorldMapView(activity).apply {
            colorBg = bg
            colorLand = surface
            colorLandActive = teal and 0x30FFFFFF or (teal ushr 1) and 0x007F7F7F or 0x001A3A40
            colorStroke = teal and 0x66FFFFFF
            colorDot = teal
            colorDotActive = 0xFFB7F0E3.toInt()
            colorArc = teal
        }
        val mapCard = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                cornerRadius = dp(activity, 18).toFloat(); setColor(surface)
            }
            setPadding(dp(activity, 8), dp(activity, 8), dp(activity, 8), dp(activity, 8))
            clipToOutline = true
        }
        mapCard.addView(mapView, LinearLayout.LayoutParams(-1, 0, 1f))
        val mapHolder = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(activity, 16), dp(activity, 6), dp(activity, 16), 0)
        }
        mapHolder.addView(mapCard, LinearLayout.LayoutParams(-1, 0, 1f))
        main.addView(mapHolder, LinearLayout.LayoutParams(-1, 0, 1.15f))

        // ===== Легенда =====
        val scroll = ScrollView(activity).apply { setBackgroundColor(bg); isFillViewport = true }
        val legend = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(activity, 20), dp(activity, 12), dp(activity, 20), dp(activity, 32))
        }
        scroll.addView(legend)
        main.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        // данные: активный канал + флаг из рантайм-стора
        val prefs = activity.getSharedPreferences("white_dns_runtime_state", Activity.MODE_PRIVATE)
        val stateStarted = prefs.getString("state", "") == "started"
        val activeFlag = prefs.getString("connection_country_flag", "").orEmpty()
        val activeTag = prefs.getString("active_connection_tag", "").orEmpty()

        // все каналы из ассета
        val names = mutableListOf<String>()
        runCatching {
            val yaml = ShieldChannelsAsset.yaml
            for (line in yaml.lineSequence()) {
                val t = line.trim()
                if (t.startsWith("- name: ") && !t.contains("Каналы") && !t.contains("Авто")) {
                    names.add(t.removePrefix("- name: ").trim().trim('"'))
                }
            }
        }
        // группировка по странам (по флагу в имени)
        val byCountry = LinkedHashMap<String, Int>()
        for (n in names) {
            val f = WorldMapAsset.flagFromName(n)
            val key = f ?: "—"
            byCountry[key] = (byCountry[key] ?: 0) + 1
        }
        val total = names.size
        val countries = byCountry.count { it.key != "—" }
        subtitle.text = "$total каналов · $countries стран"

        // узлы для карты
        val nodes = names.mapNotNull { n ->
            val f = WorldMapAsset.flagFromName(n) ?: return@mapNotNull null
            WorldMapView.Node(n, WorldMapAsset.flagToCode(f), stateStarted && f == activeFlag && activeTag.isNotEmpty())
        }
        mapView.nodes = nodes
        mapView.loadMap(activity)

        // карточка активного подключения
        val activeCard = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                cornerRadius = dp(activity, 16).toFloat(); setColor(surface)
            }
            setPadding(dp(activity, 16), dp(activity, 14), dp(activity, 16), dp(activity, 14))
        }
        activeCard.addView(TextView(activity).apply {
            text = activity.getString(R.string.map_active_connection)
            textSize = 12f; setTextColor(ts)
        })
        activeCard.addView(TextView(activity).apply {
            text = if (stateStarted && activeTag.isNotEmpty()) "$activeFlag $activeTag"
            else activity.getString(R.string.map_no_active)
            textSize = 16f
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            setTextColor(if (stateStarted) teal else ts)
        }, LinearLayout.LayoutParams(-2, -2).apply { topMargin = dp(activity, 4) })
        legend.addView(activeCard)

        // топ стран
        legend.addView(TextView(activity).apply {
            text = activity.getString(R.string.map_by_country)
            textSize = 12f; setTextColor(ts)
        }, LinearLayout.LayoutParams(-2, -2).apply { topMargin = dp(activity, 16) })
        val sorted = byCountry.entries.filter { it.key != "—" }.sortedByDescending { it.value }.take(10)
        for ((flag, cnt) in sorted) {
            val row = LinearLayout(activity).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
            row.addView(TextView(activity).apply { text = flag; textSize = 16f })
            row.addView(TextView(activity).apply {
                text = "×$cnt"; textSize = 13f; setTextColor(tp); setPadding(dp(activity, 10), 0, 0, 0)
            })
            row.addView(View(activity), LinearLayout.LayoutParams(0, 1, 1f))
            row.addView(TextView(activity).apply {
                text = cnt.toString(); textSize = 13f; setTextColor(ts)
            })
            legend.addView(row, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(activity, 8) })
        }

        return root
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
