package com.whitedns.vpn

internal object ShieldChannelsAsset {
    val yaml: String = """# Enterprise Shield - All servers (offline)
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
  - name: "⚡ NX 1"
    type: vless
    server: 158.160.160.12
    port: 8445
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    network: tcp
    tls: true
    flow: xtls-rprx-vision
    servername: ya.nexblic.online
    client-fingerprint: edge
    reality-opts:
      public-key: o1rDY5JhNUcCzAzHWd5Whd_CeTuJN8hgB0V_prgJfFE
      short-id: 6ba85179e30d4fc2
  - name: "⚡ NX 3"
    type: vless
    server: 193.124.125.116
    port: 2447
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    network: tcp
    tls: true
    flow: xtls-rprx-vision
    servername: max.ru
    client-fingerprint: edge
    reality-opts:
      public-key: 2PWetHSylRFyfbv80P3VNVabyB64Vyr2Uwo8PQUmvw4
  - name: "⚡ NX 4"
    type: vless
    server: 193.124.125.191
    port: 2447
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    network: tcp
    tls: true
    flow: xtls-rprx-vision
    servername: max.ru
    client-fingerprint: edge
    reality-opts:
      public-key: 2PWetHSylRFyfbv80P3VNVabyB64Vyr2Uwo8PQUmvw4
  - name: "⚡ NX 5"
    type: vless
    server: 88.216.189.239
    port: 2087
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    network: grpc
    tls: true
    servername: potent.tuzvpn.com
    grpc-opts:
      serviceName: grpc
      mode: gun
  - name: "⚡ NX 7"
    type: vless
    server: 95.85.251.64
    port: 8446
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    network: grpc
    tls: true
    servername: fi2.nexblic.online
    grpc-opts:
      serviceName: grpc
      mode: gun
  - name: "⚡ NX 8"
    type: vless
    server: 217.60.182.13
    port: 2087
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    network: grpc
    tls: true
    servername: potent.tuzvpn.com
    grpc-opts:
      serviceName: grpc
      mode: gun
  - name: "⚡ NX 9"
    type: vless
    server: 84.32.231.110
    port: 443
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    network: tcp
    tls: true
    flow: xtls-rprx-vision
    servername: ned4.nexblic.online
    client-fingerprint: edge
    reality-opts:
      public-key: _etdRTxfgV8-8qS4vNC2lTgXCEq4PLoJUb51_UM9WgE
      short-id: c8bcb79975a85955
  - name: "⚡ NX 10"
    type: vless
    server: 193.124.125.116
    port: 2445
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    network: tcp
    tls: true
    flow: xtls-rprx-vision
    servername: max.ru
    client-fingerprint: edge
    reality-opts:
      public-key: 2PWetHSylRFyfbv80P3VNVabyB64Vyr2Uwo8PQUmvw4
  - name: "⚡ NX 11"
    type: vless
    server: 193.124.125.191
    port: 2445
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    network: tcp
    tls: true
    flow: xtls-rprx-vision
    servername: max.ru
    client-fingerprint: edge
    reality-opts:
      public-key: 2PWetHSylRFyfbv80P3VNVabyB64Vyr2Uwo8PQUmvw4
  - name: "⚡ NX 12"
    type: vless
    server: 95.85.251.64
    port: 8446
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    network: grpc
    tls: true
    servername: fi2.nexblic.online
    grpc-opts:
      serviceName: grpc
      mode: gun
  - name: "⚡ NX 13"
    type: vless
    server: 217.60.182.13
    port: 2087
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    network: grpc
    tls: true
    servername: potent.tuzvpn.com
    grpc-opts:
      serviceName: grpc
      mode: gun
  - name: "⚡ NX 14"
    type: vless
    server: 84.32.231.110
    port: 443
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    network: tcp
    tls: true
    flow: xtls-rprx-vision
    servername: ned4.nexblic.online
    client-fingerprint: edge
    reality-opts:
      public-key: _etdRTxfgV8-8qS4vNC2lTgXCEq4PLoJUb51_UM9WgE
      short-id: c8bcb79975a85955
  - name: "⚡ NX 15"
    type: vless
    server: 217.60.182.13
    port: 2087
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    network: grpc
    tls: true
    servername: potent.tuzvpn.com
    grpc-opts:
      serviceName: grpc
      mode: gun
  - name: "⚡ NX 16"
    type: vless
    server: 95.85.251.64
    port: 8446
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    network: grpc
    tls: true
    servername: fi2.nexblic.online
    grpc-opts:
      serviceName: grpc
      mode: gun
  - name: "⚡ NX 17"
    type: vless
    server: 84.32.231.110
    port: 443
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    network: tcp
    tls: true
    flow: xtls-rprx-vision
    servername: ned4.nexblic.online
    client-fingerprint: edge
    reality-opts:
      public-key: _etdRTxfgV8-8qS4vNC2lTgXCEq4PLoJUb51_UM9WgE
      short-id: c8bcb79975a85955
  - name: "⚡ NX 18"
    type: vless
    server: 158.160.160.12
    port: 8448
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    network: tcp
    tls: true
    flow: xtls-rprx-vision
    servername: ya.nexblic.online
    client-fingerprint: edge
    reality-opts:
      public-key: o1rDY5JhNUcCzAzHWd5Whd_CeTuJN8hgB0V_prgJfFE
      short-id: 6ba85179e30d4fc2
  - name: "⚡ NX 19"
    type: vless
    server: 193.124.125.116
    port: 2443
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    network: tcp
    tls: true
    flow: xtls-rprx-vision
    servername: max.ru
    client-fingerprint: edge
    reality-opts:
      public-key: 2PWetHSylRFyfbv80P3VNVabyB64Vyr2Uwo8PQUmvw4
  - name: "⚡ NX 20"
    type: vless
    server: 95.85.251.64
    port: 8446
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    network: grpc
    tls: true
    servername: fi2.nexblic.online
    grpc-opts:
      serviceName: grpc
      mode: gun
  - name: "⚡ NX 21"
    type: vless
    server: 217.60.182.13
    port: 2087
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    network: grpc
    tls: true
    servername: potent.tuzvpn.com
    grpc-opts:
      serviceName: grpc
      mode: gun
  - name: "⚡ NX 22"
    type: vless
    server: 84.32.231.110
    port: 443
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    network: tcp
    tls: true
    flow: xtls-rprx-vision
    servername: ned4.nexblic.online
    client-fingerprint: edge
    reality-opts:
      public-key: _etdRTxfgV8-8qS4vNC2lTgXCEq4PLoJUb51_UM9WgE
      short-id: c8bcb79975a85955
  - name: "⚡ NX 23"
    type: vless
    server: 144.31.148.212
    port: 443
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    network: tcp
    tls: true
    flow: xtls-rprx-vision
    servername: ger3.nexblic.online
    client-fingerprint: edge
    reality-opts:
      public-key: IiYicvR96i1hDkiX7FuZS4aZ46kl_72zJMOz7ZkeVw4
      short-id: c7e91a4f28b6d503
  - name: "⚡ NX 24"
    type: vless
    server: 158.160.160.12
    port: 2443
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    network: tcp
    tls: true
    flow: xtls-rprx-vision
    servername: ya.nexblic.online
    client-fingerprint: edge
    reality-opts:
      public-key: o1rDY5JhNUcCzAzHWd5Whd_CeTuJN8hgB0V_prgJfFE
      short-id: 6ba85179e30d4fc2
  - name: "⚡ NX 25"
    type: vless
    server: 193.124.125.116
    port: 2448
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    network: tcp
    tls: true
    flow: xtls-rprx-vision
    servername: max.ru
    client-fingerprint: edge
    reality-opts:
      public-key: 2PWetHSylRFyfbv80P3VNVabyB64Vyr2Uwo8PQUmvw4
  - name: "⚡ NX 26"
    type: vless
    server: 193.124.125.191
    port: 2448
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    network: tcp
    tls: true
    flow: xtls-rprx-vision
    servername: max.ru
    client-fingerprint: edge
    reality-opts:
      public-key: 2PWetHSylRFyfbv80P3VNVabyB64Vyr2Uwo8PQUmvw4
  - name: "⚡ NX 27"
    type: vless
    server: 95.85.251.64
    port: 8446
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    network: grpc
    tls: true
    servername: fi2.nexblic.online
    grpc-opts:
      serviceName: grpc
      mode: gun
  - name: "⚡ NX 28"
    type: vless
    server: 217.60.182.13
    port: 2087
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    network: grpc
    tls: true
    servername: potent.tuzvpn.com
    grpc-opts:
      serviceName: grpc
      mode: gun
  - name: "⚡ NX 29"
    type: vless
    server: 84.32.231.110
    port: 443
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    network: tcp
    tls: true
    flow: xtls-rprx-vision
    servername: ned4.nexblic.online
    client-fingerprint: edge
    reality-opts:
      public-key: _etdRTxfgV8-8qS4vNC2lTgXCEq4PLoJUb51_UM9WgE
      short-id: c8bcb79975a85955
  - name: Enterprise NX 30
    type: vless
    server: 84.32.231.110
    port: 443
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    network: tcp
    tls: true
    flow: xtls-rprx-vision
    servername: ned4.nexblic.online
    client-fingerprint: edge
    reality-opts:
      public-key: _etdRTxfgV8-8qS4vNC2lTgXCEq4PLoJUb51_UM9WgE
      short-id: c8bcb79975a85955
  - name: Enterprise NX 31
    type: vless
    server: 46.28.64.204
    port: 2087
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    network: grpc
    tls: true
    servername: potent.tuzvpn.com
    grpc-opts:
      serviceName: grpc
      mode: gun
  - name: Enterprise NX 32
    type: vless
    server: 217.177.34.137
    port: 2087
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    network: grpc
    tls: true
    servername: potent.tuzvpn.com
    grpc-opts:
      serviceName: grpc
      mode: gun
  - name: Enterprise NX 33
    type: vless
    server: 144.31.148.212
    port: 443
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    network: tcp
    tls: true
    flow: xtls-rprx-vision
    servername: ger3.nexblic.online
    client-fingerprint: edge
    reality-opts:
      public-key: IiYicvR96i1hDkiX7FuZS4aZ46kl_72zJMOz7ZkeVw4
      short-id: c7e91a4f28b6d503
  - name: Enterprise NX 36
    type: vless
    server: 84.32.231.110
    port: 443
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    network: tcp
    tls: true
    flow: xtls-rprx-vision
    servername: ned4.nexblic.online
    client-fingerprint: edge
    reality-opts:
      public-key: _etdRTxfgV8-8qS4vNC2lTgXCEq4PLoJUb51_UM9WgE
      short-id: c8bcb79975a85955
  - name: "⚡ NX 37"
    type: vless
    server: 193.124.125.116
    port: 2449
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    network: tcp
    tls: true
    flow: xtls-rprx-vision
    servername: max.ru
    client-fingerprint: edge
    reality-opts:
      public-key: 2PWetHSylRFyfbv80P3VNVabyB64Vyr2Uwo8PQUmvw4
  - name: "⚡ NX 39"
    type: vless
    server: 158.160.160.12
    port: 2443
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    network: tcp
    tls: true
    flow: xtls-rprx-vision
    servername: ya.nexblic.online
    client-fingerprint: edge
    reality-opts:
      public-key: o1rDY5JhNUcCzAzHWd5Whd_CeTuJN8hgB0V_prgJfFE
      short-id: 6ba85179e30d4fc2
  - name: "⚡ NX 40"
    type: vless
    server: 158.160.160.12
    port: 8445
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    network: tcp
    tls: true
    flow: xtls-rprx-vision
    servername: ya.nexblic.online
    client-fingerprint: edge
    reality-opts:
      public-key: o1rDY5JhNUcCzAzHWd5Whd_CeTuJN8hgB0V_prgJfFE
      short-id: 6ba85179e30d4fc2
  - name: "⚡ NX 41"
    type: vless
    server: 193.124.125.116
    port: 2449
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    network: tcp
    tls: true
    flow: xtls-rprx-vision
    servername: max.ru
    client-fingerprint: edge
    reality-opts:
      public-key: 2PWetHSylRFyfbv80P3VNVabyB64Vyr2Uwo8PQUmvw4
  - name: "⚡ NX 42"
    type: vless
    server: 158.160.160.12
    port: 8447
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    network: tcp
    tls: true
    flow: xtls-rprx-vision
    servername: ya.nexblic.online
    client-fingerprint: edge
    reality-opts:
      public-key: o1rDY5JhNUcCzAzHWd5Whd_CeTuJN8hgB0V_prgJfFE
      short-id: 6ba85179e30d4fc2
  - name: "⚡ NX 43"
    type: vless
    server: 158.160.160.12
    port: 8443
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    network: tcp
    tls: true
    flow: xtls-rprx-vision
    servername: ya.nexblic.online
    client-fingerprint: edge
    reality-opts:
      public-key: o1rDY5JhNUcCzAzHWd5Whd_CeTuJN8hgB0V_prgJfFE
      short-id: 6ba85179e30d4fc2
  - name: Enterprise NX 44
    type: vless
    server: 84.32.231.110
    port: 443
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    network: tcp
    tls: true
    flow: xtls-rprx-vision
    servername: ned4.nexblic.online
    client-fingerprint: edge
    reality-opts:
      public-key: _etdRTxfgV8-8qS4vNC2lTgXCEq4PLoJUb51_UM9WgE
      short-id: c8bcb79975a85955
  - name: Enterprise NX 47
    type: vless
    server: 158.255.213.238
    port: 443
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    network: tcp
    tls: true
    flow: xtls-rprx-vision
    servername: usa.tuzvpn.com
    client-fingerprint: edge
    reality-opts:
      public-key: 49Z9WX-pyvF2Foz-r8oyRSyCX2B6iT-JwbUUp86LV2U
  - name: Enterprise NX 49
    type: vless
    server: 31.210.52.199
    port: 2087
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    network: grpc
    tls: true
    servername: potent.tuzvpn.com
    grpc-opts:
      serviceName: grpc
      mode: gun
  - name: Enterprise NX 50
    type: vless
    server: lt2.h1cloud.net
    port: 25011
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    network: tcp
    tls: true
    flow: xtls-rprx-vision
    servername: www.google.com
    client-fingerprint: chrome
    reality-opts:
      public-key: ZKMdCqLnCM0HEWlnme1G6fwQmVI9-APX3BPHau1jI3Y
      short-id: a8e084a20e542573
  - name: Enterprise NX 51
    type: vless
    server: md1.h1cloud.net
    port: 25001
    uuid: 757809f1-1c85-408d-995d-648906496f19
    udp: true
    network: tcp
    tls: true
    flow: xtls-rprx-vision
    servername: www.google.com
    client-fingerprint: chrome
    reality-opts:
      public-key: IPjEYAg4Zc1TgwulsIOPYueyBHc3_QyudEV5ZPe9eio
      short-id: 8d1531718c1b26e3
  - name: "🇵🇱 Польша"
    type: vless
    server: pl.iapps.sbs
    port: 443
    uuid: 4f65191b-145b-4143-9814-00e5ba480f96
    udp: true
    network: tcp
    tls: true
    flow: xtls-rprx-vision
    servername: pl.iapps.sbs
    client-fingerprint: firefox
    reality-opts:
      public-key: _wvCK2gR70iNCupUDfmLsvtZOIMvHfg9ZBFqbHCfunI
      short-id: b6c826c38f85690e
  - name: "🇵🇱 Польша 2"
    type: vless
    server: main.iapps.sbs
    port: 2096
    uuid: 4f65191b-145b-4143-9814-00e5ba480f96
    udp: true
    network: tcp
    tls: true
    flow: xtls-rprx-vision
    servername: pl2.iapps.sbs
    client-fingerprint: firefox
    reality-opts:
      public-key: pnuL4lC83zWU2pps-n7xjEZ57UFihuu8CXRpD2G1ohM
      short-id: 4022a52a4bfebe57
  - name: "🇩🇪 Германия"
    type: vless
    server: main.iapps.sbs
    port: 8880
    uuid: 4f65191b-145b-4143-9814-00e5ba480f96
    udp: true
    network: tcp
    tls: true
    flow: xtls-rprx-vision
    servername: de.iapps.sbs
    client-fingerprint: firefox
    reality-opts:
      public-key: THLsoaSfze_QSwiphxzvGt_1JluHMxbV1H1Z1FHOZiU
      short-id: 9fa993935e441c65
  - name: "🇩🇪 Германия 2"
    type: vless
    server: de2.iapps.sbs
    port: 443
    uuid: 4f65191b-145b-4143-9814-00e5ba480f96
    udp: true
    network: tcp
    tls: true
    flow: xtls-rprx-vision
    servername: de2.iapps.sbs
    client-fingerprint: firefox
    reality-opts:
      public-key: wDbxHpiwgCMpKm9vYbCeAmlR9d8Kxuk6uS_1A6Lw4xE
      short-id: bcb9d0ab5c6269a5
  - name: "🇨🇿 Чехия"
    type: vless
    server: cz.iapps.sbs
    port: 443
    uuid: 4f65191b-145b-4143-9814-00e5ba480f96
    udp: true
    network: tcp
    tls: true
    flow: xtls-rprx-vision
    servername: cz.iapps.sbs
    client-fingerprint: firefox
    reality-opts:
      public-key: zDC-DrJmOpnMCDmJQyupmEE3WiHsPzSG7jVGmDTm2h4
      short-id: b6c826c38f85690e
  - name: "🇫🇷 Франция"
    type: vless
    server: main.iapps.sbs
    port: 2053
    uuid: 4f65191b-145b-4143-9814-00e5ba480f96
    udp: true
    network: tcp
    tls: true
    flow: xtls-rprx-vision
    servername: fr.iapps.sbs
    client-fingerprint: firefox
    reality-opts:
      public-key: V4PHqKe0p8V4s0Kge67ht2w4anX_cZxNSV45bYGu4BA
      short-id: 94a04f42566bb7ca
  - name: "🇳🇱 Нидерланды"
    type: vless
    server: nl.iapps.sbs
    port: 443
    uuid: 4f65191b-145b-4143-9814-00e5ba480f96
    udp: true
    network: tcp
    tls: true
    flow: xtls-rprx-vision
    servername: nl.iapps.sbs
    client-fingerprint: firefox
    reality-opts:
      public-key: 29qamlv7B6iBxJNsXAV97T3Ce3u1L4X0MbVkk4YF3kE
      short-id: e0cb6e9745aa4f8b
  - name: "🇳🇱 Нидерланды 2"
    type: vless
    server: main.iapps.sbs
    port: 2087
    uuid: 4f65191b-145b-4143-9814-00e5ba480f96
    udp: true
    network: tcp
    tls: true
    flow: xtls-rprx-vision
    servername: nl2.iapps.sbs
    client-fingerprint: firefox
    reality-opts:
      public-key: KSY7fkF8gzAY9eFjFGekAD59qPz-I06GA8Vkkan65Ac
      short-id: 143af67427834e10
  - name: "🇷🇺 Россия"
    type: vless
    server: ru.iapps.sbs
    port: 443
    uuid: 4f65191b-145b-4143-9814-00e5ba480f96
    udp: true
    network: tcp
    tls: true
    flow: xtls-rprx-vision
    servername: ru.iapps.sbs
    client-fingerprint: firefox
    reality-opts:
      public-key: IPHV0O25uMwLQQfytVztnF-9iW2mw-I76CoPNzFyOQc
      short-id: 6fc438fe38d5018b
  - name: "🇧🇾 Беларусь"
    type: vless
    server: by.iapps.sbs
    port: 443
    uuid: 4f65191b-145b-4143-9814-00e5ba480f96
    udp: true
    network: tcp
    tls: true
    flow: xtls-rprx-vision
    servername: by.iapps.sbs
    client-fingerprint: firefox
    reality-opts:
      public-key: ArpQ56NmCBjOYGEejjYj8pB9PyDM_FgEQ6TMySKOMx8
      short-id: 91883d70222b006d
  - name: "🇫🇮 Финляндия"
    type: vless
    server: fi.iapps.sbs
    port: 443
    uuid: 4f65191b-145b-4143-9814-00e5ba480f96
    udp: true
    network: tcp
    tls: true
    flow: xtls-rprx-vision
    servername: fi.iapps.sbs
    client-fingerprint: firefox
    reality-opts:
      public-key: 8Cu6QJUvBG0QosuGVsyA2G2Y3y5jgK8DaTwNZNJ6mXc
      short-id: be896fcf88db63e0
  - name: "🇨🇦 Канада"
    type: vless
    server: main.iapps.sbs
    port: 443
    uuid: 4f65191b-145b-4143-9814-00e5ba480f96
    udp: true
    network: tcp
    tls: true
    flow: xtls-rprx-vision
    servername: ca.iapps.sbs
    client-fingerprint: firefox
    reality-opts:
      public-key: Ejl8ZrKF4mELqxQ_lHrNV5AJI7O2EOJP8FBurZMGdnY
      short-id: 678098e22bac8a8e
  - name: "🇮🇳 Индия"
    type: vless
    server: in.iapps.sbs
    port: 443
    uuid: 4f65191b-145b-4143-9814-00e5ba480f96
    udp: true
    network: tcp
    tls: true
    flow: xtls-rprx-vision
    servername: in.iapps.sbs
    client-fingerprint: firefox
    reality-opts:
      public-key: rU6n8PIhbrFueSifm4LcyrcjdLqKYInQMfu653LS7Cc
      short-id: 213ddc36ed98af7c
  - name: "🇰🇿 Казахстан"
    type: vless
    server: main.iapps.sbs
    port: 2083
    uuid: 4f65191b-145b-4143-9814-00e5ba480f96
    udp: true
    network: tcp
    tls: true
    flow: xtls-rprx-vision
    servername: kazah.iapps.sbs
    client-fingerprint: firefox
    reality-opts:
      public-key: dYg-nojflWbpoB4MFQRy878sxe4xaTCZEh6xEP9qTVk
      short-id: bb39a8c7a263f780
  - name: "🇹🇷 Турция"
    type: vless
    server: main.iapps.sbs
    port: 4443
    uuid: 4f65191b-145b-4143-9814-00e5ba480f96
    udp: true
    network: tcp
    tls: true
    flow: xtls-rprx-vision
    servername: turk.iapps.sbs
    client-fingerprint: firefox
    reality-opts:
      public-key: 9uITYNOn-z6f_k3keV6M07TjAN-J8FKVfJJ5AO7jSlM
      short-id: cc6303b83a257b52
  - name: "🇺🇸 США"
    type: vless
    server: us.iapps.sbs
    port: 443
    uuid: 4f65191b-145b-4143-9814-00e5ba480f96
    udp: true
    network: tcp
    tls: true
    flow: xtls-rprx-vision
    servername: us.iapps.sbs
    client-fingerprint: firefox
    reality-opts:
      public-key: E72BpOBGvjxUONZbzPaAHxoqL7wvvtF4b8ScVNxZxVc
      short-id: 48f9aff751ea9ca0
  - name: "🇬🇧 Великобритания"
    type: vless
    server: uk.iapps.sbs
    port: 443
    uuid: 4f65191b-145b-4143-9814-00e5ba480f96
    udp: true
    network: tcp
    tls: true
    flow: xtls-rprx-vision
    servername: uk.iapps.sbs
    client-fingerprint: firefox
    reality-opts:
      public-key: y-KKifqqjx0Hby6TEadzGVzr3M9ifd0PwIe9TGpK-24
      short-id: 213ddc36ed98af7c
  - name: "🇸🇬 Сингапур"
    type: vless
    server: main.iapps.sbs
    port: 8443
    uuid: 4f65191b-145b-4143-9814-00e5ba480f96
    udp: true
    network: tcp
    tls: true
    flow: xtls-rprx-vision
    servername: si.iapps.sbs
    client-fingerprint: firefox
    reality-opts:
      public-key: t9YS6d9flJnGA6jRNB4Rx887ph5XyjMIrRxtfw5ZvgU
      short-id: 5db390a3cbbedfbf
  - name: "📶 LTE сервер"
    type: vless
    server: choosetime.iapps.sbs
    port: 3443
    uuid: 4f65191b-145b-4143-9814-00e5ba480f96
    udp: true
    network: tcp
    tls: true
    flow: xtls-rprx-vision
    servername: choosetime.iapps.sbs
    client-fingerprint: firefox
    reality-opts:
      public-key: EMCEP4pq5E1jxhdODv7P8FjShFYQGFn5fVsX0plzkwY
      short-id: e2b51272dadf9632
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
  - name: "♻️ Авто-выбор"
    type: url-test
    url: "https://www.gstatic.com/generate_204"
    interval: 300
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
      - "⚡ NX 1"
      - "⚡ NX 3"
      - "⚡ NX 4"
      - "⚡ NX 5"
      - "⚡ NX 7"
      - "⚡ NX 8"
      - "⚡ NX 9"
      - "⚡ NX 10"
      - "⚡ NX 11"
      - "⚡ NX 12"
      - "⚡ NX 13"
      - "⚡ NX 14"
      - "⚡ NX 15"
      - "⚡ NX 16"
      - "⚡ NX 17"
      - "⚡ NX 18"
      - "⚡ NX 19"
      - "⚡ NX 20"
      - "⚡ NX 21"
      - "⚡ NX 22"
      - "⚡ NX 23"
      - "⚡ NX 24"
      - "⚡ NX 25"
      - "⚡ NX 26"
      - "⚡ NX 27"
      - "⚡ NX 28"
      - "⚡ NX 29"
      - "Enterprise NX 30"
      - "Enterprise NX 31"
      - "Enterprise NX 32"
      - "Enterprise NX 33"
      - "Enterprise NX 36"
      - "⚡ NX 37"
      - "⚡ NX 39"
      - "⚡ NX 40"
      - "⚡ NX 41"
      - "⚡ NX 42"
      - "⚡ NX 43"
      - "Enterprise NX 44"
      - "Enterprise NX 47"
      - "Enterprise NX 49"
      - "Enterprise NX 50"
      - "Enterprise NX 51"
      - "🇵🇱 Польша"
      - "🇵🇱 Польша 2"
      - "🇩🇪 Германия"
      - "🇩🇪 Германия 2"
      - "🇨🇿 Чехия"
      - "🇫🇷 Франция"
      - "🇳🇱 Нидерланды"
      - "🇳🇱 Нидерланды 2"
      - "🇷🇺 Россия"
      - "🇧🇾 Беларусь"
      - "🇫🇮 Финляндия"
      - "🇨🇦 Канада"
      - "🇮🇳 Индия"
      - "🇰🇿 Казахстан"
      - "🇹🇷 Турция"
      - "🇺🇸 США"
      - "🇬🇧 Великобритания"
      - "🇸🇬 Сингапур"
      - "📶 LTE сервер"
rules:
  - MATCH,🚀 Каналы
"""
}
