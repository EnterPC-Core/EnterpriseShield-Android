package com.whitedns.vpn

import android.app.Activity
import android.content.ClipData
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.core.text.BidiFormatter
import androidx.core.text.TextDirectionHeuristicsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat

/** Activity-owned controls; Android owns the download after this activity closes. */
internal class AppUpdateUi(
    private val activity: Activity,
    private val scope: CoroutineScope,
    private val palette: WhiteDnsPalette,
    private val showBadge: (Boolean) -> Unit,
) {
    private val manager = AppUpdateManager(activity)
    private var state: AppDownloadState = AppDownloadState.Idle
    private var polling: Job? = null
    private var resumed = false
    private var checking = false
    private var busy = false
    private var installWhenReady = false
    private var installAfterPermission = false
    private var requestedDownloadVersion: String? = null
    private var readyDialogVersion: String? = null
    private var notice: String? = null
    private var dialog: AlertDialog? = null
    private lateinit var versions: TextView
    private lateinit var status: TextView
    private lateinit var badge: TextView
    private lateinit var progress: ProgressBar
    private lateinit var checkButton: MaterialButton
    private lateinit var actionButton: MaterialButton
    private lateinit var releaseButton: MaterialButton

    fun createView(panel: LinearLayout): View {
        panel.setPadding(dp(16), dp(16), dp(16), dp(12))
        val heading = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutDirection = View.LAYOUT_DIRECTION_LOCALE
        }
        heading.addView(label(activity.getString(R.string.update_settings_title), true),
            LinearLayout.LayoutParams(0, -2, 1f))
        badge = label(activity.getString(R.string.update_badge), true).apply {
            setTextColor(palette.teal)
            contentDescription = activity.getString(R.string.update_available_title)
        }
        heading.addView(badge, LinearLayout.LayoutParams(-2, -2).apply { marginStart = dp(12) })
        panel.addView(heading)
        versions = label("")
        panel.addView(versions, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(8) })
        status = label("")
        panel.addView(status, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(6) })
        progress = ProgressBar(activity, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = 100
            progressTintList = ColorStateList.valueOf(palette.teal)
            indeterminateTintList = ColorStateList.valueOf(palette.teal)
        }
        panel.addView(progress, LinearLayout.LayoutParams(-1, dp(8)).apply { topMargin = dp(8) })
        checkButton = button(R.string.update_check) { check(manual = true) }
        actionButton = button(R.string.update_download) {
            when (state) {
                is AppDownloadState.Downloading -> cancelDownload()
                is AppDownloadState.Ready -> install()
                else -> manager.availableRelease()?.let(::download)
            }
        }
        releaseButton = button(R.string.update_view_release) {
            manager.availableRelease()?.let { release ->
                try {
                    activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(release.url)))
                } catch (error: Exception) {
                    fail("update.release.open.failed", error, R.string.update_install_failed)
                }
            }
        }
        listOf(checkButton, actionButton, releaseButton).forEach {
            panel.addView(it, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(4) })
        }
        render()
        return panel
    }

    fun onResume() {
        resumed = true
        poll()
        if (installAfterPermission) {
            installAfterPermission = false
            install()
        }
    }

    fun onPause() {
        resumed = false
        installWhenReady = false
        polling?.cancel()
        dialog?.dismiss()
        dialog = null
    }

    fun onActivityResult(requestCode: Int): Boolean {
        if (requestCode != INSTALL_PERMISSION_REQUEST) return false
        installAfterPermission = activity.packageManager.canRequestPackageInstalls()
        if (!installAfterPermission) notice = activity.getString(R.string.update_permission_denied)
        render()
        return true
    }

    fun check(manual: Boolean) {
        if (checking || busy) return
        checking = true
        if (manual) notice = null
        render()
        scope.launch {
            try {
                val release = manager.check()
                val newer = AppUpdatePolicy.isNewer(release.version, BuildConfig.VERSION_NAME)
                if (manual) notice = activity.getString(when {
                    !newer -> R.string.update_up_to_date
                    !release.downloadable -> R.string.update_not_ready
                    else -> R.string.update_available_title
                })
                val idle = state is AppDownloadState.Idle || state is AppDownloadState.Failed
                val prompt = manual || AppUpdatePolicy.shouldPrompt(
                    release.version, BuildConfig.VERSION_NAME, manager.skippedVersion(),
                )
                val pendingVersion = manager.pendingRelease()?.version
                val promptSuppressed = requestedDownloadVersion == release.version ||
                    pendingVersion == release.version ||
                    readyDialogVersion == release.version
                if (resumed && newer && release.downloadable && idle && prompt &&
                    !promptSuppressed && (manual || pendingVersion == null)) {
                    showUpdate(release)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                DiagnosticLogger.warn(activity, "update.check.failed", error = error)
                if (manual) notice = activity.getString(R.string.update_check_failed) +
                    (error.message?.let { "\n" + it } ?: "")
            } finally {
                checking = false
                render()
            }
        }
    }

    private fun showUpdate(release: AppRelease) {
        dialog?.dismiss()
        dialog = MaterialAlertDialogBuilder(activity)
            .setTitle(R.string.update_available_title)
            .setMessage(activity.getString(R.string.update_available_message, version(release.version)))
            .setNegativeButton(R.string.update_skip) { _, _ -> manager.skipVersion(release.version); dialog = null; render() }
            .setPositiveButton(R.string.update_download) { _, _ ->
                requestedDownloadVersion = release.version
                dialog = null
                download(release)
            }
            .create().also { alert ->
                alert.setOnDismissListener { if (dialog === alert) dialog = null }
                alert.show()
            }
    }

    private fun download(release: AppRelease) {
        if (busy) return
        if (!release.downloadable) {
            // Раньше кнопка просто ничего не делала — теперь объясняем причину.
            notice = activity.getString(R.string.update_not_ready)
            DiagnosticLogger.warn(
                activity, "update.download.not_ready",
                error = IllegalStateException("apk=${release.apk?.name} checksums=${release.checksums?.name}")
            )
            render()
            return
        }
        busy = true
        notice = activity.getString(R.string.update_preparing)
        render()
        scope.launch {
            try {
                manager.download(release)
                notice = null
                // Не запускаем установщик автоматически: это вызывало повторное окно
                // после возврата из системных настроек неизвестных источников.
                installWhenReady = false
                busy = false
                poll()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                requestedDownloadVersion = null
                state = AppDownloadState.Failed
                fail("update.download.failed", error, R.string.update_download_failed)
            } finally {
                busy = false
                render()
            }
        }
    }

    private fun cancelDownload() {
        if (busy) return
        installWhenReady = false
        requestedDownloadVersion = null
        busy = true
        polling?.cancel()
        scope.launch {
            try {
                manager.cancelDownload()
                state = AppDownloadState.Idle
                notice = null
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                fail("update.cancel.failed", error, R.string.update_download_failed)
            } finally {
                busy = false
                render()
            }
        }
    }

    private fun poll() {
        polling?.cancel()
        if (!resumed) return
        polling = scope.launch {
            do {
                try {
                    state = manager.refreshDownload()
                    render()
                    val readyState = state as? AppDownloadState.Ready
                    if (readyState != null && resumed &&
                        readyDialogVersion != readyState.release.version) {
                        readyDialogVersion = readyState.release.version
                        showInstallReady(readyState.release)
                    }
                    if (readyState != null && installWhenReady && resumed) {
                        installWhenReady = false
                        install()
                    }
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    state = AppDownloadState.Failed
                    fail("update.refresh.failed", error, R.string.update_download_failed)
                    notice = "Ошибка OTA: ${error.message ?: error.javaClass.simpleName}"
                }
                if (state !is AppDownloadState.Downloading && state !is AppDownloadState.Verifying) break
                delay(1_000)
            } while (resumed)
        }
    }

    private fun showInstallReady(release: AppRelease) {
        dialog?.dismiss()
        dialog = MaterialAlertDialogBuilder(activity)
            .setTitle(R.string.update_ready)
            .setMessage(activity.getString(R.string.update_ready, version(release.version)))
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.update_install) { _, _ -> install() }
            .create().also { alert ->
                alert.setOnDismissListener { if (dialog === alert) dialog = null }
                alert.show()
            }
    }

    @Suppress("DEPRECATION")
    private fun install() {
        if (!resumed || busy) return
        // Only an explicit tap, a newly completed foreground download, or a permission result enters here.
        // Returning from a cancelled installer must leave the Install button available.
        if (!activity.packageManager.canRequestPackageInstalls()) {
            try {
                activity.startActivityForResult(
                    Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${activity.packageName}")),
                    INSTALL_PERMISSION_REQUEST,
                )
            } catch (error: Exception) {
                fail("update.permission.failed", error, R.string.update_permission_denied)
            }
            return
        }
        busy = true
        notice = activity.getString(R.string.update_verifying)
        render()
        scope.launch {
            var verifiedForInstall = false
            try {
                val file = manager.apkForInstall() ?: error("No verified update available")
                verifiedForInstall = true
                if (!resumed) return@launch
                val uri = FileProvider.getUriForFile(activity, "${activity.packageName}.updates", file)
                openApkInstaller(uri)
                notice = null
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                if (!verifiedForInstall) state = AppDownloadState.Failed
                fail("update.install.failed", error, R.string.update_install_failed)
            } finally {
                busy = false
                render()
            }
        }
    }

    /** Opens Android's package installer reliably on Android 12+/OEM firmware. */
    private fun openApkInstaller(uri: Uri) {
        val install = Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            clipData = ClipData.newRawUri("Enterprise Shield update", uri)
            putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
            putExtra(Intent.EXTRA_RETURN_RESULT, false)
        }
        try {
            activity.startActivity(install)
        } catch (primary: Exception) {
            // Some vendor installers expose only ACTION_VIEW.
            val view = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                clipData = ClipData.newRawUri("Enterprise Shield update", uri)
            }
            try {
                activity.startActivity(view)
            } catch (fallback: Exception) {
                fallback.addSuppressed(primary)
                throw fallback
            }
        }
    }

    private fun render() {
        if (!::versions.isInitialized) return
        val release = manager.availableRelease()
        val available = release != null
        showBadge(available)
        badge.visibility = if (available) View.VISIBLE else View.GONE
        versions.text = activity.getString(R.string.update_installed_version, version(BuildConfig.VERSION_NAME)) +
            (release?.let { "\n" + activity.getString(R.string.update_latest_version, version(it.version)) } ?: "")
        val download = state as? AppDownloadState.Downloading
        val ready = state is AppDownloadState.Ready
        val verifying = state is AppDownloadState.Verifying
        val percent = download?.takeIf { it.total > 0 }?.let {
            (it.downloaded.toDouble() / it.total).coerceIn(0.0, 1.0)
        }
        status.text = when {
            checking -> activity.getString(R.string.update_checking)
            busy -> notice
            download?.paused == true -> activity.getString(R.string.update_download_paused)
            download != null && percent != null -> activity.getString(R.string.update_download_progress,
                NumberFormat.getPercentInstance(activity.resources.configuration.locales[0]).format(percent))
            download != null -> activity.getString(R.string.update_downloading)
            verifying -> activity.getString(R.string.update_verifying)
            notice != null -> notice
            ready -> activity.getString(R.string.update_ready,
                version((state as AppDownloadState.Ready).release.version))
            state is AppDownloadState.Failed -> notice ?: activity.getString(R.string.update_download_failed)
            release != null && !release.downloadable -> activity.getString(R.string.update_not_ready)
            else -> ""
        }
        status.visibility = if (status.text.isNullOrEmpty()) View.GONE else View.VISIBLE
        progress.visibility = if (download != null || verifying || busy || checking) View.VISIBLE else View.GONE
        progress.isIndeterminate = percent == null || verifying || busy || checking
        if (percent != null) progress.progress = (percent * 100).toInt()
        checkButton.isEnabled = !checking && !busy
        actionButton.visibility = if (download != null || ready || release?.downloadable == true) View.VISIBLE else View.GONE
        actionButton.isEnabled = !busy && !verifying && !checking
        actionButton.setText(when {
            download != null -> R.string.update_cancel_download
            ready -> R.string.update_install
            state is AppDownloadState.Failed -> R.string.update_retry
            else -> R.string.update_download
        })
        releaseButton.visibility = if (available) View.VISIBLE else View.GONE
    }

    private fun fail(event: String, error: Exception, message: Int) {
        DiagnosticLogger.warn(activity, event, error = error)
        notice = activity.getString(message)
        render()
    }

    private fun label(value: String, heading: Boolean = false) = TextView(activity).apply {
        text = value
        textSize = if (heading) 16f else 12f
        typeface = if (heading) WhiteDnsBodyBoldTypeface else WhiteDnsBodyTypeface
        setTextColor(if (heading) palette.textPrimary else palette.textSecondary)
        includeFontPadding = false
        layoutDirection = View.LAYOUT_DIRECTION_LOCALE
        textDirection = View.TEXT_DIRECTION_LOCALE
        gravity = Gravity.START
    }

    private fun button(textRes: Int, action: () -> Unit) = MaterialButton(activity).apply {
        setText(textRes)
        setAllCaps(false)
        typeface = WhiteDnsBodyBoldTypeface
        textSize = 14f
        minHeight = dp(48)
        backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
        setTextColor(palette.teal)
        rippleColor = ColorStateList.valueOf(palette.teal and 0x00ffffff or 0x18000000)
        setOnClickListener { action() }
    }

    private fun version(value: String): String = BidiFormatter.getInstance().unicodeWrap(
        AppUpdatePolicy.normalizedVersion(value), TextDirectionHeuristicsCompat.LTR,
    )

    private fun dp(value: Int) = (value * activity.resources.displayMetrics.density).toInt()

    private companion object {
        const val INSTALL_PERMISSION_REQUEST = 4308
    }
}
