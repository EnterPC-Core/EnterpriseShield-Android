package com.whitedns.vpn

import android.content.Context

/** WARP-каналы в рантайм: YAML-блок + вставка имён в группы. */
object WarpRuntime {

    fun appendYaml(context: Context?): String {
        if (context == null) return ""
        val configs = WarpGenerator.loadConfigs(context)
        if (configs.isEmpty()) return ""
        return buildString {
            for (c in configs) {
                append(WarpGenerator.toMihomoYaml(c))
            }
        }
    }

    /** Имена WARP-каналов — для добавления в proxy-groups. */
    fun names(context: Context?): List<String> {
        if (context == null) return emptyList()
        return WarpGenerator.loadConfigs(context).map { it.name }
    }
}
