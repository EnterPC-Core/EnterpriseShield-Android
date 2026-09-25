package com.whitedns.vpn

import android.app.Activity
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView

/**
 * Панель «Диагностика сети» для раздела Тестирование.
 * Показывает цепочку: Wi-Fi -> Роутер -> Линия -> Провайдер.
 */
object DiagnosticSectionBuilder {
    private var rows = mutableListOf<TextView>()

    fun build(activity: Activity, panel: LinearLayout, dp: (Int) -> Int): View {
        rows.clear()
        val container = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(4), dp(4), dp(4), dp(4))
        }
        repeat(4) { idx ->
            val row = TextView(activity).apply {
                textSize = 13.5f
                setPadding(dp(8), dp(7), dp(8), dp(7))
                text = activity.getString(R.string.diag_step_waiting, idx + 1)
                setTextColor(0xFF8A94A6.toInt())
            }
            rows.add(row)
            container.addView(
                row,
                LinearLayout.LayoutParams(-1, -2),
            )
        }
        panel.addView(container)
        runDiagnostics(activity)
        return container
    }

    fun runDiagnostics(activity: Activity) {
        rows.forEach { it.text = activity.getString(R.string.diag_step_waiting, 0) }
        NetworkDiagnostics.run(activity) { steps ->
            activity.runOnUiThread { applySteps(activity, steps) }
        }
    }

    fun applySteps(activity: Activity, steps: List<NetworkDiagnostics.StepResult>) {
        for (i in rows.indices) {
            val row = rows[i]
            val step = steps.getOrNull(i) ?: continue
            row.text = activity.getString(step.labelRes) + "  ·  " + step.detail
            row.setTextColor(if (step.ok) 0xFF7FD6C4.toInt() else 0xFFE26565.toInt())
        }
    }
}
