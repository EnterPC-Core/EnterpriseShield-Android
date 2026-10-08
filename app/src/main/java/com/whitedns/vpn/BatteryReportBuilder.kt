package com.whitedns.vpn

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.math.min

/**
 * Страница «Кто жрёт батарею»: аналитика использования приложений,
 * фонового трафика и оценка фонового вредительства.
 * Стиль: MaxWidthLinearLayout, карточки SURFACE, заголовки секций.
 */
object BatteryReportBuilder {

    fun buildScreen(activity: Activity): View {
        val bg = paletteColor(activity, "background", 0xFF101418)
        val surface = paletteColor(activity, "surface", 0xFF1A2028)
        val textPrimary = paletteColor(activity, "textPrimary", 0xFFE8EDF2)
        val textSecondary = paletteColor(activity, "textSecondary", 0xFF8A94A0)
        val teal = paletteColor(activity, "teal", 0xFF4DD0C4)
        val amber = paletteColor(activity, "amber", 0xFFFFB74D)
        val red = paletteColor(activity, "red", 0xFFEF5350)

        val root = FrameLayout(activity).apply { setBackgroundColor(bg) }
        val scrollView = ScrollView(activity).apply {
            isFillViewport = true
            clipToPadding = false
            setBackgroundColor(bg)
        }
        ViewCompat.setOnApplyWindowInsetsListener(scrollView) { view, insets ->
            val top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            val bottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            view.setPadding(view.paddingLeft, top, view.paddingRight, bottom)
            insets
        }
        val body = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(activity, 24), dp(activity, 20), dp(activity, 24), dp(activity, 40))
        }

        // ===== Заголовок =====
        body.addView(TextView(activity).apply {
            text = activity.getString(R.string.battery_report_title)
            textSize = 22f
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            setTextColor(textPrimary)
        })
        body.addView(TextView(activity).apply {
            text = activity.getString(R.string.battery_report_subtitle)
            textSize = 13f
            setTextColor(textSecondary)
            setPadding(0, dp(activity, 4), 0, dp(activity, 16))
        })

        val contentHolder = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
        }
        body.addView(contentHolder)

        // ===== Проверка permissions =====
        if (!BatteryUsageTracker.hasUsageAccess(activity)) {
            body.addView(card(activity, surface) { cardBody ->
                cardBody.addView(TextView(activity).apply {
                    text = activity.getString(R.string.battery_report_need_access)
                    textSize = 15f
                    setTextColor(textPrimary)
                })
                cardBody.addView(TextView(activity).apply {
                    text = activity.getString(R.string.battery_report_access_hint)
                    textSize = 13f
                    setTextColor(textSecondary)
                    setPadding(0, dp(activity, 8), 0, dp(activity, 12))
                })
                cardBody.addView(button(activity, teal, activity.getString(R.string.battery_report_grant)) {
                    BatteryUsageTracker.requestUsageAccess(activity)
                })
            })
        } else {
            body.addView(TextView(activity).apply {
                text = activity.getString(R.string.battery_report_loading)
                textSize = 14f
                setTextColor(textSecondary)
                setPadding(0, dp(activity, 16), 0, dp(activity, 16))
            })
            // Загружаем данные асинхронно
            Thread {
                val sinceMs = System.currentTimeMillis() - 24 * 3_600_000L
                val usage = BatteryUsageTracker.collect(activity, sinceMs)
                activity.runOnUiThread {
                    contentHolder.removeAllViews()
                    renderReport(activity, contentHolder, usage, surface, textPrimary, textSecondary, teal, amber, red)
                }
            }.apply { isDaemon = true }.start()
        }

        scrollView.addView(body)
        root.addView(scrollView)
        return root
    }

    @SuppressLint("DefaultLocale")
    private fun renderReport(
        activity: Activity,
        holder: LinearLayout,
        usage: List<BatteryUsageTracker.AppUsage>,
        surface: Int, textPrimary: Int, textSecondary: Int, teal: Int, amber: Int, red: Int,
    ) {
        if (usage.isEmpty()) {
            holder.addView(TextView(activity).apply {
                text = activity.getString(R.string.battery_report_empty)
                textSize = 14f
                setTextColor(textSecondary)
            })
            return
        }

        // ===== Сводка =====
        val totalFg = usage.sumOf { it.foregroundMs }
        val totalTraffic = usage.sumOf { it.totalTrafficBytes }
        val bgSuspects = usage.filter { it.backgroundScore >= 30 }

        holder.addView(sectionLabel(activity, activity.getString(R.string.battery_report_summary), textSecondary))
        holder.addView(card(activity, surface) { c ->
            c.addView(statRow(activity, "📱", activity.getString(R.string.battery_report_screen_time),
                BatteryUsageTracker.formatDuration(totalFg), textPrimary, textSecondary))
            c.addView(statRow(activity, "📡", activity.getString(R.string.battery_report_total_traffic),
                BatteryUsageTracker.formatBytes(totalTraffic), textPrimary, textSecondary))
            c.addView(statRow(activity, "⚠️", activity.getString(R.string.battery_report_bg_suspects),
                "${bgSuspects.size}", textPrimary, textSecondary))
        })

        // ===== Топ по экранному времени =====
        val screenTop = usage.sortedByDescending { it.foregroundMs }.take(10)
        val maxFg = screenTop.firstOrNull()?.foregroundMs ?: 1L
        holder.addView(sectionLabel(activity, activity.getString(R.string.battery_report_top_screen), textSecondary))
        screenTop.forEachIndexed { i, app ->
            holder.addView(appCard(activity, surface, textPrimary, textSecondary) { c ->
                c.addView(appHeader(activity, "#${i + 1}", app.appName, app.isSystem, textPrimary, textSecondary, teal, amber))
                c.addView(progressBar(activity, teal, (app.foregroundMs.toFloat() / maxFg * 100).toInt()))
                c.addView(appDetail(activity,
                    BatteryUsageTracker.formatDuration(app.foregroundMs), textSecondary))
            })
        }

        // ===== Фоновые прожоры (трафик без использования) =====
        if (bgSuspects.isNotEmpty()) {
            holder.addView(sectionLabel(activity, activity.getString(R.string.battery_report_bg_hogs), textSecondary))
            val maxScore = bgSuspects.maxOf { it.backgroundScore }
            bgSuspects.sortedByDescending { it.backgroundScore }.take(10).forEach { app ->
                val color = when {
                    app.backgroundScore >= 70 -> red
                    app.backgroundScore >= 50 -> amber
                    else -> teal
                }
                holder.addView(appCard(activity, surface, textPrimary, textSecondary) { c ->
                    c.addView(appHeader(activity, "⚠", app.appName, app.isSystem, textPrimary, textSecondary, teal, amber))
                    c.addView(progressBar(activity, color, app.backgroundScore * 100 / maxScore))
                    c.addView(appDetail(activity,
                        "${activity.getString(R.string.battery_report_traffic)}: ${BatteryUsageTracker.formatBytes(app.totalTrafficBytes)}"
                        + " · ${activity.getString(R.string.battery_report_used_ago)}: ${formatAgo(activity, app.lastUsed)}",
                        textSecondary))
                })
            }
        }

        // ===== Топ по мобильному трафику =====
        val mobileTop = usage.filter { (it.mobileRxBytes + it.mobileTxBytes) > 0 }
            .sortedByDescending { it.mobileRxBytes + it.mobileTxBytes }
            .take(10)
        if (mobileTop.isNotEmpty()) {
            holder.addView(sectionLabel(activity, activity.getString(R.string.battery_report_top_mobile), textSecondary))
            val maxMb = mobileTop.firstOrNull()?.let { (it.mobileRxBytes + it.mobileTxBytes) / 1_048_576.0 } ?: 1.0
            mobileTop.forEach { app ->
                val mb = (app.mobileRxBytes + app.mobileTxBytes) / 1_048_576.0
                holder.addView(appCard(activity, surface, textPrimary, textSecondary) { c ->
                    c.addView(appHeader(activity, "📶", app.appName, app.isSystem, textPrimary, textSecondary, teal, amber))
                    c.addView(progressBar(activity, amber, (mb / maxMb * 100).toInt().coerceAtMost(100)))
                    c.addView(appDetail(activity, String.format("%.1f MB", mb), textSecondary))
                })
            }
        }

        // ===== Рекомендация =====
        if (bgSuspects.isNotEmpty()) {
            holder.addView(sectionLabel(activity, activity.getString(R.string.battery_report_recommend), textSecondary))
            holder.addView(card(activity, surface) { c ->
                c.addView(TextView(activity).apply {
                    val top = bgSuspects.first()
                    text = activity.getString(R.string.battery_report_recommend_text, top.appName)
                    textSize = 14f
                    setTextColor(textPrimary)
                })
            })
        }
    }

    // ===== UI helpers =====

    private fun paletteColor(activity: Activity, name: String, default: Long): Int {
        return try {
            val field = activity.javaClass.getDeclaredField("palette")
            field.isAccessible = true
            val palette = field.get(activity)
            val getter = palette.javaClass.getMethod("get" + name[0].uppercaseChar() + name.substring(1))
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
            letterSpacing = 0f
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
            addView(inner)
        }
    }

    private fun appCard(activity: Activity, surface: Int, tp: Int, ts: Int, content: (LinearLayout) -> Unit): LinearLayout {
        return card(activity, surface) { c ->
            c.addView(LinearLayout(activity).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(0, dp(activity, 2), 0, dp(activity, 6))
            }.also { inner ->
                content(inner)
            })
        }
    }

    private fun appHeader(activity: Activity, badge: String, name: String, isSystem: Boolean, tp: Int, ts: Int, teal: Int, amber: Int): LinearLayout {
        return LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            addView(TextView(activity).apply {
                text = badge
                textSize = 14f
                setTextColor(teal)
                setPadding(0, 0, dp(activity, 8), 0)
            })
            addView(TextView(activity).apply {
                text = name
                textSize = 15f
                typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                setTextColor(tp)
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                maxLines = 1
            })
            addView(TextView(activity).apply {
                text = if (isSystem) "SYS" else ""
                textSize = 10f
                setTextColor(amber)
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

    private fun appDetail(activity: Activity, text: String, color: Int): TextView {
        return TextView(activity).apply {
            this.text = text
            textSize = 12f
            setTextColor(color)
        }
    }

    private fun statRow(activity: Activity, icon: String, label: String, value: String, tp: Int, ts: Int): LinearLayout {
        return LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(activity, 4), 0, dp(activity, 4))
            addView(TextView(activity).apply {
                text = icon
                textSize = 16f
                setPadding(0, 0, dp(activity, 10), 0)
            })
            addView(TextView(activity).apply {
                text = label
                textSize = 14f
                setTextColor(ts)
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
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
            hours > 24 -> activity.getString(R.string.battery_report_days_ago, hours / 24)
            hours > 0 -> activity.getString(R.string.battery_report_hours_ago, hours)
            else -> activity.getString(R.string.battery_report_min_ago, min(minutes, 1))
        }
    }
}
