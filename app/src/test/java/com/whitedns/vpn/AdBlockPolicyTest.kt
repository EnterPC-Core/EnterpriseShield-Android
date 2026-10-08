package com.whitedns.vpn

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdBlockPolicyTest {
    private val baseYaml = """
        proxies:
          - name: example
            type: http
            server: example.com
            port: 443
        rules:
          - 'MATCH,example'
    """.trimIndent()

    @Test
    fun `adblock rules injected when enabled in russia bypass mode`() {
        val yaml = MihomoRuntimeConfigBuilder.flClashRuntimeYaml(
            rawYaml = baseYaml,
            secret = "test-only",
            routingMode = RoutingMode.RussiaBypass,
            shieldGuardEnabled = true,
            adBlockEnabled = true,
        )
        assertTrue(yaml.contains("DOMAIN-SUFFIX,api.ad.xiaomi.com,REJECT"))
        assertTrue(yaml.contains("DOMAIN-SUFFIX,ad.poco.com,REJECT"))
        assertTrue(yaml.contains("DOMAIN-SUFFIX,z0.xiaomi.com,REJECT"))
        // критичные домены НЕ блокируются
        assertFalse(yaml.contains("push.xiaomi.com,REJECT"))
        assertFalse(yaml.contains("account.xiaomi.com,REJECT"))
        // adblock правила идут ПЕРВЫМИ — до сплит-правил
        val adIdx = yaml.indexOf("api.ad.xiaomi.com,REJECT")
        val splitIdx = yaml.indexOf("GEOSITE,")
        assertTrue(adIdx in 0 until splitIdx)
    }

    @Test
    fun `no adblock rules when disabled`() {
        val yaml = MihomoRuntimeConfigBuilder.flClashRuntimeYaml(
            rawYaml = baseYaml,
            secret = "test-only",
            routingMode = RoutingMode.RussiaBypass,
            shieldGuardEnabled = true,
            adBlockEnabled = false,
        )
        assertFalse(yaml.contains("api.ad.xiaomi.com,REJECT"))
    }

    @Test
    fun `domain list excludes critical xiaomi hosts`() {
        val critical = setOf(
            "push.xiaomi.com", "account.xiaomi.com", "i.mi.com", "update.miui.com",
        )
        AdBlockPolicy.DOMAINS.forEach { d ->
            critical.forEach { c -> assertFalse("$d blocks critical $c", d == c) }
        }
        assertEquals(117, AdBlockPolicy.DOMAINS.size)
    }
}
