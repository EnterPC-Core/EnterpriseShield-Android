package com.whitedns.vpn

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.materialswitch.MaterialSwitch
import dev.minios.tgwsproxy.proxy.ProxyConfig
import dev.minios.tgwsproxy.proxy.TgWsProxyServer

/**
 * «Telegram-прокси»: локальный MTProto-прокси на устройстве (tg-ws-proxy, MIT © crim50n).
 * Telegram подключается по tg://proxy к 127.0.0.1:1443, трафик идёт WebSocket'ами
 * к Telegram DC напрямую/через Cloudflare. Работает независимо от туннеля Щита.
 */
object TgProxyManager {

    private var server: TgWsProxyServer? = null

    fun isRunning(): Boolean = server?.isRunning ?: false

    @Synchronized
    fun start(context: android.content.Context): Boolean {
        if (server?.isRunning == true) return true
        return runCatching {
            val prefs = context.getSharedPreferences("white_dns_tg_proxy", android.content.Context.MODE_PRIVATE)
            val secret = prefs.getString("secret", null) ?: ProxyConfig.generateSecret().also {
                prefs.edit().putString("secret", it).apply()
            }
            val cfg = ProxyConfig(
                host = "127.0.0.1",
                port = 1443,
                secret = secret,
                autoOptimizeConnection = true,
                cfProxyEnabled = true,
                cfProxyPriority = true,
            )
            val s = TgWsProxyServer(cfg)
            // запуск в фоне, ждём слушающий сокет до 3с
            val thread = Thread {
                runCatching { kotlinx.coroutines.runBlocking { s.start() } }
                    .onFailure { android.util.Log.e("TgProxy", "start failed", it) }
            }
            thread.isDaemon = true
            thread.start()
            var waited = 0
            while (!s.isRunning && waited < 3000) {
                Thread.sleep(100); waited += 100
            }
            server = s
            true
        }.getOrDefault(false)
    }

    @Synchronized
    fun stop() {
        runCatching { server?.stop() }
        server = null
    }

    @Synchronized
    fun toggle(context: android.content.Context, on: Boolean) {
        // сохранить состояние — при следующем запуске приложения восстановится
        context.getSharedPreferences("white_dns_tg_proxy", android.content.Context.MODE_PRIVATE)
            .edit().putBoolean("auto_start", on).apply()
        if (on) start(context) else stop()
    }

    /** Восстановить сохранённое состояние при запуске приложения. */
    fun restoreIfNeeded(context: android.content.Context) {
        val saved = context.getSharedPreferences("white_dns_tg_proxy", android.content.Context.MODE_PRIVATE)
            .getBoolean("auto_start", false)
        if (saved && !isRunning()) {
            start(context)
            android.util.Log.i("TgProxy", "автовосстановление из сохранённых настроек")
        }
    }

    fun proxyLink(context: android.content.Context): String {
        val prefs = context.getSharedPreferences("white_dns_tg_proxy", android.content.Context.MODE_PRIVATE)
        val secret = prefs.getString("secret", null) ?: ProxyConfig.generateSecret().also {
            prefs.edit().putString("secret", it).apply()
        }
        return "tg://proxy?server=127.0.0.1&port=1443&secret=dd$secret"
    }
}

/** Экран «Telegram-прокси»: тумблер + ссылка + кнопка «Открыть в Telegram». */
object TgProxyPageBuilder {

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
            text = "✈️  Telegram-прокси"
            textSize = 22f
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            setTextColor(tp)
        })
        content.addView(TextView(activity).apply {
            text = "Локальный MTProto-прокси: Telegram ходит через WebSocket напрямую или через Cloudflare. Работает независимо от Щита, без серверов."
            textSize = 13f; setTextColor(ts)
        }, LinearLayout.LayoutParams(-2, -2).apply { topMargin = dp(activity, 4) })

        // ===== тумблер =====
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
        val row = LinearLayout(activity).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val col = LinearLayout(activity).apply { orientation = LinearLayout.VERTICAL }
        col.addView(TextView(activity).apply {
            text = "Включить прокси"
            textSize = 15f; setTextColor(tp)
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
        })
        val statusValue = TextView(activity).apply {
            text = if (TgProxyManager.isRunning()) "Работает · 127.0.0.1:1443" else "Выключен"
            textSize = 13f; setTextColor(if (TgProxyManager.isRunning()) teal else ts)
        }
        col.addView(statusValue, LinearLayout.LayoutParams(-2, -2).apply { topMargin = dp(activity, 2) })
        val switch = MaterialSwitch(activity).apply {
            isChecked = TgProxyManager.isRunning()
            setOnCheckedChangeListener { _, checked ->
                TgProxyManager.toggle(activity, checked)
                statusValue.text = if (checked) "Работает · 127.0.0.1:1443" else "Выключен"
                statusValue.setTextColor(if (checked) teal else ts)
            }
        }
        row.addView(col, LinearLayout.LayoutParams(0, -2, 1f))
        row.addView(switch)
        card.addView(row)
        content.addView(card, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(activity, 14) })

        // ===== ссылка =====
        content.addView(TextView(activity).apply {
            text = "Ссылка для Telegram"
            textSize = 12f; setTextColor(ts)
        }, LinearLayout.LayoutParams(-2, -2).apply { topMargin = dp(activity, 16) })
        val linkCard = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(activity, 14).toFloat()
                setColor((surface.toLong() and 0xFFFFFFC8L).toInt())
            }
            setPadding(dp(activity, 14), dp(activity, 12), dp(activity, 14), dp(activity, 12))
        }
        linkCard.addView(TextView(activity).apply {
            text = TgProxyManager.proxyLink(activity)
            textSize = 12f; setTextColor(tp)
            setPadding(0, 0, 0, dp(activity, 8))
        })
        linkCard.addView(TextView(activity).apply {
            text = "tg://proxy · секрет хранится только на устройстве"
            textSize = 11f; setTextColor(ts)
        })
        content.addView(linkCard, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(activity, 6) })

        // ===== кнопка =====
        val btn = com.google.android.material.button.MaterialButton(activity).apply {
            text = "Открыть в Telegram"
            setOnClickListener {
                runCatching {
                    activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(TgProxyManager.proxyLink(activity))))
                }.onFailure {
                    Toast.makeText(activity, "Telegram не найден", Toast.LENGTH_SHORT).show()
                }
            }
        }
        content.addView(btn, LinearLayout.LayoutParams(-1, dp(activity, 52)).apply { topMargin = dp(activity, 16) })

        content.addView(TextView(activity).apply {
            text = "Если Telegram не подключается — включите Щит и повторите: прокси сам выберёт маршрут (WebSocket напрямую, Cloudflare или TCP)."
            textSize = 11f; setTextColor(ts)
        }, LinearLayout.LayoutParams(-2, -2).apply { topMargin = dp(activity, 12) })

        return root
    }

    private fun pal(activity: Activity): com.whitedns.vpn.WhiteDnsPalette =
        com.whitedns.vpn.WhiteDnsDesignTokens.forContext(activity)

    private fun dp(activity: Activity, v: Int): Int = (v * activity.resources.displayMetrics.density).toInt()
}
