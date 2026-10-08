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
 * Стиль: SURFACE-карточки, sectionLabel, progressbar с tint.
 */
object BatteryReportBuilder {

    fun buildScreen(activity: Activity): View {
        val bg = paletteColor(activity, "background", 0xFF101418)
        val surface = paletteColor(activity, "surface", 0xFF1A2028)
        val textPrimary = paletteColor(activity, "textPrimary", 0xFFE8EDF2)
        val textSecondary = paletteColor(activity, "textSecondary", 0xFF8A94A0)
        val teal = paletteColor(activity, "teal", 0xFF4DD0C4)
        val amber = paletteColor(activity, "amber", 0xFF5F8AA8) 
        val red = paletteColor(activity, "red", 0xFFEF5350)

        val root = FrameLayout(activity).apply { setBackgroundColor(bg) }

        val mainLayout = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
        }

        // ===== Табы =====
        val tabRow = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(activity, 16), dp(activity, 12), dp(activity, 16), dp(activity, 8))
        }
        val contentHolder = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(-1, 0, 1f)
        }
        val scrollView = ScrollView(activity).apply {
            isFillViewport = true
            clipToPadding = false
            setBackgroundColor(bg)
        }
        scrollView.addView(LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(activity, 24), 0, dp(activity, 24), dp(activity, 40))
            addView(contentHolder)
        })
        mainLayout.addView(tabRow, LinearLayout.LayoutParams(-1, -2))
        mainLayout.addView(scrollView, LinearLayout.LayoutParams(-1, 0, 1f))

        // Заголовок
        contentHolder.addView(TextView(activity).apply {
            text = activity.getString(R.string.battery_report_title)
            textSize = 22f
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            setTextColor(textPrimary)
            setPadding(0, dp(activity, 8), 0, dp(activity, 4))
        })

        // ===== Таб-кнопки =====
        data class Tab(val label: String, val icon: String, val content: () -> Unit)
        val tabs = listOf(
            Tab(activity.getString(R.string.battery_tab_battery), "🔋") { renderBatteryTab(activity, contentHolder, surface, textPrimary, textSecondary, teal, amber, red) },
            Tab(activity.getString(R.string.battery_tab_system), "⚙️") { renderSystemTab(activity, contentHolder, surface, textPrimary, textSecondary, teal, amber, red) },
            Tab(activity.getString(R.string.battery_tab_apps), "📱") { renderAppsTab(activity, contentHolder, surface, textPrimary, textSecondary, teal, amber, red) },
        )
        val tabButtons = mutableListOf<TextView>()
        tabs.forEachIndexed { i, tabInfo ->
            val btn = TextView(activity).apply {
                text = tabInfo.icon + " " + tabInfo.label
                textSize = 13f
                typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
                setTextColor(if (i == 0) Color.BLACK else textSecondary)
                gravity = Gravity.CENTER
                background = GradientDrawable().apply {
                    cornerRadius = dp(activity, 20).toFloat()
                    setColor(if (i == 0) teal else surface)
                }
                setPadding(dp(activity, 16), dp(activity, 10), dp(activity, 16), dp(activity, 10))
                setOnClickListener {
                    tabButtons.forEachIndexed { j, b ->
                        (b.background as? GradientDrawable)?.setColor(if (j == i) teal else surface)
                        b.setTextColor(if (j == i) Color.BLACK else textSecondary)
                    }
                    contentHolder.removeViews(1, contentHolder.childCount - 1)
                    tabs[i].content()
                }
            }
            tabButtons.add(btn)
            tabRow.addView(btn, LinearLayout.LayoutParams(0, -2, 1f).apply {
                setMargins(dp(activity, 4), 0, dp(activity, 4), 0)
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
        val battery = BatteryUsageTracker.getBatteryInfo()
        
        // ===== Статус батареи =====
        if (battery != null) {
            holder.addView(sectionLabel(activity, activity.getString(R.string.battery_status_section), ts))
            holder.addView(card(activity, surface) { c ->
                // Большой процент
                c.addView(TextView(activity).apply {
                    text = "${battery.level}%"
                    textSize = 44f
                    typeface = Typeface.create("sans-serif-light", Typeface.BOLD)
                    setTextColor(if (battery.level <= 20) red else if (battery.level <= 40) amber else teal)
                    gravity = Gravity.CENTER
                    setPadding(0, dp(activity, 8), 0, dp(activity, 4))
                })
                c.addView(ProgressBar(activity, null, android.R.attr.progressBarStyleHorizontal).apply {
                    max = 100
                    progress = battery.level
                    progressTintList = android.content.res.ColorStateList.valueOf(if (battery.level <= 20) red else teal)
                    layoutParams = LinearLayout.LayoutParams(-1, dp(activity, 10))
                })
                c.addView(TextView(activity).apply {
                    text = if (battery.isCharging) "⚡ ${activity.getString(R.string.battery_charging)} (${battery.chargingType})" else activity.getString(R.string.battery_discharging)
                    textSize = 13f
                    setTextColor(ts)
                    gravity = Gravity.CENTER
                    setPadding(0, dp(activity, 8), 0, dp(activity, 8))
                })
                // Детали
                c.addView(statRow(activity, "🌡", activity.getString(R.string.battery_temp), "${battery.temperature}°C", tp, ts))
                c.addView(statRow(activity, "❤️", activity.getString(R.string.battery_health), battery.health, tp, ts))
                c.addView(statRow(activity, "⚡", activity.getString(R.string.battery_voltage), "${battery.voltage}mV", tp, ts))
                c.addView(statRow(activity, "🔋", activity.getString(R.string.battery_tech), battery.technology, tp, ts))
            })
        }

        // ===== Аналитика приложений =====
        if (!BatteryUsageTracker.hasUsageAccess(activity)) {
            holder.addView(sectionLabel(activity, activity.getString(R.string.battery_report_summary), ts))
            holder.addView(card(activity, surface) { c ->
                c.addView(TextView(activity).apply {
                    text = activity.getString(R.string.battery_report_need_access)
                    textSize = 14f
                    setTextColor(tp)
                })
                c.addView(TextView(activity).apply {
                    text = activity.getString(R.string.battery_report_access_hint)
                    textSize = 12f
                    setTextColor(ts)
                    setPadding(0, dp(activity, 8), 0, dp(activity, 12))
                })
                c.addView(button(activity, teal, activity.getString(R.string.battery_report_grant)) {
                    BatteryUsageTracker.requestUsageAccess(activity)
                })
            })
            return
        }

        holder.addView(TextView(activity).apply {
            text = activity.getString(R.string.battery_report_loading)
            textSize = 13f
            setTextColor(ts)
            setPadding(0, dp(activity, 12), 0, dp(activity, 12))
        })

        Thread {
            val usage = BatteryUsageTracker.collect(activity)
            activity.runOnUiThread {
                holder.removeViews(1, holder.childCount - 1)  // keep title
                renderAppStats(activity, holder, usage, surface, tp, ts, teal, amber, red)
            }
        }.apply { isDaemon = true }.start()
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
            })
            return
        }

        val totalFg = usage.sumOf { it.foregroundMs }
        val totalTraffic = usage.sumOf { it.totalTrafficBytes }
        val bgSuspects = usage.filter { it.backgroundScore >= 30 }

        // ===== Сводка =====
        holder.addView(sectionLabel(activity, activity.getString(R.string.battery_report_summary), ts))
        holder.addView(card(activity, surface) { c ->
            c.addView(statRow(activity, "📱", activity.getString(R.string.battery_report_screen_time), BatteryUsageTracker.formatDuration(totalFg), tp, ts))
            c.addView(statRow(activity, "📡", activity.getString(R.string.battery_report_total_traffic), BatteryUsageTracker.formatBytes(totalTraffic), tp, ts))
            c.addView(statRow(activity, "⚠️", activity.getString(R.string.battery_report_bg_suspects), "${bgSuspects.size}", tp, ts))
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
                    text = BatteryUsageTracker.formatDuration(app.foregroundMs)
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
                        text = "${BatteryUsageTracker.formatBytes(app.totalTrafficBytes)} · ${formatAgo(activity, app.lastUsed)}"
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
                        text = String.format("%.1f MB", mb)
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
        activity: Activity, holder: LinearLayout, surface: Int, tp: Int, ts: Int, teal: Int, amber: Int, red: Int,
    ) {
        // ===== Щит: вкл/выкл =====
        holder.addView(sectionLabel(activity, "Shield", ts))
        holder.addView(card(activity, surface) { c ->
            c.addView(TextView(activity).apply {
                text = "Быстрое управление Щитом"
                textSize = 15f
                typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                setTextColor(tp)
            })
            c.addView(TextView(activity).apply {
                text = "Включить или выключить сервис без выхода на главный экран"
                textSize = 12f
                setTextColor(ts)
                setPadding(0, dp(activity, 4), 0, dp(activity, 10))
            })
            val btnRow = LinearLayout(activity).apply { orientation = LinearLayout.HORIZONTAL }
            btnRow.addView(button(activity, teal, "Включить") {
                activity.startService(android.content.Intent(activity, WhiteDnsVpnService::class.java)
                    .setAction(Actions.CONNECT)
                    .putExtra(Actions.EXTRA_APP_INITIATED, true))
            })
            (btnRow.layoutParams as? LinearLayout.LayoutParams)?.rightMargin = dp(activity, 8)
            c.addView(btnRow)
            c.addView(button(activity, red, "Выключить") {
                activity.startService(android.content.Intent(activity, WhiteDnsVpnService::class.java)
                    .setAction(Actions.DISCONNECT)
                    .putExtra(Actions.EXTRA_APP_INITIATED, true))
            })
        })

        holder.addView(sectionLabel(activity, activity.getString(R.string.sys_device_section), ts))
        holder.addView(card(activity, surface) { c ->
            c.addView(statRow(activity, "📱", "Модель", Build.MODEL, tp, ts))
            c.addView(statRow(activity, "🏭", "Производитель", Build.MANUFACTURER, tp, ts))
            c.addView(statRow(activity, "🤖", "Android", Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")", tp, ts))
            if (Build.VERSION.SDK_INT >= 31) {
                c.addView(statRow(activity, "🏷", "MIUI/HyperOS", Build.VERSION.INCREMENTAL, tp, ts))
            }
        })

        // ===== Батарея =====
        BatteryUsageTracker.startBatteryMonitor(activity)
        val battery = BatteryUsageTracker.getBatteryInfo()
        if (battery != null) {
            holder.addView(sectionLabel(activity, activity.getString(R.string.sys_battery_section), ts))
            holder.addView(card(activity, surface) { c ->
                c.addView(statRow(activity, "🔋", "Уровень", "${battery.level}%", tp, ts))
                c.addView(statRow(activity, "🌡", "Температура", "${battery.temperature}°C", tp, ts))
                c.addView(statRow(activity, "❤️", "Здоровье", battery.health, tp, ts))
                c.addView(statRow(activity, "⚡", "Напряжение", "${battery.voltage}mV", tp, ts))
                c.addView(statRow(activity, "🔬", "Технология", battery.technology, tp, ts))
            })
        }

        // ===== Память =====
        val am = activity.getSystemService(Activity.ACTIVITY_SERVICE) as? ActivityManager
        val mi = ActivityManager.MemoryInfo()
        am?.getMemoryInfo(mi)
        if (mi != null) {
            holder.addView(sectionLabel(activity, activity.getString(R.string.sys_memory_section), ts))
            holder.addView(card(activity, surface) { c ->
                c.addView(statRow(activity, "🧠", "ОЗУ всего", BatteryUsageTracker.formatBytes(mi.totalMem), tp, ts))
                c.addView(statRow(activity, "🧠", "ОЗУ свободно", BatteryUsageTracker.formatBytes(mi.availMem), tp, ts))
                c.addView(statRow(activity, "📊", "Использовано", "${(100 - mi.availMem * 100 / mi.totalMem)}%", tp, ts))
                c.addView(ProgressBar(activity, null, android.R.attr.progressBarStyleHorizontal).apply {
                    max = 100
                    progress = (100 - mi.availMem * 100 / mi.totalMem).toInt()
                    progressTintList = android.content.res.ColorStateList.valueOf(teal)
                    layoutParams = LinearLayout.LayoutParams(-1, dp(activity, 8)).apply { topMargin = dp(activity, 8) }
                })
            })
        }

        // ===== Хранилище =====
        try {
            val internal = StatFs(activity.filesDir.absolutePath)
            val totalGB = internal.totalBytes / 1_073_741_824.0
            val freeGB = internal.availableBytes / 1_073_741_824.0
            holder.addView(sectionLabel(activity, activity.getString(R.string.sys_storage_section), ts))
            holder.addView(card(activity, surface) { c ->
                c.addView(statRow(activity, "💾", "Всего", String.format("%.1f GB", totalGB), tp, ts))
                c.addView(statRow(activity, "💾", "Свободно", String.format("%.1f GB", freeGB), tp, ts))
                c.addView(statRow(activity, "📊", "Занято", String.format("%.1f GB", totalGB - freeGB), tp, ts))
                c.addView(ProgressBar(activity, null, android.R.attr.progressBarStyleHorizontal).apply {
                    max = 100
                    progress = ((totalGB - freeGB) / totalGB * 100).toInt()
                    progressTintList = android.content.res.ColorStateList.valueOf(teal)
                    layoutParams = LinearLayout.LayoutParams(-1, dp(activity, 8)).apply { topMargin = dp(activity, 8) }
                })
            })
        } catch (_: Exception) { }

        // ===== Экран =====
        val dm = activity.resources.displayMetrics
        holder.addView(sectionLabel(activity, activity.getString(R.string.sys_display_section), ts))
        holder.addView(card(activity, surface) { c ->
            c.addView(statRow(activity, "🖥", "Разрешение", "${dm.widthPixels}×${dm.heightPixels}", tp, ts))
            c.addView(statRow(activity, "📐", "Плотность", "${dm.densityDpi}dpi (${dm.xdpi.toInt()}×${dm.ydpi.toInt()})", tp, ts))
            c.addView(statRow(activity, "🔆", "Яркость", "${(activity.getSystemService(Activity.WINDOW_SERVICE) as? android.view.WindowManager)?.currentWindowMetrics?.let { 100 } ?: 100}%", tp, ts))
        })

        // ===== Время работы =====
        holder.addView(sectionLabel(activity, activity.getString(R.string.sys_uptime_section), ts))
        holder.addView(card(activity, surface) { c ->
            val uptimeMs = android.os.SystemClock.elapsedRealtime()
            c.addView(statRow(activity, "⏱", "Включён", BatteryUsageTracker.formatDuration(uptimeMs), tp, ts))
            val awakeMs = android.os.SystemClock.uptimeMillis()
            c.addView(statRow(activity, "👁", "Без сна", BatteryUsageTracker.formatDuration(awakeMs), tp, ts))
            c.addView(statRow(activity, "😴", "В сне", BatteryUsageTracker.formatDuration(uptimeMs - awakeMs), tp, ts))
        })
    }

    // ==================== TAB: Приложения ====================
    private fun renderAppsTab(
        activity: Activity, holder: LinearLayout, surface: Int, tp: Int, ts: Int, teal: Int, amber: Int, red: Int,
    ) {
        holder.addView(TextView(activity).apply {
            text = activity.getString(R.string.battery_report_loading)
            textSize = 13f
            setTextColor(ts)
            setPadding(0, dp(activity, 12), 0, dp(activity, 12))
        })
        Thread {
            val pm = activity.packageManager
            val apps = pm.getInstalledApplications(0)
                .filter { it.loadLabel(pm).isNotBlank() }
                .sortedBy { it.loadLabel(pm).toString().lowercase() }
            activity.runOnUiThread {
                holder.removeViews(1, holder.childCount - 1)
                if (!BatteryUsageTracker.hasUsageAccess(activity)) {
                    holder.addView(card(activity, surface) { c ->
                        c.addView(TextView(activity).apply {
                            text = activity.getString(R.string.battery_report_need_access)
                            textSize = 14f
                            setTextColor(tp)
                        })
                        c.addView(button(activity, teal, activity.getString(R.string.battery_report_grant)) {
                            BatteryUsageTracker.requestUsageAccess(activity)
                        })
                    })
                    return@runOnUiThread
                }
                val usage = BatteryUsageTracker.collect(activity)
                val usageMap = usage.associateBy { it.packageName }
                holder.addView(sectionLabel(activity, "${apps.size} ${activity.getString(R.string.apps_count)}", ts))
                apps.take(100).forEach { appInfo ->
                    val appName = appInfo.loadLabel(pm).toString()
                    val isSystem = (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
                    val usageStat = usageMap[appInfo.packageName]
                    holder.addView(card(activity, surface) { c ->
                        c.addView(appHeader(activity, if (isSystem) "⚙️" else "📱", appName, isSystem, tp, ts))
                        if (usageStat != null) {
                            c.addView(TextView(activity).apply {
                                text = "Экран: ${BatteryUsageTracker.formatDuration(usageStat.foregroundMs)} · Трафик: ${BatteryUsageTracker.formatBytes(usageStat.totalTrafficBytes)}"
                                textSize = 12f
                                setTextColor(ts)
                            })
                        }
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
        return LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(activity, 4), 0, dp(activity, 4))
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
                setTextColor(tp)
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
            hours > 24 -> "${hours / 24}д"
            hours > 0 -> "${hours}ч"
            else -> "${min(minutes, 1)}м"
        }
    }
}
