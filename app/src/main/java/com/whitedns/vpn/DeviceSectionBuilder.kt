package com.whitedns.vpn

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.button.MaterialButton

/**
 * Секция «Устройство» для настроек: код устройства, статус привязки,
 * копирование кода (код отправляется боту @Enterprise_vpn_bot).
 */
object DeviceSectionBuilder {

    fun build(
        activity: Activity,
        palette: Any?,
        panel: LinearLayout,
        dp: (Int) -> Int,
        refresh: () -> Unit,
    ): View {
        val mono: Typeface = Typeface.MONOSPACE
        val statusView = TextView(activity).apply {
            text = activity.getString(R.string.device_status_checking)
            textSize = 14f
        }
        val codeView = TextView(activity).apply {
            text = ShieldDevice.code(activity).chunked(8).joinToString(" ")
            typeface = mono
            textSize = 15f
            setTextIsSelectable(true)
            letterSpacing = 0.04f
        }
        val copyButton = MaterialButton(activity).apply {
            text = activity.getString(R.string.device_copy_code)
            insetTop = 0
            insetBottom = 0
            cornerRadius = dp(8)
            strokeWidth = dp(1)
            elevation = 0f
            stateListAnimator = null
            setOnClickListener {
                ShieldDevice.copyCode(activity)
                text = activity.getString(R.string.device_code_copied)
                postDelayed({ text = activity.getString(R.string.device_copy_code) }, 2000)
            }
        }
        val checkButton = MaterialButton(activity).apply {
            text = activity.getString(R.string.device_check_status)
            insetTop = 0
            insetBottom = 0
            cornerRadius = dp(8)
            strokeWidth = dp(1)
            elevation = 0f
            stateListAnimator = null
            setOnClickListener {
                statusView.text = activity.getString(R.string.device_status_checking)
                refresh()
            }
        }
        val hint = TextView(activity).apply {
            text = activity.getString(R.string.device_link_hint)
            textSize = 13f
        }
        val row = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
            addView(copyButton, LinearLayout.LayoutParams(-2, dp(40)).apply { marginEnd = dp(8) })
            addView(checkButton, LinearLayout.LayoutParams(-2, dp(40)))
        }
        panel.apply {
            addView(statusView, LinearLayout.LayoutParams(-1, -2).apply {
                marginStart = dp(16); marginEnd = dp(16); topMargin = dp(14)
            })
            addView(codeView, LinearLayout.LayoutParams(-1, -2).apply {
                marginStart = dp(16); marginEnd = dp(16); topMargin = dp(10)
            })
            addView(row, LinearLayout.LayoutParams(-1, -2).apply {
                marginStart = dp(16); marginEnd = dp(16); topMargin = dp(10)
            })
            addView(hint, LinearLayout.LayoutParams(-1, -2).apply {
                marginStart = dp(16); marginEnd = dp(16); topMargin = dp(10); bottomMargin = dp(14)
            })
        }
        return panel
    }

    fun applyStatus(activity: Activity, status: ShieldDevice.DeviceStatus?, statusView: TextView?) {
        val view = statusView ?: return
        if (status == null) {
            view.text = activity.getString(R.string.device_status_error)
            return
        }
        when {
            status.banned -> view.text = activity.getString(R.string.device_status_banned)
            status.active -> view.text =
                if (status.telegram.isNotBlank()) activity.getString(R.string.device_status_linked, status.telegram)
                else activity.getString(R.string.device_status_linked_no_name)
            else -> view.text = activity.getString(R.string.device_status_not_linked)
        }
    }
}
