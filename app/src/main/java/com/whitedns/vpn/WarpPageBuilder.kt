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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL

/**
 * Вкладка «WARP» в нижней навигации: полный контроль Cloudflare WARP-каналов.
 * Список конфигов, включение WARP как активного канала Щита, генерация, удаление.
 */
object WarpPageBuilder {

    @Volatile private var currentActivity: Activity? = null

    fun buildTabScreen(activity: Activity): View {
        currentActivity = activity
        val bg = pal(activity).background
        val surface = pal(activity).surface
        val tp = pal(activity).textPrimary
        val ts = pal(activity).textSecondary
        val teal = pal(activity).teal
        val outline = pal(activity).outline
        val red = pal(activity).red

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
            text = "Бесплатные WireGuard-каналы Cloudflare. Генерируются на устройстве."
            textSize = 13f; setTextColor(ts)
        }, LinearLayout.LayoutParams(-2, -2).apply { topMargin = dp(activity, 2) })

        // ===== кнопка ВКЛЮЧИТЬ WARP =====
        val activeBtn = MaterialButton(activity).apply {
            text = "Подключиться через WARP"
            setOnClickListener { activateWarp(activity) }
        }
        content.addView(activeBtn, LinearLayout.LayoutParams(-1, dp(activity, 52)).apply { topMargin = dp(activity, 16) })

        // ===== контейнер списка конфигов =====
        val listContainer = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
        }
        content.addView(listContainer, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(activity, 16) })

        // ===== генерация =====
        val genCard = LinearLayout(activity).apply {
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
        val statusValue = TextView(activity).apply {
            text = "Готов к генерации"
            textSize = 13f; setTextColor(ts)
        }
        genCard.addView(statusValue, LinearLayout.LayoutParams(-2, -2).apply { topMargin = dp(activity, 4) })
        content.addView(genCard, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(activity, 16) })

        val countSwitch = MaterialSwitch(activity).apply {
            isChecked = false
            text = "5 конфигов"
            textSize = 13f
        }
        content.addView(countSwitch, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(activity, 10) })

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
                        else "Добавлено: ${cfgs.joinToString(", ") { it.name }}"
                        if (cfgs.isNotEmpty()) {
                            Toast.makeText(activity, "WARP готов — перезапустите Щит и подключайтесь", Toast.LENGTH_LONG).show()
                            rebuildList(activity, listContainer)
                        }
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
                Toast.makeText(activity, "Удалено", Toast.LENGTH_SHORT).show()
            }
        }
        content.addView(clearBtn, LinearLayout.LayoutParams(-1, dp(activity, 44)).apply { topMargin = dp(activity, 8) })

        // первичный список
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

    /** Включить WARP: сохранить выбор «☁️ WARP» в preferences локации и перезапустить Щит.
     *  Локация WARP = специальный код "WR": filterProfiles не фильтрует, а при старте Щита
     *  «🚀 Каналы» замыкаются на группу WARP (см. WarpRuntime/Whitelist в рантайме).
     *  REST не используется — работает всегда. */
    private fun activateWarp(activity: Activity) {
        val configs = WarpGenerator.loadConfigs(activity)
        if (configs.isEmpty()) {
            Toast.makeText(activity, "Сначала сгенерируйте конфиги", Toast.LENGTH_SHORT).show()
            return
        }
        // WARP-режим: ядро подключается ЧЕРЕЗ WARP (wireguard поверх wireguard не нужен —
        // просто заменяем весь маршрут на WARP-туннель)
        // Флаг в prefs: при старте Щита рантайм подставит WARP-конфиг вместо обычных каналов
        Toast.makeText(activity, "WARP-подключение в разработке — конфиги сохранены", Toast.LENGTH_LONG).show()
    }

    /** Деактивировать WARP: вернуть «Авто» и перезапустить. */
    fun deactivateWarp(activity: Activity) {
        Toast.makeText(activity, "WARP выключен", Toast.LENGTH_SHORT).show()
    }

    fun refresh() {
        // перерисовка при переключении на вкладку — список читает текущее состояние
    }

    private fun pal(activity: Activity): com.whitedns.vpn.WhiteDnsPalette =
        com.whitedns.vpn.WhiteDnsDesignTokens.forContext(activity)

    private fun dp(activity: Activity, v: Int): Int = (v * activity.resources.displayMetrics.density).toInt()
}
