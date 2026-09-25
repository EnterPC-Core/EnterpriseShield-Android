package com.whitedns.vpn

import android.app.Activity
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

object DiagnosticSectionBuilder {
    private const val OK = 0xFF7FD6C4.toInt()
    private const val BAD = 0xFFFF6B6B.toInt()
    private const val MUTED = 0xFF8A94A6.toInt()

    fun build(
        activity: Activity,
        panel: LinearLayout,
        dp: (Int) -> Int,
        strings: DiagStrings,
    ) {
        val rows = mutableListOf<TextView>()
        val titles = listOf(strings.wifi, strings.router, strings.line, strings.isp)
        val results = mutableListOf<DiagStep?>()

        repeat(4) { idx ->
            val row = TextView(activity).apply {
                textSize = 14f
                setPadding(dp(10), dp(7), dp(10), dp(7))
                text = titles[idx] + " · " + strings.waiting
                setTextColor(MUTED)
            }
            rows.add(row)
            results.add(null)
            panel.addView(
                row,
                LinearLayout.LayoutParams(-1, -2).apply {
                    topMargin = if (idx == 0) dp(10) else dp(2)
                },
            )
        }

        val verdict = TextView(activity).apply {
            textSize = 13.5f
            setPadding(dp(10), dp(8), dp(10), dp(4))
            setTextColor(MUTED)
            visibility = android.view.View.GONE
        }
        panel.addView(verdict, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(6) })

        fun refresh() {
            rows.forEachIndexed { i, tv ->
                val st = results[i]
                tv.text = when {
                    st == null -> titles[i] + " · " + strings.waiting
                    st.ok == true -> "${st.title}: ${st.detail}  ✓"
                    st.ok == false -> "${st.title}: ${st.detail}  ✗"
                    else -> "${st.title}: ${st.detail}"
                }
                st?.ok.let { ok ->
                    tv.setTextColor(if (ok == true) OK else if (ok == false) BAD else MUTED)
                }
            }
            val all = results.filterNotNull()
            if (all.size == 4) {
                val firstBad = all.firstOrNull { it.ok == false }
                verdict.text = if (firstBad == null) strings.verdictOk else strings.verdictBadFmt.format(firstBad.title)
                verdict.setTextColor(if (firstBad == null) OK else BAD)
                verdict.visibility = android.view.View.VISIBLE
            }
        }

        val runBtn = com.google.android.material.button.MaterialButton(activity).apply {
            text = strings.run
            insetTop = 0; insetBottom = 0
            cornerRadius = dp(10)
            strokeWidth = dp(1)
            elevation = 0f
            stateListAnimator = null
            setOnClickListener {
                isEnabled = false
                results.clear(); repeat(4) { results.add(null) }
                verdict.visibility = android.view.View.GONE
                refresh()
                NetworkDiagnostics.run(activity) { step ->
                    activity.runOnUiThread {
                        results[step.index] = step
                        refresh()
                        if (step.index == 3) isEnabled = true
                    }
                }
            }
        }
        panel.addView(
            runBtn,
            LinearLayout.LayoutParams(-1, dp(46)).apply {
                topMargin = dp(12); bottomMargin = dp(8)
            },
        )
        refresh()
    }

    data class DiagStrings(
        val wifi: String,
        val router: String,
        val line: String,
        val isp: String,
        val waiting: String,
        val run: String,
        val verdictOk: String,
        val verdictBadFmt: String,
    )
}
