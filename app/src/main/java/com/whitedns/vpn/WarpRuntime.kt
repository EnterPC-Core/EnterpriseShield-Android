package com.whitedns.vpn

import android.content.Context

/** WARP-каналы в рантайм: YAML-блок (proxies + группа «☁️ WARP») и имена. */
object WarpRuntime {

    const val GROUP_NAME = "☁️ WARP"

    /** Полный YAML-блок: прокси + url-test группа, для вставки после proxies подписки. */
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

    fun names(context: Context?): List<String> {
        if (context == null) return emptyList()
        return WarpGenerator.loadConfigs(context).map { it.name }
    }

    /** Блок группы для секции proxy-groups. */
    fun groupYaml(context: Context?): String {
        val names = names(context)
        if (names.isEmpty()) return ""
        return buildString {
            appendLine("  - name: \"$GROUP_NAME\"")
            appendLine("    type: url-test")
            appendLine("    url: \"https://www.gstatic.com/generate_204\"")
            appendLine("    interval: 900")
            appendLine("    lazy: true")
            appendLine("    tolerance: 100")
            appendLine("    proxies:")
            for (n in names) appendLine("      - \"$n\"")
        }
    }

    /** Вставить имя группы в select-список «🚀 Каналы» YAML (после «♻️ Авто-выбор»). */
    fun injectIntoChannels(yaml: String, context: Context?): String {
        if (context == null) return yaml
        if (names(context).isEmpty()) return yaml
        // select-список «Каналы» содержит строку "      - "♻️ Авто-выбор"" — после неё
        val anchor = "      - \"♻️ Авто-выбор\""
        val idx = yaml.indexOf(anchor)
        if (idx < 0) return yaml
        val insertAt = idx + anchor.length
        return yaml.substring(0, insertAt) + "\n      - \"$GROUP_NAME\"" + yaml.substring(insertAt)
    }
}
