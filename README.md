# Enterprise Shield

<p align="center">
  <b>Сетевой клиент для Android: профили подключения, оптимизация маршрутов и автоматические обновления.</b>
</p>

<p align="center">
  <a href="https://github.com/EnterPC-Core/EnterpriseShield-Android/releases/latest"><img src="https://img.shields.io/github/v/release/EnterPC-Core/EnterpriseShield-Android?label=Скачать&style=for-the-badge" alt="Скачать"></a>
  <img src="https://img.shields.io/badge/license-GPL--3.0--only-blue.svg?style=for-the-badge" alt="GPL-3.0">
  <img src="https://img.shields.io/badge/Android-6.0+-3FBE90?style=for-the-badge" alt="Android 6.0+">
</p>

<p align="center">
  <a href="https://t.me/Enterprise_vpn_bot">🤖 Telegram-бот</a> ·
  <a href="https://github.com/EnterPC-Core/EnterpriseShield-Android/releases/latest">⬇️ Релизы</a>
</p>

---

## О приложении

Enterprise Shield — клиент для Android, который упрощает работу с профилями подключения: хранит подписки, выбирает маршрут для каждого типа трафика и обновляется автоматически.

## Возможности

- 🔌 **Профили подключения** — несколько готовых профилей и автоматический выбор активного
- 🧭 **Маршрутизация** — локальные ресурсы открываются напрямую, остальной трафик идёт через выбранный профиль
- 📚 **Подписки** — поддержка mihomo (YAML), Xray JSON, а также списков ссылок VLESS, VMess, Trojan, Shadowsocks, SOCKS5, WireGuard, AnyTLS, Hysteria2
- 🔄 **Автообновление** — приложение само проверяет новые версии, показывает прогресс, сверяет SHA-256 и запускает установку
- 🩺 **Диагностика** — встроенный журнал состояния подключения
- 🌓 **Темы** — светлая и тёмная
- 📱 **Совместимость** — Android 6.0+, отдельные сборки под архитектуры

## Скачать

**[⬇️ Последняя версия](https://github.com/EnterPC-Core/EnterpriseShield-Android/releases/latest)** — страница релиза со сборкой и контрольной суммой.

| Устройство | Файл | Размер |
|---|---|---|
| Современные телефоны | `EnterpriseShield-<версия>-arm64.apk` | ~42 МБ |
| Если не знаешь, что выбрать | `universal` (если опубликован в релизе) | ~107 МБ |

Рядом с APK публикуется `SHA256SUMS` — контрольная сумма для проверки файла.

> **Важно:** если установлена предыдущая версия с другой подписью, перед установкой удалите её. Обновления «поверх» работают только между сборками с одинаковой подписью.

## Подписки

Добавить свою подписку можно на экране серверов: укажите HTTPS-ссылку, приложение загрузит профили и покажет их в общем списке. Поддерживаются подписки с проверкой устройства по заголовкам `x-hwid`, `x-device-os`, `x-ver-os`, `x-device-model`.

## Сборка из исходников

```bash
git clone https://github.com/EnterPC-Core/EnterpriseShield-Android.git
cd EnterpriseShield-Android
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export ANDROID_HOME=/opt/android-sdk
./gradlew assembleRelease
```

## Поддержать проект

**💳 [Поддержать проект](https://t.tb.ru/pm_short/ABY6zKxIepI)**

Спасибо, что пользуешься Enterprise Shield 🛡
