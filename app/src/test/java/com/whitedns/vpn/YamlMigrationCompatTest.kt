package com.whitedns.vpn

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class YamlMigrationCompatTest {
    @Test
    fun migratesLegacyWhiteDnsGroupNames() {
        val legacy = """
            proxies:
              - name: Test1
                type: vless
            proxy-groups:
              - name: 'WhiteDNS Select'
                type: select
                proxies:
                  - 'WhiteDNS Auto'
                  - Test1
              - name: 'WhiteDNS Auto'
                type: url-test
                proxies:
                  - Test1
            rules:
              - 'MATCH,WhiteDNS Proxy'
        """.trimIndent()
        val migrated = MihomoLinkConfigBuilder.migrateGeneratedYaml(legacy)
        assertTrue("group renamed", migrated.contains("'Enterprise Proxy'"))
        assertTrue("auto renamed", migrated.contains("'Enterprise Auto'"))
        assertTrue("rule renamed", migrated.contains("MATCH,Enterprise Proxy"))
        assertTrue("no legacy left", !migrated.contains("WhiteDNS"))
    }

    @Test
    fun doesNotAppendDuplicateRulesWhenMatchExists() {
        val modern = """
            proxies:
              - name: Test1
                type: vless
            proxy-groups:
              - name: 'Enterprise Proxy'
                type: select
                proxies:
                  - 'Enterprise Auto'
                  - Test1
            rules:
              - 'MATCH,Enterprise Proxy'
        """.trimIndent()
        val migrated = MihomoLinkConfigBuilder.migrateGeneratedYaml(modern)
        assertEquals("unchanged modern yaml", modern, migrated)
    }
}
