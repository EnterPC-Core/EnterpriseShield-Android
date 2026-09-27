package com.whitedns.vpn

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.provider.Settings
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Привязка устройства к Telegram-аккаунту через @Enterprise_vpn_bot.
 * Механика (совместима с основным приложением Enterprise Shield):
 *  1. Приложение показывает код устройства (32 hex).
 *  2. Пользователь отправляет код боту, бот вызывает /v1/device/link.
 *  3. Приложение проверяет статус /v1/device/status и шлёт heartbeat.
 * hwid = ANDROID_ID: привязка переживает переустановку и обновление.
 */
object ShieldDevice {
    private const val PREF = "enterprise_device_bridge"
    private const val KEY_CODE = "device_code"
    private const val BASE = "https://api.enterprise-shield.ru"
    const val BOT_URL = "https://t.me/Enterprise_vpn_bot"

    private val executor: ExecutorService = Executors.newSingleThreadExecutor()

    fun code(context: Context): String {
        val p = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        return p.getString(KEY_CODE, null) ?: UUID.randomUUID().toString().replace("-", "").also {
            p.edit().putString(KEY_CODE, it).apply()
        }
    }

    fun hwid(context: Context): String {
        val raw = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: ""
        return if (raw.length >= 12) raw else code(context).take(16)
    }

    fun deviceModel(): String = (Build.MANUFACTURER + " " + Build.MODEL).take(48)

    fun copyCode(context: Context) {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("device_code", code(context)))
    }

    data class DeviceStatus(
        val linked: Boolean,
        val status: String,
        val banned: Boolean,
        val telegram: String,
        val commandTitle: String? = null,
        val commandBody: String? = null,
        val commandUrl: String? = null,
        val appDisabled: Boolean = false,
        val forceVersion: String = ""
    ) {
        val active: Boolean get() = linked && !banned
    }

    fun queryStatus(context: Context, callback: (DeviceStatus?) -> Unit) {
        executor.execute {
            val result = runCatching { fetchStatus(context) }.getOrNull()
            android.os.Handler(android.os.Looper.getMainLooper()).post { callback(result) }
        }
    }

    private fun fetchStatus(context: Context): DeviceStatus {
        val url = URL("$BASE/v1/device/status?code=${code(context)}&hwid=${hwid(context)}")
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 10_000
        conn.readTimeout = 10_000
        try {
            if (conn.responseCode != 200) throw IOException("HTTP ${conn.responseCode}")
            val body = conn.inputStream.bufferedReader().readText()
            val json = JSONObject(body)
            return DeviceStatus(
                linked = json.optBoolean("linked", false),
                status = json.optString("status", "trial"),
                banned = json.optBoolean("banned", false),
                telegram = json.optString("telegram", ""),
            )
        } finally {
            conn.disconnect()
        }
    }

    fun heartbeat(
        context: Context,
        network: String,
        channel: String,
        callback: (DeviceStatus?) -> Unit,
    ) {
        executor.execute {
            val result = runCatching { sendHeartbeat(context, network, channel) }.getOrNull()
            android.os.Handler(android.os.Looper.getMainLooper()).post { callback(result) }
        }
    }

    fun sendHeartbeatSync(context: Context, network: String, channel: String): DeviceStatus? {
        return runCatching { sendHeartbeat(context, network, channel) }.getOrNull()
    }

    private fun sendHeartbeat(context: Context, network: String, channel: String): DeviceStatus {
        val payload = JSONObject()
            .put("code", code(context))
            .put("model", deviceModel())
            .put("version", BuildConfig.VERSION_NAME)
            .put("network", network)
            .put("channel", channel)
            .put("specs", JSONObject().put("hwid", hwid(context)))
        val conn = URL("$BASE/v1/device/heartbeat").openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.connectTimeout = 10_000
        conn.readTimeout = 10_000
        conn.doOutput = true
        conn.setRequestProperty("Content-Type", "application/json")
        try {
            conn.outputStream.use { it.write(payload.toString().toByteArray()) }
            if (conn.responseCode != 200) throw IOException("HTTP ${conn.responseCode}")
            val json = JSONObject(conn.inputStream.bufferedReader().readText())
            return DeviceStatus(
                linked = json.optBoolean("linked", false),
                status = json.optString("status", "active"),
                banned = json.optBoolean("banned", false),
                telegram = json.optString("telegram", ""),
                commandTitle = json.optJSONObject("command")?.optString("title"),
                commandBody = json.optJSONObject("command")?.optString("body"),
                commandUrl = json.optJSONObject("command")?.optString("url"),
                appDisabled = json.optBoolean("app_disabled", false),
                forceVersion = json.optString("min_app_version", ""),
            )
        } finally {
            conn.disconnect()
        }
    }
}
