package com.whitedns.vpn

import android.app.Activity
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch

/**
 * Вкладка «WARP»: генерация Cloudflare WireGuard-конфигов на устройстве.
 * Подключение: список каналов → «🚀 Каналы» → «☁️ WARP».
 */
object WarpPageBuilder {

    fun buildTabScreen(activity: Activity): View {
        val bg = pal(activity).background
        val surface = pal(activity).surface
        val tp = pal(activity).textPrimary
        val ts = pal(activity).textSecondary
        val teal = pal(activity).teal
        val outline = pal(activity).outline

        val root = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
        }
        val scroll = ScrollView(activity).apply { setBackgroundColor(bg); isFillViewport = true }
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(scroll) { view, insets ->
            val bars = insets.getInsets(
                androidx.core.view.WindowInsetsCompat.Type.statusBars() or
                    androidx.core.view.WindowInsetsCompat.Type.navigationBars() or
                    androidx.core.view.WindowInsetsCompat.Type.displayCutout(),
            )
            view.setPadding(view.paddingLeft, bars.top, view.paddingRight, bars.bottom)
            insets
        }
        val content = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(activity, 20), dp(activity, 14), dp(activity, 20), dp(activity, 40))
        }
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        content.addView(TextView(activity).apply {
            text = "☁️  Cloudflare WARP"
            textSize = 24f
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            setTextColor(tp)
        })
        content.addView(TextView(activity).apply {
            text = "Бесплатные WireGuard-каналы Cloudflare. После генерации перезапустите Щит и выберите «☁️ WARP» в списке каналов."
            textSize = 13f; setTextColor(ts)
        }, LinearLayout.LayoutParams(-2, -2).apply { topMargin = dp(activity, 2) })

        // ===== кнопка ВКЛ/ВЫКЛ WARP =====
        val warpSwitch = MaterialSwitch(activity).apply {
            isChecked = WarpVpnService.isActive
            text = "WARP подключение"
            textSize = 15f
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            setOnCheckedChangeListener { _, checked ->
                if (checked) {
                    WarpVpnService.start(activity)
                    Toast.makeText(activity, "Подключаю WARP…", Toast.LENGTH_SHORT).show()
                } else {
                    WarpVpnService.stop(activity)
                    Toast.makeText(activity, "WARP отключён", Toast.LENGTH_SHORT).show()
                }
            }
        }
        val warpCard = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(activity, 16).toFloat()
                setColor((pal(activity).surface.toLong() and 0xFFFFFFC8L).toInt())
                setStroke(dp(activity, 1), (pal(activity).outline.toLong() and 0xB4FFFFFFL).toInt())
            }
            clipToOutline = true
            setPadding(dp(activity, 16), dp(activity, 14), dp(activity, 16), dp(activity, 14))
            addView(warpSwitch, LinearLayout.LayoutParams(-1, -2))
        }
        content.addView(warpCard, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(activity, 16) })

        // ===== список конфигов =====
        val listContainer = LinearLayout(activity).apply { orientation = LinearLayout.VERTICAL }
        content.addView(listContainer, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(activity, 16) })

        // ===== статус =====
        val statusValue = TextView(activity).apply {
            text = "Готов к генерации"
            textSize = 13f; setTextColor(ts)
        }
        content.addView(statusValue, LinearLayout.LayoutParams(-2, -2).apply { topMargin = dp(activity, 12) })

        val countSwitch = MaterialSwitch(activity).apply {
            isChecked = false
            text = "5 конфигов (вместо 1)"
            textSize = 13f
        }
        content.addView(countSwitch, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(activity, 8) })

        val genBtn = MaterialButton(activity).apply {
            text = "Сгенерировать"
            setOnClickListener {
                isEnabled = false
                text = "Генерация…"
                val n = if (countSwitch.isChecked) 5 else 1
                statusValue.text = "Регистрирую в Cloudflare…"
                WarpGenerator.generateBatch(activity, n,
                    onProgress = { i, total -> statusValue.text = "Готово $i из $total" },
                    onDone = { cfgs ->
                        isEnabled = true
                        text = if (cfgs.isEmpty()) "Сгенерировать" else "Ещё"
                        statusValue.text = if (cfgs.isEmpty()) "Ошибка — попробуйте позже"
                        else "Готово. Перезапустите Щит → Каналы → ☁️ WARP"
                        if (cfgs.isNotEmpty()) rebuildList(activity, listContainer)
                    })
            }
        }
        content.addView(genBtn, LinearLayout.LayoutParams(-1, dp(activity, 52)).apply { topMargin = dp(activity, 10) })

        val clearBtn = MaterialButton(activity).apply {
            text = "Удалить все конфиги"
            setOnClickListener {
                WarpGenerator.clearConfigs(activity)
                rebuildList(activity, listContainer)
                statusValue.text = "Удалено — перезапустите Щит"
            }
        }
        content.addView(clearBtn, LinearLayout.LayoutParams(-1, dp(activity, 44)).apply { topMargin = dp(activity, 8) })

        rebuildList(activity, listContainer)
        return root
    }

    private fun rebuildList(activity: Activity, container: LinearLayout) {
        container.removeAllViews()
        val configs = WarpGenerator.loadConfigs(activity)
        if (configs.isEmpty()) {
            container.addView(TextView(activity).apply {
                text = "Конфигов нет — сгенерируйте"
                textSize = 13f; setTextColor(pal(activity).textSecondary)
            })
            return
        }
        for (c in configs) {
            val card = LinearLayout(activity).apply {
                orientation = LinearLayout.VERTICAL
                background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = dp(activity, 14).toFloat()
                    setColor((pal(activity).surface.toLong() and 0xFFFFFFC8L).toInt())
                    setStroke(dp(activity, 1), (pal(activity).outline.toLong() and 0xB4FFFFFFL).toInt())
                }
                clipToOutline = true
                setPadding(dp(activity, 14), dp(activity, 12), dp(activity, 14), dp(activity, 12))
            }
            card.addView(TextView(activity).apply {
                text = c.name
                textSize = 15f; setTextColor(pal(activity).textPrimary)
                typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            })
            card.addView(TextView(activity).apply {
                text = "${c.server}:${c.port} · ${c.ip4}"
                textSize = 12f; setTextColor(pal(activity).textSecondary)
            }, LinearLayout.LayoutParams(-2, -2).apply { topMargin = dp(activity, 2) })
            container.addView(card, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(activity, 8) })
        }
    }

    private fun pal(activity: Activity): com.whitedns.vpn.WhiteDnsPalette =
        com.whitedns.vpn.WhiteDnsDesignTokens.forContext(activity)

    private fun dp(activity: Activity, v: Int): Int = (v * activity.resources.displayMetrics.density).toInt()
}
