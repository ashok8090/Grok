package com.wnm.granthprabandhan

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent

import com.facebook.react.ReactActivity
import com.facebook.react.ReactActivityDelegate
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint.fabricEnabled
import com.facebook.react.defaults.DefaultReactActivityDelegate

import expo.modules.ReactActivityDelegateWrapper
import com.wnm.granthprabandhan.back.GranthBack

class MainActivity : ReactActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    setTheme(R.style.AppTheme)
    super.onCreate(null)
    GranthBack.install(this)
  }

  override fun onResume() {
    super.onResume()
    GranthBack.raise(this)
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
  }

  override fun dispatchKeyEvent(event: KeyEvent): Boolean {
    if (event.keyCode == KeyEvent.KEYCODE_BACK) {
      if (event.action == KeyEvent.ACTION_UP && !event.isCanceled) GranthBack.poke(this)
      return true
    }
    return super.dispatchKeyEvent(event)
  }

  override fun invokeDefaultOnBackPressed() {
    if (GranthBack.allowExit) {
      super.invokeDefaultOnBackPressed()
      return
    }
    GranthBack.poke(this)
  }

  override fun finish() {
    if (!GranthBack.allowExit) {
      GranthBack.poke(this)
      return
    }
    super.finish()
  }

  override fun finishAffinity() {
    if (!GranthBack.allowExit) {
      GranthBack.poke(this)
      return
    }
    super.finishAffinity()
  }

  override fun moveTaskToBack(nonRoot: Boolean): Boolean {
    if (!GranthBack.allowExit) {
      GranthBack.poke(this)
      return true
    }
    return super.moveTaskToBack(nonRoot)
  }

  override fun getMainComponentName(): String = "main"

  override fun createReactActivityDelegate(): ReactActivityDelegate {
    return ReactActivityDelegateWrapper(
          this,
          BuildConfig.IS_NEW_ARCHITECTURE_ENABLED,
          object : DefaultReactActivityDelegate(
              this,
              mainComponentName,
              fabricEnabled
          ){})
  }
}
