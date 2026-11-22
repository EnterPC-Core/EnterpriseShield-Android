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

<p align="center">
  <a href="https://t.me/Enterprise_vpn_bot">🤖 Telegram-бот</a> ·
  <a href="https://github.com/EnterPC-Core/EnterpriseShield-Android/releases/latest">⬇️ Релизы</a> ·
  <a href="https://t.me/+8MRRCNZ5wOdkZjdi">💬 Чат тестирования</a>
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

**[⬇️ Скачать последнюю версию](https://github.com/EnterPC-Core/EnterpriseShield-Android/releases/latest)** — страница релиза со всеми сборками и контрольными суммами.

Прямые ссылки на актуальные APK:

| Устройство | Файл в релизе | Размер |
|---|---|---|
| Почти все современные телефоны | `EnterpriseShield-<версия>-arm64-v8a.apk` | ~42 МБ |
| Старые 32-битные телефоны | `EnterpriseShield-<версия>-armeabi-v7a.apk` | ~43 МБ |
| Планшеты и эмуляторы Intel/AMD | `EnterpriseShield-<версия>-x86_64.apk` / `-x86.apk` | ~44 МБ |
| Если не знаешь, что выбрать | `EnterpriseShield-<версия>-universal.apk` | ~107 МБ |

Рядом с APK лежит `SHA256SUMS` — контрольные суммы для проверки целостности.

Обновления приходят автоматически прямо в приложение: приложение само проверяет GitHub Releases, показывает прогресс загрузки, сверяет SHA-256 и запускает системный установщик.

## Встроенные каналы

В приложении два готовых канала:

- 🇩🇪 **Экстрим** — REALITY с набором масок (`max.ru`, `ya.ru`, `vk.com`, `ok.ru`, `mail.ru`, `sber.ru`, `rutube.ru`); если одна маска перестаёт работать, балансировщик переключает трафик на живую.
- 🇩🇪 **Enterprise LTE** — резервный канал через WebSocket + TLS на порту 443: выглядит как обычный HTTPS-трафик и помогает в мобильных сетях с жёстким DPI.

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

## Поддержать проект

Приложение бесплатное и без лимитов. Если хочется отблагодарить — вот ссылка:

**💳 [Поддержать проект](https://t.tb.ru/pm_short/ABY6zKxIepI)**

Спасибо, что пользуешься Enterprise Shield 🛡
