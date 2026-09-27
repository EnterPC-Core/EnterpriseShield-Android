package com.whitedns.vpn

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import java.util.WeakHashMap

/**
 * Enterprise Shield: адаптация UI под Android TV (D-pad).
 *
 * Правила (ревизия 1.0.54):
 *  - фокус получают ТОЛЬКО кликабельные элементы; некликабельные контейнеры (ScrollView,
 *    карточки, хосты страниц) явно нефокусируемы — системный «огромный оранжевый квадрат»
 *    на них больше не появляется, D-pad ходит строго по кнопкам;
 *  - в покое интерфейс не меняется; выделение — только на сфокусированном элементе:
 *    скруглённая бирюзовая рамка + лёгкий тинт (foreground, background не трогаем);
 *  - системный defaultFocusHighlight отключён — рисуем только свою аккуратную рамку.
 */
object TvUi {

    @Volatile private var tvMode: Boolean = false
    private val styled = WeakHashMap<View, Boolean>()

    const val FOCUS_COLOR = 0xFF7FD6C4.toInt()

    fun detect(context: Context): Boolean {
        tvMode = runCatching {
            android.os.Build.MODEL?.contains("TV") == true ||
                context.packageManager.hasSystemFeature(android.content.pm.PackageManager.FEATURE_LEANBACK)
        }.getOrDefault(false)
        return tvMode
    }

    fun isTv(): Boolean = tvMode

    fun styleTree(root: View?) {
        if (!tvMode || root == null) return
        runCatching {
            if (styled.containsKey(root)) return
            val isTabRow = root is com.google.android.material.tabs.TabLayout
            if (root.isClickable && !isTabRow) {
                styleItem(root)
            } else if (root is ViewGroup && !isTabRow) {
                // контейнер без клика не должен перехватывать фокус и рисовать системный квадрат
                root.isFocusable = false
                root.isFocusableInTouchMode = false
                if (Build.VERSION.SDK_INT >= 26) root.defaultFocusHighlightEnabled = false
            }
            styled[root] = true
            if (root is ViewGroup) {
                for (i in 0 until root.childCount) styleTree(root.getChildAt(i))
            }
        }
    }

    private fun styleItem(view: View) {
        runCatching {
            view.isFocusable = true
            view.isFocusableInTouchMode = false
            if (Build.VERSION.SDK_INT >= 26) view.defaultFocusHighlightEnabled = false
            view.setOnFocusChangeListener { v, hasFocus ->
                runCatching {
                    v.foreground = if (hasFocus) focusFrame(v.context) else null
                }
            }
        }
    }

    private fun focusFrame(context: Context): GradientDrawable = GradientDrawable().apply {
        cornerRadius = 12 * context.resources.displayMetrics.density
        setStroke((3 * context.resources.displayMetrics.density).toInt(), FOCUS_COLOR)
        setColor(0x2E7FD6C4)
    }

    /** Автодовеска: страницы строятся динамически, стилизуем новое при каждом layout. */
    fun attachAutoStyling(host: View) {
        if (!tvMode) return
        runCatching {
            host.viewTreeObserver.addOnGlobalLayoutListener {
                runCatching { styleTree(host) }
            }
        }
    }
}
