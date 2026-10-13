package com.whitedns.vpn

import android.content.Context

/**
 * WARP в рантайм: YAML-блок (wireguard-прокси + группа «☁️ WARP»).
 * Вставляется через MihomoConnectionOptionsPatcher.patch() — полный путь патчеров.
 */
object WarpRuntime {

    const val GROUP_NAME = "☁️ WARP"

    /** YAML-блок: прокси (для вставки в конец proxies перед proxy-groups). */
    fun proxiesYaml(context: Context?): String {
        if (context == null) return ""
        val configs = WarpGenerator.loadConfigs(context)
        if (configs.isEmpty()) return ""
        return buildString {
            for (c in configs) append(WarpGenerator.toMihomoYaml(c))
        }.trimEnd()
    }

    /** YAML-блок: группа (для вставки в конец proxy-groups). */
    fun groupYaml(context: Context?): String {
        if (context == null) return ""
        val configs = WarpGenerator.loadConfigs(context)
        if (configs.isEmpty()) return ""
        return buildString {
            appendLine("  - name: \"$GROUP_NAME\"")
            appendLine("    type: select")
            appendLine("    proxies:")
            for (c in configs) appendLine("      - \"${c.name}\"")
        }.trimEnd()
    }

    /** Вставить WARP-блок в rawYaml ПОСЛЕ патчеров (безопасная строковая операция). */
    fun inject(rawYaml: String, context: Context?): String {
        if (context == null) return rawYaml
        val proxies = proxiesYaml(context)
        if (proxies.isBlank()) return rawYaml
        val group = groupYaml(context)

        // 1) вставить WARP-прокси перед "proxy-groups:" (top-level, без отступа)
        val pgIdx = rawYaml.indexOf("\nproxy-groups:")
        var result = if (pgIdx >= 0) {
            rawYaml.substring(0, pgIdx) + "\n" + proxies + "\n" + rawYaml.substring(pgIdx)
        } else {
            rawYaml + "\n" + proxies
        }

        // 2) вставить WARP-группу перед "\nrules:" или в конец
        if (group.isNotBlank()) {
            val rulesIdx = result.indexOf("\nrules:")
            result = if (rulesIdx >= 0) {
                result.substring(0, rulesIdx) + "\n" + group + result.substring(rulesIdx)
            } else {
                result + "\n" + group
            }
        }

        // 3) добавить WARP в select-список «🚀 Каналы» (после «♻️ Авто-выбор»)
        val anchor = "      - \"♻️ Авто-выбор\""
        val kanIdx = result.indexOf(anchor)
        if (kanIdx >= 0) {
            val insertAt = kanIdx + anchor.length
            result = result.substring(0, insertAt) + "\n      - \"$GROUP_NAME\"" + result.substring(insertAt)
        }
        return result
    }
}
