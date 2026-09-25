package com.whitedns.vpn

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.graphics.Color
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

object SharesPageBuilder {
    private val executor = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())
    private const val TEAL = 0xFF7FD6C4.toInt()
    private const val TEXT_PRIMARY = 0xFFE8ECF4.toInt()
    private const val TEXT_SECONDARY = 0xFF8A94A6.toInt()
    private const val SURFACE = 0xFF13203C.toInt()
    private const val OUTLINE = 0xFF24406B.toInt()

    fun build(activity: Activity, dp: (Int) -> Int): View {
        val scroll = ScrollView(activity).apply { isFillViewport = true }
        val content = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(34), dp(24), dp(40))
        }
        content.addView(TextView(activity).apply {
            text = activity.getString(R.string.shares_title)
            textSize = 28f
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            setTextColor(TEXT_PRIMARY)
            includeFontPadding = false
        })
        val list = LinearLayout(activity).apply { orientation = LinearLayout.VERTICAL }
        val status = TextView(activity).apply {
            text = activity.getString(R.string.shares_loading)
            textSize = 13.5f
            setTextColor(TEXT_SECONDARY)
            setPadding(0, dp(10), 0, 0)
        }
        content.addView(status)
        content.addView(list, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(14) })
        scroll.addView(content)

        fun refresh() {
            status.text = activity.getString(R.string.shares_loading)
            list.removeAllViews()
            executor.execute {
                var error = false
                val shares = mutableListOf<JSONArray>()
                try {
                    val c = URL("https://api.enterprise-shield.ru/v1/shares").openConnection() as HttpURLConnection
                    c.connectTimeout = 8000; c.readTimeout = 8000
                    val arr = org.json.JSONObject(c.inputStream.bufferedReader().use { it.readText() })
                        .optJSONArray("shares") ?: JSONArray()
                    for (i in 0 until arr.length()) shares.add(arr.getJSONArray(i))
                } catch (e: Exception) { error = true }
                main.post {
                    if (error) {
                        status.text = activity.getString(R.string.shares_error)
                        return@post
                    }
                    if (shares.isEmpty()) {
                        status.text = activity.getString(R.string.shares_empty)
                        return@post
                    }
                    status.visibility = View.GONE
                    shares.forEachIndexed { i, s ->
                        val title = s.optString(1); val proto = s.optString(2)
                        val host = s.optString(3); val url = s.optString(4)
                        val owner = s.optString(6).ifBlank { s.optString(5) }
                        val taken = s.optInt(7); val health = s.optInt(9)
                        list.addView(shareRow(activity, dp, i + 1, title, proto, host, owner, taken, health, url))
                    }
                }
            }
        }

        content.addView(android.view.View(activity), 0)
        refresh()
        return scroll
    }

    private fun shareRow(
        activity: Activity, dp: (Int) -> Int, place: Int, title: String, proto: String,
        host: String, owner: String, taken: Int, health: Int, url: String,
    ): View {
        val card = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(12), dp(14), dp(12))
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(SURFACE); cornerRadius = dp(14).toFloat()
                setStroke(1, OUTLINE)
            }
        }
        card.addView(TextView(activity).apply {
            text = "$place · $title"
            textSize = 15.5f
            setTextColor(TEXT_PRIMARY)
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        })
        card.addView(TextView(activity).apply {
            text = "$proto · $host"
            textSize = 12.5f
            setTextColor(TEXT_SECONDARY)
            setPadding(0, dp(3), 0, 0)
        })
        card.addView(TextView(activity).apply {
            text = activity.getString(R.string.shares_meta_fmt, taken, health, owner)
            textSize = 12.5f
            setTextColor(TEAL)
            setPadding(0, dp(3), 0, 0)
        })
        val take = com.google.android.material.button.MaterialButton(activity).apply {
            text = activity.getString(R.string.shares_take)
            insetTop = 0; insetBottom = 0
            cornerRadius = dp(10)
            strokeWidth = 1
            strokeColor = android.content.res.ColorStateList.valueOf(OUTLINE)
            backgroundTintList = android.content.res.ColorStateList.valueOf(SURFACE)
            setTextColor(TEAL)
            elevation = 0f
            stateListAnimator = null
            setOnClickListener {
                val cm = activity.getSystemService(Activity.CLIPBOARD_SERVICE) as ClipboardManager
                cm.setPrimaryClip(ClipData.newPlainText("share", url))
                Toast.makeText(activity, activity.getString(R.string.shares_copied), Toast.LENGTH_SHORT).show()
            }
        }
        card.addView(take, LinearLayout.LayoutParams(-2, dp(38)).apply {
            topMargin = dp(8); gravity = Gravity.END
        })
        return card
    }
}
