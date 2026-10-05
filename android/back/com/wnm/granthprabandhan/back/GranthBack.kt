package com.wnm.granthprabandhan.back

import android.os.Build
import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.window.OnBackInvokedDispatcher
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback

/**
 * Hardware and gesture back stay inside the app on every phone.
 * The activity is finished only after the in-app confirm button.
 */
object GranthBack {
  @JvmField
  var allowExit = false

  private var last = 0L

  fun install(activity: ComponentActivity) {
    activity.onBackPressedDispatcher.addCallback(
      activity,
      object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
          poke(activity)
        }
      },
    )
    if (Build.VERSION.SDK_INT >= 33) {
      activity.onBackInvokedDispatcher.registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_OVERLAY) {
        poke(activity)
      }
    }
  }

  fun poke(activity: ComponentActivity) {
    if (allowExit) return
    val now = SystemClock.uptimeMillis()
    if (now - last < 280) return
    last = now
    val web = find(activity.window?.decorView) ?: return
    web.post {
      web.evaluateJavascript(
        "(function(){try{if(window.__granthBack)window.__granthBack();}catch(e){}})()",
        null,
      )
    }
  }

  fun swallowed(): Boolean = SystemClock.uptimeMillis() - last < 1500

  private fun find(view: View?): WebView? {
    if (view is WebView) return view
    if (view is ViewGroup) {
      for (index in 0 until view.childCount) {
        find(view.getChildAt(index))?.let { return it }
      }
    }
    return null
  }
}
