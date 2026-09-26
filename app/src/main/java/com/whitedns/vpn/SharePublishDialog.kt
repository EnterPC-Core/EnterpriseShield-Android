package com.whitedns.vpn

import android.app.Activity
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object SharePublishDialog {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    fun show(activity: Activity, onPublished: () -> Unit) {
        val title = EditText(activity).apply { hint = "Название раздачи" }
        val url = EditText(activity).apply { hint = "Ссылка сервера (VLESS, VMess, Trojan, HY2)"; minLines = 3 }
        val box = LinearLayout(activity).apply { orientation = LinearLayout.VERTICAL; setPadding(48, 8, 48, 8); addView(title); addView(url) }
        val dialog = MaterialAlertDialogBuilder(activity).setTitle("Добавить раздачу").setView(box)
            .setNegativeButton("Отмена", null).setPositiveButton("Опубликовать", null).create()
        dialog.setOnShowListener {
            dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                val link = url.text.toString().trim()
                if (!link.contains("://")) { url.error = "Введите ссылку сервера"; return@setOnClickListener }
                dialog.dismiss(); publish(activity, title.text.toString().trim(), link, onPublished)
            }
        }
        dialog.show()
    }
    private fun publish(activity: Activity, title: String, link: String, done: () -> Unit) = scope.launch(Dispatchers.IO) {
        val result = runCatching {
            val u = java.net.URI(link).toURL(); val proto = u.protocol.uppercase(); val host = u.host ?: ""
            val body = JSONObject().put("code", ShieldDevice.code(activity)).put("title", title.ifBlank { host }).put("url", link).put("proto", proto).put("host", host).toString().toByteArray()
            val c = URL("https://api.enterprise-shield.ru/v1/shares/publish").openConnection() as HttpURLConnection
            c.requestMethod = "POST"; c.doOutput = true; c.setRequestProperty("Content-Type", "application/json"); c.outputStream.use { it.write(body) }
            val text = (if (c.responseCode in 200..299) c.inputStream else c.errorStream).bufferedReader().use { it.readText() }
            if (c.responseCode !in 200..299) throw IllegalStateException(JSONObject(text).optString("detail", "Не удалось опубликовать"))
        }
        activity.runOnUiThread { result.onSuccess { Toast.makeText(activity, "Раздача добавлена в Топ", Toast.LENGTH_SHORT).show(); done() }.onFailure { Toast.makeText(activity, it.message ?: "Ошибка публикации", Toast.LENGTH_LONG).show() } }
    }
}
