package com.whitedns.vpn

import kotlinx.coroutines.*
import java.io.InputStream
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URL
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * Спид-тест — точная методика оригинала (Cloudflare):
 * 9 волн: 100КБ(1) → 100КБ(9) → 1МБ(8) → ↑100КБ(8) → ↑1МБ(6) → 10МБ(6) → ↑10МБ(4) → 25МБ(4) → ↑25МБ(4)
 * Скорость волны = 90-й перцентиль всех сэмплов волны.
 * Учёт TTFB и Server-Timing (заголовок CF), пинг idle/под нагрузкой, джиттер, bufferbloat.
 */
class SpeedTestEngine(private val socksPort: Int?) {

    data class SpeedSample(val mbps: Double)

    data class State(
        val phase: Phase = Phase.Idle,
        val downloadMbps: Double? = null,
        val uploadMbps: Double? = null,
        val pingMs: Double? = null,
        val loadedPingMs: Double? = null,
        val jitterMs: Double? = null,
        val downSamples: List<SpeedSample> = emptyList(),
        val upSamples: List<SpeedSample> = emptyList(),
        val spentBytes: Long = 0,
        val viaTunnel: Boolean = false,
    ) {
        val bufferbloatMs: Double? get() = loadedPingMs?.let { l -> pingMs?.let { i -> (l - i).coerceAtLeast(0.0) } }
    }

    enum class Phase { Idle, Ping, Download, Upload, Done, Stopped }

    /** (upload, bytes, threads, warmup) — как в оригинале */
    private val schedule = listOf(
        Wave(false, 100_000L, 1, true),
        Wave(false, 100_000L, 9, false),
        Wave(false, 1_000_000L, 8, false),
        Wave(true, 100_000L, 8, false),
        Wave(true, 1_000_000L, 6, false),
        Wave(false, 10_000_000L, 6, false),
        Wave(true, 10_000_000L, 4, false),
        Wave(false, 25_000_000L, 4, false),
        Wave(true, 25_000_000L, 4, false),
    )

    class Wave(val upload: Boolean, val bytes: Long, val threads: Int, val warmup: Boolean)

    class HttpTiming(val bytes: Long, val ttfbNanos: Long, val wallNanos: Long, val serverMillis: Double)
    class Sample(val bps: Double, val mbps: Double)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val stopped = AtomicBoolean(false)
    var onState: ((State) -> Unit)? = null

    @Volatile private var state = State(viaTunnel = socksPort != null)
        set(v) { field = v; onState?.invoke(v) }

    private var spentBytes = 0L
    private val idlePings = mutableListOf<Double>()
    private val loadedPings = mutableListOf<Double>()

    fun proxy(): Proxy? = socksPort?.let { Proxy(Proxy.Type.SOCKS, InetSocketAddress("127.0.0.1", it)) }

    private fun push(transform: State.() -> State) { state = state.transform() }

    fun stop() {
        if (stopped.getAndSet(true)) return
        push { copy(phase = Phase.Stopped) }
    }

    fun start() {
        stopped.set(false)
        spentBytes = 0
        idlePings.clear(); loadedPings.clear()
        scope.launch {
            // ===== фаза пинга (idle) =====
            push { copy(phase = Phase.Ping, downloadMbps = null, uploadMbps = null, pingMs = null, loadedPingMs = null, jitterMs = null, downSamples = emptyList(), upSamples = emptyList(), spentBytes = 0) }
            repeat(4) {
                val p = probeLatency()
                if (p != null) {
                    idlePings.add(p)
                    push { copy(pingMs = idlePings.median(), jitterMs = idlePings.jitter()) }
                }
            }

            // ===== loaded latency loop параллельно =====
            val loadedJob = scope.launch {
                while (!stopped.get()) {
                    val p = probeLatency()
                    if (p != null) {
                        loadedPings.add(p)
                        push { copy(loadedPingMs = loadedPings.takeLast(5).median()) }
                    }
                    delay(700)
                }
            }

            // ===== волны =====
            for (wave in schedule) {
                if (stopped.get()) break
                runWave(wave)
            }
            loadedJob.cancel()

            if (!stopped.get()) push { copy(phase = Phase.Done) }
        }
    }

    private class HttpTimingResult(val ok: Boolean, val t: HttpTiming?)

    private fun open(url: String, post: Long?): Pair<java.net.HttpURLConnection, HttpTimingBuilder> {
        val conn = (URL(url).let { u -> proxy()?.let { p -> u.openConnection(p) } ?: u.openConnection() }) as java.net.HttpURLConnection
        conn.connectTimeout = 8000
        conn.readTimeout = 20000
        val b = HttpTimingBuilder()
        b.start = System.nanoTime()
        if (post != null) {
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/octet-stream")
            conn.setFixedLengthStreamingMode(post.toInt())
        }
        return conn to b
    }

    class HttpTimingBuilder { var start = 0L; var ttfb = 0L; var end = 0L; var bytes = 0L; var serverMs = 0.0 }

    private fun timing(conn: java.net.HttpURLConnection, b: HttpTimingBuilder, post: Long?): HttpTiming? {
        try {
            if (post != null) {
                val payload = ByteArray(32 * 1024)
                conn.outputStream.use { outs ->
                    var sent = 0L
                    while (sent < post && !stopped.get()) {
                        val chunk = minOf(payload.size.toLong(), post - sent).toInt()
                        outs.write(payload, 0, chunk); sent += chunk
                    }
                    outs.flush()
                }
                b.ttfb = System.nanoTime()
                conn.inputStream.use { it.read(ByteArray(256)) }
                b.end = System.nanoTime()
                b.bytes = post
            } else {
                conn.inputStream.use { ins ->
                    val buf = ByteArray(64 * 1024)
                    var n: Int
                    while (ins.read(buf).also { n = it } >= 0 && !stopped.get()) {
                        if (b.ttfb == 0L) b.ttfb = System.nanoTime()
                        b.bytes += n
                    }
                    if (b.ttfb == 0L) b.ttfb = System.nanoTime()
                }
                b.end = System.nanoTime()
            }
            // Server-Timing: cfRequestDuration;dur=123.4
            val st = conn.getHeaderField("Server-Timing")
            if (st != null) {
                val m = Regex("dur=([0-9.]+)").find(st)
                if (m != null) b.serverMs = m.groupValues[1].toDouble()
            }
            return HttpTiming(b.bytes, b.ttfb - b.start, b.end - b.start, b.serverMs)
        } catch (_: Exception) {
            return null
        }
    }

    private fun runWave(wave: Wave) {
        push { copy(phase = if (wave.upload) Phase.Upload else Phase.Download) }
        val results = mutableListOf<HttpTiming>()
        runBlocking {
            val jobs = (0 until wave.threads).map {
                launch(Dispatchers.IO) {
                    val (conn, b) = open(
                        if (wave.upload) "https://speed.cloudflare.com/__up" else "https://speed.cloudflare.com/__down?bytes=${wave.bytes}",
                        if (wave.upload) wave.bytes else null
                    )
                    val t = timing(conn, b, if (wave.upload) wave.bytes else null)
                    if (t != null) synchronized(results) { results.add(t) }
                    conn.disconnect()
                }
            }
            jobs.joinAll()
        }
        if (stopped.get()) return

        // ===== расчёт как в оригинале =====
        val samples = mutableListOf<Sample>()
        for (t in results) {
            var ttfbMs = t.ttfbNanos / 1e6
            val server = t.serverMillis
            val netTtfb = maxOf(0.0, ttfbMs - server)
            val rest = maxOf(0.0, (t.wallNanos / 1e6) - ttfbMs)
            var elapsed = if (!wave.upload) netTtfb + rest else ttfbMs
            if (elapsed <= 0) ttfbMs = Double.MAX_VALUE.also { elapsed = 1.0 }
            if (elapsed <= 0) continue
            val bytes: Double = if (wave.upload) wave.bytes * 1.005 else (if (t.bytes > 0) t.bytes.toDouble() else wave.bytes.toDouble())
            val secs = elapsed / 1000.0
            val bps = bytes * 8.0 / secs
            samples.add(Sample(bps, bps / 1e6))
            spentBytes += maxOf(t.bytes, wave.bytes)
        }
        push { copy(spentBytes = spentBytes) }
        if (samples.isEmpty()) return

        val p90 = percentile(samples.map { it.bps }, 0.9) ?: return
        val mbps = p90 / 1e6
        val newSamples = samples.map { SpeedSample(it.mbps) }
        if (wave.warmup) return
        if (wave.upload) {
            val ups = state.upSamples + newSamples
            push { copy(uploadMbps = maxOf(uploadMbps ?: 0.0, mbps), upSamples = ups) }
        } else {
            val downs = state.downSamples + newSamples
            push { copy(downloadMbps = maxOf(downloadMbps ?: 0.0, mbps), downSamples = downs) }
        }
    }

    private fun probeLatency(): Double? {
        return runCatching {
            val t0 = System.nanoTime()
            val (conn, b) = open("https://speed.cloudflare.com/__down?bytes=0", null)
            conn.inputStream.use { it.read(ByteArray(128)) }
            val dt = (System.nanoTime() - t0) / 1e6
            conn.disconnect()
            dt
        }.getOrNull()
    }

    private fun percentile(list: List<Double>, p: Double): Double? {
        if (list.isEmpty()) return null
        val sorted = list.sorted()
        val idx = ((sorted.size - 1) * p).toInt().coerceIn(0, sorted.size - 1)
        return sorted[idx]
    }

    private fun List<Double>.median(): Double = percentile(this, 0.5) ?: 0.0

    private fun List<Double>.jitter(): Double? {
        if (size < 2) return null
        val diffs = zipWithNext { a, b -> abs(b - a) }
        return diffs.average()
    }
}
