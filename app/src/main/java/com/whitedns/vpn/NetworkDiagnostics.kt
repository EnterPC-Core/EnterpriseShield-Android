package com.whitedns.vpn

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Сетевая диагностика: цепочка до интернета
 * Wi-Fi -> Роутер -> Линия -> Провайдер
 */
object NetworkDiagnostics {
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()

    data class StepResult(
        val labelRes: Int,
        val detail: String,
        val ok: Boolean,
    )

    fun interface Callback {
        fun onDone(steps: List<StepResult>)
    }

    fun run(context: Context, callback: Callback) {
        executor.execute {
            val results = mutableListOf<StepResult>()
            results.add(stepWifi(context))
            callback.onDone(results.toList())
            results.add(stepRouter())
            callback.onDone(results.toList())
            results.add(stepLine())
            callback.onDone(results.toList())
            results.add(stepIsp())
            callback.onDone(results.toList())
        }
    }

    private fun stepWifi(context: Context): StepResult {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val net = cm?.activeNetwork
        val caps = net?.let { cm.getNetworkCapabilities(it) }
        return try {
            when {
                caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> {
                    val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
                    val rssi = wm?.connectionInfo?.rssi ?: 0
                    StepResult(
                        R.string.diag_wifi,
                        "Wi-Fi · " + rssi + " dBm",
                        rssi > -90,
                    )
                }
                caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true ->
                    StepResult(R.string.diag_wifi, context.getString(R.string.diag_cellular), true)
                caps?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true ->
                    StepResult(R.string.diag_wifi, "Ethernet", true)
                else -> StepResult(R.string.diag_wifi, context.getString(R.string.diag_no_network), false)
            }
        } catch (e: Exception) {
            StepResult(R.string.diag_wifi, e.message ?: "?", false)
        }
    }

    private fun gatewayIp(): String? = try {
        BufferedReader(InputStreamReader(Runtime.getRuntime().exec("ip route").inputStream)).use { r ->
            r.readLines().firstOrNull { it.startsWith("default") }
                ?.split(" ")?.firstOrNull { it.contains('.') }
        }
    } catch (e: Exception) {
        null
    }

    private fun stepRouter(): StepResult {
        val gw = gatewayIp() ?: return StepResult(R.string.diag_router, "—", false)
        return try {
            val t0 = System.currentTimeMillis()
            val reachable = InetAddress.getByName(gw).isReachable(2000)
            val ms = System.currentTimeMillis() - t0
            StepResult(R.string.diag_router, gw + " · " + ms + " ms", reachable)
        } catch (e: Exception) {
            StepResult(R.string.diag_router, gw, false)
        }
    }

    private fun httpGet(url: String, timeout: Int): Pair<String, Long> {
        val t0 = System.currentTimeMillis()
        val c = URL(url).openConnection() as HttpURLConnection
        c.connectTimeout = timeout
        c.readTimeout = timeout
        try {
            val body = c.inputStream.bufferedReader().use { it.readText() }
            return body to (System.currentTimeMillis() - t0)
        } finally {
            c.disconnect()
        }
    }

    private fun stepLine(): StepResult {
        return try {
            val (body, ms) = httpGet("https://api.enterprise-shield.ru/v1/ip", 6000)
            val ip = org.json.JSONObject(body).optString("ip", "?")
            StepResult(R.string.diag_line, ip + " · " + ms + " ms", ip != "?")
        } catch (e: Exception) {
            StepResult(R.string.diag_line, e.message ?: "—", false)
        }
    }

    private fun stepIsp(): StepResult {
        return try {
            val t0 = System.currentTimeMillis()
            val ok = InetAddress.getByName("8.8.8.8").isReachable(3000)
            val ms = System.currentTimeMillis() - t0
            StepResult(
                R.string.diag_isp,
                contextlessInternetLabel(ok) + " · " + ms + " ms",
                ok,
            )
        } catch (e: Exception) {
            StepResult(R.string.diag_isp, e.message ?: "—", false)
        }
    }

    private fun contextlessInternetLabel(ok: Boolean): String = if (ok) "OK" else "FAIL"
}
