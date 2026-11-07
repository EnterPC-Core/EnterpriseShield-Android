package com.whitedns.vpn

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
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
 * Экран «Тест скорости» — методика и UI оригинала, всё на русском:
 * фазы, загрузка/отдача, пинг idle/под нагрузкой, джиттер, bufferbloat,
 * график сэмплов, счётчик трафика, «Через туннель / Прямое соединение».
 */
object SpeedTestPageBuilder {

    private var engine: SpeedTestEngine? = null

    fun buildScreen(activity: Activity): View {
        val bg = pal(activity).background
        val surface = pal(activity).surface
        val tp = pal(activity).textPrimary
        val ts = pal(activity).textSecondary
        val teal = pal(activity).teal
        val amber = pal(activity).amber

        val started = isStarted(activity)

        val root = FrameLayout(activity).apply { setBackgroundColor(bg) }
        val scroll = ScrollView(activity).apply { setBackgroundColor(bg); isFillViewport = true }
        val content = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(activity, 20), dp(activity, 20), dp(activity, 20), dp(activity, 40))
        }
        scroll.addView(content)
        root.addView(scroll, FrameLayout.LayoutParams(-1, -1))

        // заголовок
        content.addView(TextView(activity).apply {
            text = activity.getString(R.string.speedtest_title)
            textSize = 24f
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            setTextColor(tp)
        })
        content.addView(TextView(activity).apply {
            text = activity.getString(R.string.speedtest_subtitle)
            textSize = 13f; setTextColor(ts)
        }, LinearLayout.LayoutParams(-2, -2).apply { topMargin = dp(activity, 2) })

        // индикатор маршрута
        val viaChip = TextView(activity).apply {
            text = if (started) activity.getString(R.string.speedtest_via_tunnel)
            else activity.getString(R.string.speedtest_via_direct)
            textSize = 12f
            setTextColor(if (started) teal else amber)
            background = GradientDrawable().apply {
                cornerRadius = dp(activity, EsDs.Radius.pill).toFloat()
                setColor((if (started) teal else amber) and 0x1AFFFFFF)
                setStroke(dp(activity, 1), (if (started) teal else amber) and 0x33FFFFFF)
            }
            setPadding(dp(activity, EsDs.Space.sm), dp(activity, 6), dp(activity, EsDs.Space.sm), dp(activity, 6))
        }
        if (!started) {
            content.addView(TextView(activity).apply {
                text = activity.getString(R.string.speedtest_not_connected)
                textSize = 12f; setTextColor(amber)
            }, LinearLayout.LayoutParams(-2, -2).apply { topMargin = dp(activity, 10) })
        }
        content.addView(viaChip, LinearLayout.LayoutParams(-2, -2).apply { topMargin = dp(activity, 12) })

        // ===== большая цифра =====
        val mainCard = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            background = GradientDrawable().apply {
                cornerRadius = dp(activity, 18).toFloat(); setColor(surface)
            }
            setPadding(dp(activity, 16), dp(activity, 20), dp(activity, 16), dp(activity, 16))
        }
        val phaseView = TextView(activity).apply {
            text = activity.getString(R.string.speedtest_phase_idle)
            textSize = 13f; setTextColor(ts); gravity = Gravity.CENTER_HORIZONTAL
        }
        val bigValue = TextView(activity).apply {
            text = "—"; textSize = 54f
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            setTextColor(tp); gravity = Gravity.CENTER_HORIZONTAL
        }
        val bigUnit = TextView(activity).apply {
            text = activity.getString(R.string.speedtest_mbps)
            textSize = 13f; setTextColor(ts); gravity = Gravity.CENTER_HORIZONTAL
        }
        mainCard.addView(phaseView)
        mainCard.addView(bigValue, LinearLayout.LayoutParams(-2, -2).apply { topMargin = dp(activity, 4) })
        mainCard.addView(bigUnit)
        // график сэмплов
        val spark = SparklineView(activity).apply {
            setColors(teal, (teal and 0x40FFFFFF) or (surface and 0xFF000000.toInt()))
        }
        mainCard.addView(spark, LinearLayout.LayoutParams(-1, dp(activity, 64)).apply { topMargin = dp(activity, 14) })
        content.addView(mainCard, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(activity, 14) })

        // ===== метрики =====
        val statCard = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                cornerRadius = dp(activity, 16).toFloat(); setColor(surface)
            }
            setPadding(dp(activity, 16), dp(activity, 14), dp(activity, 16), dp(activity, 14))
        }
        fun statRow(labelRes: Int, value: TextView, tint: Int = tp) {
            val r = LinearLayout(activity).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
            r.addView(TextView(activity).apply { text = activity.getString(labelRes); textSize = 14f; setTextColor(ts) })
            r.addView(LinearLayout(activity), LinearLayout.LayoutParams(0, 1, 1f))
            value.textSize = 15f; value.setTextColor(tint); value.typeface = Typeface.DEFAULT_BOLD
            r.addView(value, LinearLayout.LayoutParams(-2, -2))
            statCard.addView(r, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(activity, 8) })
        }
        val downV = TextView(activity); val upV = TextView(activity)
        val pingV = TextView(activity); val loadedV = TextView(activity)
        val jitterV = TextView(activity); val bloatV = TextView(activity)
        statRow(R.string.speedtest_download, downV, teal)
        statRow(R.string.speedtest_upload, upV, teal)
        statRow(R.string.speedtest_ping, pingV)
        statRow(R.string.speedtest_loaded_ping, loadedV)
        statRow(R.string.speedtest_jitter, jitterV)
        statRow(R.string.speedtest_bufferbloat, bloatV)
        content.addView(statCard, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(activity, 12) })

        // трафик
        val dataNote = TextView(activity).apply {
            text = activity.getString(R.string.speedtest_data_note, 96)
            textSize = 12f; setTextColor(ts)
        }
        val dataUsed = TextView(activity).apply {
            text = ""; textSize = 12f; setTextColor(ts)
        }
        content.addView(dataNote, LinearLayout.LayoutParams(-2, -2).apply { topMargin = dp(activity, 10) })
        content.addView(dataUsed, LinearLayout.LayoutParams(-2, -2).apply { topMargin = dp(activity, 2) })

        // кнопка
        val startBtn = MaterialButton(activity).apply { text = activity.getString(R.string.speedtest_start) }
        content.addView(startBtn, LinearLayout.LayoutParams(-1, dp(activity, 52)).apply { topMargin = dp(activity, 16) })

        fun fmt(v: Double?): String = when {
            v == null -> "—"
            v >= 100 -> "%.0f".format(v)
            else -> "%.1f".format(v)
        }

        fun applyState(s: SpeedTestEngine.State) {
            GlobalScope.launch(Dispatchers.Main) {
                phaseView.text = when (s.phase) {
                    SpeedTestEngine.Phase.Idle -> activity.getString(R.string.speedtest_phase_idle)
                    SpeedTestEngine.Phase.Ping -> activity.getString(R.string.speedtest_phase_ping)
                    SpeedTestEngine.Phase.Download -> activity.getString(R.string.speedtest_phase_download)
                    SpeedTestEngine.Phase.Upload -> activity.getString(R.string.speedtest_phase_upload)
                    SpeedTestEngine.Phase.Done -> activity.getString(R.string.speedtest_phase_done)
                    SpeedTestEngine.Phase.Stopped -> activity.getString(R.string.speedtest_phase_stopped)
                }
                val cur = when (s.phase) {
                    SpeedTestEngine.Phase.Download -> s.downloadMbps
                    SpeedTestEngine.Phase.Upload -> s.uploadMbps
                    SpeedTestEngine.Phase.Done -> s.downloadMbps
                    else -> null
                }
                bigValue.text = if (cur != null) fmt(cur) else "—"
                downV.text = if (s.downloadMbps != null) fmt(s.downloadMbps) else "—"
                upV.text = if (s.uploadMbps != null) fmt(s.uploadMbps) else "—"
                pingV.text = if (s.pingMs != null) "${s.pingMs?.toInt()} ${activity.getString(R.string.speedtest_ms)}" else "—"
                loadedV.text = if (s.loadedPingMs != null) "${s.loadedPingMs?.toInt()} ${activity.getString(R.string.speedtest_ms)}" else "—"
                jitterV.text = if (s.jitterMs != null) "${s.jitterMs?.toInt()} ${activity.getString(R.string.speedtest_ms)}" else "—"
                bloatV.text = if (s.bufferbloatMs != null) "${s.bufferbloatMs?.toInt()} ${activity.getString(R.string.speedtest_ms)}" else "—"
                dataUsed.text = if (s.spentBytes > 0)
                    activity.getString(R.string.speedtest_data_used, "%.1f".format(s.spentBytes / 1048576.0)) else ""
                spark.setSamples(s.downSamples.map { it.mbps }, s.upSamples.map { it.mbps })
                val running = s.phase in setOf(SpeedTestEngine.Phase.Ping, SpeedTestEngine.Phase.Download, SpeedTestEngine.Phase.Upload)
                startBtn.text = when {
                    running -> activity.getString(R.string.speedtest_stop)
                    s.phase == SpeedTestEngine.Phase.Done || s.phase == SpeedTestEngine.Phase.Stopped -> activity.getString(R.string.speedtest_restart)
                    else -> activity.getString(R.string.speedtest_start)
                }
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
            val e = SpeedTestEngine(if (started) MihomoRuntimeDefaults.MIXED_PORT else null)
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

    private fun isStarted(activity: Activity): Boolean =
        activity.getSharedPreferences("white_dns_runtime_state", Activity.MODE_PRIVATE)
            .getString("state", "") == "Started"

    /** Спарклайн скорости: загрузка (заливка) + отдача (линия). */
    private class SparklineView(context: Context) : View(context) {
        private var down: List<Double> = emptyList()
        private var up: List<Double> = emptyList()
        private var cFill: Int = 0xFF4DD0C4.toInt()
        private var cLine: Int = 0xFF9FE8DB.toInt()
        private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE; strokeWidth = 2f; strokeCap = Paint.Cap.ROUND
        }
        private val path = Path()

        fun setColors(main: Int, line: Int) { cFill = main; cLine = line }
        fun setSamples(d: List<Double>, u: List<Double>) {
            down = d; up = u
            postInvalidateOnAnimation()
        }

        override fun onDraw(canvas: Canvas) {
            if (down.isEmpty() && up.isEmpty()) {
                val p = fillPaint
                p.color = (cFill and 0x18FFFFFF)
                canvas.drawRoundRect(0f, height / 2f, width.toFloat(), height.toFloat(), 8f, 8f, p)
                return
            }
            val all = down + up
            val maxV = (all.maxOrNull() ?: 1.0).coerceAtLeast(0.5)
            val h = height.toFloat() - 2f
            fun drawSeries(series: List<Double>, fill: Boolean, color: Int) {
                if (series.isEmpty()) return
                path.reset()
                val n = series.size
                val step = width.toFloat() / maxOf(n - 1, 1).coerceAtLeast(1)
                series.forEachIndexed { i, v ->
                    val x = i * step
                    val y = h - (v / maxV * h).toFloat()
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                if (fill) {
                    val fillPath = Path(path)
                    fillPath.lineTo(width.toFloat(), h)
                    fillPath.lineTo(0f, h)
                    fillPath.close()
                    fillPaint.color = (color and 0x33FFFFFF) or 0x66000000.toInt()
                    canvas.drawPath(fillPath, fillPaint)
                }
                linePaint.color = (color and 0xEEFFFFFF.toInt()) or 0xFF000000.toInt()
                canvas.drawPath(path, linePaint)
            }
            drawSeries(down, fill = true, cFill)
            drawSeries(up, fill = false, cLine)
        }
    }

    private fun pal(activity: Activity): com.whitedns.vpn.WhiteDnsPalette =
        com.whitedns.vpn.WhiteDnsDesignTokens.forContext(activity)

    private fun dp(activity: Activity, v: Int): Int = (v * activity.resources.displayMetrics.density).toInt()
}
