package com.whitedns.vpn

import android.content.Context

/** Тумблер «Умный выбор сервера» (фиксация лучшего при включении Щита). Default ON. */
object UniversalPinPolicy {
    private const val PREFS = "white_dns_smart_pin"
    private const val KEY = "enabled"

    fun readEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY, true)

    fun saveEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY, enabled).apply()
    }
}
