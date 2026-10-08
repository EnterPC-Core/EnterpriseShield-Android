package com.whitedns.vpn

import android.app.AppOpsManager
import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.NetworkCapabilities
import android.os.Build

/**
 * Сбор статистики по приложениям: время использования, фоновое время,
 * трафик (моб. + Wi-Fi). Без root — через UsageStatsManager + NetworkStatsManager.
 * Требует special access PACKAGE_USAGE_STATS (юзер выдаёт одним тапом).
 */
object BatteryUsageTracker {

    data class AppUsage(
        val packageName: String,
        val appName: String,
        val isSystem: Boolean,
        val foregroundMs: Long,      // время на экране
        val lastUsed: Long,          // когда последний раз использовался
        val mobileRxBytes: Long,     // мобильный приём
        val mobileTxBytes: Long,     // мобильная отправка
        val wifiRxBytes: Long,       // Wi-Fi приём
        val wifiTxBytes: Long,       // Wi-Fi отправка
    ) {
        val totalTrafficBytes: Long get() = mobileRxBytes + mobileTxBytes + wifiRxBytes + wifiTxBytes
        val backgroundScore: Int get() {
            // Оценка фонового вредительства: трафик без использования экрана
            val trafficMb = totalTrafficBytes / 1_048_576.0
            val ageHours = (System.currentTimeMillis() - lastUsed) / 3_600_000.0
            val idleTraffic = if (foregroundMs < 60_000 && trafficMb > 1) trafficMb else 0.0
            val score = (idleTraffic * 10 + (if (ageHours > 72) 15 else 0)).toInt().coerceIn(0, 100)
            return score
        }
    }

    fun hasUsageAccess(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName,
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun requestUsageAccess(context: Context) {
        context.startActivity(android.content.Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    fun collect(context: Context, sinceMs: Long = System.currentTimeMillis() - 24 * 3_600_000L): List<AppUsage> {
        if (!hasUsageAccess(context)) return emptyList()
        val pm = context.packageManager
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return emptyList()
        val nsm = context.getSystemService(Context.NETWORK_STATS_SERVICE) as? NetworkStatsManager

        val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, sinceMs, System.currentTimeMillis())
        val result = mutableListOf<AppUsage>()

        val bucketMobile = try { nsm?.querySummaryForDevice(
            NetworkCapabilities.TRANSPORT_CELLULAR, null, sinceMs, System.currentTimeMillis()
        ) } catch (_: Exception) { null }
        val bucketWifi = try { nsm?.querySummaryForDevice(
            NetworkCapabilities.TRANSPORT_WIFI, null, sinceMs, System.currentTimeMillis()
        ) } catch (_: Exception) { null }

        // Собираем per-app трафик через querySummary (нужен uid)
        val uidTraffic = mutableMapOf<Int, LongArray>() // uid -> [rxMob, txMob, rxWifi, txWifi]
        if (nsm != null) {
            try {
                val mobile = nsm.querySummary(
                    NetworkCapabilities.TRANSPORT_CELLULAR, null, sinceMs, System.currentTimeMillis()
                )
                while (mobile.hasNextBucket()) {
                    val b = NetworkStats.Bucket()
                    if (!mobile.getNextBucket(b)) break
                    val arr = uidTraffic.getOrPut(b.uid) { LongArray(4) }
                    arr[0] += b.rxBytes; arr[1] += b.txBytes
                }
                mobile.close()
            } catch (_: Exception) { }
            try {
                val wifi = nsm.querySummary(
                    NetworkCapabilities.TRANSPORT_WIFI, null, sinceMs, System.currentTimeMillis()
                )
                while (wifi.hasNextBucket()) {
                    val b = NetworkStats.Bucket()
                    if (!wifi.getNextBucket(b)) break
                    val arr = uidTraffic.getOrPut(b.uid) { LongArray(4) }
                    arr[2] += b.rxBytes; arr[3] += b.txBytes
                }
                wifi.close()
            } catch (_: Exception) { }
        }

        for (stat in stats) {
            val pkg = stat.packageName
            if (pkg == context.packageName) continue
            val foregroundMs = stat.totalTimeInForeground
            if (foregroundMs <= 0) continue
            val appInfo: ApplicationInfo = try {
                pm.getApplicationInfo(pkg, 0)
            } catch (_: Exception) {
                continue
            }
            val traffic = uidTraffic[appInfo.uid] ?: LongArray(4)
            result.add(
                AppUsage(
                    packageName = pkg,
                    appName = appInfo.loadLabel(pm)?.toString() ?: pkg,
                    isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                    foregroundMs = foregroundMs,
                    lastUsed = stat.lastTimeUsed,
                    mobileRxBytes = traffic[0],
                    mobileTxBytes = traffic[1],
                    wifiRxBytes = traffic[2],
                    wifiTxBytes = traffic[3],
                )
            )
        }
        return result.sortedByDescending { it.foregroundMs }
    }

    fun formatDuration(ms: Long): String {
        val hours = ms / 3_600_000
        val minutes = (ms % 3_600_000) / 60_000
        return when {
            hours > 0 -> "${hours}ч ${minutes}м"
            minutes > 0 -> "${minutes}м"
            else -> "${ms / 1000}с"
        }
    }

    fun formatBytes(bytes: Long): String {
        return when {
            bytes >= 1_073_741_824 -> "%.1f GB".format(bytes / 1_073_741_824.0)
            bytes >= 1_048_576 -> "%.1f MB".format(bytes / 1_048_576.0)
            bytes >= 1_024 -> "%.0f KB".format(bytes / 1_024.0)
            else -> "$bytes B"
        }
    }
}
