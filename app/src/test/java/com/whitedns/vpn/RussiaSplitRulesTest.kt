package com.whitedns.vpn

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RussiaSplitRulesTest {
    @Test
    fun blockedDomainsGoBeforeRuDirect() {
        val sb = StringBuilder()
        sb.appendRussiaSplitRules("Enterprise Proxy") { "'$it'" }
        val rules = sb.toString().trim().lines()
        val blocked = rules.indexOfFirst { it.contains("habr.com") }
        val ruDirect = rules.indexOfFirst { it.contains("GEOSITE,CATEGORY-RU,DIRECT") }
        assertTrue("blocked rule exists", blocked >= 0)
        assertTrue("ru direct exists", ruDirect >= 0)
        assertTrue("blocked before ru direct", blocked < ruDirect)
        assertEquals("13 blocked domains", 13, rules.count { it.contains("DOMAIN-SUFFIX") })
        assertTrue(rules.any { it.contains("GEOIP,RU,DIRECT") })
        assertTrue(rules.any { it.contains("GEOSITE,APPLE,DIRECT") })
        assertTrue(rules.any { it.contains("GEOSITE,XIAOMI,DIRECT") })
    }

    @Test
    fun nullTargetSkipsProxyDomainsButKeepsDirect() {
        val sb = StringBuilder()
        sb.appendRussiaSplitRules(null) { "'$it'" }
        val out = sb.toString()
        assertTrue("no DOMAIN-SUFFIX", !out.contains("DOMAIN-SUFFIX"))
        assertTrue(out.contains("GEOSITE,CATEGORY-RU,DIRECT"))
        assertTrue(out.contains("GEOIP,RU,DIRECT"))
    }
}
