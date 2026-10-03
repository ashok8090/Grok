package com.wnm.granthprabandhan.back

import android.app.Activity
import android.os.Build
import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback

/**
 * One back path for every phone, including Vivo.
 * The system back is always consumed here, then the page stack moves inside the app.
 */
object GranthBack {
  private var last = 0L

  fun install(activity: Activity) {
    activity.onBackPressedDispatcher.addCallback(
      activity,
      object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
          step(activity)
        }
      },
    )
    if (Build.VERSION.SDK_INT >= 33) {
      activity.onBackInvokedDispatcher.registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_OVERLAY) {
        step(activity)
      }
    }
  }

  private fun step(activity: Activity) {
    val now = SystemClock.uptimeMillis()
    if (now - last < 260) return
    last = now
    val web = find(activity.window?.decorView) ?: return
    web.post {
      web.evaluateJavascript(
        "(function(){try{if(window.__granthBack)window.__granthBack();}catch(e){}})()",
        null,
      )
    }
  }

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
