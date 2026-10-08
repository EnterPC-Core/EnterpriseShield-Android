package com.whitedns.vpn

import android.content.Context

/**
 * Умный сон VPN: при выключенном экране N минут туннель засыпает,
 * при разблокировке — мгновенно восстанавливается.
 * Тумблер в настройках, по умолчанию выключен.
 */
object SmartSleepPolicy {
    private const val PREFS = "white_dns_smart_sleep"
    private const val KEY_ENABLED = "smart_sleep_enabled"
    private const val KEY_AUTO_RESUME = "smart_sleep_auto_resume"

    /** Сколько минут экран должен быть выключен, прежде чем VPN заснёт */
    const val SLEEP_DELAY_MINUTES = 10L

    fun readEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_ENABLED, false)
    }

    fun saveEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_ENABLED, enabled).apply()
    }

    /** Запоминает что VPN уснул сам и должен проснуться при разблокировке */
    fun readAutoResume(context: Context): Boolean {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_AUTO_RESUME, false)
    }

    fun saveAutoResume(context: Context, value: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_AUTO_RESUME, value).apply()
    }
}
