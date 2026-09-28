package com.whitedns.vpn

import android.app.Application
import android.content.Context
import java.io.File
import java.security.SecureRandom

internal object MihomoControllerSecret {
    fun generate(random: SecureRandom = SecureRandom()): String {
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it.toInt() and 0xff) }
    }
}

class WhiteDnsApplication : Application() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(AppLocale.wrap(AppTheme.wrap(base)))
    }

    override fun onCreate() {
        super.onCreate()
        runCatching { resetStaleCoreCacheOnUpdate() }
        runCatching { CrashReporter.install(this) }
        initializeWhiteDnsTypefaces(this)
        File(filesDir, "mihomo").mkdirs()
        File(cacheDir, "mihomo").mkdirs()
        DiagnosticLogger.info(this, "mihomo.app.ready", "basePath=${filesDir.absolutePath}")
        VpnWidgetProvider.refresh(this)
        runCatching {
            contentResolver.registerContentObserver(
                android.provider.Settings.Secure.getUriFor(ALWAYS_ON_VPN_SETTING), false,
                object : android.database.ContentObserver(android.os.Handler(mainLooper)) {
                    override fun onChange(selfChange: Boolean) {
                        VpnWidgetProvider.refresh(this@WhiteDnsApplication)
                    }
                },
            )
        }.onFailure { DiagnosticLogger.warn(this, "widget.settingsObserver.failed", error = it) }
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        VpnWidgetProvider.refresh(this)
    }
    /**
     * Обновление поверх старой версии оставляет кэш ядра (cache/mihomo), который может
     * рассинхронизироваться с новой конфигурацией: соединение "показывает", но каналы
     * не работают до полного сноса приложения. Сбрасываем кэш ядра один раз при смене
     * версии — пользовательские настройки и подписки не трогаем.
     */
    private fun resetStaleCoreCacheOnUpdate() {
        val prefs = getSharedPreferences("es_meta", MODE_PRIVATE)
        val current = BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")"
        val previous = prefs.getString("last_version", null)
        if (previous != null && previous != current) {
            runCatching { File(cacheDir, "mihomo").deleteRecursively() }
            runCatching { File(cacheDir, "clash").deleteRecursively() }
            DiagnosticLogger.info(this, "app.update.cache.reset", "from=$previous to=$current")
        }
        prefs.edit().putString("last_version", current).apply()
    }

}
