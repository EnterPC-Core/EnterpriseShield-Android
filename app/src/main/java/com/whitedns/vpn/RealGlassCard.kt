package com.whitedns.vpn

import android.content.Context
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewOutlineProvider
import eightbitlab.com.blurview.BlurView
import eightbitlab.com.blurview.BlurTarget

/**
 * Настоящее стеклянное стекло (как в Telegram / iOS):
 * BlurView размывает РЕАЛЬНЫЙ контент под собой (RenderEffect на API 31+),
 * поверх — полупрозрачный тинт + градиентный блик сверху + светлая кромка.
 *
 * Использование:
 *   val glass = RealGlassCard(context, radiusDp = 22)
 *   glass.bind(rootContent)   // rootContent — что размывать (контейнер контента)
 *   glass.addView(внутренности)
 */
class RealGlassCard(
    context: Context,
    radiusDp: Int = EsDs.Radius.card,
    tintAlpha: Int = 120,
) : BlurView(context) {

    private val radiusPx = (radiusDp * resources.displayMetrics.density).toInt().toFloat()
    private val blurRadiusPx = 24f

    private val tintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(tintAlpha, 8, 20, 15)
    }
    private val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = LinearGradient(
            0f, 0f, 0f, radiusPx * 2f,
            intArrayOf(Color.argb(46, 255, 255, 255), Color.TRANSPARENT),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP,
        )
    }
    private val edgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = resources.displayMetrics.density
        color = Color.argb(64, 255, 255, 255)
    }

    init {
        // скругление: клип по аутлайну
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, radiusPx)
            }
        }
        clipToOutline = true
        // на старых API (до 31) BlurView сам уходит на RenderScript — тинт плотнее
        if (android.os.Build.VERSION.SDK_INT < 31) {
            tintPaint.color = Color.argb((tintAlpha + 60).coerceAtMost(210), 8, 20, 15)
        }
    }

    fun bind(target: BlurTarget) {
        setupWith(target)
            .setBlurRadius(blurRadiusPx)
            .setBlurAutoUpdate(true)
        // первый кадр после attach
        postInvalidateOnAnimation()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // тинт поверх размытия
        canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), radiusPx, radiusPx, tintPaint)
        // верхний световой блик
        canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), radiusPx, radiusPx, highlightPaint)
        // кромка
        canvas.drawRoundRect(
            edgePaint.strokeWidth / 2, edgePaint.strokeWidth / 2,
            width - edgePaint.strokeWidth / 2, height - edgePaint.strokeWidth / 2,
            radiusPx, radiusPx, edgePaint,
        )
    }
}
