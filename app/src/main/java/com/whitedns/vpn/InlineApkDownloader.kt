package com.whitedns.vpn

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

/** In-app APK downloader: replaces system DownloadManager (unreliable on some ROMs). */
object InlineApkDownloader {
    val active = AtomicBoolean(false)
    val failed = AtomicBoolean(false)
    val done = AtomicBoolean(false)
    val bytes = AtomicLong(0)
    val total = AtomicLong(-1)

    private val executor: ExecutorService = Executors.newSingleThreadExecutor()

    fun reset() {
        active.set(false); failed.set(false); done.set(false)
        bytes.set(0); total.set(-1)
    }

    fun start(context: Context, url: String, target: File) {
        if (active.get()) return
        reset(); active.set(true)
        target.parentFile?.mkdirs()
        val tmp = File(target.parentFile, "update.apk.part")
        executor.execute {
            var conn: HttpURLConnection? = null
            try {
                conn = URL(url).openConnection() as HttpURLConnection
                conn.connectTimeout = 15000
                conn.readTimeout = 30000
                conn.instanceFollowRedirects = true
                conn.setRequestProperty("User-Agent", "EnterpriseShield/inline")
                val code = conn.responseCode
                if (code !in 200..299) throw IOException("HTTP $code")
                total.set(conn.contentLength.toLong())
                val input = conn.inputStream
                val out = FileOutputStream(tmp)
                val buf = ByteArray(64 * 1024)
                while (true) {
                    val n = input.read(buf)
                    if (n < 0) break
                    out.write(buf, 0, n)
                    bytes.addAndGet(n.toLong())
                }
                out.flush(); out.close(); input.close()
                if (target.exists()) target.delete()
                if (!tmp.renameTo(target)) throw IOException("rename failed")
                done.set(true)
            } catch (e: Exception) {
                tmp.delete()
                failed.set(true)
            } finally {
                conn?.disconnect()
                active.set(false)
            }
        }
    }
}
