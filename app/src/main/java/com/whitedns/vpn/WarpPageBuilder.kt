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
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch

/**
 * Экран «WARP»: генерация Cloudflare WireGuard-конфигов, добавление в пул Щита.
 */
object WarpPageBuilder {

    fun buildScreen(activity: Activity): View {
        val bg = pal(activity).background
        val surface = pal(activity).surface
        val tp = pal(activity).textPrimary
        val ts = pal(activity).textSecondary
        val teal = pal(activity).teal
        val outline = pal(activity).outline

        val root = FrameLayout(activity).apply { setBackgroundColor(bg) }
        val scroll = ScrollView(activity).apply { setBackgroundColor(bg); isFillViewport = true }
        val content = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(activity, 20), dp(activity, 18), dp(activity, 20), dp(activity, 40))
        }
        scroll.addView(content)
        root.addView(scroll, FrameLayout.LayoutParams(-1, -1))

        content.addView(TextView(activity).apply {
            text = "☁️  Cloudflare WARP"
            textSize = 22f
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            setTextColor(tp)
        })
        content.addView(TextView(activity).apply {
            text = "Бесплатные WireGuard-каналы Cloudflare: чистые IP, быстрый обход блокировок. Конфиги генерируются на устройстве и добавляются в пул каналов Щита."
            textSize = 13f; setTextColor(ts)
        }, LinearLayout.LayoutParams(-2, -2).apply { topMargin = dp(activity, 4) })

        // ===== статус =====
        val card = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(activity, 16).toFloat()
                setColor((surface.toLong() and 0xFFFFFFC8L).toInt())
                setStroke(dp(activity, 1), (outline.toLong() and 0xB4FFFFFFL).toInt())
            }
            clipToOutline = true
            setPadding(dp(activity, 16), dp(activity, 14), dp(activity, 16), dp(activity, 14))
        }
        val countValue = TextView(activity).apply {
            textSize = 15f; setTextColor(tp)
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            text = "Конфигов: ${WarpGenerator.loadConfigs(activity).size}"
        }
        card.addView(countValue)
        val statusValue = TextView(activity).apply {
            text = "Готов к генерации"
            textSize = 13f; setTextColor(ts)
        }
        card.addView(statusValue, LinearLayout.LayoutParams(-2, -2).apply { topMargin = dp(activity, 4) })
        content.addView(card, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(activity, 14) })

        // ===== количество =====
        content.addView(TextView(activity).apply {
            text = "Сколько конфигов генерировать"
            textSize = 12f; setTextColor(ts)
        }, LinearLayout.LayoutParams(-2, -2).apply { topMargin = dp(activity, 16) })
        val countRow = LinearLayout(activity).apply { orientation = LinearLayout.HORIZONTAL }
        val countSwitch = MaterialSwitch(activity).apply {
            isChecked = false
            text = "5 конфигов (вместо 1)"
            textSize = 13f
        }
        countRow.addView(countSwitch, LinearLayout.LayoutParams(-1, -2))
        content.addView(countRow)

        // ===== кнопка =====
        val genBtn = MaterialButton(activity).apply {
            text = "Сгенерировать"
            setOnClickListener {
                isEnabled = false
                text = "Генерация…"
                val n = if (countSwitch.isChecked) 5 else 1
                statusValue.text = "Регистрирую устройство в Cloudflare…"
                WarpGenerator.generateBatch(activity, n,
                    onProgress = { i, total ->
                        statusValue.text = "Готово $i из $total"
                        countValue.text = "Конфигов: ${WarpGenerator.loadConfigs(activity).size}"
                    },
                    onDone = { cfgs ->
                        isEnabled = true
                        text = if (cfgs.isEmpty()) "Сгенерировать" else "Ещё"
                        statusValue.text = if (cfgs.isEmpty()) "Ошибка генерации — попробуйте позже"
                        else "Добавлено: ${cfgs.joinToString(", ") { it.name }}"
                        countValue.text = "Конфигов: ${WarpGenerator.loadConfigs(activity).size}"
                        if (cfgs.isNotEmpty()) {
                            Toast.makeText(activity, "WARP добавлен в пул каналов — перезапустите Щит", Toast.LENGTH_LONG).show()
                        }
                    })
            }
        }
        content.addView(genBtn, LinearLayout.LayoutParams(-1, dp(activity, 52)).apply { topMargin = dp(activity, 16) })

        content.addView(TextView(activity).apply {
            text = "Конфиги хранятся на устройстве. После генерации перезапустите Щит — WARP-каналы появятся в списке каналов (группа Авто-выбор)."
            textSize = 11f; setTextColor(ts)
        }, LinearLayout.LayoutParams(-2, -2).apply { topMargin = dp(activity, 12) })

        return root
    }

    private fun pal(activity: Activity): com.whitedns.vpn.WhiteDnsPalette =
        com.whitedns.vpn.WhiteDnsDesignTokens.forContext(activity)

    private fun dp(activity: Activity, v: Int): Int = (v * activity.resources.displayMetrics.density).toInt()
}
