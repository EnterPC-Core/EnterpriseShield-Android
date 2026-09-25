package com.whitedns.vpn

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Global root gate: shows a non-cancelable dialog and blocks the app
 * until the device is approved via @Enterprise_vpn_bot (v1/root).
 */
object RootGate {
    private var dialog: AlertDialog? = null
    private var requested = false

    fun enforce(activity: MainActivity) {
        if (!ShieldRootPolicy.isRooted()) return
        if (activity.isFinishing || activity.isDestroyed) return
        if (dialog?.isShowing == true) return
        android.os.Handler(android.os.Looper.getMainLooper()).post { checkThenShow(activity) }
    }

    private fun checkThenShow(activity: MainActivity) {
        ShieldRootPolicy.checkAccess(activity) { ok ->
            activity.runOnUiThread {
                if (ok) {
                    dismiss()
                    return@runOnUiThread
                }
                if (activity.isFinishing || activity.isDestroyed) return@runOnUiThread
                if (dialog?.isShowing == true) return@runOnUiThread
                show(activity)
            }
        }
    }

    private fun show(activity: MainActivity) {
        val builder = MaterialAlertDialogBuilder(activity)
            .setTitle(R.string.root_blocker_title)
            .setMessage(R.string.root_blocker_message)
            .setCancelable(false)
        val btnRes = if (requested) R.string.root_blocker_check else R.string.root_blocker_request
        builder.setPositiveButton(btnRes, null)
        builder.setNegativeButton(R.string.exit_action, null)
        dialog = builder.create()
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.show()
        dialog?.getButton(DialogInterface.BUTTON_POSITIVE)?.setOnClickListener {
            if (!requested) {
                requested = true
                ShieldRootPolicy.requestPermission(activity) { sent ->
                    activity.runOnUiThread {
                        android.widget.Toast.makeText(
                            activity,
                            if (sent) R.string.root_blocker_requested else R.string.root_blocker_denied,
                            android.widget.Toast.LENGTH_LONG,
                        ).show()
                        if (sent) {
                            dialog?.getButton(DialogInterface.BUTTON_POSITIVE)?.setText(R.string.root_blocker_check)
                        }
                    }
                }
            } else {
                checkThenShow(activity)
            }
        }
        dialog?.getButton(DialogInterface.BUTTON_NEGATIVE)?.setOnClickListener {
            activity.finishAffinity()
        }
    }

    fun dismiss() {
        dialog?.dismiss()
        dialog = null
    }
}
