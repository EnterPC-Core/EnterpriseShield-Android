package com.whitedns.vpn

import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject

object SupportLink {
    @Volatile var url: String = "https://t.tb.ru/pm_short/ABY6zKxIepI"
    private val executor = java.util.concurrent.Executors.newSingleThreadExecutor()

    fun refresh() {
        executor.execute {
            runCatching {
                val conn = URL("http://89.208.103.229/v1/config").openConnection() as HttpURLConnection
                conn.connectTimeout = 8000
                conn.readTimeout = 8000
                if (conn.responseCode == 200) {
                    val json = JSONObject(conn.inputStream.bufferedReader().readText())
                    val u = json.optString("support_url", "").trim()
                    if (u.startsWith("http")) url = u
                }
            }
        }
    }
}
