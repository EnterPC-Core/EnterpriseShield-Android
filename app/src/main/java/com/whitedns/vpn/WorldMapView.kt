package com.whitedns.vpn

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Отрисовка карты мира с точками каналов Щита и дугой активного подключения.
 * Стиль: тёмный фон, полигоны стран полупрозрачным бирюзовым, активная страна подсвечена,
 * дуга подключения анимирована (бегущая точка).
 */
class WorldMapView(context: Context) : View(context) {

    class Node(val label: String, val iso: String?, val active: Boolean)

    var nodes: List<Node> = emptyList()
        set(value) { field = value; highlightIso = value.firstOrNull { it.active }?.iso; postInvalidateOnAnimation() }

    private var highlightIso: String? = null
    private var countries: List<WorldMapAsset.Country> = emptyList()
    private var mapLoaded = false

    // палитра под стиль Щита (SURFACE/BG/TEAL — финальные цвета придёт из MainActivity)
    var colorBg: Int = Color.parseColor("#0B1218")
    var colorLand: Int = Color.parseColor("#1C3238")
    var colorLandActive: Int = Color.parseColor("#2E6570")
    var colorStroke: Int = Color.parseColor("#3D7A87")
    var colorDot: Int = Color.parseColor("#7FD1DB")
    var colorDotActive: Int = Color.parseColor("#B7F0E3")
    var colorArc: Int = Color.parseColor("#5FB8C4")

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeWidth = 1f; color = colorStroke
    }
    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeWidth = 2.5f; pathEffect = DashPathEffect(floatArrayOf(6f, 6f), 0f)
    }
    private val path = Path()
    private var arcPhase = 0f
    private var animator: ValueAnimator? = null

    fun loadMap(context: Context) {
        countries = WorldMapAsset.countries(context)
        mapLoaded = countries.isNotEmpty()
        postInvalidateOnAnimation()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 2200; repeatCount = ValueAnimator.INFINITE; interpolator = LinearInterpolator()
            addUpdateListener {
                arcPhase = it.animatedValue as Float
                if (isAttachedToWindow && (highlightIso != null)) invalidate()
            }
            start()
        }
    }

    override fun onDetachedFromWindow() {
        animator?.cancel(); animator = null
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(colorBg)
        if (!mapLoaded) return
        val w = width.toFloat(); val h = height.toFloat()
        // поле по краям
        val pad = w * 0.03f
        val mw = w - pad * 2; val mh = h - pad * 2
        fun tx(x: Float) = pad + x * mw
        fun ty(y: Float) = pad + y * mh

        // полигоны
        for (c in countries) {
            val active = c.code == highlightIso
            fillPaint.color = if (active) colorLandActive else colorLand
            for (ring in c.rings) {
                path.reset()
                var i = 0
                path.moveTo(tx(ring[0] / 65535f), ty(ring[1] / 32767.5f))
                i = 2
                while (i < ring.size) {
                    path.lineTo(tx(ring[i] / 65535f), ty(ring[i + 1] / 32767.5f))
                    i += 2
                }
                path.close()
                canvas.drawPath(path, fillPaint)
                if (active) canvas.drawPath(path, strokePaint)
            }
        }

        // домашняя точка (RU — центр России) — старт дуги
        val homeIso = "RU"
        val homeAnchor = WorldMapAsset.anchorOf(homeIso, countries)

        // точки каналов
        val anchors = ArrayList<Pair<WorldMapAsset.Country, Boolean>>()
        for (n in nodes) {
            val a = WorldMapAsset.anchorOf(n.iso, countries) ?: continue
            val cx = tx(a[0]); val cy = ty(a[1])
            val act = n.active
            dotPaint.color = if (act) colorDotActive else colorDot
            canvas.drawCircle(cx, cy, if (act) 9f else 4.5f, dotPaint)
            if (act) {
                dotPaint.alpha = 60
                canvas.drawCircle(cx, cy, 16f, dotPaint)
                dotPaint.alpha = 255
                for (c in countries) if (c.code == n.iso) { anchors.add(c to true); break }
            }
        }

        // дуга активного подключения
        val target = anchors.firstOrNull()
        if (target != null && homeAnchor != null) {
            val x1 = tx(homeAnchor[0]); val y1 = ty(homeAnchor[1])
            val x2 = tx(target.first.anchorX / 65535f); val y2 = ty(target.first.anchorY / 32767.5f)
            val mx = (x1 + x2) / 2f
            val my = (y1 + y2) / 2f - kotlin.math.abs(x2 - x1) * 0.22f - 40f
            path.reset()
            path.moveTo(x1, y1)
            path.quadTo(mx, my, x2, y2)
            arcPaint.color = colorArc
            canvas.drawPath(path, arcPaint)
            // бегущая точка по дуге
            val pm = PathMeasure(path, false)
            val len = pm.length
            val d = len * arcPhase
            val pos = FloatArray(2)
            if (pm.getPosTan(d, pos, null)) {
                dotPaint.color = colorDotActive
                canvas.drawCircle(pos[0], pos[1], 5f, dotPaint)
            }
            _lastArcEnd = floatArrayOf(x2, y2)
        }
    }

    private var _lastArcEnd: FloatArray? = null
}
