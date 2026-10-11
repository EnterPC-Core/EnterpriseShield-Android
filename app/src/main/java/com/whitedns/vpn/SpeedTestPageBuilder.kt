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
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Экран «Спид-тест»: замер скорости через ядро Щита (SOCKS5 127.0.0.1:2080).
 * Cloudflare-методика: адаптивные волны загрузка/отдача, idle/loaded пинг -> bufferbloat.
 */
object SpeedTestPageBuilder {

    private var engine: SpeedTestEngine? = null

    fun buildScreen(activity: Activity): View {
        val bg = paletteColor(activity, "background", 0xFF101418)
        val surface = paletteColor(activity, "surface", 0xFF1A2028)
        val tp = paletteColor(activity, "textPrimary", 0xFFE8EDF2)
        val ts = paletteColor(activity, "textSecondary", 0xFF8A94A0)
        val teal = paletteColor(activity, "teal", 0xFF4DD0C4)

        val prefs = activity.getSharedPreferences("white_dns_runtime_state", Activity.MODE_PRIVATE)
        val stateStarted = prefs.getString("state", "") == "started"

        val root = FrameLayout(activity).apply { setBackgroundColor(bg) }
        val scroll = ScrollView(activity).apply { setBackgroundColor(bg); isFillViewport = true }
        val content = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(activity, 20), dp(activity, 20), dp(activity, 20), dp(activity, 40))
        }
        scroll.addView(content)
        root.addView(scroll, FrameLayout.LayoutParams(-1, -1))

        content.addView(TextView(activity).apply {
            text = activity.getString(R.string.speedtest_page_title)
            textSize = 20f
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            setTextColor(tp)
        })

        // ===== Главная цифра =====
        val mainCard = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            background = GradientDrawable().apply {
                cornerRadius = dp(activity, 18).toFloat(); setColor(surface)
            }
            setPadding(dp(activity, 16), dp(activity, 20), dp(activity, 16), dp(activity, 20))
        }
        val phaseView = TextView(activity).apply {
            text = activity.getString(R.string.speedtest_phase_idle)
            textSize = 13f; setTextColor(ts); gravity = Gravity.CENTER_HORIZONTAL
        }
        mainCard.addView(phaseView)
        val bigValue = TextView(activity).apply {
            text = "—"
            textSize = 56f
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            setTextColor(tp); gravity = Gravity.CENTER_HORIZONTAL
        }
        mainCard.addView(bigValue, LinearLayout.LayoutParams(-2, -2).apply { topMargin = dp(activity, 6) })
        val bigUnit = TextView(activity).apply {
            text = activity.getString(R.string.speedtest_unit_mbps)
            textSize = 14f; setTextColor(ts); gravity = Gravity.CENTER_HORIZONTAL
        }
        mainCard.addView(bigUnit)
        content.addView(mainCard, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(activity, 14) })

        // ===== Статистика =====
        val statCard = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                cornerRadius = dp(activity, 16).toFloat(); setColor(surface)
            }
            setPadding(dp(activity, 16), dp(activity, 14), dp(activity, 16), dp(activity, 14))
        }
        fun statRow(label: String, valueView: TextView) {
            val row = LinearLayout(activity).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
            row.addView(TextView(activity).apply { text = label; textSize = 14f; setTextColor(ts) })
            row.addView(LinearLayout(activity), LinearLayout.LayoutParams(0, 1, 1f))
            row.addView(valueView, LinearLayout.LayoutParams(-2, -2))
            statCard.addView(row, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(activity, 8) })
        }
        val downView = TextView(activity).apply { text = "—"; textSize = 15f; setTextColor(tp); typeface = Typeface.DEFAULT_BOLD }
        val upView = TextView(activity).apply { text = "—"; textSize = 15f; setTextColor(tp); typeface = Typeface.DEFAULT_BOLD }
        val idleView = TextView(activity).apply { text = "—"; textSize = 15f; setTextColor(tp); typeface = Typeface.DEFAULT_BOLD }
        val loadedView = TextView(activity).apply { text = "—"; textSize = 15f; setTextColor(tp); typeface = Typeface.DEFAULT_BOLD }
        val bloatView = TextView(activity).apply { text = "—"; textSize = 15f; setTextColor(tp); typeface = Typeface.DEFAULT_BOLD }
        statRow(activity.getString(R.string.speedtest_down), downView)
        statRow(activity.getString(R.string.speedtest_up), upView)
        statRow(activity.getString(R.string.speedtest_ping_idle), idleView)
        statRow(activity.getString(R.string.speedtest_ping_loaded), loadedView)
        statRow(activity.getString(R.string.speedtest_bufferbloat), bloatView)
        content.addView(statCard, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(activity, 12) })

        // ===== Примечание =====
        content.addView(TextView(activity).apply {
            text = activity.getString(R.string.speedtest_note)
            textSize = 12f; setTextColor(ts)
        }, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(activity, 12) })

        // ===== Кнопка =====
        val startBtn = MaterialButton(activity).apply { text = activity.getString(R.string.speedtest_start) }
        content.addView(startBtn, LinearLayout.LayoutParams(-1, dp(activity, 52)).apply { topMargin = dp(activity, 18) })

        fun fmt(v: Double) = if (v >= 100) "%.0f".format(v) else "%.1f".format(v)

        fun applyState(s: SpeedTestEngine.State) {
            GlobalScope.launch(Dispatchers.Main) {
                phaseView.text = when (s.phase) {
                    SpeedTestEngine.Phase.Idle -> activity.getString(R.string.speedtest_phase_idle)
                    SpeedTestEngine.Phase.Warmup -> activity.getString(R.string.speedtest_phase_warmup)
                    SpeedTestEngine.Phase.Download -> activity.getString(R.string.speedtest_phase_download)
                    SpeedTestEngine.Phase.Upload -> activity.getString(R.string.speedtest_phase_upload)
                    SpeedTestEngine.Phase.Done -> activity.getString(R.string.speedtest_phase_done)
                    SpeedTestEngine.Phase.Failed -> activity.getString(R.string.speedtest_phase_failed)
                }
                bigValue.text = when (s.phase) {
                    SpeedTestEngine.Phase.Download -> fmt(s.downMbps)
                    SpeedTestEngine.Phase.Upload, SpeedTestEngine.Phase.Done -> fmt(s.upMbps)
                    else -> "—"
                }
                downView.text = if (s.downMbps > 0) fmt(s.downMbps) + " " + activity.getString(R.string.speedtest_unit_mbps) else "—"
                upView.text = if (s.upMbps > 0) fmt(s.upMbps) + " " + activity.getString(R.string.speedtest_unit_mbps) else "—"
                idleView.text = if (s.idlePingMs > 0) "${s.idlePingMs} ms" else "—"
                loadedView.text = if (s.loadedPingMs > 0) "${s.loadedPingMs} ms" else "—"
                bloatView.text = if (s.loadedPingMs > 0 && s.idlePingMs > 0) "${s.bufferbloatMs} ms" else "—"
                val running = s.phase in setOf(
                    SpeedTestEngine.Phase.Warmup, SpeedTestEngine.Phase.Download, SpeedTestEngine.Phase.Upload
                )
                startBtn.text = if (running) activity.getString(R.string.speedtest_stop)
                else if (s.phase == SpeedTestEngine.Phase.Done) activity.getString(R.string.speedtest_again)
                else activity.getString(R.string.speedtest_start)
            }
        }

        startBtn.setOnClickListener {
            val running = engine != null
            if (running) {
                engine?.stop()
                engine?.onState = null
                engine = null
                return@setOnClickListener
            }
            if (!stateStarted) {
                phaseView.text = activity.getString(R.string.speedtest_offline)
                return@setOnClickListener
            }
            val e = SpeedTestEngine(MihomoRuntimeDefaults.MIXED_PORT)
            engine = e
            e.onState = { s -> applyState(s) }
            e.start()
        }

        return root
    }

    fun stopEngine() {
        engine?.stop()
        engine?.onState = null
        engine = null
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
