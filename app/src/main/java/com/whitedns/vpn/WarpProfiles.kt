package com.whitedns.vpn

import android.content.Context

/**
 * WARP-профили в списке серверов: конвертирует WarpConfig в ConnectionProfile
 * для отображения в списке каналов (connectionProfiles).
 */
object WarpProfiles {

    /** Получить WARP-конфиги как список ConnectionProfile. */
    fun connectionProfiles(context: Context): List<ConnectionProfile> {
        val configs = WarpGenerator.loadConfigs(context)
        if (configs.isEmpty()) return emptyList()
        return configs.map { c ->
            ConnectionProfile(
                tag = c.name,
                type = "wireguard",
                server = c.server,
                port = c.port,
                transport = "wireguard",
                validationHost = c.server,
                outboundJson = null,
            )
        }
    }

    /** Добавить WARP-профили к существующему списку (в конец, отдельная секция). */
    fun appendTo(profiles: List<ConnectionProfile>, context: Context): List<ConnectionProfile> {
        val warp = connectionProfiles(context)
        if (warp.isEmpty()) return profiles
        return profiles + warp
    }
}
