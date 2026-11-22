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
    enhanced-mode: fake-ip
    fake-ip-range: 198.18.0.1/16
    fake-ip-filter:
    - '*.lan'
    - '*.local'
    - +.stun.*
    default-nameserver:
    - 77.88.8.8
    - 8.8.8.8
    nameserver:
    - 77.88.8.8
    - 77.88.8.1
    - tls://8.8.8.8:853
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

  - name: "🇫🇮 Enterprise WiFi/LTE"
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
      path: /static/js/9f3a.bundle
      headers:
        Host: enterprisechat.ru
  - name: "🇷🇺 Прямой"
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
      path: /assets/img/7b2c.js
      headers:
        Host: enterprisechat.ru
proxy-groups:
  - name: "🚀 Каналы"
    type: select
    proxies:
    - "♻️ Авто-выбор"
    - "🇷🇺 Прямой"
    - "🇫🇮 Enterprise WiFi/LTE"
  - name: "♻️ Авто-выбор"
    type: url-test
    url: "https://www.gstatic.com/generate_204"
    interval: 120
    lazy: true
    tolerance: 150
    proxies:
    - "🇷🇺 Прямой"
    - "🇫🇮 Enterprise WiFi/LTE"
  - name: "🧱 Заблокированное"
    type: select
    proxies:
    - "🇷🇺 Прямой"
    - DIRECT
rules:
  - AND,((NETWORK,udp),(DST-PORT,443)),REJECT
  - GEOSITE,category-ads-all,REJECT
  - GEOSITE,category-ru,DIRECT
  - GEOIP,private,DIRECT,no-resolve
  - GEOIP,ru,DIRECT
  - MATCH,🚀 Каналы

    """
}
