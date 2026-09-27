package com.whitedns.vpn

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import com.google.android.material.button.MaterialButton

/**
 * Enterprise Shield: централизованная адаптация UI под Android TV (D-pad).
 *
 * Единый видимый индикатор фокуса для всех кликабельных элементов:
 *  - рамка TEAL + лёгкое увеличение при фокусе;
 *  - для "плоских" вьюх (без собственного фона) — подсветка поверхности;
 *  - MaterialButton не трогаем (у него свой фон и_SHAPE, крашил приложение до 1.0.52).
 */
object TvUi {

    @Volatile private var tvMode: Boolean = false

    const val FOCUS_COLOR = 0xFF7FD6C4.toInt()
    const val FOCUS_SURFACE = 0x337FD6C4

    fun detect(context: Context): Boolean {
        tvMode = runCatching {
            android.os.Build.MODEL?.contains("TV") == true ||
                context.packageManager.hasSystemFeature(android.content.pm.PackageManager.FEATURE_LEANBACK)
        }.getOrDefault(false)
        return tvMode
    }

    fun isTv(): Boolean = tvMode

    fun style(view: View) {
        if (!tvMode) return
        runCatching {
            val vg = view as? ViewGroup
            if (vg is com.google.android.material.tabs.TabLayout) return
            if (view.isClickable && view !is MaterialButton && view.background == null) {
                view.background = GradientDrawable().apply {
                    cornerRadius = dp(view.context, 10).toFloat()
                    setColor(FOCUS_SURFACE)
                }
            }
            if (view.isClickable || vg != null) {
                view.setOnFocusChangeListener { v, hasFocus ->
                    runCatching {
                        v.animate().scaleX(if (hasFocus) 1.04f else 1f).scaleY(if (hasFocus) 1.04f else 1f)
                            .setDuration(120).start()
                        if (v.background is GradientDrawable && v !is MaterialButton) {
                            (v.background as GradientDrawable).apply {
                                if (hasFocus) {
                                    setStroke(dp(v.context, 2), FOCUS_COLOR)
                                    setColor(0x3D7FD6C4)
                                } else {
                                    setStroke(0, 0)
                                    setColor(FOCUS_SURFACE)
                                }
                            }
                        } else if (hasFocus) {
                            v.foreground = GradientDrawable().apply {
                                cornerRadius = dp(v.context, 10).toFloat()
                                setStroke(dp(v.context, 2), FOCUS_COLOR)
                            }
                        } else {
                            v.foreground = null
                        }
                    }
                }
            }
            view.isFocusable = view.isClickable || vg != null
            view.isFocusableInTouchMode = false
        }
    }

    /** Обойти дерево и стилизовать все кликабельные элементы (безопасно, идемпотентно). */
    fun styleTree(root: View) {
        if (!tvMode) return
        runCatching {
            style(root)
            if (root is ViewGroup) {
                for (i in 0 until root.childCount) styleTree(root.getChildAt(i))
            }
        }
    }

    /** Подключить авто-довеску стилей на каждый layout новой вьюхи (страницы строятся динамически). */
    fun attachAutoStyling(host: View) {
        if (!tvMode) return
        runCatching {
            host.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    runCatching { styleTree(host) }
                }
            })
        }
    }

    private fun dp(context: Context, value: Int): Int =
        (value * context.resources.displayMetrics.density).toInt()
}
