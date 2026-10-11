package com.whitedns.vpn

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.Typeface
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.math.max
import kotlin.math.min

/**
 * Полноэкранная карта мира в стиле Cybershield: pinch-зум (1..24x), панорамирование,
 * неоновые контуры стран (тройной stroke), glow-точки с пульсацией, флажки-маркеры,
 * подписи морей, glow-дуга подключения с бегущей точкой.
 * Координаты стран: 0..65535 x 0..32767 (после *0.5), нормализуются в базовое поле.
 */
class WorldMapView(context: Context) : View(context) {

    class Node(val label: String, val iso: String?, val flag: String, val active: Boolean, val big: Boolean = false)

    var nodes: List<Node> = emptyList()
        set(value) { field = value; postInvalidateOnAnimation() }

    /** Точка юзера (по GeoIP или RU по умолчанию) — старт дуги. */
    var myPoint: Pair<Float, Float>? = null   // 0..1
        set(value) { field = value; postInvalidateOnAnimation() }
    var myFlag: String = "🇷🇺"
    var myCity: String = ""

    class SeaLabel(val x: Float, val y: Float, val text: String, val sizeSp: Float)

    var seaLabels: List<SeaLabel> = emptyList()

    private var countries: List<WorldMapAsset.Country> = emptyList()
    private var mapLoaded = false
    private var highlightIso: String? = null

    // палитра (подгоняется из билдера)
    var colorBg: Int = Color.parseColor("#070B10")
    var colorLand: Int = Color.parseColor("#14202B")
    var colorNeon: Int = Color.parseColor("#5AC8D4")
    var colorAccent: Int = Color.parseColor("#7FE3D2")
    var colorDim: Int = Color.parseColor("#6E7B88")

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val strokeW = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        pathEffect = DashPathEffect(floatArrayOf(7f, 7f), 0f)
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorDim; typeface = Typeface.create("sans-serif-light", Typeface.ITALIC)
    }
    private val flagPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = Typeface.DEFAULT }
    private val path = Path()

    // === view-состояние (зум/пан) ===
    private var scale = 1f
    private var offX = 0f
    private var offY = 0f
    private var baseW = 1f
    private var baseH = 1f

    private val scaleDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val whole = wholeWorldScale()
            val newScale = (scale * detector.scaleFactor).coerceIn(whole, whole * 24f)
            val focusX = detector.focusX
            val focusY = detector.focusY
            val k = if (scale == 0f) 1f else newScale / scale
            offX = focusX - k * (focusX - offX)
            offY = focusY - k * (focusY - offY)
            scale = newScale
            clampOffset()
            postInvalidateOnAnimation()
            return true
        }
    })
    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean = true
        override fun onScroll(e1: MotionEvent?, e2: MotionEvent, dx: Float, dy: Float): Boolean {
            offX -= dx; offY -= dy
            clampOffset()
            postInvalidateOnAnimation()
            return true
        }
    })

    private fun wholeWorldScale(): Float {
        if (width == 0 || height == 0) return 1f
        baseW = width.toFloat(); baseH = height.toFloat()
        // карта занимает весь экран при scale = wholeWorldScale
        return max(baseW / (baseW * 1f), 1f).coerceAtLeast(1f)
    }

    private fun clampOffset() {
        val maxX = 0f
        val minX = width - width * scale
        val maxY = 0f
        val minY = height - height * scale
        offX = offX.coerceIn(minX, maxX)
        offY = offY.coerceIn(minY, maxY)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)
        return true
    }

    fun resetView() {
        scale = wholeWorldScale(); offX = 0f; offY = 0f
        postInvalidateOnAnimation()
    }

    private var pulse = 0f
    private var animator: ValueAnimator? = null

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (scale <= 0f || scale == 1f) resetView()
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 2000; repeatCount = ValueAnimator.INFINITE; interpolator = LinearInterpolator()
            addUpdateListener {
                pulse = it.animatedValue as Float
                if (isAttachedToWindow) invalidate()
            }
            start()
        }
    }

    override fun onDetachedFromWindow() {
        animator?.cancel(); animator = null
        super.onDetachedFromWindow()
    }

    fun loadMap(context: Context) {
        countries = WorldMapAsset.countries(context)
        mapLoaded = countries.isNotEmpty()
        highlightIso = nodes.firstOrNull { it.active }?.iso
        postInvalidateOnAnimation()
    }

    /** 0..1 координата -> экранные пиксели с учётом зума/пана */
    private fun px(nx: Float) = (nx * width) * scale + offX
    private fun py(ny: Float) = (ny * height) * scale + offY

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(colorBg)
        if (!mapLoaded || width == 0) return
        canvas.save()
        canvas.translate(offX, offY)
        canvas.scale(scale, scale)

        val w = width.toFloat()
        val h = height.toFloat()

        // ===== полигоны стран: заливка + неоновый тройной контур =====
        fillPaint.color = colorLand
        strokeW.strokeCap = Paint.Cap.ROUND
        for (c in countries) {
            val active = c.code == highlightIso
            for (ring in c.rings) {
                path.reset()
                var i = 2
                path.moveTo(ring[0] / 65535f * w, ring[1] / 32767.5f * h)
                while (i < ring.size) {
                    path.lineTo(ring[i] / 65535f * w, ring[i + 1] / 32767.5f * h)
                    i += 2
                }
                path.close()
                fillPaint.alpha = if (active) 255 else 200
                if (active) fillPaint.color = blend(colorLand, colorNeon, 0.28f) else fillPaint.color = colorLand
                canvas.drawPath(path, fillPaint)
                // неон: широкий тусклый -> средний -> тонкий яркий
                strokeW.color = withAlpha(colorNeon, 0.12f); strokeW.strokeWidth = 9f
                canvas.drawPath(path, strokeW)
                strokeW.color = withAlpha(colorNeon, 0.45f); strokeW.strokeWidth = 3.4f
                canvas.drawPath(path, strokeW)
                strokeW.color = withAlpha(colorNeon, 0.95f); strokeW.strokeWidth = 1.4f
                canvas.drawPath(path, strokeW)
            }
        }

        // ===== подписи морей =====
        for (s in seaLabels) {
            textPaint.textSize = s.sizeSp * resources.displayMetrics.scaledDensity
            textPaint.alpha = 150
            canvas.drawText(s.text, s.x / 100f * w, s.y / 100f * h, textPaint)
        }

        // ===== точки каналов =====
        for (n in nodes) {
            val anchor = WorldMapAsset.anchorOf(n.iso, countries) ?: continue
            val cx = anchor[0] * w
            val cy = anchor[1] * h
            if (!n.big) {
                // halo + ядро
                dotPaint.color = withAlpha(colorNeon, 0.22f)
                canvas.drawCircle(cx, cy, 10f, dotPaint)
                dotPaint.color = withAlpha(colorNeon, 0.9f)
                canvas.drawCircle(cx, cy, 3.4f, dotPaint)
                // флажок при достаточном зуме
                if (scale > 2.2f) {
                    flagPaint.textSize = 13f
                    flagPaint.alpha = 220
                    canvas.drawText(n.flag, cx - 7f, cy - 16f, flagPaint)
                }
            }
        }

        // ===== активная точка (пульс) =====
        val activeNode = nodes.firstOrNull { it.active }
        if (activeNode != null) {
            val anchor = WorldMapAsset.anchorOf(activeNode.iso, countries)
            if (anchor != null) {
                val cx = anchor[0] * w
                val cy = anchor[1] * h
                val pr = 14f + 10f * pulse
                dotPaint.color = withAlpha(colorAccent, (0.35f * (1f - pulse)).coerceIn(0f, 1f))
                canvas.drawCircle(cx, cy, pr, dotPaint)
                dotPaint.color = withAlpha(colorAccent, 0.25f)
                canvas.drawCircle(cx, cy, 18f, dotPaint)
                dotPaint.color = colorAccent
                canvas.drawCircle(cx, cy, 4.5f, dotPaint)
                flagPaint.textSize = 15f; flagPaint.alpha = 255
                canvas.drawText(activeNode.flag, cx - 8f, cy - 24f, flagPaint)
            }
        }

        // ===== точка юзера =====
        var homeX: Float? = null
        var homeY: Float? = null
        myPoint?.let { (nx, ny) ->
            homeX = nx * w; homeY = ny * h
            val pr = 9f + 7f * pulse
            dotPaint.color = withAlpha(colorDim, (0.3f * (1f - pulse)).coerceIn(0f, 1f))
            canvas.drawCircle(homeX!!, homeY!!, pr, dotPaint)
            dotPaint.color = withAlpha(colorAccent, 0.85f)
            canvas.drawCircle(homeX!!, homeY!!, 3.6f, dotPaint)
            flagPaint.textSize = 15f; flagPaint.alpha = 255
            canvas.drawText(myFlag, homeX!! - 8f, homeY!! - 24f, flagPaint)
            if (myCity.isNotEmpty()) {
                textPaint.textSize = 11f * resources.displayMetrics.scaledDensity
                textPaint.alpha = 200
                canvas.drawText(myCity, homeX!! - textPaint.measureText(myCity) / 2f, homeY!! + 26f, textPaint)
            }
        }

        // ===== glow-дуга юзер -> активный сервер =====
        val target = activeNode?.let { WorldMapAsset.anchorOf(it.iso, countries) }
        if (target != null && homeX != null) {
            val x1 = homeX!!; val y1 = homeY!!
            val x2 = target[0] * w; val y2 = target[1] * h
            val mx = (x1 + x2) / 2f
            val my = (y1 + y2) / 2f - max(kotlin.math.abs(x2 - x1) * 0.24f, 30f)
            path.reset()
            path.moveTo(x1, y1)
            path.quadTo(mx, my, x2, y2)
            arcPaint.strokeWidth = 8f; arcPaint.color = withAlpha(colorAccent, 0.10f)
            canvas.drawPath(path, arcPaint)
            arcPaint.strokeWidth = 3.4f; arcPaint.color = withAlpha(colorAccent, 0.45f)
            canvas.drawPath(path, arcPaint)
            arcPaint.strokeWidth = 1.6f; arcPaint.color = withAlpha(colorAccent, 0.95f)
            canvas.drawPath(path, arcPaint)
            // бегущая точка
            val pm = PathMeasure(path, false)
            val len = pm.length
            if (len > 0) {
                val pos = FloatArray(2)
                if (pm.getPosTan(len * pulse, pos, null)) {
                    dotPaint.color = colorAccent
                    canvas.drawCircle(pos[0], pos[1], 4.2f, dotPaint)
                    dotPaint.color = withAlpha(colorAccent, 0.3f)
                    canvas.drawCircle(pos[0], pos[1], 9f, dotPaint)
                }
            }
        }
        canvas.restore()
    }

    private fun withAlpha(c: Int, a: Float): Int =
        (c and 0x00FFFFFF) or ((a.coerceIn(0f, 1f) * 255).toInt() shl 24)

    private fun blend(a: Int, b: Int, t: Float): Int {
        val ar = (a shr 16 and 0xFF); val ag = (a shr 8 and 0xFF); val ab = (a and 0xFF)
        val br = (b shr 16 and 0xFF); val bg = (b shr 8 and 0xFF); val bb = (b and 0xFF)
        val r = (ar + (br - ar) * t).toInt(); val g = (ag + (bg - ag) * t).toInt(); val bl = (ab + (bb - ab) * t).toInt()
        return (0xFF shl 24) or (r shl 16) or (g shl 8) or bl
    }
}
