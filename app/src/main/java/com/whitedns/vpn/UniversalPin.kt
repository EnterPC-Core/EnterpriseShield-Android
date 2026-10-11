package com.whitedns.vpn

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.atomic.AtomicBoolean

/**
 * «🌐 Универсал» — заморозка выбора:
 * 1) при включении Щита ядро (url-test по Instagram) само тестирует весь пул и выбирает лучшего;
 * 2) через 45 секунд мы читаем победителя и ФИКСИРУЕМ его в «🚀 Каналы» напрямую;
 * 3) дальше сервер сам НЕ меняется — стабильно, пока юзер сам не сменит локацию/канал
 *    или не переподключит Щит.
 */
object UniversalPin {

    private const val GROUP_AUTO = "♻️ Авто-выбор"
    private const val GROUP_MAIN = "🚀 Каналы"
    private val pinning = AtomicBoolean(false)

    fun pinBestOnce(context: Context, controlPort: Int, secret: String) {
        if (!UniversalPinPolicy.readEnabled(context)) return   // тумблер выключен — не трогаем
        if (!pinning.compareAndSet(false, true)) return
        GlobalScope.launch(Dispatchers.IO) {
            try {
                delay(45_000)   // дать url-test ядра прогреть пул и выбрать лучшего
                val endpoint = CoreEndpoint.resolve(context) ?: return@launch
                val port = endpoint.first
                val sec = endpoint.second
                val base = "http://127.0.0.1:$port"
                fun api(path: String, method: String = "GET", body: String? = null): JSONObject? =
                    CoreEndpoint.api(port, sec, path)

                val auto = api("/proxies/" + java.net.URLEncoder.encode(GROUP_AUTO, "UTF-8")) ?: return@launch
                val winner = auto.optString("now").takeIf { it.isNotBlank() } ?: return@launch
                val hist = auto.optJSONArray("history")
                var delayMs = 0L
                if (hist != null && hist.length() > 0) {
                    delayMs = hist.optJSONObject(hist.length() - 1).optLong("delay", 0)
                }
                if (delayMs <= 0) return@launch   // победитель мёртв — не фиксируем, ядро продолжит искать

                // зафиксировать победителя в основной группе
                val code = if (CoreEndpoint.put(port, sec,
                        "/proxies/" + java.net.URLEncoder.encode(GROUP_MAIN, "UTF-8"),
                        """{"name":"$winner"}""")) 200 else 500
                if (code in 200..299) {
                    context.getSharedPreferences("white_dns_smart_selector", Context.MODE_PRIVATE).edit()
                        .putString("last_server", winner)
                        .putInt("last_passed", 8)
                        .putInt("last_total", 8)
                        .putLong("last_at", System.currentTimeMillis())
                        .apply()
                }
            } catch (_: Exception) {
            } finally {
                pinning.set(false)
            }
        }
    }
}
