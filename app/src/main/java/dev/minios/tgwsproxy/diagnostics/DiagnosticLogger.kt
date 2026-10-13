package dev.minios.tgwsproxy.diagnostics

import android.content.Context
import android.util.Log

/**
 * Минимальная замена DiagnosticLogger из оригинала (tg-ws-proxy, MIT © crim50n):
 * пишет в logcat, без файлового хранилища и BuildConfig-зависимостей.
 */
internal fun parseDiagnosticLine(line: String): DiagnosticEntry? = null

data class DiagnosticEntry(val text: String = "")

object DiagnosticLogger {
    private const val TAG = "TgWsProxy"

    fun initialize(context: Context) {}
    fun event(name: String, vararg fields: Pair<String, Any?>) {
        Log.i(TAG, "$name ${fields.joinToString(" ") { "${it.first}=${it.second}" }}")
    }
    fun debug(name: String, vararg fields: Pair<String, Any?>) {
        Log.d(TAG, "$name ${fields.joinToString(" ") { "${it.first}=${it.second}" }}")
    }
    fun warning(name: String, vararg fields: Pair<String, Any?>) {
        Log.w(TAG, "$name ${fields.joinToString(" ") { "${it.first}=${it.second}" }}")
    }
    fun failure(name: String, error: Throwable, vararg fields: Pair<String, Any?>) {
        Log.e(TAG, "$name ${fields.joinToString(" ") { "${it.first}=${it.second}" }}", error)
    }
    fun recordCrash(thread: Thread, error: Throwable) {
        Log.e(TAG, "crash thread=${thread.name}", error)
    }
    fun clear() {}
    fun flush(timeoutMs: Long = 2_000L) {}
}
