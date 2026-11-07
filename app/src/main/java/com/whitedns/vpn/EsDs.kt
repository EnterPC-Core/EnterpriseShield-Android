package com.whitedns.vpn

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RenderEffect
import android.graphics.RenderNode
import android.graphics.Shader
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.math.roundToInt

/**
 * Enterprise Shield — дизайн-система.
 *
 * Единые метрики, материалы и фабрики компонентов для всех экранов.
 * Все экраны обязаны брать радиусы, отступы, цвета и материалы отсюда,
 * чтобы интерфейс оставался цельным.
 */
object EsDs {

    /** Шкала отступов (8pt grid). */
    object Space {
        const val xxs = 4
        const val xs = 8
        const val sm = 12
        const val md = 16
        const val lg = 20
        const val xl = 24
        const val xxl = 32
        const val huge = 40
    }

    /** Шкала радиусов. */
    object Radius {
        const val pill = 100
        const val card = 22
        const val cardSmall = 16
        const val sheet = 28
        const val chip = 12
        const val control = 14
    }

    /** Шкала типографики (sp). */
    object Text {
        const val display = 34f
        const val title = 22f
        const val headline = 17f
        const val body = 15f
        const val caption = 13f
        const val micro = 11f
    }

    fun dp(context: Context, value: Int): Int =
        (value * context.resources.displayMetrics.density).roundToInt()

    fun dpF(context: Context, value: Int): Float =
        value * context.resources.displayMetrics.density

    // ---------- материалы ----------

    /**
     * Стеклянная поверхность: многослойная — тонированное стекло, тонкая
     * светлая кромка сверху (иммитация отражения), тень-объём.
     */
    fun glassBackground(
        context: Context,
        palette: WhiteDnsPalette,
        radiusDp: Int = Radius.card,
        tintAlpha: Int = if (palette.isDark) 190 else 165,
        strongEdge: Boolean = true
    ): GradientDrawable = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = dpF(context, radiusDp)
        setColor(withAlpha(palette.surfaceElevated1, tintAlpha))
        setStroke(
            dp(context, 1),
            withAlpha(if (palette.isDark) palette.outline else palette.surfaceVariant, 210)
        )
        // Верхняя светлая кромка — «отражение света» на стекле.
        if (strongEdge) {
            gradientType = GradientDrawable.LINEAR_GRADIENT
            orientation = GradientDrawable.Orientation.TOP_BOTTOM
            colors = intArrayOf(
                withAlpha(Color.WHITE, if (palette.isDark) 26 else 120),
                withAlpha(Color.WHITE, 0),
                withAlpha(Color.BLACK, if (palette.isDark) 40 else 8)
            )
            // Градиент только между кромками: цвет фона оставляем ниже.
        }
    }

    /** Плоская (не стеклянная) поверхность для плотных списков. */
    fun solidBackground(
        context: Context,
        palette: WhiteDnsPalette,
        radiusDp: Int = Radius.cardSmall
    ): GradientDrawable = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = dpF(context, radiusDp)
        setColor(palette.surfaceElevated1)
        setStroke(dp(context, 1), withAlpha(palette.outline, 170))
    }

    fun withAlpha(color: Int, alpha: Int): Int =
        (color and 0x00FFFFFF) or (alpha.coerceIn(0, 255) shl 24)

    // ---------- карточки ----------

    /**
     * Карточка с настоящим liquid-glass: размывает то, что под ней
     * (RenderEffect на API 31+), плюс блик и объём.
     */
    fun card(
        context: Context,
        palette: WhiteDnsPalette,
        radiusDp: Int = Radius.card,
        blurRadiusDp: Int = 0,
        padding: Int = Space.lg
    ): LinearLayout = GlassCard(context, palette, radiusDp, blurRadiusDp).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(context, padding), dp(context, padding), dp(context, padding), dp(context, padding))
    }

    /** Заголовок секции. */
    fun sectionTitle(
        context: Context,
        palette: WhiteDnsPalette,
        text: String,
        topPaddingDp: Int = Space.xl
    ): TextView = TextView(context).apply {
        this.text = text
        textSize = Text.caption
        typeface = WhiteDnsBodyBoldTypeface
        setTextColor(palette.textTertiary)
        letterSpacing = 0.09f
        isAllCaps = true
        includeFontPadding = false
        setPadding(dp(context, Space.xxs), dp(context, topPaddingDp), 0, dp(context, Space.xs))
    }

    /** Заголовок карточки. */
    fun cardTitle(
        context: Context,
        palette: WhiteDnsPalette,
        text: String
    ): TextView = TextView(context).apply {
        this.text = text
        textSize = Text.headline
        typeface = WhiteDnsBodyBoldTypeface
        setTextColor(palette.textPrimary)
        includeFontPadding = false
        letterSpacing = -0.01f
    }

    /** Строка «название — значение» внутри карточки. */
    fun row(
        context: Context,
        palette: WhiteDnsPalette,
        label: String,
        value: String
    ): LinearLayout = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(0, dp(context, Space.xs), 0, dp(context, Space.xs))
        addView(TextView(context).apply {
            text = label
            textSize = Text.body
            typeface = WhiteDnsBodyTypeface
            setTextColor(palette.textSecondary)
            includeFontPadding = false
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        })
        addView(TextView(context).apply {
            text = value
            textSize = Text.body
            typeface = WhiteDnsBodyBoldTypeface
            setTextColor(palette.textPrimary)
            includeFontPadding = false
            gravity = Gravity.END
        })
    }

    /** Тонкий разделитель. */
    fun divider(context: Context, palette: WhiteDnsPalette): View = View(context).apply {
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(context, 1)
        ).also { it.topMargin = dp(context, Space.sm); it.bottomMargin = dp(context, Space.sm) }
        setBackgroundColor(withAlpha(palette.outline, 150))
    }
}

/**
 * Карточка с эффектом liquid glass.
 *
 * API 31+: настоящий blur того, что под карточкой (RenderEffect).
 * Ниже — многослойная имитация: тонировка + градиентная кромка + блик,
 * без дорогих эффектов, чтобы не просаживать FPS.
 */
internal class GlassCard(
    context: Context,
    private val palette: WhiteDnsPalette,
    radiusDp: Int,
    blurRadiusDp: Int
) : LinearLayout(context) {

    private val radiusPx = EsDs.dpF(context, radiusDp)
    private val blurRadiusPx = EsDs.dpF(context, blurRadiusDp.coerceAtLeast(0))

    init {
        setWillNotDraw(false)
        clipToOutline = true
        background = EsDs.glassBackground(context, palette, radiusDp, tintAlpha = if (palette.isDark) 196 else 178)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && blurRadiusPx > 0f) {
            // Настоящее размытие содержимого под карточкой.
            setRenderEffect(RenderEffect.createBlurEffect(blurRadiusPx, blurRadiusPx, Shader.TileMode.CLAMP))
        }
        // Мягкая тень-объём.
        elevation = EsDs.dpF(context, if (palette.isDark) 2 else 3)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Верхний блик: узкий световой градиент у кромки.
        val sheen = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, 0f, height * 0.42f,
                intArrayOf(
                    EsDs.withAlpha(Color.WHITE, if (palette.isDark) 20 else 96),
                    EsDs.withAlpha(Color.WHITE, 0)
                ),
                null, Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), radiusPx, radiusPx, sheen)
    }
}