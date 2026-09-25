package com.whitedns.vpn

import android.content.Context
import android.net.wifi.WifiManager
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

data class DiagStep(
    val index: Int,
    var title: String = "",
    var detail: String = "",
    var ok: Boolean? = null,
)

object NetworkDiagnostics {
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()

    fun interface DiagCallback {
        fun onStep(step: DiagStep)
    }

    fun run(context: Context, cb: DiagCallback) {
        executor.execute {
            // 1. Wi-Fi
            val wifi = DiagStep(0)
            wifi.title = context.getString(R.string.diag_wifi)
            try {
                val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
                val info = wm?.connectionInfo
                if (info != null && info.bssid != null) {
                    val freq = if (info.frequency >= 5000) "5 ГГц" else "2,4 ГГц"
                    wifi.detail = "${info.rssi} dBm · $freq"
                    wifi.ok = info.rssi > -85
                } else {
                    wifi.detail = context.getString(R.string.diag_cellular)
                    wifi.ok = true
                }
            } catch (e: Exception) {
                wifi.detail = "—"; wifi.ok = null
            }
            cb.onStep(wifi)

            // 2. Router (gateway TCP:53 ping)
            val router = DiagStep(1)
            router.title = context.getString(R.string.diag_router)
            try {
                val gw = gateway(context)
                if (gw == null) {
                    router.detail = "—"; router.ok = false
                } else {
                    val t0 = System.currentTimeMillis()
                    Socket().use { s ->
                        s.tcpNoDelay = true
                        s.connect(InetSocketAddress(gw, 53), 1200)
                    }
                    val ms = System.currentTimeMillis() - t0
                    router.detail = "$gw · $ms ms"
                    router.ok = ms < 800
                }
            } catch (e: Exception) {
                router.detail = context.getString(R.string.diag_fail)
                router.ok = false
            }
            cb.onStep(router)

            // 3. Line (external IP via our API)
            val line = DiagStep(2)
            line.title = context.getString(R.string.diag_line)
            try {
                val t0 = System.currentTimeMillis()
                val conn = URL("https://api.enterprise-shield.ru/v1/ip").openConnection() as HttpURLConnection
                conn.connectTimeout = 4000; conn.readTimeout = 4000
                val ip = JSONObject(conn.inputStream.bufferedReader().use { it.readText() }).optString("ip")
                val ms = System.currentTimeMillis() - t0
                line.detail = "$ip · $ms ms"
                line.ok = ip.isNotBlank()
            } catch (e: Exception) {
                line.detail = context.getString(R.string.diag_fail); line.ok = false
            }
            cb.onStep(line)

            // 4. Provider (internet reachability)
            val isp = DiagStep(3)
            isp.title = context.getString(R.string.diag_isp)
            try {
                val t0 = System.currentTimeMillis()
                val conn = URL("https://www.gstatic.com/generate_204").openConnection() as HttpURLConnection
                conn.connectTimeout = 3500; conn.readTimeout = 3500
                val code = conn.responseCode
                val ms = System.currentTimeMillis() - t0
                isp.detail = "$code · $ms ms"
                isp.ok = code == 204 || code == 200
            } catch (e: Exception) {
                isp.detail = context.getString(R.string.diag_fail); isp.ok = false
            }
            cb.onStep(isp)
        }
    }

    private fun gateway(context: Context): String? = try {
        val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        val d = wm?.dhcpInfo
        if (d != null && d.gateway != 0) {
            intToIp(d.gateway)
        } else {
            val route = java.io.File("/proc/net/route").readLines()
            route.firstOrNull { it.split("\t").size > 2 && it.split("\t")[1] == "00000000" }
                ?.let { l -> intToIp(java.lang.Long.parseLong(l.split("\t")[2], 16).toInt()) }
        }
    } catch (e: Exception) { null }

    private fun intToIp(v: Int): String =
        String.format("%d.%d.%d.%d", v and 0xFF, v shr 8 and 0xFF, v shr 16 and 0xFF, v shr 24 and 0xFF)
}
