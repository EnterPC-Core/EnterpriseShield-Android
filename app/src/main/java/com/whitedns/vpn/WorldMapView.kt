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
 * Карта мира в стиле оригинала Cybershield:
 *  - мировые координаты 0..65535 x 0..32767.5, wholeWorldScale + центрирование (пропорции!)
 *  - страны: только неоновые контуры (тройной stroke)
 *  - подсветка заливкой: страна юзера + страна сервера
 *  - два пин-маркера: флаг-эмодзи + подпись города
 *  - крупные пульсирующие гало (26..34 dp), ядро 16 dp
 *  - glow-дуга с бегущей точкой, подписи морей
 *  - pinch-зум 1..24x, пан с ограничением
 */
class WorldMapView(context: Context) : View(context) {

    class SeaLabel(val x: Float, val y: Float, val text: String, val sizeSp: Float)

    /** Маркер: мировые координаты (0..65535 / 0..32767.5), флаг, подпись */
    class Pin(val wx: Float, val wy: Float, val flag: String, val label: String, val accent: Boolean = false)

    var myPin: Pin? = null
        set(v) { field = v; homeCountryPath = null; postInvalidateOnAnimation() }
    var serverPin: Pin? = null
        set(v) { field = v; serverCountryPath = null; postInvalidateOnAnimation() }

    var seaLabels: List<SeaLabel> = emptyList()

    var colorBg: Int = Color.parseColor("#0A0E14")
    var colorNeon: Int = Color.parseColor("#4FC3D9")
    var colorAccent: Int = Color.parseColor("#7FE8D8")
    var colorText: Int = Color.parseColor("#93A3B0")

    private var countries: List<WorldMapAsset.Country> = emptyList()
    private var mapLoaded = false
    private var worldPath: Path? = null
    private var homeCountryPath: Path? = null
    private var serverCountryPath: Path? = null

    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        pathEffect = DashPathEffect(floatArrayOf(7f, 7f), 0f)
    }
    private val flagPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = Typeface.DEFAULT }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorText; typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        textAlign = Paint.Align.CENTER
    }
    private val seaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create("sans-serif-light", Typeface.ITALIC)
        textAlign = Paint.Align.CENTER
    }
    private val density = resources.displayMetrics.density

    // === зум/пан ===
    private var scale = 1f
    private var offX = 0f
    private var offY = 0f
    private var minScale = 1f

    private fun wholeWorldScale(): Float {
        if (width <= 0 || height <= 0) return 1f
        return min(width / 65535f, height / 32767.5f)
    }

    private fun baseOffsetX(): Float = (width - 65535f * scale) / 2f
    private fun baseOffsetY(): Float = (height - 32767.5f * scale) / 2f

    private fun clampPan() {
        val marginX = width * 0.25f * scale / minScale
        val marginY = height * 0.25f * scale / minScale
        offX = offX.coerceIn(-marginX, marginX)
        offY = offY.coerceIn(-marginY, marginY)
    }

    private val scaleDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val whole = wholeWorldScale()
            val newScale = (scale * detector.scaleFactor).coerceIn(whole, whole * 24f)
            val k = newScale / scale
            // зум к точке фокуса (в экранных координатах относительно центра мира)
            val focusX = detector.focusX - baseOffsetX()
            val focusY = detector.focusY - baseOffsetY()
            offX = focusX - k * (focusX - offX)
            offY = focusY - k * (focusY - offY)
            scale = newScale
            clampPan()
            postInvalidateOnAnimation()
            return true
        }
    })
    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean = true
        override fun onScroll(e1: MotionEvent?, e2: MotionEvent, dx: Float, dy: Float): Boolean {
            offX -= dx; offY -= dy
            clampPan()
            postInvalidateOnAnimation()
            return true
        }
    })

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)
        return true
    }

    fun resetView() {
        scale = wholeWorldScale()
        minScale = scale
        offX = 0f; offY = 0f
        postInvalidateOnAnimation()
    }

    private var pulse = 0f
    private var animator: ValueAnimator? = null

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (scale <= 0f || worldPath == null) resetView()
        if (animator == null) {
            animator = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 1800; repeatCount = ValueAnimator.INFINITE; interpolator = LinearInterpolator()
                addUpdateListener {
                    pulse = it.animatedValue as Float
                    if (isAttachedToWindow) invalidate()
                }
                start()
            }
        }
    }

    override fun onDetachedFromWindow() {
        animator?.cancel(); animator = null
        super.onDetachedFromWindow()
    }

    fun loadMap(context: Context) {
        countries = WorldMapAsset.countries(context)
        mapLoaded = countries.isNotEmpty()
        // единый путь мира
        val p = Path()
        for (c in countries) for (ring in c.rings) {
            if (ring.size < 6) continue
            p.moveTo(ring[0], ring[1])
            var i = 2
            while (i < ring.size) { p.lineTo(ring[i], ring[i + 1]); i += 2 }
            p.close()
        }
        worldPath = p
        rebuildCountryPaths()
        resetView()
        postInvalidateOnAnimation()
    }

    /** путь страны, в которой стоит пин (мировые координаты) */
    private fun countryPathAt(wx: Float, wy: Float): Path? {
        for (c in countries) {
            for (ring in c.rings) {
                if (pointInRing(wx, wy, ring)) return ringToPath(ring)
            }
        }
        return null
    }

    private fun ringToPath(ring: FloatArray): Path {
        val p = Path()
        p.moveTo(ring[0], ring[1])
        var i = 2
        while (i < ring.size) { p.lineTo(ring[i], ring[i + 1]); i += 2 }
        p.close()
        return p
    }

    private fun pointInRing(x: Float, y: Float, ring: FloatArray): Boolean {
        var inside = false
        var j = ring.size - 2
        var i = 0
        while (i < ring.size) {
            val xi = ring[i]; val yi = ring[i + 1]
            val xj = ring[j]; val yj = ring[j + 1]
            if ((yi > y) != (yj > y) && x < (xj - xi) * (y - yi) / (yj - yi) + xi) inside = !inside
            j = i
            i += 2
        }
        return inside
    }

    private fun rebuildCountryPaths() {
        homeCountryPath = myPin?.let { countryPathAt(it.wx, it.wy) }
        serverCountryPath = serverPin?.let { countryPathAt(it.wx, it.wy) }
    }

    fun pinsUpdated() { rebuildCountryPaths(); postInvalidateOnAnimation() }

    fun anchorOf(code: String?): Pair<Float, Float>? {
        val a = WorldMapAsset.anchorOf(code, countries) ?: return null
        return a[0] to a[1]   // мировые координаты уже: ax / 65535 — НЕТ, anchorOf возвращает нормализованные
    }

    /** экранные координаты для мировых (0..65535) */
    private fun sx(wx: Float) = wx * scale + baseOffsetX() + offX
    private fun sy(wy: Float) = wy * scale + baseOffsetY() + offY

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (scale <= 0f || worldPath == null) resetView() else { minScale = wholeWorldScale(); clampPan() }
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(colorBg)
        val world = worldPath ?: return
        if (width == 0 || height == 0) return
        canvas.save()
        canvas.translate(baseOffsetX() + offX, baseOffsetY() + offY)
        canvas.scale(scale, scale)

        // ===== контуры мира: тройной неон (мировые px -> ширина штриха делится на scale) =====
        strokePaint.strokeCap = Paint.Cap.ROUND
        strokePaint.color = withAlpha(colorNeon, 0.10f); strokePaint.strokeWidth = 9f
        canvas.drawPath(world, strokePaint)
        strokePaint.color = withAlpha(colorNeon, 0.40f); strokePaint.strokeWidth = 3.2f
        canvas.drawPath(world, strokePaint)
        strokePaint.color = withAlpha(colorNeon, 0.90f); strokePaint.strokeWidth = 1.3f
        canvas.drawPath(world, strokePaint)

        // ===== подсветка страны юзера и страны сервера =====
        for (hl in listOfNotNull(homeCountryPath, serverCountryPath)) {
            fillPaint.color = withAlpha(colorAccent, 0.16f)
            canvas.drawPath(hl, fillPaint)
            strokePaint.color = withAlpha(colorAccent, 0.30f); strokePaint.strokeWidth = 6f
            canvas.drawPath(hl, strokePaint)
            strokePaint.color = withAlpha(colorAccent, 0.85f); strokePaint.strokeWidth = 1.6f
            canvas.drawPath(hl, strokePaint)
        }

        // ===== подписи морей =====
        for (s in seaLabels) {
            seaPaint.textSize = s.sizeSp * resources.displayMetrics.scaledDensity
            seaPaint.alpha = 140
            canvas.drawText(s.text, s.x / 100f * 65535f, s.y / 100f * 32767.5f, seaPaint)
        }

        canvas.restore()

        // ===== маркеры, дуга — в экранных координатах (текст не масштабируем зумом) =====
        val my = myPin
        val sv = serverPin

        // дуга юзер -> сервер
        if (my != null && sv != null) {
            val x1 = sx(my.wx); val y1 = sy(my.wy)
            val x2 = sx(sv.wx); val y2 = sy(sv.wy)
            val mx = (x1 + x2) / 2f
            val myy = (y1 + y2) / 2f - max(kotlin.math.abs(x2 - x1) * 0.22f, 24f * density)
            val p = Path()
            p.moveTo(x1, y1); p.quadTo(mx, myy, x2, y2)
            arcPaint.strokeWidth = 7f; arcPaint.color = withAlpha(colorAccent, 0.10f)
            canvas.drawPath(p, arcPaint)
            arcPaint.strokeWidth = 3.2f; arcPaint.color = withAlpha(colorAccent, 0.45f)
            canvas.drawPath(p, arcPaint)
            arcPaint.strokeWidth = 1.5f; arcPaint.color = withAlpha(colorAccent, 0.95f)
            canvas.drawPath(p, arcPaint)
            val pm = PathMeasure(p, false)
            if (pm.length > 0) {
                val pos = FloatArray(2)
                if (pm.getPosTan(pm.length * pulse, pos, null)) {
                    dotPaint.color = withAlpha(colorAccent, 0.30f)
                    canvas.drawCircle(pos[0], pos[1], 8f * density, dotPaint)
                    dotPaint.color = colorAccent
                    canvas.drawCircle(pos[0], pos[1], 3.4f * density, dotPaint)
                }
            }
        }

        // ===== пин: гало 16dp + пульс до 34dp + ядро + флаг + подпись =====
        fun drawPin(pin: Pin, accent: Boolean) {
            val cx = sx(pin.wx); val cy = sy(pin.wy)
            val c = if (accent) colorAccent else colorNeon
            val pr = (26f + 8f * pulse) * density
            dotPaint.color = withAlpha(c, 0.10f + 0.08f * pulse)
            canvas.drawCircle(cx, cy, pr, dotPaint)
            dotPaint.color = withAlpha(c, 0.22f)
            canvas.drawCircle(cx, cy, 16f * density, dotPaint)
            dotPaint.color = c
            canvas.drawCircle(cx, cy, 4.6f * density, dotPaint)
            // флаг над точкой
            flagPaint.textSize = 17f * density
            flagPaint.alpha = 255
            canvas.drawText(pin.flag, cx - flagPaint.measureText(pin.flag) / 2f, cy - 22f * density, flagPaint)
            // подпись под точкой
            if (pin.label.isNotEmpty()) {
                labelPaint.textSize = 11.5f * resources.displayMetrics.scaledDensity
                labelPaint.color = withAlpha(colorText, 0.95f)
                canvas.drawText(pin.label, cx, cy + 30f * density, labelPaint)
            }
        }
        my?.let { drawPin(it, accent = false) }
        sv?.let { drawPin(it, accent = true) }
    }

    private fun withAlpha(c: Int, a: Float): Int =
        (c and 0x00FFFFFF) or ((a.coerceIn(0f, 1f) * 255).toInt() shl 24)
}
