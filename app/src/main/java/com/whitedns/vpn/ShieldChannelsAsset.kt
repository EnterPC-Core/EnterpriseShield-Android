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

    private val embedded: String = """# Enterprise Shield - All servers (offline)
proxies:
  - name: Enterprise 12
    type: vless
    server: ch12.enterprise-shield.ru
    port: 443
    uuid: 28857cfc-5701-4952-8330-d2640618f184
    udp: true
    tls: true
    servername: cdn.arcadia-cloud.24haze.online
    client-fingerprint: qq
    alpn:
    - h2
    - http/1.1
    network: xhttp
    xhttp-opts:
      path: /api/v1/sync.php
      mode: packet-up
      host: cdn.arcadia-cloud.24haze.online
      reuse-settings:
        c-max-reuse-times: 128-256
        max-connections: 2-4
        h-max-request-times: 600-800
        h-max-reusable-secs: 1800-2400
        h-keep-alive-period: 0
      x-padding-bytes: 100-128
      x-padding-obfs-mode: true
      x-padding-key: _dc
      x-padding-placement: query
      x-padding-method: tokenish
      uplink-http-method: GET
      session-placement: query
      session-key: token
      seq-placement: query
      seq-key: offset
      sc-max-each-post-bytes: 524288-1048576
      sc-min-posts-interval-ms: 50-100
  - name: Enterprise 8
    type: vless
    server: ch8.enterprise-shield.ru
    port: 443
    uuid: 28857cfc-5701-4952-8330-d2640618f184
    udp: true
    tls: true
    flow: xtls-rprx-vision
    servername: ch8.enterprise-shield.ru
    client-fingerprint: qq
    network: tcp
    reality-opts:
      public-key: FzJ88k4bLz-Omx6s-0AKqj1YxEvETzk5gxqz16N6iT0
  - name: Enterprise 13
    type: vless
    server: ch13.enterprise-shield.ru
    port: 443
    uuid: 28857cfc-5701-4952-8330-d2640618f184
    udp: true
    tls: true
    servername: cdn.safe-corp.24haze.online
    client-fingerprint: chrome
    alpn:
    - h2
    - http/1.1
    network: xhttp
    xhttp-opts:
      path: /api/v1/sync.php
      mode: packet-up
      host: cdn.safe-corp.24haze.online
      reuse-settings:
        c-max-reuse-times: 128-256
        max-connections: 2-4
        h-max-request-times: 600-800
        h-max-reusable-secs: 1800-2400
        h-keep-alive-period: 0
      x-padding-bytes: 100-128
      x-padding-obfs-mode: true
      x-padding-key: _dc
      x-padding-placement: query
      x-padding-method: tokenish
      uplink-http-method: GET
      session-placement: query
      session-key: token
      seq-placement: query
      seq-key: offset
      sc-max-each-post-bytes: 524288-1048576
      sc-min-posts-interval-ms: 50-100
  - name: Enterprise 7
    type: vless
    server: ch7.enterprise-shield.ru
    port: 443
    uuid: 28857cfc-5701-4952-8330-d2640618f184
    udp: true
    tls: true
    servername: ch7.enterprise-shield.ru
    client-fingerprint: chrome
    network: grpc
    grpc-opts:
      grpc-service-name: grpc
    reality-opts:
      public-key: 2tesN19MYsNBCztMZTIar3PLC7C5zB8QgEwFNPw3Rlo
  - name: Enterprise 9
    type: vless
    server: ch9.enterprise-shield.ru
    port: 443
    uuid: 28857cfc-5701-4952-8330-d2640618f184
    udp: true
    tls: true
    flow: xtls-rprx-vision
    servername: ch9.enterprise-shield.ru
    client-fingerprint: firefox
    network: tcp
    reality-opts:
      public-key: XbvqMWDH402it1ceTFlXm1Ptc72YPv6Q3I-eDRmg51Y
  - name: Enterprise 11
    type: vless
    server: ch11.enterprise-shield.ru
    port: 443
    uuid: 28857cfc-5701-4952-8330-d2640618f184
    udp: true
    tls: true
    servername: cdn.jet-offer.24haze.online
    client-fingerprint: qq
    alpn:
    - h2
    - http/1.1
    network: xhttp
    xhttp-opts:
      path: /api/v1/sync.php
      mode: packet-up
      host: cdn.jet-offer.24haze.online
      reuse-settings:
        c-max-reuse-times: 128-256
        max-connections: 2-4
        h-max-request-times: 600-800
        h-max-reusable-secs: 1800-2400
        h-keep-alive-period: 0
      x-padding-bytes: 100-128
      x-padding-obfs-mode: true
      x-padding-key: _dc
      x-padding-placement: query
      x-padding-method: tokenish
      uplink-http-method: GET
      session-placement: query
      session-key: token
      seq-placement: query
      seq-key: offset
      sc-max-each-post-bytes: 524288-1048576
      sc-min-posts-interval-ms: 50-100
  - name: Enterprise 2
    type: vless
    server: ch2.enterprise-shield.ru
    port: 443
    uuid: 28857cfc-5701-4952-8330-d2640618f184
    udp: true
    tls: true
    servername: ch2.enterprise-shield.ru
    client-fingerprint: chrome
    network: grpc
    grpc-opts:
      grpc-service-name: grpc
    reality-opts:
      public-key: 2tesN19MYsNBCztMZTIar3PLC7C5zB8QgEwFNPw3Rlo
  - name: Enterprise 3
    type: vless
    server: ch3.enterprise-shield.ru
    port: 443
    uuid: 28857cfc-5701-4952-8330-d2640618f184
    udp: true
    tls: true
    flow: xtls-rprx-vision
    servername: ch3.enterprise-shield.ru
    client-fingerprint: qq
    network: tcp
    reality-opts:
      public-key: 5klL9RHUDth_uDdaAYVQgyYmboqrd4upaL7HHhgmT3I
      short-id: b04dd3564373ba03
  - name: Enterprise 14
    type: vless
    server: ch14.enterprise-shield.ru
    port: 443
    uuid: 28857cfc-5701-4952-8330-d2640618f184
    udp: true
    tls: true
    servername: cdn.cause-models.24haze.online
    client-fingerprint: chrome
    alpn:
    - h2
    - http/1.1
    network: xhttp
    xhttp-opts:
      path: /api/v1/sync.php
      mode: packet-up
      host: cdn.cause-models.24haze.online
      reuse-settings:
        c-max-reuse-times: 128-256
        max-connections: 2-4
        h-max-request-times: 600-800
        h-max-reusable-secs: 1800-2400
        h-keep-alive-period: 0
      x-padding-bytes: 100-128
      x-padding-obfs-mode: true
      x-padding-key: _dc
      x-padding-placement: query
      x-padding-method: tokenish
      uplink-http-method: GET
      session-placement: query
      session-key: token
      seq-placement: query
      seq-key: offset
      sc-max-each-post-bytes: 524288-1048576
      sc-min-posts-interval-ms: 50-100
  - name: Enterprise 10
    type: vless
    server: ch10.enterprise-shield.ru
    port: 443
    uuid: 28857cfc-5701-4952-8330-d2640618f184
    udp: true
    tls: true
    servername: cdn.metrics.24haze.online
    client-fingerprint: chrome
    alpn:
    - h2
    - http/1.1
    network: xhttp
    xhttp-opts:
      path: /api/v1/sync.php
      mode: packet-up
      host: cdn.metrics.24haze.online
      reuse-settings:
        c-max-reuse-times: 128-256
        max-connections: 2-4
        h-max-request-times: 600-800
        h-max-reusable-secs: 1800-2400
        h-keep-alive-period: 0
      x-padding-bytes: 100-128
      x-padding-obfs-mode: true
      x-padding-key: _dc
      x-padding-placement: query
      x-padding-method: tokenish
      uplink-http-method: GET
      session-placement: query
      session-key: token
      seq-placement: query
      seq-key: offset
      sc-max-each-post-bytes: 524288-1048576
      sc-min-posts-interval-ms: 50-100
  - name: Enterprise Extra 1
    type: vless
    server: s11.cpii.ru
    port: 1443
    uuid: 9576180b-1c0b-4832-bdef-69fd8e03ddc5
    udp: true
    tls: true
    servername: fonts.gstatic.com
    client-fingerprint: chrome
    network: tcp
    reality-opts:
      public-key: w8GkraWIIMmDE8mFQEbMtM7GajfNPRb2htJxBV8zgE8
      short-id: b0c357d9785679c0
  - name: Enterprise Extra 2
    type: vless
    server: s11.cpii.ru
    port: 1443
    uuid: 9576180b-1c0b-4832-bdef-69fd8e03ddc5
    udp: true
    tls: true
    servername: vk.com
    client-fingerprint: chrome
    network: tcp
    reality-opts:
      public-key: w8GkraWIIMmDE8mFQEbMtM7GajfNPRb2htJxBV8zgE8
      short-id: b0c357d9785679c0
  - name: Enterprise Extra 3
    type: vless
    server: s11.cpii.ru
    port: 2443
    uuid: 9576180b-1c0b-4832-bdef-69fd8e03ddc5
    udp: true
    tls: true
    servername: max.ru
    client-fingerprint: chrome
    network: ws
    reality-opts:
      public-key: w8GkraWIIMmDE8mFQEbMtM7GajfNPRb2htJxBV8zgE8
      short-id: a1b2c3d4e5f67890
  - name: Enterprise Extra 4
    type: vless
    server: s11.cpii.ru
    port: 2443
    uuid: 9576180b-1c0b-4832-bdef-69fd8e03ddc5
    udp: true
    tls: true
    servername: fonts.gstatic.com
    client-fingerprint: chrome
    network: ws
    reality-opts:
      public-key: w8GkraWIIMmDE8mFQEbMtM7GajfNPRb2htJxBV8zgE8
      short-id: a1b2c3d4e5f67890
  - name: Enterprise Extra 5
    type: vless
    server: s11.cpii.ru
    port: 9443
    uuid: 9576180b-1c0b-4832-bdef-69fd8e03ddc5
    udp: true
    tls: true
    servername: eh.vk.com
    client-fingerprint: chrome
    network: grpc
    grpc-opts:
      grpc-service-name: grpc
    reality-opts:
      public-key: w8GkraWIIMmDE8mFQEbMtM7GajfNPRb2htJxBV8zgE8
      short-id: 0987654321fedcba
  - name: Enterprise Extra 6
    type: vless
    server: s11.cpii.ru
    port: 9443
    uuid: 9576180b-1c0b-4832-bdef-69fd8e03ddc5
    udp: true
    tls: true
    servername: api-maps.yandex.ru
    client-fingerprint: chrome
    network: grpc
    grpc-opts:
      grpc-service-name: grpc
    reality-opts:
      public-key: w8GkraWIIMmDE8mFQEbMtM7GajfNPRb2htJxBV8zgE8
      short-id: 0987654321fedcba
  - name: Enterprise Extra 11
    type: vless
    server: 217.19.122.65
    port: 443
    uuid: bc5ec86c-3e65-4272-994c-59a924c72a68
    udp: true
    tls: true
    servername: fbsv6.guardora.pro
    client-fingerprint: safari
    network: ws
  - name: Enterprise Extra 12
    type: vless
    server: 45.12.75.242
    port: 26424
    uuid: 633f424e-2b11-48da-a6b3-a849dd71456f
    udp: true
    tls: true
    servername: ya.ru
    client-fingerprint: chrome
    network: tcp
    reality-opts:
      public-key: l8AubqcxQO-HRFJy4pZL1vbLOo-eXit69s-XulSELE0
      short-id: 23103e1e
  - name: Enterprise Extra 13
    type: vless
    server: 45.12.75.242
    port: 26424
    uuid: 633f424e-2b11-48da-a6b3-a849dd71456f
    udp: true
    tls: true
    servername: ya.ru
    client-fingerprint: qq
    network: tcp
    reality-opts:
      public-key: l8AubqcxQO-HRFJy4pZL1vbLOo-eXit69s-XulSELE0
      short-id: 23103e1e
  - name: Enterprise Extra 14
    type: vless
    server: 45.12.75.242
    port: 26424
    uuid: 633f424e-2b11-48da-a6b3-a849dd71456f
    udp: true
    tls: true
    servername: ya.ru
    network: tcp
    reality-opts:
      public-key: l8AubqcxQO-HRFJy4pZL1vbLOo-eXit69s-XulSELE0
      short-id: 23103e1e
  - name: Enterprise Extra 15
    type: vless
    server: 45.12.75.242
    port: 26424
    uuid: 633f424e-2b11-48da-a6b3-a849dd71456f
    udp: true
    tls: true
    servername: ya.ru
    client-fingerprint: firefox
    network: tcp
    reality-opts:
      public-key: l8AubqcxQO-HRFJy4pZL1vbLOo-eXit69s-XulSELE0
      short-id: 23103e1e
  - name: Enterprise Extra 16
    type: vless
    server: 185.141.227.180
    port: 2053
    uuid: 2c634977-584e-4e1f-b501-1b70c77a6743
    udp: true
    tls: true
    servername: dismissaltree.net
    client-fingerprint: chrome
    network: ws
  - name: Enterprise Extra 17
    type: vless
    server: 185.141.227.180
    port: 2053
    uuid: 2c634977-584e-4e1f-b501-1b70c77a6743
    udp: true
    tls: true
    servername: dismissaltree.net
    client-fingerprint: firefox
    network: ws
  - name: Enterprise Extra 18
    type: vless
    server: 185.141.227.180
    port: 2053
    uuid: 2c634977-584e-4e1f-b501-1b70c77a6743
    udp: true
    tls: true
    servername: dismissaltree.net
    client-fingerprint: random
    network: ws
  - name: Enterprise Extra 19
    type: vless
    server: 185.141.227.180
    port: 2053
    uuid: 2c634977-584e-4e1f-b501-1b70c77a6743
    udp: true
    tls: true
    servername: dismissaltree.net
    client-fingerprint: random
    network: ws
  - name: Enterprise Extra 20
    type: vless
    server: 185.141.227.180
    port: 2053
    uuid: 2c634977-584e-4e1f-b501-1b70c77a6743
    udp: true
    tls: true
    servername: dismissaltree.net
    client-fingerprint: random
    network: ws
  - name: Enterprise Extra 21
    type: vless
    server: 185.141.227.180
    port: 2053
    uuid: 2c634977-584e-4e1f-b501-1b70c77a6743
    udp: true
    tls: true
    servername: dismissaltree.net
    network: ws
  - name: Enterprise Extra 22
    type: vless
    server: 185.141.227.180
    port: 2053
    uuid: 2c634977-584e-4e1f-b501-1b70c77a6743
    udp: true
    tls: true
    servername: dismissaltree.net
    client-fingerprint: random
    network: ws
  - name: Enterprise Extra 23
    type: vless
    server: 185.141.227.180
    port: 2053
    uuid: 2c634977-584e-4e1f-b501-1b70c77a6743
    udp: true
    tls: true
    servername: dismissaltree.net
    client-fingerprint: random
    network: ws
  - name: Enterprise Extra 24
    type: vless
    server: 185.141.227.180
    port: 2053
    uuid: 2c634977-584e-4e1f-b501-1b70c77a6743
    udp: true
    tls: true
    servername: dismissaltree.net
    client-fingerprint: random
    network: ws
  - name: Enterprise Extra 25
    type: vless
    server: 185.141.227.180
    port: 2053
    uuid: 2c634977-584e-4e1f-b501-1b70c77a6743
    udp: true
    tls: true
    servername: dismissaltree.net
    client-fingerprint: random
    network: ws
  - name: Enterprise Extra 26
    type: vless
    server: 185.141.227.180
    port: 2053
    uuid: 2c634977-584e-4e1f-b501-1b70c77a6743
    udp: true
    tls: true
    servername: dismissaltree.net
    client-fingerprint: random
    network: ws
  - name: Enterprise Extra 27
    type: vless
    server: 185.141.227.180
    port: 2053
    uuid: 2c634977-584e-4e1f-b501-1b70c77a6743
    udp: true
    tls: true
    servername: dismissaltree.net
    client-fingerprint: random
    network: ws
  - name: Enterprise Extra 28
    type: vless
    server: 185.141.227.180
    port: 2053
    uuid: 2c634977-584e-4e1f-b501-1b70c77a6743
    udp: true
    tls: true
    servername: dismissaltree.net
    client-fingerprint: random
    network: ws
  - name: Enterprise Extra 29
    type: vless
    server: 94.139.255.25
    port: 11443
    uuid: cbb4a068-5f6e-4bd8-9f41-c5c32650b775
    udp: true
    tls: true
    servername: shprcdn.xyz
    client-fingerprint: firefox
    network: ws
    reality-opts:
      public-key: 5hhzoP441DrGa2l5YlwizPSA5chdY2VjRP7CiEfLLA0
      short-id: eea4ab7bddaeea39
  - name: Enterprise Extra 30
    type: vless
    server: 46.243.142.63
    port: 8443
    uuid: cbb4a068-5f6e-4bd8-9f41-c5c32650b775
    udp: true
    tls: true
    servername: an9b3d-07.p.portshaper.com
    client-fingerprint: firefox
    network: ws
    reality-opts:
      public-key: 5hhzoP441DrGa2l5YlwizPSA5chdY2VjRP7CiEfLLA0
      short-id: eea4ab7bddaeea39
  - name: Enterprise Extra 31
    type: vless
    server: 194.55.239.135
    port: 8448
    uuid: cbb4a068-5f6e-4bd8-9f41-c5c32650b775
    udp: true
    tls: true
    servername: an9b3d-08.p.portshaper.com
    client-fingerprint: firefox
    network: grpc
    grpc-opts:
      grpc-service-name: google.storage.v2.Storage
    reality-opts:
      public-key: E60vw24Kf87MVXrsIjt1xON8LstSH9yunWUVFZX_xTM
  - name: Enterprise Extra 32
    type: vless
    server: 194.55.239.135
    port: 8447
    uuid: cbb4a068-5f6e-4bd8-9f41-c5c32650b775
    udp: true
    tls: true
    servername: an9b3d-08.p.portshaper.com
    client-fingerprint: firefox
    network: grpc
    grpc-opts:
      grpc-service-name: google.storage.v2.Storage
    reality-opts:
      public-key: E60vw24Kf87MVXrsIjt1xON8LstSH9yunWUVFZX_xTM
  - name: Enterprise Extra 33
    type: vless
    server: 94.139.255.254
    port: 5443
    uuid: cbb4a068-5f6e-4bd8-9f41-c5c32650b775
    udp: true
    tls: true
    servername: shprcdn.net
    client-fingerprint: firefox
    network: ws
    reality-opts:
      public-key: 5hhzoP441DrGa2l5YlwizPSA5chdY2VjRP7CiEfLLA0
      short-id: eea4ab7bddaeea39
  - name: Enterprise Extra 34
    type: vless
    server: 91.196.32.216
    port: 9543
    uuid: ff6d2dda-7b90-404e-9951-34b3124bfdfa
    udp: true
    tls: true
    flow: xtls-rprx-vision
    servername: storage.yandexcloud.net
    client-fingerprint: chrome
    network: tcp
    reality-opts:
      public-key: M6Ck7wUfOLhQl4582CBnaXwscPEP6SCpdEkUDzoj4Fc
      short-id: 123abcdf
  - name: Enterprise Extra 35
    type: vless
    server: 188.225.62.104
    port: 443
    uuid: bbbdf62a-0f7e-45f4-9d03-3fe52f599e1f
    udp: true
    tls: true
    flow: xtls-rprx-vision
    servername: yastatic.net
    client-fingerprint: firefox
    network: tcp
    reality-opts:
      public-key: AiG06E-jizo9xtBbulKQvdQqhZfobZJBTQIKkaDjaiU
      short-id: a1b2c3d4e5
  - name: Enterprise Extra 36
    type: vless
    server: 188.225.62.104
    port: 443
    uuid: bbbdf62a-0f7e-45f4-9d03-3fe52f599e1f
    udp: true
    tls: true
    flow: xtls-rprx-vision
    servername: passport.yandex.ru
    client-fingerprint: firefox
    network: tcp
    reality-opts:
      public-key: AiG06E-jizo9xtBbulKQvdQqhZfobZJBTQIKkaDjaiU
      short-id: a1b2c3d4e5
  - name: Enterprise Extra 37
    type: vless
    server: 188.225.62.104
    port: 443
    uuid: bbbdf62a-0f7e-45f4-9d03-3fe52f599e1f
    udp: true
    tls: true
    flow: xtls-rprx-vision
    servername: max.ru
    client-fingerprint: firefox
    network: tcp
    reality-opts:
      public-key: AiG06E-jizo9xtBbulKQvdQqhZfobZJBTQIKkaDjaiU
      short-id: a1b2c3d4e5
  - name: Enterprise Extra 38
    type: vless
    server: 185.229.9.175
    port: 443
    uuid: 52e954ba-9980-4d60-957e-6ac389b0105a
    udp: true
    tls: true
    flow: xtls-rprx-vision
    servername: yandex.ru
    client-fingerprint: firefox
    network: tcp
    reality-opts:
      public-key: 1ea_kTf2MvXh9PtpzQdjQWb5ls5BjyUtSbn-qiR8ZXk
      short-id: cb0e256070ac91c7
  - name: Enterprise Extra 39
    type: vless
    server: 185.229.9.234
    port: 443
    uuid: 52e954ba-9980-4d60-957e-6ac389b0105a
    udp: true
    tls: true
    flow: xtls-rprx-vision
    servername: api.ok.ru
    client-fingerprint: firefox
    network: tcp
    reality-opts:
      public-key: lIgF8B3ICGAQ6ExbRe9YDu8ZD1Ozzqu7-YO7hfuXfRw
      short-id: cc8069f6c2cbcb32
  - name: Enterprise Extra 40
    type: vless
    server: 185.229.9.74
    port: 443
    uuid: 52e954ba-9980-4d60-957e-6ac389b0105a
    udp: true
    tls: true
    flow: xtls-rprx-vision
    servername: api.ok.ru
    client-fingerprint: firefox
    network: tcp
    reality-opts:
      public-key: lIgF8B3ICGAQ6ExbRe9YDu8ZD1Ozzqu7-YO7hfuXfRw
      short-id: cc8069f6c2cbcb32
  - name: Enterprise Extra 41
    type: vless
    server: 185.229.9.234
    port: 443
    uuid: 52e954ba-9980-4d60-957e-6ac389b0105a
    udp: true
    tls: true
    flow: xtls-rprx-vision
    servername: max.ru
    client-fingerprint: firefox
    network: tcp
    reality-opts:
      public-key: AD7roAsP-vrlxeyfnVEWj7Cq_2viTSICXo2Psm6B_3E
      short-id: cb425a961bb7d67c
  - name: Enterprise Extra 42
    type: vless
    server: 31.177.111.75
    port: 8443
    uuid: 52e954ba-9980-4d60-957e-6ac389b0105a
    udp: true
    tls: true
    flow: xtls-rprx-vision
    servername: wap.yandex.ru
    client-fingerprint: firefox
    network: tcp
    reality-opts:
      public-key: rVAe7lMXUS4ie-OaecaucJOyl0y1gVmaWFrRq2gMRG8
      short-id: c1756ac68565f85c
  - name: Enterprise Extra 43
    type: vless
    server: 95.129.236.28
    port: 443
    uuid: 52e954ba-9980-4d60-957e-6ac389b0105a
    udp: true
    tls: true
    servername: my.pc-club.app
    client-fingerprint: firefox
    network: ws
  - name: Enterprise Extra 44
    type: vless
    server: 185.229.9.74
    port: 443
    uuid: 52e954ba-9980-4d60-957e-6ac389b0105a
    udp: true
    tls: true
    flow: xtls-rprx-vision
    servername: www.vk.com
    client-fingerprint: firefox
    network: tcp
    reality-opts:
      public-key: 7o36V1H4H_34kTMpwCfJmDB1bbd21Rgbk2dM_dAonyM
      short-id: 0bb3fbdc6cb178d4
  - name: Enterprise Extra 45
    type: vless
    server: 185.229.9.175
    port: 443
    uuid: 52e954ba-9980-4d60-957e-6ac389b0105a
    udp: true
    tls: true
    flow: xtls-rprx-vision
    servername: www.vk.com
    client-fingerprint: firefox
    network: tcp
    reality-opts:
      public-key: ZO7C9aMpoVJ6CmkKWUBS20eKe542_tGIc6wZFGB-yjY
      short-id: b4e5b7cefcd0d47c
  - name: Enterprise Extra 46
    type: vless
    server: 185.229.9.234
    port: 443
    uuid: 52e954ba-9980-4d60-957e-6ac389b0105a
    udp: true
    tls: true
    flow: xtls-rprx-vision
    servername: www.vk.com
    client-fingerprint: firefox
    network: tcp
    reality-opts:
      public-key: 7o36V1H4H_34kTMpwCfJmDB1bbd21Rgbk2dM_dAonyM
      short-id: 0bb3fbdc6cb178d4
  - name: Enterprise Extra 47
    type: vless
    server: 185.229.9.74
    port: 443
    uuid: 52e954ba-9980-4d60-957e-6ac389b0105a
    udp: true
    tls: true
    flow: xtls-rprx-vision
    servername: yandex.ru
    client-fingerprint: firefox
    network: tcp
    reality-opts:
      public-key: fsyvGBPzzwpnSA0UnJML_cDqviNSEdj9zktpC1EeFhY
      short-id: 743541f932fa3959
  - name: Enterprise Extra 48
    type: vless
    server: 91.240.87.166
    port: 443
    uuid: 52e954ba-9980-4d60-957e-6ac389b0105a
    udp: true
    tls: true
    flow: xtls-rprx-vision
    servername: yandex.ru
    client-fingerprint: firefox
    network: tcp
    reality-opts:
      public-key: 2ZUQYIhUijdJ1vRL8iR8RxjmJvRVHYp-JoWevQ2LyR4
      short-id: 7bb730e3e6616d31
  - name: Enterprise Extra 49
    type: vless
    server: 185.229.9.74
    port: 443
    uuid: 52e954ba-9980-4d60-957e-6ac389b0105a
    udp: true
    tls: true
    flow: xtls-rprx-vision
    servername: max.ru
    client-fingerprint: firefox
    network: tcp
    reality-opts:
      public-key: AD7roAsP-vrlxeyfnVEWj7Cq_2viTSICXo2Psm6B_3E
      short-id: cb425a961bb7d67c
  - name: Enterprise Extra 50
    type: vless
    server: 185.229.9.175
    port: 443
    uuid: 52e954ba-9980-4d60-957e-6ac389b0105a
    udp: true
    tls: true
    flow: xtls-rprx-vision
    servername: max.ru
    client-fingerprint: firefox
    network: tcp
    reality-opts:
      public-key: 2QoNARj5aaa5-kUEiwx38KzbbiENQZ95V7x9jFo6f2A
      short-id: 88a84e3390fcf4a7
  - name: Enterprise Extra 51
    type: vless
    server: 91.240.87.166
    port: 443
    uuid: 52e954ba-9980-4d60-957e-6ac389b0105a
    udp: true
    tls: true
    flow: xtls-rprx-vision
    servername: www.vk.com
    client-fingerprint: firefox
    network: tcp
    reality-opts:
      public-key: nkpZyXHfG7nhEh1vjBjEDV-Tn7UhHEb5iN9iA8gc9VE
      short-id: c31188450fe50718
  - name: Enterprise Extra 52
    type: vless
    server: 185.229.9.234
    port: 443
    uuid: 52e954ba-9980-4d60-957e-6ac389b0105a
    udp: true
    tls: true
    flow: xtls-rprx-vision
    servername: yandex.ru
    client-fingerprint: firefox
    network: tcp
    reality-opts:
      public-key: fsyvGBPzzwpnSA0UnJML_cDqviNSEdj9zktpC1EeFhY
      short-id: 743541f932fa3959
  - name: Enterprise Extra 53
    type: vless
    server: 185.229.9.175
    port: 443
    uuid: 52e954ba-9980-4d60-957e-6ac389b0105a
    udp: true
    tls: true
    flow: xtls-rprx-vision
    servername: api.ok.ru
    client-fingerprint: firefox
    network: tcp
    reality-opts:
      public-key: Eutx4frdhG1y7THj7k3CjazAsrCMj932RtbV4a1QfQk
      short-id: d5b6308860d578bb
  - name: Enterprise Extra 54
    type: vless
    server: 185.229.9.235
    port: 43005
    uuid: 52e954ba-9980-4d60-957e-6ac389b0105a
    udp: true
    tls: true
    flow: xtls-rprx-vision
    servername: max.ru
    client-fingerprint: firefox
    network: tcp
    reality-opts:
      public-key: tP_mtYuA3fZq8C6WatAA4TmDv0_Wigq9xYcmb5UsTRA
      short-id: 93653ee07ff5124c
  - name: Enterprise Extra 55
    type: vless
    server: 31.177.111.114
    port: 443
    uuid: 52e954ba-9980-4d60-957e-6ac389b0105a
    udp: true
    tls: true
    flow: xtls-rprx-vision
    servername: yandex.ru
    client-fingerprint: firefox
    network: tcp
    reality-opts:
      public-key: k-E1YJ4oOVPhmILq1gBiBm_u3Vo8oYrMNDp85h80Pig
      short-id: 035818587187be68
  - name: Enterprise Extra 56
    type: vless
    server: 31.177.111.114
    port: 443
    uuid: 52e954ba-9980-4d60-957e-6ac389b0105a
    udp: true
    tls: true
    flow: xtls-rprx-vision
    servername: www.vk.com
    client-fingerprint: firefox
    network: tcp
    reality-opts:
      public-key: cKE5quiMcRgilQjWqhvZiKzHZi2XQDsL9fbEOZykWi0
      short-id: 73ac62e56cfd3455
  - name: Enterprise Extra 57
    type: vless
    server: 5.178.85.132
    port: 443
    uuid: 89f0fbb2-ae52-4eb9-b24a-e6dbd201f372
    udp: true
    tls: true
    servername: selbs3.luxusvpn.app
    client-fingerprint: firefox
    network: ws
  - name: Enterprise Extra 58
    type: vless
    server: 188.72.103.4
    port: 443
    uuid: 89f0fbb2-ae52-4eb9-b24a-e6dbd201f372
    udp: true
    tls: true
    servername: cdnyan7.floatingcars.net
    client-fingerprint: firefox
    network: ws
  - name: Enterprise Extra 59
    type: vless
    server: 188.72.103.4
    port: 443
    uuid: 89f0fbb2-ae52-4eb9-b24a-e6dbd201f372
    udp: true
    tls: true
    servername: cdnyan8.floatingcars.net
    client-fingerprint: firefox
    network: ws
  - name: Enterprise Extra 60
    type: vless
    server: 188.225.72.20
    port: 443
    uuid: 89f0fbb2-ae52-4eb9-b24a-e6dbd201f372
    udp: true
    tls: true
    servername: ob3.floatingcars.net
    client-fingerprint: firefox
    network: ws
  - name: Enterprise Extra 61
    type: vless
    server: 188.72.103.4
    port: 443
    uuid: 89f0fbb2-ae52-4eb9-b24a-e6dbd201f372
    udp: true
    tls: true
    servername: cdnyan6.floatingcars.net
    client-fingerprint: firefox
    network: ws
  - name: Enterprise Extra 62
    type: vless
    server: 188.72.103.4
    port: 443
    uuid: 89f0fbb2-ae52-4eb9-b24a-e6dbd201f372
    udp: true
    tls: true
    servername: cdnyan2.floatingcars.net
    client-fingerprint: firefox
    network: ws
  - name: Enterprise Extra 63
    type: vless
    server: 188.72.103.4
    port: 443
    uuid: 89f0fbb2-ae52-4eb9-b24a-e6dbd201f372
    udp: true
    tls: true
    servername: cdnyan4.floatingcars.net
    client-fingerprint: firefox
    network: ws
  - name: Enterprise Extra 64
    type: vless
    server: 188.225.24.73
    port: 443
    uuid: 89f0fbb2-ae52-4eb9-b24a-e6dbd201f372
    udp: true
    tls: true
    servername: ob1.floatingcars.net
    client-fingerprint: firefox
    network: ws
  - name: Enterprise Extra 65
    type: vless
    server: 188.72.103.4
    port: 443
    uuid: 89f0fbb2-ae52-4eb9-b24a-e6dbd201f372
    udp: true
    tls: true
    servername: cdnyan5.floatingcars.net
    client-fingerprint: firefox
    network: ws
  - name: Enterprise Extra 66
    type: vless
    server: 188.72.103.4
    port: 443
    uuid: 89f0fbb2-ae52-4eb9-b24a-e6dbd201f372
    udp: true
    tls: true
    servername: cdnyan.floatingcars.net
    client-fingerprint: firefox
    network: ws
  - name: Enterprise Extra 67
    type: vless
    server: 188.72.103.4
    port: 443
    uuid: 89f0fbb2-ae52-4eb9-b24a-e6dbd201f372
    udp: true
    tls: true
    servername: cdnyan3.floatingcars.net
    client-fingerprint: firefox
    network: ws
  - name: Enterprise Extra 68
    type: vless
    server: 46.254.18.60
    port: 443
    uuid: 89f0fbb2-ae52-4eb9-b24a-e6dbd201f372
    udp: true
    tls: true
    servername: ihcbs1.luxusvpn.app
    client-fingerprint: firefox
    network: ws
  - name: Enterprise Extra 69
    type: vless
    server: 188.225.57.223
    port: 443
    uuid: 1eaba86a-7257-4a8a-a542-5d5e1caeb493
    udp: true
    tls: true
    servername: cloudcdn-m9-12.cdn.yandex.net
    client-fingerprint: qq
    network: grpc
    grpc-opts:
      grpc-service-name: grpc_vless
    reality-opts:
      public-key: 9YcV-gA7iaXFnI0HXQa624OIqm4zxO_U2eMCWXQAiCQ
      short-id: 8375b32501373f41
  - name: Enterprise Extra 70
    type: vless
    server: 176.123.162.100
    port: 9880
    uuid: 5d16ac22-6eea-426f-b778-6f4c2961faef
    udp: true
    tls: true
    servername: dl.google.com
    client-fingerprint: firefox
    network: grpc
    grpc-opts:
      grpc-service-name: grpc-tunnel
    reality-opts:
      public-key: bnRIb3Er1i-K6NGGByCO9UbGfOvu43ZoiK7ulPd1SzU
  - name: Enterprise Extra 71
    type: vless
    server: 84.38.181.249
    port: 443
    uuid: 9737cc16-7266-45ce-94d9-6e05a45e0b9c
    udp: true
    tls: true
    servername: open.spotify.com
    client-fingerprint: safari
    network: ws
    reality-opts:
      public-key: pyZ0M0nE3aGx2UtCXNhajcneyCF4E8J3zpxhIKkQw0w
      short-id: 6bc02f1c474260c3
  - name: Enterprise Extra 72
    type: vless
    server: 185.22.235.63
    port: 443
    uuid: 283a2217-6d5e-410a-8ab5-ff597b6b954c
    udp: true
    tls: true
    servername: web.max.ru
    client-fingerprint: firefox
    network: grpc
    grpc-opts:
      grpc-service-name: grpc-vk
    reality-opts:
      public-key: 1vSZjvhZO01oAEH3b7eebR1qF5dLU1Dq2E7xu8pwGSs
      short-id: 428ef87fd47a3a33
  - name: Enterprise Extra 73
    type: vless
    server: 185.22.235.181
    port: 443
    uuid: 283a2217-6d5e-410a-8ab5-ff597b6b954c
    udp: true
    tls: true
    servername: smartcaptcha.yandexcloud.net
    client-fingerprint: firefox
    network: grpc
    grpc-opts:
      grpc-service-name: grpc-vk
    reality-opts:
      public-key: 1vSZjvhZO01oAEH3b7eebR1qF5dLU1Dq2E7xu8pwGSs
      short-id: 428ef87fd47a3a33
  - name: Enterprise Extra 74
    type: vless
    server: 185.22.235.181
    port: 443
    uuid: 283a2217-6d5e-410a-8ab5-ff597b6b954c
    udp: true
    tls: true
    servername: ads.x5.ru
    client-fingerprint: firefox
    network: grpc
    grpc-opts:
      grpc-service-name: grpc-vk
    reality-opts:
      public-key: 1vSZjvhZO01oAEH3b7eebR1qF5dLU1Dq2E7xu8pwGSs
      short-id: 428ef87fd47a3a33
  - name: Enterprise Extra 75
    type: vless
    server: 185.22.235.63
    port: 443
    uuid: 283a2217-6d5e-410a-8ab5-ff597b6b954c
    udp: true
    tls: true
    servername: api-maps.yandex.ru
    client-fingerprint: firefox
    network: grpc
    grpc-opts:
      grpc-service-name: grpc-vk
    reality-opts:
      public-key: 1vSZjvhZO01oAEH3b7eebR1qF5dLU1Dq2E7xu8pwGSs
      short-id: 428ef87fd47a3a33
  - name: Enterprise Extra 76
    type: vless
    server: 185.22.235.63
    port: 443
    uuid: 283a2217-6d5e-410a-8ab5-ff597b6b954c
    udp: true
    tls: true
    servername: sso.passport.yandex.ru
    client-fingerprint: firefox
    network: grpc
    grpc-opts:
      grpc-service-name: grpc-vk
    reality-opts:
      public-key: 1vSZjvhZO01oAEH3b7eebR1qF5dLU1Dq2E7xu8pwGSs
      short-id: 428ef87fd47a3a33
  - name: Enterprise Extra 77
    type: vless
    server: 185.22.235.181
    port: 443
    uuid: ff430239-a420-442a-a91b-415542532415
    udp: true
    tls: true
    servername: api-maps.yandex.ru
    client-fingerprint: firefox
    network: grpc
    grpc-opts:
      grpc-service-name: grpc-vk
    reality-opts:
      public-key: 1vSZjvhZO01oAEH3b7eebR1qF5dLU1Dq2E7xu8pwGSs
      short-id: 428ef87fd47a3a33
  - name: Enterprise Extra 78
    type: vless
    server: 185.22.235.63
    port: 443
    uuid: ff430239-a420-442a-a91b-415542532415
    udp: true
    tls: true
    servername: ads.x5.ru
    client-fingerprint: firefox
    network: grpc
    grpc-opts:
      grpc-service-name: grpc-vk
    reality-opts:
      public-key: 1vSZjvhZO01oAEH3b7eebR1qF5dLU1Dq2E7xu8pwGSs
      short-id: 428ef87fd47a3a33
  - name: Enterprise Extra 79
    type: vless
    server: 185.22.235.181
    port: 443
    uuid: ff430239-a420-442a-a91b-415542532415
    udp: true
    tls: true
    servername: sso.passport.yandex.ru
    client-fingerprint: firefox
    network: grpc
    grpc-opts:
      grpc-service-name: grpc-vk
    reality-opts:
      public-key: 1vSZjvhZO01oAEH3b7eebR1qF5dLU1Dq2E7xu8pwGSs
      short-id: 428ef87fd47a3a33
  - name: Enterprise Extra 80
    type: vless
    server: 185.22.235.63
    port: 443
    uuid: ff430239-a420-442a-a91b-415542532415
    udp: true
    tls: true
    servername: web.max.ru
    client-fingerprint: firefox
    network: grpc
    grpc-opts:
      grpc-service-name: grpc-vk
    reality-opts:
      public-key: 1vSZjvhZO01oAEH3b7eebR1qF5dLU1Dq2E7xu8pwGSs
      short-id: 428ef87fd47a3a33
  - name: Enterprise Extra 81
    type: vless
    server: 37.139.42.235
    port: 443
    uuid: 1eaba86a-7257-4a8a-a542-5d5e1caeb493
    udp: true
    tls: true
    servername: katering.coteite.pro
    client-fingerprint: edge
    network: ws
  - name: Enterprise Extra 82
    type: vless
    server: 37.18.15.228
    port: 10022
    uuid: 1eaba86a-7257-4a8a-a542-5d5e1caeb493
    udp: true
    tls: true
    flow: xtls-rprx-vision
    servername: 32tkm.guardora.pro
    client-fingerprint: firefox
    network: tcp
    reality-opts:
      public-key: qTfZpilVyQrFDIgxmEsd18Ph3c_-8O-R_af4o_WQ_Tg
      short-id: f74c6a18133e2e72
  - name: Enterprise Extra 83
    type: vless
    server: 194.87.239.50
    port: 443
    uuid: 25284368-40d5-4abe-9f56-54a07e88a7af
    udp: true
    tls: true
    flow: xtls-rprx-vision
    servername: ya.ru
    client-fingerprint: firefox
    network: tcp
    reality-opts:
      public-key: RE8_xTgxCN-ZHuYHBVpXjS6_X-2jfiurSl5CF0ATLxs
      short-id: 701c4492fe75c46b
  - name: Enterprise Extra 84
    type: vless
    server: 95.129.236.216
    port: 443
    uuid: cbb4a068-5f6e-4bd8-9f41-c5c32650b775
    udp: true
    tls: true
    servername: crm.hourtley.com
    client-fingerprint: firefox
    network: ws
  - name: Enterprise Extra 85
    type: vless
    server: 84.201.169.241
    port: 443
    uuid: 1e2b9c0c-4f9b-56f5-a062-1c4f2d2aa200
    udp: true
    tls: true
    flow: xtls-rprx-vision
    servername: api-maps.yandex.ru
    client-fingerprint: firefox
    network: tcp
    reality-opts:
      public-key: xsKg-Qi8sZNRRaNf6svIUwSw6iMdOTc6Cl82inZFM1Y
  - name: Enterprise Extra 86
    type: vless
    server: 84.201.169.241
    port: 443
    uuid: 1e2b9c0c-4f9b-56f5-a062-1c4f2d2aa200
    udp: true
    tls: true
    flow: xtls-rprx-vision
    servername: api-maps.yandex.ru
    client-fingerprint: firefox
    network: tcp
    reality-opts:
      public-key: xsKg-Qi8sZNRRaNf6svIUwSw6iMdOTc6Cl82inZFM1Y
  - name: Enterprise Extra 87
    type: vless
    server: 45.11.27.86
    port: 443
    uuid: a2738d8d-ed26-4b13-a4a2-b89c7238cd10
    udp: true
    tls: true
    flow: xtls-rprx-vision
    servername: 5post-gate.x5.ru
    client-fingerprint: firefox
    network: tcp
    reality-opts:
      public-key: IBzFSziv_xISB-Q6X90PocTBde6jb4X6swEvdZQhqzk
      short-id: d0aae44f
  - name: Enterprise Extra 88
    type: vless
    server: 45.11.27.86
    port: 443
    uuid: a2738d8d-ed26-4b13-a4a2-b89c7238cd10
    udp: true
    tls: true
    flow: xtls-rprx-vision
    servername: 5post-gate.x5.ru
    client-fingerprint: chrome
    network: tcp
    reality-opts:
      public-key: IBzFSziv_xISB-Q6X90PocTBde6jb4X6swEvdZQhqzk
      short-id: d0aae44f
  - name: Enterprise Extra 89
    type: vless
    server: 46.243.142.184
    port: 8444
    uuid: 932dbcd4-a9aa-4e90-bcec-1e683b8249be
    udp: true
    tls: true
    servername: ppfu3n-01.p.portshaper.com
    client-fingerprint: edge
    network: grpc
    grpc-opts:
      grpc-service-name: google.storage.v2.Storage
    reality-opts:
      public-key: 28g5n3dLFkPoggCYNdyOemaK3r55baTU42SYh0cxzUw
  - name: Enterprise Extra 90
    type: vless
    server: 185.241.195.151
    port: 9444
    uuid: cbb4a068-5f6e-4bd8-9f41-c5c32650b775
    udp: true
    tls: true
    flow: xtls-rprx-vision
    servername: res.cdnguard.org
    client-fingerprint: firefox
    network: tcp
    reality-opts:
      public-key: zoQ5Lz62_sEMvHJywCYjYpunMOlGG1rf_jL81iCbwQk
      short-id: 413da1312ccce8fb
  - name: Enterprise Extra 91
    type: vless
    server: 81.94.148.178
    port: 7445
    uuid: cbb4a068-5f6e-4bd8-9f41-c5c32650b775
    udp: true
    tls: true
    flow: xtls-rprx-vision
    servername: s1.cdnguard.org
    client-fingerprint: firefox
    network: tcp
    reality-opts:
      public-key: wBB_aSYGm86IhxUcO8jccIsaBJZdNGlo3hTQlJFftkE
      short-id: f7d9e9a2e78e9a17
  - name: Enterprise Extra 92
    type: vless
    server: 176.123.161.222
    port: 9892
    uuid: 5d16ac22-6eea-426f-b778-6f4c2961faef
    udp: true
    tls: true
    servername: dl.google.com
    client-fingerprint: firefox
    network: grpc
    grpc-opts:
      grpc-service-name: grpc-tunnel
    reality-opts:
      public-key: bnRIb3Er1i-K6NGGByCO9UbGfOvu43ZoiK7ulPd1SzU
  - name: Enterprise Extra 93
    type: vless
    server: 46.243.142.42
    port: 7443
    uuid: 65cd649c-f2f5-49c4-8f57-94d9b706e846
    udp: true
    tls: true
    servername: shprcdn.homes
    client-fingerprint: safari
    network: ws
    reality-opts:
      public-key: ypzgxQ7VhTlTqjT0pAzWBIyotcG7SYqMpErwMBDci0g
  - name: Enterprise Extra 94
    type: vless
    server: 46.243.142.42
    port: 7443
    uuid: 65cd649c-f2f5-49c4-8f57-94d9b706e846
    udp: true
    tls: true
    servername: shprcdn.homes
    client-fingerprint: firefox
    network: ws
    reality-opts:
      public-key: ypzgxQ7VhTlTqjT0pAzWBIyotcG7SYqMpErwMBDci0g
  - name: Enterprise Extra 95
    type: vless
    server: 176.108.246.110
    port: 9830
    uuid: 5d16ac22-6eea-426f-b778-6f4c2961faef
    udp: true
    tls: true
    servername: dl.google.com
    client-fingerprint: chrome
    network: grpc
    grpc-opts:
      grpc-service-name: grpc-tunnel
    reality-opts:
      public-key: bnRIb3Er1i-K6NGGByCO9UbGfOvu43ZoiK7ulPd1SzU
  - name: Enterprise Extra 96
    type: vless
    server: 45.8.228.167
    port: 443
    uuid: 953358cb-82ba-4296-960d-6d2ec3468f4c
    udp: true
    tls: true
    servername: pl314j2.sysopnova.art
    client-fingerprint: qq
    network: ws
    reality-opts:
      public-key: n7Eivi9aG8PMxvaP5flLiukPADZIDVuzyOsOWDG43kQ
      short-id: 5eca
  - name: Enterprise Extra 97
    type: vless
    server: 95.129.236.239
    port: 443
    uuid: d3a0f09f-f72c-4bb0-8cba-e15791e6b897
    udp: true
    tls: true
    servername: login.opendns.stream
    client-fingerprint: firefox
    network: ws
  - name: Enterprise Extra 98
    type: vless
    server: 80.90.184.193
    port: 443
    uuid: 93996a55-febe-4183-9366-df6cb11a34dd
    udp: true
    tls: true
    flow: xtls-rprx-vision
    servername: sun9-38.userapi.com
    client-fingerprint: ios
    network: tcp
    reality-opts:
      public-key: r6lN34m1nN-xQZ458j5NPD5xJ3_QBF2bGzY4KJEo4ic
      short-id: abbcd128
  - name: Enterprise Extra 99
    type: vless
    server: 176.108.246.73
    port: 9873
    uuid: 5d16ac22-6eea-426f-b778-6f4c2961faef
    udp: true
    tls: true
    servername: dl.google.com
    client-fingerprint: chrome
    network: grpc
    grpc-opts:
      grpc-service-name: grpc-tunnel
    reality-opts:
      public-key: bnRIb3Er1i-K6NGGByCO9UbGfOvu43ZoiK7ulPd1SzU
      short-id: aabbccdd
  - name: Enterprise Ultra LTE Sweden
    type: vless
    server: 158.160.160.12
    port: 8445
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    tls: true
    flow: xtls-rprx-vision
    servername: ya.nexblic.online
    client-fingerprint: edge
    network: tcp
    reality-opts:
      public-key: o1rDY5JhNUcCzAzHWd5Whd_CeTuJN8hgB0V_prgJfFE
      short-id: 6ba85179e30d4fc2
  - name: Enterprise Ultra WiFi+LTE XHTTP
    type: vless
    server: 185.134.201.58
    port: 8444
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    tls: true
    servername: max.ru
    client-fingerprint: edge
    network: xhttp
    xhttp-opts:
      path: /
      mode: auto
    reality-opts:
      public-key: pb5GyC5vHPg83UYKTaYYdTwKKNwa_SFg0fEbyMpRwAE
      short-id: 3b9e1d7a5c20f846
  - name: Enterprise Ultra WiFi+LTE A1
    type: vless
    server: 193.124.125.116
    port: 2447
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    tls: true
    flow: xtls-rprx-vision
    servername: max.ru
    client-fingerprint: edge
    network: tcp
    reality-opts:
      public-key: 2PWetHSylRFyfbv80P3VNVabyB64Vyr2Uwo8PQUmvw4
  - name: Enterprise Ultra WiFi+LTE A2
    type: vless
    server: 193.124.125.116
    port: 2445
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    tls: true
    flow: xtls-rprx-vision
    servername: max.ru
    client-fingerprint: edge
    network: tcp
    reality-opts:
      public-key: 2PWetHSylRFyfbv80P3VNVabyB64Vyr2Uwo8PQUmvw4
  - name: Enterprise Ultra WiFi+LTE A3
    type: vless
    server: 193.124.125.116
    port: 2448
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    tls: true
    flow: xtls-rprx-vision
    servername: max.ru
    client-fingerprint: edge
    network: tcp
    reality-opts:
      public-key: 2PWetHSylRFyfbv80P3VNVabyB64Vyr2Uwo8PQUmvw4
  - name: Enterprise Ultra WiFi+LTE A4
    type: vless
    server: 193.124.125.116
    port: 2443
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    tls: true
    flow: xtls-rprx-vision
    servername: max.ru
    client-fingerprint: edge
    network: tcp
    reality-opts:
      public-key: 2PWetHSylRFyfbv80P3VNVabyB64Vyr2Uwo8PQUmvw4
  - name: Enterprise Ultra WiFi+LTE B1
    type: vless
    server: 193.124.125.191
    port: 2447
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    tls: true
    flow: xtls-rprx-vision
    servername: max.ru
    client-fingerprint: edge
    network: tcp
    reality-opts:
      public-key: 2PWetHSylRFyfbv80P3VNVabyB64Vyr2Uwo8PQUmvw4
  - name: Enterprise Ultra WiFi+LTE B2
    type: vless
    server: 193.124.125.191
    port: 2445
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    tls: true
    flow: xtls-rprx-vision
    servername: max.ru
    client-fingerprint: edge
    network: tcp
    reality-opts:
      public-key: 2PWetHSylRFyfbv80P3VNVabyB64Vyr2Uwo8PQUmvw4
  - name: Enterprise Ultra WiFi+LTE B3
    type: vless
    server: 193.124.125.191
    port: 2448
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    tls: true
    flow: xtls-rprx-vision
    servername: max.ru
    client-fingerprint: edge
    network: tcp
    reality-opts:
      public-key: 2PWetHSylRFyfbv80P3VNVabyB64Vyr2Uwo8PQUmvw4
  - name: Enterprise Ultra WiFi+LTE Grpc
    type: vless
    server: 88.216.189.239
    port: 2087
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    tls: true
    servername: potent.tuzvpn.com
    client-fingerprint: edge
    network: grpc
    grpc-opts:
      grpc-service-name: grpc
  - name: Enterprise Ultra WiFi+LTE XH3
    type: vless
    server: 88.216.189.239
    port: 8083
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    tls: true
    servername: potent.tuzvpn.com
    client-fingerprint: firefox
    alpn:
    - h3
    network: xhttp
    xhttp-opts:
      path: /
      mode: auto
  - name: Enterprise Ultra WiFi+LTE XH2
    type: vless
    server: 88.216.189.239
    port: 2083
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    tls: true
    servername: potent.tuzvpn.com
    client-fingerprint: chrome
    alpn:
    - h2
    network: xhttp
    xhttp-opts:
      path: /
      mode: auto
  - name: Enterprise Ultra LTE Finland
    type: vless
    server: 95.85.251.64
    port: 8446
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    tls: true
    servername: fi2.nexblic.online
    client-fingerprint: edge
    network: grpc
    grpc-opts:
      grpc-service-name: grpc
  - name: Enterprise Ultra LTE Estonia
    type: vless
    server: 217.60.182.13
    port: 2087
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    tls: true
    servername: potent.tuzvpn.com
    client-fingerprint: chrome
    network: grpc
    grpc-opts:
      grpc-service-name: grpc
  - name: Enterprise Ultra WiFi+LTE XPlain
    type: vless
    server: 217.60.182.13
    port: 8080
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    network: xhttp
    xhttp-opts:
      path: /
      mode: auto
  - name: Enterprise Ultra WiFi+LTE Est2
    type: vless
    server: 84.32.231.110
    port: 443
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    tls: true
    flow: xtls-rprx-vision
    servername: ned4.nexblic.online
    client-fingerprint: edge
    network: tcp
    reality-opts:
      public-key: _etdRTxfgV8-8qS4vNC2lTgXCEq4PLoJUb51_UM9WgE
      short-id: c8bcb79975a85955
  - name: Enterprise Ultra WiFi+LTE Ya2
    type: vless
    server: 158.160.160.12
    port: 8448
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    tls: true
    flow: xtls-rprx-vision
    servername: ya.nexblic.online
    client-fingerprint: edge
    network: tcp
    reality-opts:
      public-key: o1rDY5JhNUcCzAzHWd5Whd_CeTuJN8hgB0V_prgJfFE
      short-id: 6ba85179e30d4fc2
  - name: Enterprise Ultra WiFi+LTE Ya3
    type: vless
    server: 158.160.160.12
    port: 2443
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    tls: true
    flow: xtls-rprx-vision
    servername: ya.nexblic.online
    client-fingerprint: edge
    network: tcp
    reality-opts:
      public-key: o1rDY5JhNUcCzAzHWd5Whd_CeTuJN8hgB0V_prgJfFE
      short-id: 6ba85179e30d4fc2
  - name: Enterprise Ultra WiFi+LTE Ya4
    type: vless
    server: 158.160.160.12
    port: 8443
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    tls: true
    flow: xtls-rprx-vision
    servername: ya.nexblic.online
    client-fingerprint: edge
    network: tcp
    reality-opts:
      public-key: o1rDY5JhNUcCzAzHWd5Whd_CeTuJN8hgB0V_prgJfFE
      short-id: 6ba85179e30d4fc2
  - name: Enterprise Ultra WiFi+LTE Germany
    type: vless
    server: 144.31.148.212
    port: 443
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    tls: true
    flow: xtls-rprx-vision
    servername: ger3.nexblic.online
    client-fingerprint: edge
    network: tcp
    reality-opts:
      public-key: IiYicvR96i1hDkiX7FuZS4aZ46kl_72zJMOz7ZkeVw4
      short-id: c7e91a4f28b6d503
  - name: Enterprise Ultra Gaming
    type: vless
    server: 88.216.70.159
    port: 443
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    tls: true
    servername: potent.tuzvpn.com
    client-fingerprint: chrome
    network: hysteria2
  - name: Enterprise Ultra Gaming 2
    type: vless
    server: 185.134.201.58
    port: 9443
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    tls: true
    servername: swbs.nexblic.online
    client-fingerprint: chrome
    network: hysteria2
  - name: Enterprise Ultra WiFi+LTE Spain
    type: vless
    server: 2.27.27.35
    port: 19082
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    tls: true
    servername: potent.tuzvpn.com
    client-fingerprint: firefox
    alpn:
    - h3
    network: xhttp
    xhttp-opts:
      path: /
      mode: auto
  - name: Enterprise Ultra WiFi+LTE Kazakhstan
    type: vless
    server: 45.43.159.180
    port: 2083
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    tls: true
    servername: potent.tuzvpn.com
    client-fingerprint: chrome
    alpn:
    - h2
    network: xhttp
    xhttp-opts:
      path: /
      mode: auto
  - name: Enterprise Ultra WiFi+LTE USA
    type: vless
    server: 158.255.213.238
    port: 443
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    tls: true
    flow: xtls-rprx-vision
    servername: usa.tuzvpn.com
    client-fingerprint: edge
    network: tcp
    reality-opts:
      public-key: 49Z9WX-pyvF2Foz-r8oyRSyCX2B6iT-JwbUUp86LV2U
  - name: Enterprise Ultra WiFi+LTE Turkey
    type: vless
    server: 31.210.52.113
    port: 18082
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    tls: true
    servername: potent.tuzvpn.com
    client-fingerprint: edge
    alpn:
    - h2
    network: xhttp
    xhttp-opts:
      path: /
      mode: auto
  - name: Enterprise Ultra WiFi+LTE Lithuania
    type: vless
    server: lt2.h1cloud.net
    port: 25011
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    tls: true
    flow: xtls-rprx-vision
    servername: www.google.com
    client-fingerprint: chrome
    network: tcp
    reality-opts:
      public-key: ZKMdCqLnCM0HEWlnme1G6fwQmVI9-APX3BPHau1jI3Y
      short-id: a8e084a20e542573
  - name: Enterprise Ultra WiFi+LTE Latvia
    type: vless
    server: 46.28.64.204
    port: 2087
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    tls: true
    servername: potent.tuzvpn.com
    client-fingerprint: chrome
    network: grpc
    grpc-opts:
      grpc-service-name: grpc
  - name: Enterprise Ultra WiFi+LTE Latvia2
    type: vless
    server: 217.177.34.137
    port: 2087
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    tls: true
    servername: potent.tuzvpn.com
    client-fingerprint: chrome
    network: grpc
    grpc-opts:
      grpc-service-name: grpc
  - name: Enterprise Ultra LTE Turkey
    type: vless
    server: 158.160.160.12
    port: 8447
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    tls: true
    flow: xtls-rprx-vision
    servername: ya.nexblic.online
    client-fingerprint: edge
    network: tcp
    reality-opts:
      public-key: o1rDY5JhNUcCzAzHWd5Whd_CeTuJN8hgB0V_prgJfFE
      short-id: 6ba85179e30d4fc2
  - name: Enterprise Ultra Moldova
    type: vless
    server: md1.h1cloud.net
    port: 25001
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    tls: true
    flow: xtls-rprx-vision
    servername: www.google.com
    client-fingerprint: chrome
    network: tcp
    reality-opts:
      public-key: IPjEYAg4Zc1TgwulsIOPYueyBHc3_QyudEV5ZPe9eio
      short-id: 8d1531718c1b26e3
  - name: Enterprise Ultra LTE Beeline
    type: vless
    server: 193.124.125.116
    port: 2449
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    tls: true
    flow: xtls-rprx-vision
    servername: max.ru
    client-fingerprint: edge
    network: tcp
    reality-opts:
      public-key: 2PWetHSylRFyfbv80P3VNVabyB64Vyr2Uwo8PQUmvw4
  - name: Enterprise Ultra LTE Sweden2
    type: vless
    server: 185.134.201.58
    port: 8443
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    tls: true
    servername: max.ru
    client-fingerprint: edge
    network: xhttp
    xhttp-opts:
      path: /
      mode: auto
    reality-opts:
      public-key: pb5GyC5vHPg83UYKTaYYdTwKKNwa_SFg0fEbyMpRwAE
      short-id: 56ccdef2fea321f0
  - name: 🇫🇮 Enterprise 201
    type: vless
    server: 170.168.4.59
    port: 17170
    uuid: 3eec23c1-2a0d-4d53-afa5-62745a1f5ffe
    udp: true
    tls: true
    servername: northerntrail.gazette.help
    client-fingerprint: random
    alpn: [h2]
    network: grpc
    grpc-opts:
      grpc-service-name: trail-grpc
  - name: 🇰🇿 Enterprise 202
    type: vless
    server: 5.252.33.175
    port: 9882
    uuid: 5d16ac22-6eea-426f-b778-6f4c2961faef
    udp: true
    tls: true
    servername: dl.google.com
    client-fingerprint: random
    network: grpc
    grpc-opts:
      grpc-service-name: grpc-tunnel
    reality-opts:
      public-key: bnRIb3Er1i-K6NGGByCO9UbGfOvu43ZoiK7ulPd1SzU
      short-id: "aabbccdd"
  - name: 🇨🇭 Enterprise 203
    type: vless
    server: 153.76.161.57
    port: 2096
    uuid: 3fcec4e4-a674-44d2-8cae-c253cec43b9c
    udp: true
    tls: true
    servername: id.vk.com
    client-fingerprint: random
    network: grpc
    grpc-opts:
      grpc-service-name: vless-grpc-reality
    reality-opts:
      public-key: SbVKOEMjK0sIlbwg4akyBg5mL5KZwwB-ed4eEE7YnRc
  - name: 🇨🇭 Enterprise 204
    type: vless
    server: 153.76.161.59
    port: 2096
    uuid: 3fcec4e4-a674-44d2-8cae-c253cec43b9c
    udp: true
    tls: true
    servername: id.vk.com
    client-fingerprint: random
    network: grpc
    grpc-opts:
      grpc-service-name: vless-grpc-reality
    reality-opts:
      public-key: SbVKOEMjK0sIlbwg4akyBg5mL5KZwwB-ed4eEE7YnRc
  - name: 🇨🇭 Enterprise 205
    type: vless
    server: ns.l90909l.vip
    port: 2096
    uuid: 3fcec4e4-a674-44d2-8cae-c253cec43b9c
    udp: true
    tls: true
    servername: id.vk.com
    client-fingerprint: random
    network: grpc
    grpc-opts:
      grpc-service-name: vless-grpc-reality
    reality-opts:
      public-key: SbVKOEMjK0sIlbwg4akyBg5mL5KZwwB-ed4eEE7YnRc
  - name: 🇳🇱 Enterprise 206
    type: vless
    server: 45.95.99.196
    port: 9443
    uuid: 7918e6f6-df8f-4a20-9c1d-59dbd1f22770
    udp: true
    tls: true
    servername: nlhy2.richforever.ru
    client-fingerprint: random
    network: grpc
    grpc-opts:
      grpc-service-name: grpc-service
    reality-opts:
      public-key: fKz8iMdY9dfMURdjdVcb9mYOJz2FlexJ3_v8mlM2RTE
  - name: 🇳🇱 Enterprise 207
    type: vless
    server: nlhy2.richforever.ru
    port: 9443
    uuid: 7918e6f6-df8f-4a20-9c1d-59dbd1f22770
    udp: true
    tls: true
    servername: nlhy2.richforever.ru
    client-fingerprint: chrome
    network: grpc
    grpc-opts:
      grpc-service-name: grpc-service
    reality-opts:
      public-key: fKz8iMdY9dfMURdjdVcb9mYOJz2FlexJ3_v8mlM2RTE
  - name: 🇳🇱 Enterprise 208
    type: vless
    server: 45.94.36.15
    port: 443
    uuid: c6e3c24c-ce20-4ad5-8c8c-68cb05890ea8
    udp: true
    tls: true
    servername: nl-1-6.unturned-rust-dead.lol
    client-fingerprint: random
    network: grpc
    grpc-opts:
      grpc-service-name: nl-grpc
    reality-opts:
      public-key: aYz4_13W8gtAXGBrWeqJXtExahAJ9kUPDapqeyAlYCk
  - name: 🇳🇱 Enterprise 209
    type: vless
    server: nl-1-6.unturned-rust-dead.lol
    port: 443
    uuid: c6e3c24c-ce20-4ad5-8c8c-68cb05890ea8
    udp: true
    tls: true
    servername: nl-1-6.unturned-rust-dead.lol
    client-fingerprint: chrome
    network: grpc
    grpc-opts:
      grpc-service-name: nl-grpc
    reality-opts:
      public-key: aYz4_13W8gtAXGBrWeqJXtExahAJ9kUPDapqeyAlYCk
  - name: 🇺🇦 Enterprise 210
    type: vless
    server: 122.8.45.3
    port: 8443
    uuid: 8ff8938c-49b6-b22f-a934-b55e91154ca3
    udp: true
    tls: true
    servername: www.hp.com
    client-fingerprint: chrome
    network: grpc
    grpc-opts:
      grpc-service-name: TunService
    reality-opts:
      public-key: QzFNQqkvBnDKIyjXUHJQe9jruaw9G5AV44vDC-qvnlo
  - name: 🇺🇸 Enterprise 211
    type: vless
    server: 5.252.33.175
    port: 9873
    uuid: 5d16ac22-6eea-426f-b778-6f4c2961faef
    udp: true
    tls: true
    servername: dl.google.com
    client-fingerprint: random
    network: grpc
    grpc-opts:
      grpc-service-name: grpc-tunnel
    reality-opts:
      public-key: bnRIb3Er1i-K6NGGByCO9UbGfOvu43ZoiK7ulPd1SzU
      short-id: "aabbccdd"
  - name: 🇷🇺 Enterprise 212
    type: vless
    server: 45.139.84.4
    port: 9443
    uuid: 7918e6f6-df8f-4a20-9c1d-59dbd1f22770
    udp: true
    tls: true
    servername: pl3.richforever.ru
    client-fingerprint: random
    network: grpc
    grpc-opts:
      grpc-service-name: grpc-service
    reality-opts:
      public-key: lQcmwpXJsT5tdh9mQhcsDyiDNFe0nAVLvSc_5WnfqSY
  - name: 🇷🇺 Enterprise 213
    type: vless
    server: pl3.richforever.ru
    port: 9443
    uuid: 7918e6f6-df8f-4a20-9c1d-59dbd1f22770
    udp: true
    tls: true
    servername: pl3.richforever.ru
    client-fingerprint: chrome
    network: grpc
    grpc-opts:
      grpc-service-name: grpc-service
    reality-opts:
      public-key: lQcmwpXJsT5tdh9mQhcsDyiDNFe0nAVLvSc_5WnfqSY
  - name: 🇷🇺 Enterprise 214
    type: vless
    server: 159.194.244.247
    port: 443
    uuid: c6e3c24c-ce20-4ad5-8c8c-68cb05890ea8
    udp: true
    tls: true
    servername: ru-1-3.unturned-rust-dead.lol
    client-fingerprint: chrome
    network: grpc
    grpc-opts:
      grpc-service-name: ru-api
    reality-opts:
      public-key: aYz4_13W8gtAXGBrWeqJXtExahAJ9kUPDapqeyAlYCk
  - name: 🇷🇺 Enterprise 215
    type: vless
    server: 93.188.184.150
    port: 443
    uuid: c6e3c24c-ce20-4ad5-8c8c-68cb05890ea8
    udp: true
    tls: true
    servername: ru-1-2.unturned-rust-dead.lol
    client-fingerprint: random
    network: grpc
    grpc-opts:
      grpc-service-name: ru-api
    reality-opts:
      public-key: aYz4_13W8gtAXGBrWeqJXtExahAJ9kUPDapqeyAlYCk
  - name: 🇷🇺 Enterprise 216
    type: vless
    server: ru-1-2.unturned-rust-dead.lol
    port: 443
    uuid: c6e3c24c-ce20-4ad5-8c8c-68cb05890ea8
    udp: true
    tls: true
    servername: ru-1-2.unturned-rust-dead.lol
    client-fingerprint: random
    network: grpc
    grpc-opts:
      grpc-service-name: ru-api
    reality-opts:
      public-key: aYz4_13W8gtAXGBrWeqJXtExahAJ9kUPDapqeyAlYCk
  - name: 🇷🇺 Enterprise 217
    type: vless
    server: ru-1-3.unturned-rust-dead.lol
    port: 443
    uuid: c6e3c24c-ce20-4ad5-8c8c-68cb05890ea8
    udp: true
    tls: true
    servername: ru-1-3.unturned-rust-dead.lol
    client-fingerprint: random
    network: grpc
    grpc-opts:
      grpc-service-name: ru-api
    reality-opts:
      public-key: aYz4_13W8gtAXGBrWeqJXtExahAJ9kUPDapqeyAlYCk
  - name: 🇦🇹 Enterprise 218
    type: vless
    server: 80.87.194.252
    port: 2096
    uuid: f13b36ac-3765-4458-8a4d-08575038c463
    udp: true
    tls: true
    servername: id.vk.com
    client-fingerprint: random
    network: grpc
    grpc-opts:
      grpc-service-name: vless-grpc-reality
    reality-opts:
      public-key: SbVKOEMjK0sIlbwg4akyBg5mL5KZwwB-ed4eEE7YnRc
  - name: 🇦🇹 Enterprise 219
    type: vless
    server: 80.87.194.253
    port: 2096
    uuid: f13b36ac-3765-4458-8a4d-08575038c463
    udp: true
    tls: true
    servername: id.vk.com
    client-fingerprint: chrome
    network: grpc
    grpc-opts:
      grpc-service-name: vless-grpc-reality
    reality-opts:
      public-key: SbVKOEMjK0sIlbwg4akyBg5mL5KZwwB-ed4eEE7YnRc
  - name: 🇦🇹 Enterprise 220
    type: vless
    server: 80.87.196.9
    port: 2096
    uuid: f13b36ac-3765-4458-8a4d-08575038c463
    udp: true
    tls: true
    servername: id.vk.com
    client-fingerprint: firefox
    network: grpc
    grpc-opts:
      grpc-service-name: vless-grpc-reality
    reality-opts:
      public-key: SbVKOEMjK0sIlbwg4akyBg5mL5KZwwB-ed4eEE7YnRc
  - name: 🇦🇹 Enterprise 221
    type: vless
    server: ns.ll0ll.vip
    port: 2096
    uuid: f13b36ac-3765-4458-8a4d-08575038c463
    udp: true
    tls: true
    servername: id.vk.com
    client-fingerprint: random
    network: grpc
    grpc-opts:
      grpc-service-name: vless-grpc-reality
    reality-opts:
      public-key: SbVKOEMjK0sIlbwg4akyBg5mL5KZwwB-ed4eEE7YnRc
  - name: 🇮🇹 Enterprise 222
    type: vless
    server: 82.202.136.204
    port: 9892
    uuid: 5d16ac22-6eea-426f-b778-6f4c2961faef
    udp: true
    tls: true
    servername: dl.google.com
    client-fingerprint: random
    network: grpc
    grpc-opts:
      grpc-service-name: grpc-tunnel
    reality-opts:
      public-key: ycPIUcY6ci2yi7YA_OHc20e4gzxEdJTqpmeShxSuHRU
      short-id: "aabbccdd"
  - name: 🇰🇿 Enterprise 223
    type: vless
    server: 82.202.139.79
    port: 9882
    uuid: 5d16ac22-6eea-426f-b778-6f4c2961faef
    udp: true
    tls: true
    servername: dl.google.com
    client-fingerprint: random
    network: grpc
    grpc-opts:
      grpc-service-name: grpc-tunnel
    reality-opts:
      public-key: bnRIb3Er1i-K6NGGByCO9UbGfOvu43ZoiK7ulPd1SzU
      short-id: "aabb"
  - name: 🇵🇱 Enterprise 224
    type: vless
    server: 185.229.9.176
    port: 46018
    uuid: a5893c8b-28a2-4947-9a1c-708db25506c3
    udp: true
    tls: true
    servername: ligastavok.ru
    client-fingerprint: random
    network: grpc
    grpc-opts:
      grpc-service-name: 
    reality-opts:
      public-key: een4U3X5mnY1gH2swiakXS9BJ8P4omf8Qp_s8sTp_3Y
      short-id: "68ae5e7c11f80af5"
  - name: 🇷🇸 Enterprise 225
    type: vless
    server: 45.132.176.172
    port: 9889
    uuid: 5d16ac22-6eea-426f-b778-6f4c2961faef
    udp: true
    tls: true
    servername: dl.google.com
    client-fingerprint: chrome
    network: grpc
    grpc-opts:
      grpc-service-name: grpc-tunnel
    reality-opts:
      public-key: ycPIUcY6ci2yi7YA_OHc20e4gzxEdJTqpmeShxSuHRU
      short-id: "aa"
  - name: 🇸🇪 Enterprise 226
    type: vless
    server: 82.202.139.225
    port: 9872
    uuid: 5d16ac22-6eea-426f-b778-6f4c2961faef
    udp: true
    tls: true
    servername: dl.google.com
    client-fingerprint: random
    network: grpc
    grpc-opts:
      grpc-service-name: grpc-tunnel
    reality-opts:
      public-key: sm3E3hjmT6_Gmg_O9rBz4nfXYrHpnl6-LvcCp6T6_xc
      short-id: "aabbccdd"
  - name: Enterprise 227
    type: vless
    server: 79.174.92.222
    port: 443
    uuid: 03ca8c3f-e777-45b5-a1b1-592094ca99ee
    udp: true
    tls: true
    servername: m.vk.com
    client-fingerprint: firefox
    network: grpc
    grpc-opts:
      grpc-service-name: 
    reality-opts:
      public-key: _PYBo3dyZRpGC44vADqcmlBCSeAVh1X8l3mulghVsTI
      short-id: "a1b2c3d4"
  - name: Enterprise 228
    type: vless
    server: 81.94.148.104
    port: 2096
    uuid: 3fcec4e4-a674-44d2-8cae-c253cec43b9c
    udp: true
    tls: true
    servername: id.vk.com
    client-fingerprint: random
    network: grpc
    grpc-opts:
      grpc-service-name: vless-grpc-reality
    reality-opts:
      public-key: SbVKOEMjK0sIlbwg4akyBg5mL5KZwwB-ed4eEE7YnRc
  - name: Enterprise 229
    type: vless
    server: 83.220.175.235
    port: 2096
    uuid: f13b36ac-3765-4458-8a4d-08575038c463
    udp: true
    tls: true
    servername: id.vk.com
    client-fingerprint: chrome
    network: grpc
    grpc-opts:
      grpc-service-name: vless-grpc-reality
    reality-opts:
      public-key: SbVKOEMjK0sIlbwg4akyBg5mL5KZwwB-ed4eEE7YnRc
  - name: Enterprise 230
    type: vless
    server: 91.107.126.228
    port: 2096
    uuid: f13b36ac-3765-4458-8a4d-08575038c463
    udp: true
    tls: true
    servername: id.vk.com
    client-fingerprint: random
    network: grpc
    grpc-opts:
      grpc-service-name: vless-grpc-reality
    reality-opts:
      public-key: SbVKOEMjK0sIlbwg4akyBg5mL5KZwwB-ed4eEE7YnRc
  - name: Enterprise 231
    type: vless
    server: 188.225.45.77
    port: 443
    uuid: 57284239-1ceb-439f-826e-cd25fcc1c1c5
    udp: true
    tls: true
    servername: 5post-gate.x5.ru
    client-fingerprint: chrome
    network: grpc
    grpc-opts:
      grpc-service-name: grpc
    reality-opts:
      public-key: 5SjHShy0uFG9MmzFbZ21Ozg623ZXKwLf3pzC_QA8CBE
      short-id: "5803463a64073dfc"
  - name: Enterprise 232
    type: vless
    server: 87.242.95.164
    port: 9443
    uuid: 9cf10268-96e2-46c0-94bd-9590d2d84ead
    udp: true
    tls: true
    servername: passport.yandex.ru
    client-fingerprint: qq
    network: grpc
    grpc-opts:
      grpc-service-name: grpc
    reality-opts:
      public-key: 9YcV-gA7iaXFnI0HXQa624OIqm4zxO_U2eMCWXQAiCQ
      short-id: "8375b32501373f41"
  - name: Enterprise 233
    type: vless
    server: ss.ll0ll.vip
    port: 2096
    uuid: f13b36ac-3765-4458-8a4d-08575038c463
    udp: true
    tls: true
    servername: id.vk.com
    client-fingerprint: chrome
    network: grpc
    grpc-opts:
      grpc-service-name: vless-grpc-reality
    reality-opts:
      public-key: SbVKOEMjK0sIlbwg4akyBg5mL5KZwwB-ed4eEE7YnRc
  - name: Enterprise 234
    type: vless
    server: ns.o11111o.club
    port: 443
    uuid: 39d927a4-b8c7-4dc7-91f1-8b2e05d8b6b6
    udp: true
    tls: true
    servername: id.vk.com
    client-fingerprint: ios
    network: grpc
    grpc-opts:
      grpc-service-name: vless-grpc-reality
    reality-opts:
      public-key: SbVKOEMjK0sIlbwg4akyBg5mL5KZwwB-ed4eEE7YnRc
  - name: Enterprise 235
    type: vless
    server: 78.159.240.41
    port: 443
    uuid: 39d927a4-b8c7-4dc7-91f1-8b2e05d8b6b6
    udp: true
    tls: true
    servername: id.vk.com
    client-fingerprint: ios
    network: grpc
    grpc-opts:
      grpc-service-name: vless-grpc-reality
    reality-opts:
      public-key: SbVKOEMjK0sIlbwg4akyBg5mL5KZwwB-ed4eEE7YnRc
  - name: Enterprise 236
    type: vless
    server: 87.242.127.240
    port: 443
    uuid: cf222b71-f3a2-44b9-8535-5c725b22eac4
    udp: true
    tls: true
    servername: 360.yandex.ru
    client-fingerprint: firefox
    network: grpc
    grpc-opts:
      grpc-service-name: 
    reality-opts:
      public-key: mK6jUHK0Iv4jpVqpvs4oadjh2Pifkz4QwrOdhWHJ9U0
  - name: Enterprise 237
    type: vless
    server: 77.110.104.125
    port: 6443
    uuid: 235fab16-5bb4-4593-b125-09f2e7ba7aa3
    udp: true
    tls: true
    servername: maps.apple.com
    client-fingerprint: chrome
    network: grpc
    grpc-opts:
      grpc-service-name: your-custom-path
    reality-opts:
      public-key: OMlQAFrCMZ3DX0xlnbEZiByLBFzpUE8XJHJsrBzLdlY
      short-id: "2da4299da566b7"
  - name: Enterprise 238
    type: vless
    server: 195.15.213.66
    port: 8082
    uuid: b4bc10bf-80f9-40c9-b4e5-29817c2f0992
    udp: true
    tls: true
    servername: id.vk.com
    client-fingerprint: chrome
    network: grpc
    grpc-opts:
      grpc-service-name: vless-grpc-reality
    reality-opts:
      public-key: SbVKOEMjK0sIlbwg4akyBg5mL5KZwwB-ed4eEE7YnRc
  - name: 🇫🇷 Enterprise 239
    type: vless
    server: 151.101.173.16
    port: 443
    uuid: 4218fba1-7df3-481d-acb8-7bff10739fc8
    udp: true
    tls: true
    servername: accounts.fastly.com
    client-fingerprint: chrome
    network: xhttp
    xhttp-opts:
      path: /
      mode: auto
      host: oh3.global.ssl.fastly.net
  - name: 🇩🇪 Enterprise 240
    type: vless
    server: fert5.localip.ir
    port: 2087
    uuid: ecbe8dbc-44f7-4275-8a6c-b0976a7bb5c8
    udp: true
    tls: true
    servername: fert5.localip.ir
    client-fingerprint: chrome
    alpn: [h2]
    network: xhttp
    xhttp-opts:
      path: /retasokrer
      mode: stream-one
      host: fert5.localip.ir
  - name: 🇩🇪 Enterprise 241
    type: vless
    server: ops4.localip.ir
    port: 8443
    uuid: ecbe8dbc-44f7-4275-8a6c-b0976a7bb5c8
    udp: true
    tls: true
    servername: ops4.localip.ir
    client-fingerprint: chrome
    alpn: [h2]
    network: xhttp
    xhttp-opts:
      path: /retasokrer
      mode: stream-one
      host: ops4.localip.ir
  - name: 🇩🇪 Enterprise 242
    type: vless
    server: spp1.localip.ir
    port: 2087
    uuid: ecbe8dbc-44f7-4275-8a6c-b0976a7bb5c8
    udp: true
    tls: true
    servername: spp1.localip.ir
    client-fingerprint: chrome
    alpn: [h2]
    network: xhttp
    xhttp-opts:
      path: /retasokrer
      mode: stream-one
      host: spp1.localip.ir
  - name: 🇵🇱 Enterprise 243
    type: vless
    server: 8.6.112.0
    port: 443
    uuid: 4218fba1-7df3-481d-acb8-7bff10739fc8
    udp: true
    tls: true
    servername: 186.baklajann.ru
    client-fingerprint: chrome
    network: xhttp
    xhttp-opts:
      path: /
      mode: auto
      host: 186.baklajann.ru
  - name: 🇷🇺 Enterprise 244
    type: vless
    server: birthdaysale.net
    port: 443
    uuid: 1ed2e903-4bbe-4dc3-abeb-91552789748c
    udp: true
    tls: true
    servername: birthdaysale.net
    client-fingerprint: firefox
    network: xhttp
    xhttp-opts:
      path: /api/v1/upload
      mode: auto
      host: birthdaysale.net
  - name: Enterprise 245
    type: vless
    server: 185.141.227.180
    port: 2053
    uuid: 2c634977-584e-4e1f-b501-1b70c77a6743
    udp: true
    tls: true
    servername: dismissaltree.net
    client-fingerprint: random
    network: xhttp
    xhttp-opts:
      path: /api/v1/upload
      mode: auto
      host: dismissaltree.net
  - name: Enterprise 246
    type: vless
    server: 78.159.250.84
    port: 2053
    uuid: 2c634977-584e-4e1f-b501-1b70c77a6743
    udp: true
    tls: true
    servername: summerculture.net
    client-fingerprint: firefox
    alpn: [h2, +http/1.1]
    network: xhttp
    xhttp-opts:
      path: /api/v1/upload
      mode: auto
      host: summerculture.net
  - name: Enterprise 247
    type: vless
    server: 109.120.188.152
    port: 443
    uuid: d1004cfe-c279-e865-d865-71964dbdf85d
    udp: true
    tls: true
    servername: ws188152.mypaneltest.online
    client-fingerprint: edge
    network: xhttp
    xhttp-opts:
      path: /cdn/static/chunks/bk-7d4a91e2
      mode: auto
      host: ws188152.mypaneltest.online
  - name: 🇫🇷 Enterprise 248
    type: vless
    server: 205.237.105.31
    port: 11443
    uuid: c6db98c5-9959-425e-9340-300779fc4e4b
    udp: true
    tls: true
    servername: f4.vimeostream.online
    client-fingerprint: firefox
    alpn: [http/1.1]
    network: ws
    ws-opts:
      path: /api/v2/stream/ws
      headers:
        Host: f4.vimeostream.online
  - name: 🇩🇪 Enterprise 249
    type: vless
    server: spb.ai-boost.tech
    port: 443
    uuid: e2599b64-2eaa-4b81-b2bc-738605ecfeec
    udp: true
    tls: true
    servername: spb.ai-boost.tech
    client-fingerprint: ios
    network: ws
    ws-opts:
      path: /bKGK1ZI0PzpQz3OeEeq1
      headers:
        Host: spb.ai-boost.tech
  - name: 🇳🇱 Enterprise 250
    type: vless
    server: 141.105.68.164
    port: 443
    uuid: 513e7ad9-318b-4495-8f5b-d88c4302fdc1
    udp: true
    tls: true
    servername: msk.ai-boost.tech
    client-fingerprint: random
    network: ws
    ws-opts:
      path: /3a7lzIHlgUuz2QnV1CaQ
      headers:
        Host: msk.ai-boost.tech
  - name: 🇳🇱 Enterprise 251
    type: vless
    server: msk.ai-boost.tech
    port: 443
    uuid: 513e7ad9-318b-4495-8f5b-d88c4302fdc1
    udp: true
    tls: true
    servername: msk.ai-boost.tech
    client-fingerprint: chrome
    network: ws
    ws-opts:
      path: /3a7lzIHlgUuz2QnV1CaQ
      headers:
        Host: msk.ai-boost.tech
  - name: 🇳🇱 Enterprise 252
    type: vless
    server: 193.233.220.194
    port: 443
    uuid: e2599b64-2eaa-4b81-b2bc-738605ecfeec
    udp: true
    tls: true
    servername: spb.ai-boost.tech
    client-fingerprint: random
    network: ws
    ws-opts:
      path: /bKGK1ZI0PzpQz3OeEeq1
      headers:
        Host: spb.ai-boost.tech
  - name: 🇺🇦 Enterprise 253
    type: vless
    server: 31.76.65.216
    port: 30002
    uuid: 3eec23c1-2a0d-4d53-afa5-62745a1f5ffe
    udp: true
    tls: true
    servername: olivejournal.gazette.help
    client-fingerprint: firefox
    alpn: [http/1.1]
    network: ws
    ws-opts:
      path: /journal-11ff0bb2fa767891c2fc03ebc9a59aa7
      headers:
        Host: olivejournal.gazette.help
  - name: 🇺🇸 Enterprise 254
    type: vless
    server: mail.loyalsmile.net
    port: 2052
    uuid: ff766b85-d50f-47b7-9b63-6ca4ea128f3f
    udp: true
    network: ws
    ws-opts:
      path: /wp-content4
  - name: 🇨🇦 Enterprise 255
    type: vless
    server: 89.116.46.50
    port: 8880
    uuid: 6202b230-417c-4d8e-b624-0f71afa9c75d
    udp: true
    network: ws
    ws-opts:
      path: /
      headers:
        Host: vms.lifetime07.workers.dev
  - name: 🇵🇱 Enterprise 256
    type: vless
    server: 91.215.43.248
    port: 443
    uuid: a5893c8b-28a2-4947-9a1c-708db25506c3
    udp: true
    tls: true
    servername: my.kempekl.online
    client-fingerprint: chrome
    network: ws
    ws-opts:
      path: /gateway/v1/ws/a64eb463966ba9c4bfd628f1de0362ae
      headers:
        Host: my.kempekl.online
  - name: 🇨🇦 Enterprise 257
    type: vless
    server: 194.58.109.206
    port: 8443
    uuid: 51a0af77-a60c-4d9b-81a4-f50e1b38d90b
    udp: true
    tls: true
    servername: cf3.schzyc.cc.cd
    client-fingerprint: chrome
    network: ws
    ws-opts:
      path: /proxyip=ProxyIP.CMLiussss.net
      headers:
        Host: cf3.schzyc.cc.cd
  - name: Enterprise 258
    type: vless
    server: bz9.wba-pn.ru
    port: 2053
    uuid: 9a47a8ff-8cdd-4a35-b765-fbad6d40e0a0
    udp: true
    tls: true
    servername: speedtest.tinkoff.ru
    client-fingerprint: chrome
    network: ws
    ws-opts:
      path: /
proxy-groups:
  - name: "🚀 Каналы"
    type: select
    proxies:
      - "♻️ Авто-выбор"
      - "Enterprise 12"
      - "Enterprise 8"
      - "Enterprise 13"
      - "Enterprise 7"
      - "Enterprise 9"
      - "Enterprise 11"
      - "Enterprise 2"
      - "Enterprise 3"
      - "Enterprise 14"
      - "Enterprise 10"
      - "Enterprise Extra 1"
      - "Enterprise Extra 2"
      - "Enterprise Extra 3"
      - "Enterprise Extra 4"
      - "Enterprise Extra 5"
      - "Enterprise Extra 6"
      - "Enterprise Extra 11"
      - "Enterprise Extra 12"
      - "Enterprise Extra 13"
      - "Enterprise Extra 14"
      - "Enterprise Extra 15"
      - "Enterprise Extra 16"
      - "Enterprise Extra 17"
      - "Enterprise Extra 18"
      - "Enterprise Extra 19"
      - "Enterprise Extra 20"
      - "Enterprise Extra 21"
      - "Enterprise Extra 22"
      - "Enterprise Extra 23"
      - "Enterprise Extra 24"
      - "Enterprise Extra 25"
      - "Enterprise Extra 26"
      - "Enterprise Extra 27"
      - "Enterprise Extra 28"
      - "Enterprise Extra 29"
      - "Enterprise Extra 30"
      - "Enterprise Extra 31"
      - "Enterprise Extra 32"
      - "Enterprise Extra 33"
      - "Enterprise Extra 34"
      - "Enterprise Extra 35"
      - "Enterprise Extra 36"
      - "Enterprise Extra 37"
      - "Enterprise Extra 38"
      - "Enterprise Extra 39"
      - "Enterprise Extra 40"
      - "Enterprise Extra 41"
      - "Enterprise Extra 42"
      - "Enterprise Extra 43"
      - "Enterprise Extra 44"
      - "Enterprise Extra 45"
      - "Enterprise Extra 46"
      - "Enterprise Extra 47"
      - "Enterprise Extra 48"
      - "Enterprise Extra 49"
      - "Enterprise Extra 50"
      - "Enterprise Extra 51"
      - "Enterprise Extra 52"
      - "Enterprise Extra 53"
      - "Enterprise Extra 54"
      - "Enterprise Extra 55"
      - "Enterprise Extra 56"
      - "Enterprise Extra 57"
      - "Enterprise Extra 58"
      - "Enterprise Extra 59"
      - "Enterprise Extra 60"
      - "Enterprise Extra 61"
      - "Enterprise Extra 62"
      - "Enterprise Extra 63"
      - "Enterprise Extra 64"
      - "Enterprise Extra 65"
      - "Enterprise Extra 66"
      - "Enterprise Extra 67"
      - "Enterprise Extra 68"
      - "Enterprise Extra 69"
      - "Enterprise Extra 70"
      - "Enterprise Extra 71"
      - "Enterprise Extra 72"
      - "Enterprise Extra 73"
      - "Enterprise Extra 74"
      - "Enterprise Extra 75"
      - "Enterprise Extra 76"
      - "Enterprise Extra 77"
      - "Enterprise Extra 78"
      - "Enterprise Extra 79"
      - "Enterprise Extra 80"
      - "Enterprise Extra 81"
      - "Enterprise Extra 82"
      - "Enterprise Extra 83"
      - "Enterprise Extra 84"
      - "Enterprise Extra 85"
      - "Enterprise Extra 86"
      - "Enterprise Extra 87"
      - "Enterprise Extra 88"
      - "Enterprise Extra 89"
      - "Enterprise Extra 90"
      - "Enterprise Extra 91"
      - "Enterprise Extra 92"
      - "Enterprise Extra 93"
      - "Enterprise Extra 94"
      - "Enterprise Extra 95"
      - "Enterprise Extra 96"
      - "Enterprise Extra 97"
      - "Enterprise Extra 98"
      - "Enterprise Extra 99"
      - "Enterprise Ultra LTE Sweden"
      - "Enterprise Ultra WiFi+LTE XHTTP"
      - "Enterprise Ultra WiFi+LTE A1"
      - "Enterprise Ultra WiFi+LTE A2"
      - "Enterprise Ultra WiFi+LTE A3"
      - "Enterprise Ultra WiFi+LTE A4"
      - "Enterprise Ultra WiFi+LTE B1"
      - "Enterprise Ultra WiFi+LTE B2"
      - "Enterprise Ultra WiFi+LTE B3"
      - "Enterprise Ultra WiFi+LTE Grpc"
      - "Enterprise Ultra WiFi+LTE XH3"
      - "Enterprise Ultra WiFi+LTE XH2"
      - "Enterprise Ultra LTE Finland"
      - "Enterprise Ultra LTE Estonia"
      - "Enterprise Ultra WiFi+LTE XPlain"
      - "Enterprise Ultra WiFi+LTE Est2"
      - "Enterprise Ultra WiFi+LTE Ya2"
      - "Enterprise Ultra WiFi+LTE Ya3"
      - "Enterprise Ultra WiFi+LTE Ya4"
      - "Enterprise Ultra WiFi+LTE Germany"
      - "Enterprise Ultra Gaming"
      - "Enterprise Ultra Gaming 2"
      - "Enterprise Ultra WiFi+LTE Spain"
      - "Enterprise Ultra WiFi+LTE Kazakhstan"
      - "Enterprise Ultra WiFi+LTE USA"
      - "Enterprise Ultra WiFi+LTE Turkey"
      - "Enterprise Ultra WiFi+LTE Lithuania"
      - "Enterprise Ultra WiFi+LTE Latvia"
      - "Enterprise Ultra WiFi+LTE Latvia2"
      - "Enterprise Ultra LTE Turkey"
      - "Enterprise Ultra Moldova"
      - "Enterprise Ultra LTE Beeline"
      - "Enterprise Ultra LTE Sweden2"
      - "🇫🇮 Enterprise 201"
      - "🇰🇿 Enterprise 202"
      - "🇨🇭 Enterprise 203"
      - "🇨🇭 Enterprise 204"
      - "🇨🇭 Enterprise 205"
      - "🇳🇱 Enterprise 206"
      - "🇳🇱 Enterprise 207"
      - "🇳🇱 Enterprise 208"
      - "🇳🇱 Enterprise 209"
      - "🇺🇦 Enterprise 210"
      - "🇺🇸 Enterprise 211"
      - "🇷🇺 Enterprise 212"
      - "🇷🇺 Enterprise 213"
      - "🇷🇺 Enterprise 214"
      - "🇷🇺 Enterprise 215"
      - "🇷🇺 Enterprise 216"
      - "🇷🇺 Enterprise 217"
      - "🇦🇹 Enterprise 218"
      - "🇦🇹 Enterprise 219"
      - "🇦🇹 Enterprise 220"
      - "🇦🇹 Enterprise 221"
      - "🇮🇹 Enterprise 222"
      - "🇰🇿 Enterprise 223"
      - "🇵🇱 Enterprise 224"
      - "🇷🇸 Enterprise 225"
      - "🇸🇪 Enterprise 226"
      - "Enterprise 227"
      - "Enterprise 228"
      - "Enterprise 229"
      - "Enterprise 230"
      - "Enterprise 231"
      - "Enterprise 232"
      - "Enterprise 233"
      - "Enterprise 234"
      - "Enterprise 235"
      - "Enterprise 236"
      - "Enterprise 237"
      - "Enterprise 238"
      - "🇫🇷 Enterprise 239"
      - "🇩🇪 Enterprise 240"
      - "🇩🇪 Enterprise 241"
      - "🇩🇪 Enterprise 242"
      - "🇵🇱 Enterprise 243"
      - "🇷🇺 Enterprise 244"
      - "Enterprise 245"
      - "Enterprise 246"
      - "Enterprise 247"
      - "🇫🇷 Enterprise 248"
      - "🇩🇪 Enterprise 249"
      - "🇳🇱 Enterprise 250"
      - "🇳🇱 Enterprise 251"
      - "🇳🇱 Enterprise 252"
      - "🇺🇦 Enterprise 253"
      - "🇺🇸 Enterprise 254"
      - "🇨🇦 Enterprise 255"
      - "🇵🇱 Enterprise 256"
      - "🇨🇦 Enterprise 257"
      - "Enterprise 258"
  - name: "♻️ Авто-выбор"
    type: url-test
    url: "https://www.gstatic.com/generate_204"
    interval: 900
    lazy: true
    proxies:
      - "Enterprise 12"
      - "Enterprise 8"
      - "Enterprise 13"
      - "Enterprise 7"
      - "Enterprise 9"
      - "Enterprise 11"
      - "Enterprise 2"
      - "Enterprise 3"
      - "Enterprise 14"
      - "Enterprise 10"
      - "Enterprise Extra 1"
      - "Enterprise Extra 2"
      - "Enterprise Extra 3"
      - "Enterprise Extra 4"
      - "Enterprise Extra 5"
      - "Enterprise Extra 6"
      - "Enterprise Extra 11"
      - "Enterprise Extra 12"
      - "Enterprise Extra 13"
      - "Enterprise Extra 14"
      - "Enterprise Extra 15"
      - "Enterprise Extra 16"
      - "Enterprise Extra 17"
      - "Enterprise Extra 18"
      - "Enterprise Extra 19"
      - "Enterprise Extra 20"
      - "Enterprise Extra 21"
      - "Enterprise Extra 22"
      - "Enterprise Extra 23"
      - "Enterprise Extra 24"
      - "Enterprise Extra 25"
      - "Enterprise Extra 26"
      - "Enterprise Extra 27"
      - "Enterprise Extra 28"
      - "Enterprise Extra 29"
      - "Enterprise Extra 30"
      - "Enterprise Extra 31"
      - "Enterprise Extra 32"
      - "Enterprise Extra 33"
      - "Enterprise Extra 34"
      - "Enterprise Extra 35"
      - "Enterprise Extra 36"
      - "Enterprise Extra 37"
      - "Enterprise Extra 38"
      - "Enterprise Extra 39"
      - "Enterprise Extra 40"
      - "Enterprise Extra 41"
      - "Enterprise Extra 42"
      - "Enterprise Extra 43"
      - "Enterprise Extra 44"
      - "Enterprise Extra 45"
      - "Enterprise Extra 46"
      - "Enterprise Extra 47"
      - "Enterprise Extra 48"
      - "Enterprise Extra 49"
      - "Enterprise Extra 50"
      - "Enterprise Extra 51"
      - "Enterprise Extra 52"
      - "Enterprise Extra 53"
      - "Enterprise Extra 54"
      - "Enterprise Extra 55"
      - "Enterprise Extra 56"
      - "Enterprise Extra 57"
      - "Enterprise Extra 58"
      - "Enterprise Extra 59"
      - "Enterprise Extra 60"
      - "Enterprise Extra 61"
      - "Enterprise Extra 62"
      - "Enterprise Extra 63"
      - "Enterprise Extra 64"
      - "Enterprise Extra 65"
      - "Enterprise Extra 66"
      - "Enterprise Extra 67"
      - "Enterprise Extra 68"
      - "Enterprise Extra 69"
      - "Enterprise Extra 70"
      - "Enterprise Extra 71"
      - "Enterprise Extra 72"
      - "Enterprise Extra 73"
      - "Enterprise Extra 74"
      - "Enterprise Extra 75"
      - "Enterprise Extra 76"
      - "Enterprise Extra 77"
      - "Enterprise Extra 78"
      - "Enterprise Extra 79"
      - "Enterprise Extra 80"
      - "Enterprise Extra 81"
      - "Enterprise Extra 82"
      - "Enterprise Extra 83"
      - "Enterprise Extra 84"
      - "Enterprise Extra 85"
      - "Enterprise Extra 86"
      - "Enterprise Extra 87"
      - "Enterprise Extra 88"
      - "Enterprise Extra 89"
      - "Enterprise Extra 90"
      - "Enterprise Extra 91"
      - "Enterprise Extra 92"
      - "Enterprise Extra 93"
      - "Enterprise Extra 94"
      - "Enterprise Extra 95"
      - "Enterprise Extra 96"
      - "Enterprise Extra 97"
      - "Enterprise Extra 98"
      - "Enterprise Extra 99"
      - "Enterprise Ultra LTE Sweden"
      - "Enterprise Ultra WiFi+LTE XHTTP"
      - "Enterprise Ultra WiFi+LTE A1"
      - "Enterprise Ultra WiFi+LTE A2"
      - "Enterprise Ultra WiFi+LTE A3"
      - "Enterprise Ultra WiFi+LTE A4"
      - "Enterprise Ultra WiFi+LTE B1"
      - "Enterprise Ultra WiFi+LTE B2"
      - "Enterprise Ultra WiFi+LTE B3"
      - "Enterprise Ultra WiFi+LTE Grpc"
      - "Enterprise Ultra WiFi+LTE XH3"
      - "Enterprise Ultra WiFi+LTE XH2"
      - "Enterprise Ultra LTE Finland"
      - "Enterprise Ultra LTE Estonia"
      - "Enterprise Ultra WiFi+LTE XPlain"
      - "Enterprise Ultra WiFi+LTE Est2"
      - "Enterprise Ultra WiFi+LTE Ya2"
      - "Enterprise Ultra WiFi+LTE Ya3"
      - "Enterprise Ultra WiFi+LTE Ya4"
      - "Enterprise Ultra WiFi+LTE Germany"
      - "Enterprise Ultra Gaming"
      - "Enterprise Ultra Gaming 2"
      - "Enterprise Ultra WiFi+LTE Spain"
      - "Enterprise Ultra WiFi+LTE Kazakhstan"
      - "Enterprise Ultra WiFi+LTE USA"
      - "Enterprise Ultra WiFi+LTE Turkey"
      - "Enterprise Ultra WiFi+LTE Lithuania"
      - "Enterprise Ultra WiFi+LTE Latvia"
      - "Enterprise Ultra WiFi+LTE Latvia2"
      - "Enterprise Ultra LTE Turkey"
      - "Enterprise Ultra Moldova"
      - "Enterprise Ultra LTE Beeline"
      - "Enterprise Ultra LTE Sweden2"
      - "🇫🇮 Enterprise 201"
      - "🇰🇿 Enterprise 202"
      - "🇨🇭 Enterprise 203"
      - "🇨🇭 Enterprise 204"
      - "🇨🇭 Enterprise 205"
      - "🇳🇱 Enterprise 206"
      - "🇳🇱 Enterprise 207"
      - "🇳🇱 Enterprise 208"
      - "🇳🇱 Enterprise 209"
      - "🇺🇦 Enterprise 210"
      - "🇺🇸 Enterprise 211"
      - "🇷🇺 Enterprise 212"
      - "🇷🇺 Enterprise 213"
      - "🇷🇺 Enterprise 214"
      - "🇷🇺 Enterprise 215"
      - "🇷🇺 Enterprise 216"
      - "🇷🇺 Enterprise 217"
      - "🇦🇹 Enterprise 218"
      - "🇦🇹 Enterprise 219"
      - "🇦🇹 Enterprise 220"
      - "🇦🇹 Enterprise 221"
      - "🇮🇹 Enterprise 222"
      - "🇰🇿 Enterprise 223"
      - "🇵🇱 Enterprise 224"
      - "🇷🇸 Enterprise 225"
      - "🇸🇪 Enterprise 226"
      - "Enterprise 227"
      - "Enterprise 228"
      - "Enterprise 229"
      - "Enterprise 230"
      - "Enterprise 231"
      - "Enterprise 232"
      - "Enterprise 233"
      - "Enterprise 234"
      - "Enterprise 235"
      - "Enterprise 236"
      - "Enterprise 237"
      - "Enterprise 238"
      - "🇫🇷 Enterprise 239"
      - "🇩🇪 Enterprise 240"
      - "🇩🇪 Enterprise 241"
      - "🇩🇪 Enterprise 242"
      - "🇵🇱 Enterprise 243"
      - "🇷🇺 Enterprise 244"
      - "Enterprise 245"
      - "Enterprise 246"
      - "Enterprise 247"
      - "🇫🇷 Enterprise 248"
      - "🇩🇪 Enterprise 249"
      - "🇳🇱 Enterprise 250"
      - "🇳🇱 Enterprise 251"
      - "🇳🇱 Enterprise 252"
      - "🇺🇦 Enterprise 253"
      - "🇺🇸 Enterprise 254"
      - "🇨🇦 Enterprise 255"
      - "🇵🇱 Enterprise 256"
      - "🇨🇦 Enterprise 257"
      - "Enterprise 258"
rules:
  - MATCH,🚀 Каналы
"""
}
