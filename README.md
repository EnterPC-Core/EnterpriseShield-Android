# 🛡️ Enterprise Shield — VPN без блокировок

<p align="center">
  <b>Бесплатно. Без регистрации. Без лимитов.</b><br/>
  Работает даже там, где другие VPN отваливаются.
</p>

<p align="center">
  <a href="https://github.com/EnterPC-Core/EnterpriseShield-Android/releases/latest"><img src="https://img.shields.io/github/v/release/EnterPC-Core/EnterpriseShield-Android?label=Скачать&style=for-the-badge" alt="Скачать"></a>
  <img src="https://img.shields.io/badge/license-GPL--3.0--only-blue.svg?style=for-the-badge" alt="GPL-3.0">
  <img src="https://img.shields.io/badge/Android-6.0+-3FBE90?style=for-the-badge" alt="Android 6.0+">
</p>

---

## Что такое Enterprise Shield

VPN-клиент для Android, заточенный под реальные условия: мобильные сети с DPI-глушилками, домашний интернет с блокировками, нестабильные операторы.

Просто установи, нажми одну кнопку — и забудь о том, что что-то не открывается.

## Возможности

- 🚫 **Обход DPI** — встроенный десинхронизатор TCP (ByeByeDPI) работает на всех серверах: трафик разрезается на нестандартные куски, DPI не распознаёт VPN
- 🎭 **Маскировка** — REALITY-маски (трафик выглядит как заход на обычный сайт), WS+TLS-каналы, ECH через Cloudflare, случайный TLS-отпечаток
- ⚡ **Умный выбор** — приложение само тестирует каналы и переключается на живой и быстрый
- 🗺️ **Карта серверов** — интерактивная карта мира с локациями
- 📊 **Спидтест** — замер скорости прямо в приложении (методика Cloudflare)
- 📡 **Мониторинг сети** — проверка доступности 15 сервисов (YouTube, Telegram, Instagram, AI...)
- ✈️ **Telegram-прокси** — встроенный MTProto-прокси с автовосстановлением
- 🚫 **Без рекламы** — блокировка рекламных и трекинговых доменов, включая мусор Xiaomi/MIUI
- 🔒 **Два режима** — VPN (весь трафик) или прокси (выборочно)
- 📱 **Совместимость** — Android 6.0+, arm64 / armv7 / x86 / x86_64, отдельные сборки под каждую архитектуру

## Скачать

Самая свежая версия — в разделе **[Releases](https://github.com/EnterPC-Core/EnterpriseShield-Android/releases/latest)**.

Выбор сборки:
- `arm64-v8a` — почти все современные телефоны (если сомневаешься — бери эту)
- `armeabi-v7a` — старые 32-битные
- `x86` / `x86_64` — эмуляторы и планшеты на Intel/AMD
- `universal` — на всякий случай, подходит всем (большой размер)

Обновления приходят автоматически прямо в приложение.

## Свой сервер

Приложение работает из коробки на встроенных каналах. Можно добавить свою подписку — поддерживаются форматы mihomo (YAML), vless/vmess/trojan-ссылки и Happ/Xray JSON.

## Из исходников

```bash
git clone https://github.com/EnterPC-Core/EnterpriseShield-Android.git
cd EnterpriseShield-Android
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export ANDROID_HOME=/opt/android-sdk
./gradlew assembleRelease
```

## Поддержка

- Telegram-бот: [@Enterprise_vpn_bot](https://t.me/Enterprise_vpn_bot)
- Канал с новостями и обсуждением — ссылка в приложении (раздел «О проекте»)
- Баг-репорты: [Issues](https://github.com/EnterPC-Core/EnterpriseShield-Android/issues)

## Лицензия

[GPL-3.0-only](LICENSE)
