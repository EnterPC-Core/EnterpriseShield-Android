package com.whitedns.vpn

import android.content.Context
import android.os.Build
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Рут-защита: устройства с root должны запросить разрешение у администратора
 * (одобрение через @Enterprise_vpn_bot, эндпоинты v1/root).
 * Совместимо с серверной частью основного приложения Enterprise Shield.
 */
object ShieldRootPolicy {

    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    private const val PREF = "enterprise_root_policy"
    private const val KEY_ALLOW_CACHE = "root_allowed_cached"

    fun isRooted(): Boolean = testKeysPresent() || suBinaryPresent()

    private fun testKeysPresent(): Boolean = Build.TAGS?.contains("test-keys") == true

    private fun suBinaryPresent(): Boolean {
        val paths = listOf(
            "/system/bin/su", "/system/xbin/su", "/sbin/su",
            "/system/sd/xbin/su", "/system/bin/failsafe/su",
            "/data/local/xbin/su", "/data/local/bin/su", "/data/local/su",
            "/su/bin/su", "/system/app/Superuser.apk",
        )
        return paths.any { File(it).exists() }
    }

    fun interface RootCheckCallback {
        fun onResult(allowed: Boolean)
    }

    /**
     * Проверяет: устройство без root → разрешено.
     * С root → спрашивает сервер (кэш в prefs до следующего запуска).
     */
    fun checkAccess(context: Context, callback: RootCheckCallback) {
        if (!isRooted()) {
            callback.onResult(true)
            return
        }
        val prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_ALLOW_CACHE, false)) {
            callback.onResult(true)
            return
        }
        executor.execute {
            val allowed = runCatching { queryAllowed(context) }.getOrNull()
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                if (allowed == true) {
                    prefs.edit().putBoolean(KEY_ALLOW_CACHE, true).apply()
                }
                callback.onResult(allowed == true)
            }
        }
    }

    fun requestPermission(context: Context, callback: (Boolean) -> Unit) {
        executor.execute {
            val ok = runCatching { sendRequest(context) }.getOrDefault(false)
            android.os.Handler(android.os.Looper.getMainLooper()).post { callback(ok) }
        }
    }

    private fun sendRequest(context: Context): Boolean {
        val body = JSONObject()
            .put("code", ShieldDevice.code(context))
            .put("hwid", ShieldDevice.hwid(context))
            .put("model", ShieldDevice.deviceModel())
            .put("version", BuildConfig.VERSION_NAME)
            .toString()
        val conn = URL("https://api.enterprise-shield.ru/v1/root/request")
            .openConnection() as HttpURLConnection
        return try {
            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.connectTimeout = 8_000
            conn.readTimeout = 8_000
            conn.setRequestProperty("Content-Type", "application/json")
            conn.outputStream.use { it.write(body.toByteArray()) }
            conn.responseCode in 200..299
        } finally {
            conn.disconnect()
        }
    }

    private fun queryAllowed(context: Context): Boolean {
        val code = URLEncoder.encode(ShieldDevice.code(context), "UTF-8")
        val hwid = URLEncoder.encode(ShieldDevice.hwid(context), "UTF-8")
        val conn = URL("https://api.enterprise-shield.ru/v1/root/status?code=$code&hwid=$hwid")
            .openConnection() as HttpURLConnection
        return try {
            conn.connectTimeout = 8_000
            conn.readTimeout = 8_000
            if (conn.responseCode !in 200..299) throw IOException("HTTP ${conn.responseCode}")
            JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
                .optBoolean("allowed", false)
        } finally {
            conn.disconnect()
        }
    }
}
