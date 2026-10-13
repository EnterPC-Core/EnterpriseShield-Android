package com.whitedns.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import android.widget.Toast
import java.net.InetSocketAddress

/**
 * WARP VPN-сервис: полностью отдельный от Щита.
 * ВКЛ: отключить Щит → поднять TUN → WireGuard к Cloudflare.
 * ВЫКЛ: остановить туннель → можно включить Щит.
 */
class WarpVpnService : VpnService() {

    companion object {
        const val CHANNEL_ID = "warp_vpn_channel"
        const val NOTIFICATION_ID = 20001
        const val ACTION_START = "com.whitedns.vpn.WARP_START"
        const val ACTION_STOP = "com.whitedns.vpn.WARP_STOP"

        @Volatile var isActive = false
            private set

        /** Включить WARP: отключить Щит, поднять WARP VPN. */
        fun start(context: Context) {
            if (isActive) return
            val intent = Intent(context, WarpVpnService::class.java).apply { action = ACTION_START }
            context.startService(intent)
        }

        /** Выключить WARP. */
        fun stop(context: Context) {
            val intent = Intent(context, WarpVpnService::class.java).apply { action = ACTION_STOP }
            context.startService(intent)
        }
    }

    private var tunnel: WireGuardTunnel? = null
    private var tunFd: ParcelFileDescriptor? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startWarp()
            ACTION_STOP -> stopWarp()
        }
        return START_NOT_STICKY
    }

    private fun startWarp() {
        val configs = WarpGenerator.loadConfigs(this)
        if (configs.isEmpty()) {
            Log.e("WarpVpn", "нет конфигов")
            stopSelf()
            return
        }
        val cfg = configs.first()

        try {
            // 1) отключить Щит
            runCatching {
                val stopIntent = Intent(this, WhiteDnsVpnService::class.java)
                stopService(stopIntent)
            }
            Thread.sleep(2000) // дать Щиту остановиться

            // 2) поднять TUN через VpnService.Builder
            val builder = Builder()
                .setSession("Enterprise WARP")
                .setMtu(1280)
                .addAddress(cfg.ip4, 32)
                .addDnsServer("1.1.1.1")
                .addDnsServer("1.0.0.1")
                .addRoute("0.0.0.0", 0)
                .addRoute("::", 0)
                .setBlocking(true)

            tunFd = builder.establish()
            if (tunFd == null) {
                Log.e("WarpVpn", "TUN не создан")
                stopSelf()
                return
            }

            // 3) поднять WireGuard туннель
            val endpoint = InetSocketAddress(cfg.server, cfg.port)
            val privKey = android.util.Base64.decode(cfg.privateKey, android.util.Base64.NO_WRAP)
            val peerKey = android.util.Base64.decode(cfg.publicKey, android.util.Base64.NO_WRAP)

            val wg = WireGuardTunnel(privKey, peerKey, endpoint, tunFd!!, cfg.ip4)
            wg.start()
            tunnel = wg
            isActive = true

            startForeground(NOTIFICATION_ID, createNotification("WARP подключён · ${cfg.server}"))
            Log.i("WarpVpn", "WARP запущен через ${cfg.server}:${cfg.port}")
        } catch (e: Exception) {
            Log.e("WarpVpn", "start failed", e)
            isActive = false
            runCatching { tunFd?.close() }
            stopSelf()
        }
    }

    private fun stopWarp() {
        runCatching { tunnel?.stop() }
        tunnel = null
        runCatching { tunFd?.close() }
        tunFd = null
        isActive = false
        stopForeground(true)
        stopSelf()
        Log.i("WarpVpn", "WARP остановлен")
    }

    override fun onDestroy() {
        stopWarp()
        super.onDestroy()
    }

    override fun onRevoke() {
        stopWarp()
        super.onRevoke()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "WARP VPN", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun createNotification(text: String): Notification {
        val pi = PendingIntent.getActivity(this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION") Notification.Builder(this)
        }
        return builder
            .setContentTitle("Enterprise WARP")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentIntent(pi)
            .setOngoing(true)
            .build()
    }
}
