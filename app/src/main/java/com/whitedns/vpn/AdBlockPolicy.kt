package com.whitedns.vpn
import android.content.Context
/**
 * Блокировка рекламных, трекинговых и мусорных доменов Xiaomi / MIUI / POCO.
 * Список НЕ затрагивает критичные домены: push.xiaomi.com, account.xiaomi.com,
 * i.mi.com, update.miui.com. Цель — автономность и чистота смартфона.
 */
object AdBlockPolicy {
    private const val PREFERENCES_NAME = "white_dns_adblock"
    private const val KEY_ENABLED = "adblock_enabled"
    private const val KEY_QUIC = "quic_block_enabled"
    private const val DEFAULT_ENABLED = true
    /** Полный список блокируемых доменов (без критичных). */
    val DOMAINS: List<String> = listOf(
        "a0.xiaomi.com",
        "ad.intl.xiaomi.com",
        "ad.music.xiaomi.com",
        "ad.poco.com",
        "ad.video.xiaomi.com",
        "ad.xlmc.sandai.net",
        "ad1.xiaomi.com",
        "ads.poco.com",
        "adv.sec.miui.com",
        "advmob.mcookie.com",
        "api.ad.intl.xiaomi.com",
        "api.ad.xiaomi.com",
        "api.analytics.xiaomi.com",
        "api.antivirus.xiaomi.com",
        "api.bracelet.xiaomi.com",
        "api.browser.miui.com",
        "api.brs.mi.com",
        "api.cleaner.miui.com",
        "api.d.xiaomi.com",
        "api.de.xiaomi.com",
        "api.device.xiaomi.com",
        "api.game.xiaomi.com",
        "api.imtt.xiaomi.com",
        "api.jr.mi.com",
        "api.market.xiaomi.com",
        "api.mi.com",
        "api.mifit.xiaomi.com",
        "api.msa.mi.com",
        "api.music.xiaomi.com",
        "api.newsfeed.xiaomi.com",
        "api.notif.xiaomi.com",
        "api.poco.com",
        "api.sec.miui.com",
        "api.themestore.xiaomi.com",
        "api.video.xiaomi.com",
        "api.wallpaper.xiaomi.com",
        "app.market.xiaomi.com",
        "app.mi.com",
        "b0.xiaomi.com",
        "bigota.d.miui.com",
        "browser.api.miui.com",
        "c0.xiaomi.com",
        "calibrate.miui.com",
        "cdn.ad.xiaomi.com",
        "cdn.miui.com",
        "cn-ad.faizshow.com",
        "collector.xiaomi.com",
        "config.mistat.xiaomi.com",
        "d0.xiaomi.com",
        "data.ad.intl.xiaomi.com",
        "data.mistat.xiaomi.com",
        "dft.imtt.xiaomi.com",
        "e0.xiaomi.com",
        "f0.xiaomi.com",
        "feed.miui.com",
        "flash.miui.com",
        "g0.xiaomi.com",
        "globalapi.ad.xiaomi.com",
        "h0.xiaomi.com",
        "hotwords.miui.com",
        "hugeota.d.miui.com",
        "i0.xiaomi.com",
        "idmna.miui.com",
        "img.ad.xiaomi.com",
        "img.cn-ad.faizshow.com",
        "j0.xiaomi.com",
        "k0.xiaomi.com",
        "l0.xiaomi.com",
        "lockscreen.miui.com",
        "log.ad.xiaomi.com",
        "m.app.mi.com",
        "m0.xiaomi.com",
        "market.mi.com",
        "metrics.xiaomi.com",
        "mifit.xiaomi.com",
        "miui.hpyrdr.com",
        "miui.trafficmanager.net",
        "msa.mi.com",
        "n0.xiaomi.com",
        "newapi.ad.xiaomi.com",
        "news-api.xiaomi.com",
        "notif.xiaomi.com",
        "o0.xiaomi.com",
        "o2o.api.xiaomi.com",
        "p0.xiaomi.com",
        "push.adv.xiaomi.com",
        "q0.xiaomi.com",
        "r0.xiaomi.com",
        "re.api.xiaomi.com",
        "rec.api.xiaomi.com",
        "rec.md.xiaomi.com",
        "resolve.msg.xiaomi.net",
        "s0.xiaomi.com",
        "sdk.appodeal.xiaomi.com",
        "sdk.game.xiaomi.com",
        "sdk.mistat.xiaomi.com",
        "sdk.msa.mi.com",
        "sdk.newsfeed.xiaomi.com",
        "sdkconfig.ad.xiaomi.com",
        "search.api.miui.com",
        "srv.miui.com",
        "srv1.miui.com",
        "srv2.miui.com",
        "static.ad.xiaomi.com",
        "stats.xiaomi.com",
        "store.a.m.i.mi.com",
        "t0.xiaomi.com",
        "trackerview.xiaomi.com",
        "tracking.miui.com",
        "trackintl.xiaomi.com",
        "u0.xiaomi.com",
        "v0.xiaomi.com",
        "w0.xiaomi.com",
        "x0.xiaomi.com",
        "xiaomiad.trafficmanager.net",
        "y0.xiaomi.com",
        "z0.xiaomi.com",
    )
    fun readEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_ENABLED, DEFAULT_ENABLED)
    fun saveEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_ENABLED, enabled).apply()
    }
    /** mihomo rule-строки вида DOMAIN-SUFFIX,<host>,REJECT. */
    const val QUIC_RULE = "AND,((NETWORK,udp),(DST-PORT,443)),REJECT"

    fun ruleLines(): List<String> = DOMAINS.map { "DOMAIN-SUFFIX,$it,REJECT" }

    fun readQuicBlockEnabled(context: android.content.Context): Boolean {
        return context.getSharedPreferences(PREFERENCES_NAME, android.content.Context.MODE_PRIVATE)
            .getBoolean(KEY_QUIC, false)
    }

    fun saveQuicBlockEnabled(context: android.content.Context, enabled: Boolean) {
        context.getSharedPreferences(PREFERENCES_NAME, android.content.Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_QUIC, enabled).apply()
    }
}
