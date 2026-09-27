package com.whitedns.vpn

import android.content.Context
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Enterprise Shield: автоматический отчёт о крахах приложения в Telegram-чат бета-тестирования.
 * Ловит необработанные исключения, пишет стектрейс в файл и отправляет на API VPS,
 * который пересылает его в чат разработчиков. Никаких персональных данных, кроме
 * модели устройства и версии Android, не собирается.
 */
object CrashReporter {

    private const val REPORT_URL = "http://89.208.103.229:8081/v1/crash"
    private const val MAX_REPORTS = 12

    @Volatile private var enabled = true

    fun install(context: Context) {
        val appCtx = context.applicationContext
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            runCatching { persist(appCtx, thread, throwable) }
            runCatching { uploadPending(appCtx) }
            previous?.uncaughtException(thread, throwable)
        }
        runCatching { uploadPending(appCtx) }
    }

    fun setEnabled(value: Boolean) {
        enabled = value
    }

    private fun persist(context: Context, thread: Thread, throwable: Throwable) {
        val stamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
        val text = buildString {
            appendLine("app=EnterpriseShield ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
            appendLine("device=${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}")
            appendLine("android=${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})")
            appendLine("abi=${android.os.Build.SUPPORTED_ABIS?.joinToString(",") ?: "?"}")
            appendLine("time=$stamp")
            appendLine("thread=${thread.name}")
            appendLine("error=${throwable.javaClass.name}: ${throwable.message}")
            appendLine("---stack---")
            appendLine(throwable.stackTraceToString())
            var cause = throwable.cause
            var depth = 0
            while (cause != null && depth < 5) {
                appendLine("---cause ${++depth}---")
                appendLine(cause.stackTraceToString())
                cause = cause.cause
            }
        }
        val dir = File(context.filesDir, "crash").apply { mkdirs() }
        File(dir, "crash-$stamp.txt").writeText(text)
        // не копим больше MAX_REPORTS файлов
        dir.listFiles()?.sortedBy { it.name }?.dropLast(MAX_REPORTS - 1)?.forEach { it.delete() }
    }

    fun uploadPending(context: Context) {
        if (!enabled) return
        val dir = File(context.filesDir, "crash")
        val files = dir.listFiles()?.filter { it.name.startsWith("crash-") } ?: return
        if (files.isEmpty()) return
        val report = files.sortedBy { it.name }.joinToString("\n\n=====\n\n") { it.readText() }
        Thread {
            runCatching {
                val conn = URL(REPORT_URL).openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.connectTimeout = 8000
                conn.readTimeout = 15000
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "text/plain; charset=utf-8")
                conn.outputStream.use { it.write(report.toByteArray(Charsets.UTF_8)) }
                val ok = conn.responseCode == 200
                conn.disconnect()
                if (ok) files.forEach { it.delete() }
            }
        }.start()
    }
}
