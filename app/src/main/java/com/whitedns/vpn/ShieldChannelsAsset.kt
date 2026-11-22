package com.whitedns.vpn

internal val appContext: android.content.Context? by lazy {
    runCatching {
        Class.forName("android.app.ActivityThread")
            .getMethod("currentApplication")
            .invoke(null) as? android.content.Context
    }.getOrNull()
}

internal object ShieldChannelsAsset {
    @Volatile private var cachedFull: String? = null

    /** Полный каталог: assets/shield_channels_full.yaml, при отсутствии — зашитый офлайн-набор. */
    val yaml: String
        get() = (cachedFull ?: fromAssetOrNull()) ?: embedded

    private fun fromAssetOrNull(): String? = runCatching {
        val ctx = appContext ?: return@runCatching null
        val text = ctx.assets.open("shield_channels_full.yaml").bufferedReader().use { it.readText() }
        cachedFull = text
        text
    }.getOrNull()

    private val embedded: String = """
dns:
    enable: true
    listen: 0.0.0.0:1053
    enhanced-mode: fake-ip
    fake-ip-range: 198.18.0.1/16
    fake-ip-filter:
    - '*.lan'
    - '*.local'
    - time.*.com
    - +.market.xiaomi.com
    - +.msftconnecttest.com
    - +.msftncsi.com
    - +.stun.*
    - localhost.ptlogin2.qq.com
    - +.srv.nintendo.net
    default-nameserver:
    - 1.1.1.1
    - 8.8.8.8
    nameserver:
    - https://1.1.1.1/dns-query
    - https://dns.google/dns-query
    - tls://1.0.0.1:853
profile:
    store-selected: true
    store-fake-ip: true
experimental:
    fragment:
      packets: 1-3
      length: 100-200
      interval: 10-20
sniffer:
    enable: true
    sniff:
      HTTP:
        ports:
        - 80
      TLS:
        ports:
        - 443
        - 8443
proxies:

  - name: "🇩🇪 Экстрим"
    type: vless
    server: enterprisechat.ru
    port: 443
    uuid: e5714788-bab6-4502-9220-375d29428583
    udp: true
    tls: true
    servername: enterprisechat.ru
    client-fingerprint: chrome
    alpn:
    - http/1.1
    network: ws
    ws-opts:
      path: /es-vpn
      headers:
        Host: enterprisechat.ru
  - name: "🇩🇪 Enterprise LTE"
    type: vless
    server: enterprisechat.ru
    port: 443
    uuid: e5714788-bab6-4502-9220-375d29428583
    udp: true
    tls: true
    servername: enterprisechat.ru
    client-fingerprint: chrome
    alpn:
    - http/1.1
    network: ws
    ws-opts:
      path: /es-lte
      headers:
        Host: enterprisechat.ru
proxy-groups:
  - name: "🚀 Каналы"
    type: select
    proxies:
    - "♻️ Авто-выбор"
    - "🇩🇪 Экстрим"
    - "🇩🇪 Enterprise LTE"
  - name: "♻️ Авто-выбор"
    type: url-test
    url: "https://www.gstatic.com/generate_204"
    interval: 120
    lazy: true
    tolerance: 150
    proxies:
    - "🇩🇪 Экстрим"
    - "🇩🇪 Enterprise LTE"
  - name: "📹 YouTube"
    type: url-test
    url: https://www.youtube.com/generate_204
    interval: 300
    proxies:
    - "🇩🇪 Экстрим"
    - "🇩🇪 Enterprise LTE"
  - name: "✈️ Telegram"
    type: url-test
    url: https://web.telegram.org
    interval: 300
    proxies:
    - "🇩🇪 Экстрим"
    - "🇩🇪 Enterprise LTE"
  - name: "🧱 Заблокированное"
    type: select
    proxies:
    - "♻️ Авто-выбор"
    - DIRECT
rules:
  - AND,((NETWORK,udp),(DST-PORT,443)),REJECT
  - GEOSITE,category-ads-all,REJECT
  - GEOSITE,category-ru,DIRECT
  - GEOIP,private,DIRECT,no-resolve
  - GEOIP,ru,DIRECT
  - GEOSITE,telegram,✈️ Telegram
  - GEOSITE,youtube,📹 YouTube
  - MATCH,🚀 Каналы

    """
}
