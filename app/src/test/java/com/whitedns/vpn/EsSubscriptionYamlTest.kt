package com.whitedns.vpn

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class EsSubscriptionYamlTest {
    @Test
    fun parsesEnterpriseShieldYaml() {
        val yaml = javaClass.classLoader!!.getResourceAsStream("es_mihomo.yaml")!!
            .bufferedReader().readText()
        val snapshot = MihomoConfigParser.parse(yaml, fetchedAt = 1L)

        assertEquals(14, snapshot.catalog.profiles.size)
        val tags = snapshot.catalog.profiles.map { it.tag }
        assertEquals("Enterprise 3", tags[0])
        assertEquals("Enterprise 8", tags[13])

        val xhttp = snapshot.catalog.profiles.first { it.tag == "Enterprise 12" }
        assertEquals("vless", xhttp.type)
        assertEquals("ch12.enterprise-shield.ru", xhttp.server)
        assertEquals(443, xhttp.port)

        val hy2 = snapshot.catalog.profiles.first { it.tag == "Enterprise 5" }
        assertEquals("hysteria2", hy2.type)
        assertEquals("ch5.enterprise-shield.ru", hy2.server)

        assertEquals(2, snapshot.summary.groups.size)
        val target = MihomoSelectionPolicy.trafficProbeGroup(snapshot.summary)
            ?: MihomoSelectionPolicy.mainSelectorGroup(snapshot.summary)
        assertNotNull(target)
        assertEquals("🚀 Proxy Select", target?.name)
    }
}
