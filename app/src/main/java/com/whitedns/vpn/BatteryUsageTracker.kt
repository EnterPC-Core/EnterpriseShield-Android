package com.whitedns.vpn

import android.app.AppOpsManager
import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.app.usage.UsageStatsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import java.io.File

object BatteryUsageTracker {

    data class AppUsage(
        val packageName: String,
        val appName: String,
        val isSystem: Boolean,
        val foregroundMs: Long,
        val lastUsed: Long,
        val mobileRxBytes: Long,
        val mobileTxBytes: Long,
        val wifiRxBytes: Long,
        val wifiTxBytes: Long,
    ) {
        val totalTrafficBytes: Long get() = mobileRxBytes + mobileTxBytes + wifiRxBytes + wifiTxBytes
        val totalMobileBytes: Long get() = mobileRxBytes + mobileTxBytes
        val backgroundScore: Int get() {
            val trafficMb = totalTrafficBytes / 1_048_576.0
            val ageHours = (System.currentTimeMillis() - lastUsed) / 3_600_000.0
            val idleTraffic = if (foregroundMs < 60_000 && trafficMb > 1) trafficMb else 0.0
            val score = (idleTraffic * 10 + (if (ageHours > 72) 15 else 0)).toInt().coerceIn(0, 100)
            return score
        }
    }

    data class BatteryInfo(
        val level: Int,
        val isCharging: Boolean,
        val isFull: Boolean,
        val status: String,
        val health: String,
        val temperature: Float,
        val voltageMv: Int,
        val technology: String,
        val chargingType: String,
        // sysfs данные (могут быть недоступны)
        val currentUa: Int,           // ток в микроамперах
        val powerW: Double,            // мощность в ваттах
        val chargeFullUah: Int,        // реальная ёмкость
        val designCapacityUah: Int,    // заводская ёмкость
        val cycleCount: Int,           // циклы зарядки
        val healthPercent: Int,        // износ аккумулятора %
        val timeRemainingMin: Int,     // время до разряда/заряда (мин)
        val timeRemainingLabel: String,// описание времени
    )

    private var batteryReceiver: BroadcastReceiver? = null
    @Volatile private var cachedBattery: BatteryInfo? = null

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

    fun startBatteryMonitor(context: Context) {
        if (batteryReceiver != null) return
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                cachedBattery = parseBattery(intent)
            }
        }
        val sticky = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(batteryReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(batteryReceiver, filter)
        }
        if (sticky != null) cachedBattery = parseBattery(sticky)
    }

    fun stopBatteryMonitor(context: Context) {
        batteryReceiver?.let { rec ->
            runCatching { context.unregisterReceiver(rec) }
        }
        batteryReceiver = null
    }

    fun getBatteryInfo(): BatteryInfo? = cachedBattery

    private fun readSysfs(path: String): Int {
        return try {
            File(path).readText().trim().toInt()
        } catch (_: Exception) { 0 }
    }

    private fun parseBattery(intent: Intent?): BatteryInfo? {
        if (intent == null) return null
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
        val pct = if (scale > 0) (level * 100 / scale) else level
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING
        val isFull = status == BatteryManager.BATTERY_STATUS_FULL
        val statusStr = when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Заряжается"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Разряжается"
            BatteryManager.BATTERY_STATUS_FULL -> "Полностью заряжен"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Не заряжается"
            else -> "Неизвестно"
        }
        val healthCode = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)
        val health = when (healthCode) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Хорошо"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Перегрев"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Не работает"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Перенапряжение"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Сбой"
            BatteryManager.BATTERY_HEALTH_COLD -> "Холодная"
            else -> "Неизвестно"
        }
        val temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10.0f
        val volt = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
        val tech = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Li-ion"
        val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
        val chargingType = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> "от сети 220В"
            BatteryManager.BATTERY_PLUGGED_USB -> "USB"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "беспроводная"
            else -> if (isCharging) "зарядка" else "батарея"
        }

        // sysfs (Xiaomi/POCO — обычно доступно без root)
        val batDir = "/sys/class/power_supply/battery"
        val currentUa = readSysfs("$batDir/current_now")
        val voltageNow = readSysfs("$batDir/voltage_now")
        val chargeFullUah = readSysfs("$batDir/charge_full")
        val designCapUah = readSysfs("$batDir/charge_full_design").let { if (it > 0) it else readSysfs("$batDir/energy_full_design") }
        val cycleCount = readSysfs("$batDir/cycle_count")

        // Расчёты
        val voltageV = (if (voltageNow > 0) voltageNow else volt * 1000) / 1_000_000.0
        val currentA = currentUa / 1_000_000.0
        val powerW = voltageV * kotlin.math.abs(currentA)

        val healthPct = if (chargeFullUah > 0 && designCapUah > 0) {
            (chargeFullUah * 100 / designCapUah).coerceIn(0, 100)
        } else 0

        // Время до разряда / заряда
        val chargeNowUah = if (chargeFullUah > 0) (chargeFullUah * pct / 100) else 0
        var timeRemainingMin = 0
        var timeLabel = "—"
        if (isCharging && currentUa > 0 && chargeFullUah > 0) {
            val remainingUah = chargeFullUah - chargeNowUah
            val mins = (remainingUah.toFloat() / currentUa * 60).toInt()
            if (mins in 1..600) {
                timeRemainingMin = mins
                timeLabel = "до полной: ${mins / 60}ч ${mins % 60}м"
            }
        } else if (!isCharging && currentUa < 0 && chargeNowUah > 0) {
            val mins = (chargeNowUah.toFloat() / kotlin.math.abs(currentUa) * 60).toInt()
            if (mins in 1..1440) {
                timeRemainingMin = mins
                timeLabel = "до разряда: ${mins / 60}ч ${mins % 60}м"
            }
        }

        return BatteryInfo(
            level = pct, isCharging = isCharging, isFull = isFull,
            status = statusStr, health = health, temperature = temp,
            voltageMv = volt, technology = tech, chargingType = chargingType,
            currentUa = currentUa, powerW = powerW,
            chargeFullUah = chargeFullUah, designCapacityUah = designCapUah,
            cycleCount = cycleCount, healthPercent = healthPct,
            timeRemainingMin = timeRemainingMin, timeRemainingLabel = timeLabel,
        )
    }

    fun collect(context: Context, sinceMs: Long = System.currentTimeMillis() - 24 * 3_600_000L): List<AppUsage> {
        if (!hasUsageAccess(context)) return emptyList()
        val pm = context.packageManager
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return emptyList()
        val nsm = context.getSystemService(Context.NETWORK_STATS_SERVICE) as? NetworkStatsManager

        val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, sinceMs, System.currentTimeMillis())
        val result = mutableListOf<AppUsage>()

        val uidTraffic = mutableMapOf<Int, LongArray>()
        if (nsm != null) {
            try {
                val mobile = nsm.querySummary(NetworkCapabilities.TRANSPORT_CELLULAR, null, sinceMs, System.currentTimeMillis())
                while (mobile.hasNextBucket()) {
                    val b = NetworkStats.Bucket()
                    if (!mobile.getNextBucket(b)) break
                    val arr = uidTraffic.getOrPut(b.uid) { LongArray(4) }
                    arr[0] += b.rxBytes; arr[1] += b.txBytes
                }
                mobile.close()
            } catch (_: Exception) { }
            try {
                val wifi = nsm.querySummary(NetworkCapabilities.TRANSPORT_WIFI, null, sinceMs, System.currentTimeMillis())
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
            } catch (_: Exception) { continue }
            val traffic = uidTraffic[appInfo.uid] ?: LongArray(4)
            result.add(AppUsage(
                packageName = pkg,
                appName = appInfo.loadLabel(pm)?.toString() ?: pkg,
                isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                foregroundMs = foregroundMs,
                lastUsed = stat.lastTimeUsed,
                mobileRxBytes = traffic[0], mobileTxBytes = traffic[1],
                wifiRxBytes = traffic[2], wifiTxBytes = traffic[3],
            ))
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
