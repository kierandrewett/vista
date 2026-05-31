package dev.drewett.vista.service

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import dev.drewett.vista.domain.NotificationItem

/**
 * Draws a transient notification card over whatever app is on screen, using a system overlay
 * window. Requires SYSTEM_ALERT_WINDOW (granted via adb appops). Auto-dismisses after a few seconds.
 */
class NotificationOverlay(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val handler = Handler(Looper.getMainLooper())
    private val density = context.resources.displayMetrics.density
    private var current: View? = null
    private val dismiss = Runnable { remove() }

    private fun dp(value: Int): Int = (value * density).toInt()

    fun show(item: NotificationItem, icon: Drawable?) {
        handler.post {
            remove()
            val view = buildCard(item, icon)
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT,
            ).apply {
                gravity = Gravity.TOP or Gravity.END
                x = dp(40)
                y = dp(40)
            }

            if (runCatching { windowManager.addView(view, params) }.isSuccess) {
                current = view
                view.alpha = 0f
                view.translationX = dp(80).toFloat()
                view.animate().alpha(1f).translationX(0f).setDuration(260).start()
                handler.postDelayed(dismiss, 5_000)
            }
        }
    }

    private fun remove() {
        handler.removeCallbacks(dismiss)
        current?.let { view ->
            view.animate().alpha(0f).translationX(dp(80).toFloat()).setDuration(200).withEndAction {
                runCatching { windowManager.removeView(view) }
            }.start()
        }
        current = null
    }

    private fun buildCard(item: NotificationItem, icon: Drawable?): View {
        val card = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(12), dp(22), dp(12))
            elevation = dp(10).toFloat()
            background = GradientDrawable().apply {
                cornerRadius = dp(22).toFloat()
                setColor(0xF21B1F2A.toInt())
            }
        }
        if (icon != null) {
            card.addView(
                ImageView(context).apply {
                    setImageDrawable(icon)
                    layoutParams = LinearLayout.LayoutParams(dp(40), dp(40)).apply { marginEnd = dp(14) }
                },
            )
        }
        val column = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
        column.addView(
            TextView(context).apply {
                text = item.title
                setTextColor(Color.WHITE)
                textSize = 16f
                maxLines = 1
                maxWidth = dp(380)
                ellipsize = android.text.TextUtils.TruncateAt.END
            },
        )
        if (item.text.isNotBlank()) {
            column.addView(
                TextView(context).apply {
                    text = item.text
                    setTextColor(0xCCFFFFFF.toInt())
                    textSize = 13f
                    maxLines = 1
                    maxWidth = dp(380)
                    ellipsize = android.text.TextUtils.TruncateAt.END
                },
            )
        }
        card.addView(column)
        return card
    }
}
