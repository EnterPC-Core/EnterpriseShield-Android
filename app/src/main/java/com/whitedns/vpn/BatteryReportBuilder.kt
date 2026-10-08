package com.whitedns.vpn

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.StatFs
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.math.min

/**
 * Страница «Статистика смартфона»: табы Батарея / Система / Приложения.
 */
object BatteryReportBuilder {
    private var refreshHandler: android.os.Handler? = null
    private var refreshRunnable: Runnable? = null
    private const val REFRESH_INTERVAL_MS = 3000L

    fun stopRefresh() {
        refreshRunnable?.let { refreshHandler?.removeCallbacks(it) }
        refreshRunnable = null
        refreshHandler = null
    }

    fun buildScreen(activity: Activity): View {
        val bg = paletteColor(activity, "background", 0xFF101418)
        val surface = paletteColor(activity, "surface", 0xFF1A2028)
        val tp = paletteColor(activity, "textPrimary", 0xFFE8EDF2)
        val ts = paletteColor(activity, "textSecondary", 0xFF8A94A0)
        val teal = paletteColor(activity, "teal", 0xFF4DD0C4)
        val amber = paletteColor(activity, "amber", 0xFFFFB74D)
        val red = paletteColor(activity, "red", 0xFFEF5350)

        val root = FrameLayout(activity).apply { setBackgroundColor(bg) }
        val mainLayout = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
        }

        val tabRow = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(activity, 16), dp(activity, 12), dp(activity, 16), dp(activity, 4))
        }
        mainLayout.addView(tabRow, LinearLayout.LayoutParams(-1, -2))

        val scrollView = ScrollView(activity).apply {
            isFillViewport = true
            clipToPadding = false
            setBackgroundColor(bg)
        }
        val contentHolder = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
        }
        scrollView.addView(LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(activity, 20), 0, dp(activity, 20), dp(activity, 40))
            addView(contentHolder)
        })
        mainLayout.addView(scrollView, LinearLayout.LayoutParams(-1, 0, 1f))

        // ===== Табы =====
        data class Tab(val label: String, val icon: String, val content: () -> Unit)
        val tabs = listOf(
            Tab(activity.getString(R.string.battery_tab_battery), "🔋") { renderBatteryTab(activity, contentHolder, surface, tp, ts, teal, amber, red) },
            Tab(activity.getString(R.string.battery_tab_system), "⚙️") { renderSystemTab(activity, contentHolder, surface, tp, ts, teal, amber) },
            Tab(activity.getString(R.string.battery_tab_apps), "📱") { renderAppsTab(activity, contentHolder, surface, tp, ts, teal, amber, red) },
        )
        val tabButtons = mutableListOf<TextView>()
        tabs.forEachIndexed { i, tabInfo ->
            val btn = TextView(activity).apply {
                text = tabInfo.icon + " " + tabInfo.label
                textSize = 13f
                typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
                setTextColor(if (i == 0) Color.BLACK else ts)
                gravity = Gravity.CENTER
                background = GradientDrawable().apply {
                    cornerRadius = dp(activity, 20).toFloat()
                    setColor(if (i == 0) teal else surface)
                }
                setPadding(dp(activity, 12), dp(activity, 10), dp(activity, 12), dp(activity, 10))
                setOnClickListener {
                    tabButtons.forEachIndexed { j, b ->
                        (b.background as? GradientDrawable)?.setColor(if (j == i) teal else surface)
                        b.setTextColor(if (j == i) Color.BLACK else ts)
                    }
                    contentHolder.removeAllViews()
                    tabs[i].content()
                }
            }
            tabButtons.add(btn)
            tabRow.addView(btn, LinearLayout.LayoutParams(0, -2, 1f).apply {
                setMargins(dp(activity, 3), 0, dp(activity, 3), 0)
            })
        }
        tabs[0].content()

        root.addView(mainLayout, FrameLayout.LayoutParams(-1, -1))
        return root
    }

    // ==================== TAB: Батарея ====================
    private fun renderBatteryTab(
        activity: Activity, holder: LinearLayout, surface: Int, tp: Int, ts: Int, teal: Int, amber: Int, red: Int,
    ) {
        BatteryUsageTracker.startBatteryMonitor(activity)
        stopRefresh()
        val live = LinearLayout(activity).apply { orientation = LinearLayout.VERTICAL }
        holder.addView(live)
        fun drawLive() {
            BatteryUsageTracker.updateCurrentViaBatteryManager(activity)
            val battery = BatteryUsageTracker.getBatteryInfo()
            live.removeAllViews()
            if (battery == null) return
            val battColor = when {
                battery.level <= 15 -> red
                battery.level <= 30 -> amber
                else -> teal
            }
            live.addView(TextView(activity).apply {
                text = "${battery.level}%"
                textSize = 56f
                typeface = Typeface.create("sans-serif-light", Typeface.BOLD)
                setTextColor(battColor)
                gravity = Gravity.CENTER
                setPadding(0, dp(activity, 20), 0, 0)
            })
            live.addView(ProgressBar(activity, null, android.R.attr.progressBarStyleHorizontal).apply {
                max = 100
                progress = battery.level
                progressTintList = android.content.res.ColorStateList.valueOf(battColor)
                layoutParams = LinearLayout.LayoutParams(-1, dp(activity, 12)).apply {
                    setMargins(dp(activity, 20), dp(activity, 8), dp(activity, 20), dp(activity, 4))
                }
            })
            live.addView(TextView(activity).apply {
                text = "${battery.status} - ${battery.chargingType}"
                textSize = 14f
                setTextColor(ts)
                gravity = Gravity.CENTER
                setPadding(0, dp(activity, 4), 0, dp(activity, 8))
            })
            if (battery.isCharging || battery.isFull) {
                live.addView(card(activity, surface) { c ->
                    c.addView(statRow(activity, "⚡", "Мощность", String.format("%.1f W", battery.powerW), tp, ts))
                    val curMa = battery.currentUa / 1000
                    if (curMa != 0) {
                        c.addView(statRow(activity, "🔌", "Ток", "+${curMa} мА", tp, ts))
                    }
                    val voltV = battery.voltageMv / 1000.0
                    if (voltV > 0) {
                        c.addView(statRow(activity, "🔋", "Напряжение", String.format("%.2f В", voltV), tp, ts))
                    }
                    if (battery.timeRemainingLabel != "—") {
                        c.addView(statRow(activity, "⏳", "До полного", battery.timeRemainingLabel, tp, ts))
                    }
                    val tc = if (battery.temperature >= 40) red else if (battery.temperature >= 35) amber else teal
                    c.addView(statRowColor(activity, "🌡️", "Температура", "${battery.temperature}°C", tc, ts))
                })
            } else if (battery.timeRemainingLabel != "—" || battery.currentUa != 0) {
                live.addView(card(activity, surface) { c ->
                    if (battery.timeRemainingLabel != "—") {
                        c.addView(statRow(activity, "⏳", "Осталось", battery.timeRemainingLabel.replace("до разряда: ", ""), tp, ts))
                    }
                    if (battery.currentUa != 0) {
                        val cm = battery.currentUa / 1000
                        c.addView(statRow(activity, "🔌", "Ток разряда", "${cm} мА", tp, ts))
                    }
                    if (battery.powerW > 0.1) {
                        c.addView(statRow(activity, "⚡", "Потребление", String.format("%.2f W", battery.powerW), tp, ts))
                    }
                })
            }
            live.addView(sectionLabel(activity, activity.getString(R.string.sys_battery_section), ts))
            live.addView(card(activity, surface) { c ->
                val tempColor = when { battery.temperature >= 40 -> red; battery.temperature >= 35 -> amber; else -> teal }
                c.addView(statRowColor(activity, "🌡️", "Температура", "${battery.temperature}°C", tempColor, ts))
                c.addView(statRow(activity, "❤️", "Здоровье", battery.health, tp, ts))
                c.addView(statRow(activity, "⚡", "Напряжение", String.format("%.2f В", battery.voltageMv / 1000.0), tp, ts))
                c.addView(statRow(activity, "🔋", "Технология", battery.technology, tp, ts))
                if (battery.currentUa != 0) {
                    val cur = battery.currentUa / 1000
                    c.addView(statRow(activity, "🔌", "Ток", "${cur} мА", tp, ts))
                }
                if (battery.cycleCount > 0) {
                    val cc = when { battery.cycleCount > 800 -> red; battery.cycleCount > 500 -> amber; else -> teal }
                    c.addView(statRowColor(activity, "🔄", "Циклов зарядки", "${battery.cycleCount}", cc, ts))
                }
                if (battery.healthPercent > 0) {
                    val hp = when { battery.healthPercent <= 70 -> red; battery.healthPercent <= 85 -> amber; else -> teal }
                    c.addView(statRowColor(activity, "📊", "Ёмкость (износ)", "${battery.healthPercent}% от заводской", hp, ts))
                    c.addView(ProgressBar(activity, null, android.R.attr.progressBarStyleHorizontal).apply {
                        max = 100; progress = battery.healthPercent
                        progressTintList = android.content.res.ColorStateList.valueOf(hp)
                        layoutParams = LinearLayout.LayoutParams(-1, dp(activity, 8)).apply { topMargin = dp(activity, 8) }
                    })
                }
                if (battery.chargeFullUah > 0) {
                    c.addView(statRow(activity, "📦", "Реальная ёмкость", "${battery.chargeFullUah / 1000} мАч", tp, ts))
                }
                if (battery.designCapacityUah > 0) {
                    c.addView(statRow(activity, "🏭", "Заводская ёмкость", "${battery.designCapacityUah / 1000} мАч", tp, ts))
                }
            })
        }
        drawLive()
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                BatteryUsageTracker.startBatteryMonitor(activity)
                drawLive()
                handler.postDelayed(this, REFRESH_INTERVAL_MS)
            }
        }
        refreshHandler = handler
        refreshRunnable = runnable
        handler.postDelayed(runnable, REFRESH_INTERVAL_MS)

        // Статистика приложений (грузится один раз, не в refresh-цикле)
        if (BatteryUsageTracker.hasUsageAccess(activity)) {
            val statsHolder = LinearLayout(activity).apply { orientation = LinearLayout.VERTICAL }
            holder.addView(statsHolder)
            statsHolder.addView(TextView(activity).apply {
                text = activity.getString(R.string.battery_report_loading)
                textSize = 13f
                setTextColor(ts)
                setPadding(0, dp(activity, 12), 0, dp(activity, 12))
            })
            Thread {
                val usage = BatteryUsageTracker.collect(activity)
                activity.runOnUiThread {
                    statsHolder.removeAllViews()
                    renderAppStats(activity, statsHolder, usage, surface, tp, ts, teal, amber, red)
                }
            }.apply { isDaemon = true }.start()
        }
    }

    private fun renderAppStats(
        activity: Activity, holder: LinearLayout, usage: List<BatteryUsageTracker.AppUsage>,
        surface: Int, tp: Int, ts: Int, teal: Int, amber: Int, red: Int,
    ) {
        if (usage.isEmpty()) {
            holder.addView(TextView(activity).apply {
                text = activity.getString(R.string.battery_report_empty)
                textSize = 14f
                setTextColor(ts)
                setPadding(0, dp(activity, 12), 0, dp(activity, 12))
            })
            return
        }

        val totalFg = usage.sumOf { it.foregroundMs }
        val totalTraffic = usage.sumOf { it.totalTrafficBytes }
        val bgSuspects = usage.filter { it.backgroundScore >= 30 }

        holder.addView(sectionLabel(activity, activity.getString(R.string.battery_report_summary), ts))
        holder.addView(card(activity, surface) { c ->
            c.addView(statRow(activity, "📱", "Экранное время", BatteryUsageTracker.formatDuration(totalFg), tp, ts))
            c.addView(statRow(activity, "📡", "Трафик", BatteryUsageTracker.formatBytes(totalTraffic), tp, ts))
            c.addView(statRow(activity, "⚠️", "Фоновых прожор", "${bgSuspects.size}", tp, ts))
        })

        // ===== Топ по экранному времени =====
        val screenTop = usage.sortedByDescending { it.foregroundMs }.take(8)
        val maxFg = screenTop.firstOrNull()?.foregroundMs ?: 1L
        holder.addView(sectionLabel(activity, activity.getString(R.string.battery_report_top_screen), ts))
        screenTop.forEachIndexed { i, app ->
            holder.addView(card(activity, surface) { c ->
                c.addView(appHeader(activity, "#${i + 1}", app.appName, app.isSystem, tp, ts))
                c.addView(progressBar(activity, teal, (app.foregroundMs.toFloat() / maxFg * 100).toInt()))
                c.addView(TextView(activity).apply {
                    text = BatteryUsageTracker.formatDuration(app.foregroundMs) +
                        " · " + BatteryUsageTracker.formatBytes(app.totalTrafficBytes) + " · " + formatAgo(activity, app.lastUsed)
                    textSize = 12f
                    setTextColor(ts)
                })
            })
        }

        // ===== Фоновые прожоры =====
        if (bgSuspects.isNotEmpty()) {
            holder.addView(sectionLabel(activity, activity.getString(R.string.battery_report_bg_hogs), ts))
            val maxScore = bgSuspects.maxOf { it.backgroundScore }
            bgSuspects.sortedByDescending { it.backgroundScore }.take(8).forEach { app ->
                val color = when {
                    app.backgroundScore >= 70 -> red
                    app.backgroundScore >= 50 -> amber
                    else -> teal
                }
                holder.addView(card(activity, surface) { c ->
                    c.addView(appHeader(activity, "⚠", app.appName, app.isSystem, tp, ts))
                    c.addView(progressBar(activity, color, app.backgroundScore * 100 / maxScore))
                    c.addView(TextView(activity).apply {
                        text = BatteryUsageTracker.formatBytes(app.totalTrafficBytes) + " · " + formatAgo(activity, app.lastUsed)
                        textSize = 12f
                        setTextColor(ts)
                    })
                })
            }
        }

        // ===== Топ по мобильному трафику =====
        val mobileTop = usage.filter { it.totalMobileBytes > 0 }.sortedByDescending { it.totalMobileBytes }.take(8)
        if (mobileTop.isNotEmpty()) {
            holder.addView(sectionLabel(activity, activity.getString(R.string.battery_report_top_mobile), ts))
            val maxMb = mobileTop.firstOrNull()?.let { it.totalMobileBytes / 1_048_576.0 } ?: 1.0
            mobileTop.forEach { app ->
                val mb = app.totalMobileBytes / 1_048_576.0
                holder.addView(card(activity, surface) { c ->
                    c.addView(appHeader(activity, "📶", app.appName, app.isSystem, tp, ts))
                    c.addView(progressBar(activity, amber, (mb / maxMb * 100).toInt().coerceAtMost(100)))
                    c.addView(TextView(activity).apply {
                        text = String.format("%.1f МБ", mb)
                        textSize = 12f
                        setTextColor(ts)
                    })
                })
            }
        }

        if (bgSuspects.isNotEmpty()) {
            holder.addView(sectionLabel(activity, activity.getString(R.string.battery_report_recommend), ts))
            holder.addView(card(activity, surface) { c ->
                c.addView(TextView(activity).apply {
                    text = activity.getString(R.string.battery_report_recommend_text, bgSuspects.first().appName)
                    textSize = 14f
                    setTextColor(tp)
                })
            })
        }
    }

    // ==================== TAB: Система ====================
    private fun renderSystemTab(
        activity: Activity, holder: LinearLayout, surface: Int, tp: Int, ts: Int, teal: Int, amber: Int,
    ) {
        holder.addView(sectionLabel(activity, activity.getString(R.string.sys_device_section), ts))
        holder.addView(card(activity, surface) { c ->
            c.addView(statRow(activity, "📱", "Модель", Build.MODEL, tp, ts))
            c.addView(statRow(activity, "🏭", "Производитель", Build.MANUFACTURER, tp, ts))
            c.addView(statRow(activity, "🤖", "Android", Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")", tp, ts))
            if (Build.VERSION.SDK_INT >= 31) {
                c.addView(statRow(activity, "🏷", "Прошивка", Build.VERSION.INCREMENTAL, tp, ts))
            }
        })

        // ===== Память =====
        val am = activity.getSystemService(Activity.ACTIVITY_SERVICE) as? ActivityManager
        val mi = ActivityManager.MemoryInfo()
        am?.getMemoryInfo(mi)
        if (mi != null && mi.totalMem > 0) {
            val usedPct = (100 - mi.availMem * 100 / mi.totalMem).toInt()
            holder.addView(sectionLabel(activity, activity.getString(R.string.sys_memory_section), ts))
            holder.addView(card(activity, surface) { c ->
                c.addView(statRow(activity, "🧠", "Всего", BatteryUsageTracker.formatBytes(mi.totalMem), tp, ts))
                c.addView(statRow(activity, "🧠", "Свободно", BatteryUsageTracker.formatBytes(mi.availMem), tp, ts))
                c.addView(statRow(activity, "📊", "Занято", "$usedPct%", tp, ts))
                c.addView(ProgressBar(activity, null, android.R.attr.progressBarStyleHorizontal).apply {
                    max = 100
                    progress = usedPct
                    progressTintList = android.content.res.ColorStateList.valueOf(if (usedPct > 85) red_safe else teal)
                    layoutParams = LinearLayout.LayoutParams(-1, dp(activity, 8)).apply { topMargin = dp(activity, 8) }
                })
            })
        }

        // ===== Хранилище =====
        try {
            val internal = StatFs(activity.filesDir.absolutePath)
            val totalGB = internal.totalBytes / 1_073_741_824.0
            val freeGB = internal.availableBytes / 1_073_741_824.0
            val usedGB = totalGB - freeGB
            val usedPct = (usedGB / totalGB * 100).toInt()
            holder.addView(sectionLabel(activity, activity.getString(R.string.sys_storage_section), ts))
            holder.addView(card(activity, surface) { c ->
                c.addView(statRow(activity, "💾", "Всего", String.format("%.1f ГБ", totalGB), tp, ts))
                c.addView(statRow(activity, "💾", "Свободно", String.format("%.1f ГБ", freeGB), tp, ts))
                c.addView(statRow(activity, "📊", "Занято", String.format("%.1f ГБ (%d%%)", usedGB, usedPct), tp, ts))
                c.addView(ProgressBar(activity, null, android.R.attr.progressBarStyleHorizontal).apply {
                    max = 100
                    progress = usedPct
                    progressTintList = android.content.res.ColorStateList.valueOf(if (usedPct > 90) red_safe else teal)
                    layoutParams = LinearLayout.LayoutParams(-1, dp(activity, 8)).apply { topMargin = dp(activity, 8) }
                })
            })
        } catch (_: Exception) { }

        // ===== Экран =====
        val dm = activity.resources.displayMetrics
        holder.addView(sectionLabel(activity, activity.getString(R.string.sys_display_section), ts))
        holder.addView(card(activity, surface) { c ->
            c.addView(statRow(activity, "🖥", "Разрешение", "${dm.widthPixels}×${dm.heightPixels}", tp, ts))
            c.addView(statRow(activity, "📐", "Плотность", "${dm.densityDpi}dpi", tp, ts))
            c.addView(statRow(activity, "📏", "Диагональ", String.format("%.1f\"", Math.sqrt(Math.pow((dm.widthPixels / dm.xdpi).toDouble(), 2.0) + Math.pow((dm.heightPixels / dm.ydpi).toDouble(), 2.0))), tp, ts))
            val refresh = activity.display?.refreshRate ?: 60f
            c.addView(statRow(activity, "🔄", "Частота", String.format("%.0f Гц", refresh), tp, ts))
        })

        // ===== Время работы =====
        holder.addView(sectionLabel(activity, activity.getString(R.string.sys_uptime_section), ts))
        holder.addView(card(activity, surface) { c ->
            val uptimeMs = android.os.SystemClock.elapsedRealtime()
            val awakeMs = android.os.SystemClock.uptimeMillis()
            c.addView(statRow(activity, "⏱", "Включён", BatteryUsageTracker.formatDuration(uptimeMs), tp, ts))
            c.addView(statRow(activity, "👁", "Без сна", BatteryUsageTracker.formatDuration(awakeMs), tp, ts))
            c.addView(statRow(activity, "😴", "В сне", BatteryUsageTracker.formatDuration(uptimeMs - awakeMs), tp, ts))
            val deepSleepPct = if (uptimeMs > 0) ((uptimeMs - awakeMs) * 100 / uptimeMs).toInt() else 0
            c.addView(ProgressBar(activity, null, android.R.attr.progressBarStyleHorizontal).apply {
                max = 100
                progress = deepSleepPct
                progressTintList = android.content.res.ColorStateList.valueOf(teal)
                layoutParams = LinearLayout.LayoutParams(-1, dp(activity, 8)).apply { topMargin = dp(activity, 8) }
            })
            c.addView(TextView(activity).apply {
                text = "Глубокий сон: $deepSleepPct%"
                textSize = 12f
                setTextColor(ts)
            })
        })
    }

    private val red_safe = 0xFFEF5350.toInt()

    // ==================== TAB: Приложения ====================
    private fun renderAppsTab(
        activity: Activity, holder: LinearLayout, surface: Int, tp: Int, ts: Int, teal: Int, amber: Int, red: Int,
    ) {
        holder.addView(TextView(activity).apply {
            text = activity.getString(R.string.battery_report_loading)
            textSize = 13f
            setTextColor(ts)
            setPadding(0, dp(activity, 16), 0, dp(activity, 16))
        })

        Thread {
            val usage = if (BatteryUsageTracker.hasUsageAccess(activity)) {
                BatteryUsageTracker.collect(activity)
            } else emptyList()
            activity.runOnUiThread {
                holder.removeAllViews()

                if (!BatteryUsageTracker.hasUsageAccess(activity)) {
                    holder.addView(card(activity, surface) { c ->
                        c.addView(TextView(activity).apply {
                            text = activity.getString(R.string.battery_report_need_access)
                            textSize = 14f
                            setTextColor(tp)
                            setPadding(0, dp(activity, 8), 0, dp(activity, 8))
                        })
                        c.addView(button(activity, teal, activity.getString(R.string.battery_report_grant)) {
                            BatteryUsageTracker.requestUsageAccess(activity)
                        })
                    })
                    return@runOnUiThread
                }

                if (usage.isEmpty()) {
                    holder.addView(TextView(activity).apply {
                        text = activity.getString(R.string.battery_report_empty)
                        textSize = 14f
                        setTextColor(ts)
                        setPadding(0, dp(activity, 16), 0, dp(activity, 16))
                    })
                    return@runOnUiThread
                }

                // Сортировка: по экранному времени (топ используемые)
                holder.addView(sectionLabel(activity, "${usage.size} ${activity.getString(R.string.apps_count)} · по экранному времени", ts))
                usage.take(50).forEachIndexed { i, app ->
                    holder.addView(card(activity, surface) { c ->
                        c.addView(appHeader(activity, "#${i + 1}", app.appName, app.isSystem, tp, ts))
                        val maxFg = usage.first().foregroundMs
                        c.addView(progressBar(activity, teal, (app.foregroundMs.toFloat() / maxFg * 100).toInt()))
                        c.addView(TextView(activity).apply {
                            text = "Экран: " + BatteryUsageTracker.formatDuration(app.foregroundMs) +
                                " · Трафик: " + BatteryUsageTracker.formatBytes(app.totalTrafficBytes) +
                                " · " + formatAgo(activity, app.lastUsed)
                            textSize = 12f
                            setTextColor(ts)
                        })
                    })
                }
            }
        }.apply { isDaemon = true }.start()
    }

    // ==================== Helpers ====================

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

    private fun sectionLabel(activity: Activity, text: String, color: Int): TextView {
        return TextView(activity).apply {
            this.text = text
            textSize = 12f
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            setTextColor(color)
            letterSpacing = 0.05f
            includeFontPadding = false
            setPadding(0, dp(activity, 20), 0, dp(activity, 8))
        }
    }

    private fun card(activity: Activity, surface: Int, content: (LinearLayout) -> Unit): LinearLayout {
        val inner = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(activity, 16), dp(activity, 14), dp(activity, 16), dp(activity, 14))
        }
        content(inner)
        return LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                cornerRadius = dp(activity, 14).toFloat()
                setColor(surface)
            }
            addView(inner, LinearLayout.LayoutParams(-1, -2))
        }
    }

    private fun appHeader(activity: Activity, badge: String, name: String, isSystem: Boolean, tp: Int, ts: Int): LinearLayout {
        return LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            addView(TextView(activity).apply {
                text = badge
                textSize = 14f
                setPadding(0, 0, dp(activity, 8), 0)
            })
            addView(TextView(activity).apply {
                text = name
                textSize = 15f
                typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                setTextColor(tp)
                layoutParams = LinearLayout.LayoutParams(0, -2, 1f)
                maxLines = 1
                ellipsize = android.text.TextUtils.TruncateAt.END
            })
            addView(TextView(activity).apply {
                text = if (isSystem) "SYS" else ""
                textSize = 10f
                setTextColor(ts)
                setPadding(dp(activity, 6), 0, 0, 0)
            })
        }
    }

    private fun progressBar(activity: Activity, color: Int, percent: Int): ProgressBar {
        return ProgressBar(activity, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = 100
            progress = percent.coerceIn(0, 100)
            progressTintList = android.content.res.ColorStateList.valueOf(color)
            progressBackgroundTintList = android.content.res.ColorStateList.valueOf(color and 0x22FFFFFF)
            layoutParams = LinearLayout.LayoutParams(-1, dp(activity, 6)).apply {
                topMargin = dp(activity, 6)
                bottomMargin = dp(activity, 4)
            }
        }
    }

    private fun statRow(activity: Activity, icon: String, label: String, value: String, tp: Int, ts: Int): LinearLayout {
        return statRowColor(activity, icon, label, value, tp, ts)
    }

    private fun statRowColor(activity: Activity, icon: String, label: String, value: String, valueColor: Int, ts: Int): LinearLayout {
        return LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(activity, 5), 0, dp(activity, 5))
            addView(TextView(activity).apply {
                text = icon
                textSize = 14f
                setPadding(0, 0, dp(activity, 10), 0)
            })
            addView(TextView(activity).apply {
                text = label
                textSize = 14f
                setTextColor(ts)
                layoutParams = LinearLayout.LayoutParams(0, -2, 1f)
                maxLines = 1
                ellipsize = android.text.TextUtils.TruncateAt.END
            })
            addView(TextView(activity).apply {
                text = value
                textSize = 14f
                typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
                setTextColor(valueColor)
            })
        }
    }

    private fun button(activity: Activity, teal: Int, text: String, onClick: () -> Unit): TextView {
        return TextView(activity).apply {
            this.text = text
            textSize = 15f
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                cornerRadius = dp(activity, 12).toFloat()
                setColor(teal)
            }
            setPadding(dp(activity, 24), dp(activity, 12), dp(activity, 24), dp(activity, 12))
            setOnClickListener { onClick() }
        }
    }

    private fun formatAgo(activity: Activity, timestamp: Long): String {
        val diff = System.currentTimeMillis() - timestamp
        val hours = diff / 3_600_000
        val minutes = (diff % 3_600_000) / 60_000
        return when {
            hours > 24 -> "${hours / 24}д назад"
            hours > 0 -> "${hours}ч назад"
            else -> "${min(minutes, 1)}м назад"
        }
    }
}
