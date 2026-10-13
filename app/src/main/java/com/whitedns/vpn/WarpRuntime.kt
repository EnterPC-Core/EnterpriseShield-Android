package com.whitedns.vpn

import android.content.Context

/** WARP изолирован от основного конфига Щита.
 *  При warp_active=true ядро получает минимальный конфиг:
 *  только wireguard-выход → весь трафик через WARP.
 *  Основные каналы Щита не участвуют — не ломают. */
object WarpRuntime {

    const val GROUP_NAME = "☁️ WARP"
    private const val PREFS = "white_dns_warp"
    private const val KEY_ACTIVE = "warp_active"

    fun isWarpActive(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_ACTIVE, false)

    fun setActive(context: Context, active: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY_ACTIVE, active).apply()
    }

    fun names(context: Context?): List<String> {
        if (context == null) return emptyList()
        return WarpGenerator.loadConfigs(context).map { it.name }
    }

    /** Изолированный WARP-конфиг (минимальный YAML для ядра):
     *  mixed-port, external-controller, 1 wireguard outbound, rules MATCH. */
    fun isolatedYaml(context: Context?): String? {
        if (context == null) return null
        val configs = WarpGenerator.loadConfigs(context)
        if (configs.isEmpty()) return null
        val c = configs.first() // первый конфиг
        return buildString {
            appendLine("mixed-port: ${MihomoRuntimeDefaults.MIXED_PORT}")
            appendLine("external-controller: 127.0.0.1:${MihomoRuntimeDefaults.FALLBACK_CONTROL_PORT}")
            appendLine("secret: \"warp\"")
            appendLine("mode: rule")
            appendLine("proxies:")
            append(WarpGenerator.toMihomoYaml(c))
            appendLine("proxy-groups:")
            appendLine("  - name: \"$GROUP_NAME\"")
            appendLine("    type: select")
            appendLine("    proxies:")
            appendLine("      - \"${c.name}\"")
            appendLine("rules:")
            appendLine("  - MATCH,$GROUP_NAME")
        }
    }
}
