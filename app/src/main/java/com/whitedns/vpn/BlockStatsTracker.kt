package com.whitedns.vpn

import android.content.Context
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.atomic.AtomicLong

/**
 * Подсчёт заблокированных рекламных/трекинговых запросов через mihomo REST API.
 * Периодически опрашивает /connections, ищет REJECT-коннекты, копит счётчики.
 */
object BlockStatsTracker {
    private const val PREFS = "white_dns_block_stats"
    private const val KEY_COUNT = "blocked_count"
    private const val KEY_BYTES = "blocked_bytes"

    val blockedCount = AtomicLong(0)
    val blockedBytes = AtomicLong(0)
    @Volatile private var lastSnapshotIds = mutableSetOf<String>()
    @Volatile private var running = false

    fun loadCumulative(context: Context) {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        blockedCount.set(p.getLong(KEY_COUNT, 0))
        blockedBytes.set(p.getLong(KEY_BYTES, 0))
    }

    fun persistCumulative(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_COUNT, blockedCount.get())
            .putLong(KEY_BYTES, blockedBytes.get())
            .apply()
    }

    fun reset(context: Context) {
        blockedCount.set(0)
        blockedBytes.set(0)
        lastSnapshotIds.clear()
        persistCumulative(context)
    }

    fun startPolling(context: Context, controlPort: Int, secret: String) {
        if (running) return
        running = true
        loadCumulative(context)
        Thread {
            while (running) {
                try {
                    poll(controlPort, secret, context)
                } catch (_: Exception) {
                }
                try {
                    Thread.sleep(30_000)
                } catch (_: InterruptedException) {
                    break
                }
            }
        }.apply { isDaemon = true; name = "block-stats" }.start()
    }

    fun stopPolling(context: Context) {
        running = false
        persistCumulative(context)
    }

    private fun poll(controlPort: Int, secret: String, context: Context) {
        val url = URL("http://127.0.0.1:$controlPort/connections")
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 3_000
        conn.readTimeout = 3_000
        conn.setRequestProperty("Authorization", "Bearer $secret")
        if (conn.responseCode != 200) {
            conn.disconnect()
            return
        }
        val body = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
        conn.disconnect()
        val root = JSONObject(body)
        val conns = root.optJSONArray("connections") ?: return
        val currentIds = mutableSetOf<String>()
        var newRejects = 0L
        var newRejectBytes = 0L
        for (i in 0 until conns.length()) {
            val c = conns.optJSONObject(i) ?: continue
            val id = c.optString("id")
            currentIds.add(id)
            val chains = c.optJSONArray("chains") ?: continue
            var isReject = false
            for (j in 0 until chains.length()) {
                if (chains.optString(j) == "REJECT") { isReject = true; break }
            }
            if (!isReject) continue
            if (id in lastSnapshotIds) continue
            newRejects++
            newRejectBytes += c.optLong("upload", 0) + c.optLong("download", 0)
        }
        lastSnapshotIds = currentIds
        if (newRejects > 0) {
            blockedCount.addAndGet(newRejects)
            blockedBytes.addAndGet(newRejectBytes)
            persistCumulative(context)
        }
    }

    fun formatBytes(bytes: Long): String {
        return when {
            bytes >= 1_048_576 -> "%.1f MB".format(bytes / 1_048_576.0)
            bytes >= 1_024 -> "%.1f KB".format(bytes / 1_024.0)
            else -> "$bytes B"
        }
    }
}
