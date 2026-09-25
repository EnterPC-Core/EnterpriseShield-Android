package com.whitedns.vpn

import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyShareImportTest {
    @Test fun importsExistingPublicVlessShare() {
        val link = "vless://27e3b2d1-df96-03ea-827e-4f2c815c84fd@silentotter.download:443?encryption=none&type=ws&path=%2Fapi%2Fv1%2Flogs&host=cdn.emrata.top&security=tls&sni=api.vk.me&fp=firefox&alpn=http%2F1.1&vcn=pro-de.emrata.top#share"
        val proxies = SubConvConverter.convert(link)
        assertTrue(proxies.isNotEmpty())
        assertTrue(proxies.first().optString("type") == "vless")
    }
}
