package com.whitedns.vpn

import android.content.Context
import android.content.Context.MODE_PRIVATE
import java.net.InetSocketAddress
import java.net.Proxy

/**
 * Маршрутизация tg-ws-proxy через туннель Щита:
 * при включённом Щите все WS/DoH соединения идут через HTTP mixed-port 2080.
 */
object TgProxyRoutes {

    fun isShieldOn(context: Context): Boolean =
        context.getSharedPreferences("white_dns_runtime_state", MODE_PRIVATE)
            .getString("state", "") == "Started"

    fun httpProxy(): Proxy =
        Proxy(Proxy.Type.HTTP, InetSocketAddress("127.0.0.1", MihomoRuntimeDefaults.MIXED_PORT))

    const val TUNNEL_HOST = "127.0.0.1"
    val TUNNEL_PORT = MihomoRuntimeDefaults.MIXED_PORT
}
