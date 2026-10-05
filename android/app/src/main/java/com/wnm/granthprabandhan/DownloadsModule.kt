package com.wnm.granthprabandhan

import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.uimanager.ViewManager
import com.wnm.granthprabandhan.back.GranthBack
import java.io.File
import java.io.FileInputStream

class DownloadsModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
  override fun getName() = "GranthDownloads"

  @ReactMethod
  fun copyToDownloads(filePath: String, displayName: String, mime: String, promise: Promise) {
    try {
      val source = File(filePath.removePrefix("file://"))
      if (!source.exists()) {
        promise.reject("missing", "file missing")
        return
      }
      val name = displayName.ifBlank { source.name }
      val type = mime.ifBlank { "application/pdf" }
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val resolver = reactApplicationContext.contentResolver
        val values = ContentValues().apply {
          put(MediaStore.MediaColumns.DISPLAY_NAME, name)
          put(MediaStore.MediaColumns.MIME_TYPE, type)
          put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
          put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
          ?: throw IllegalStateException("downloads insert failed")
        resolver.openOutputStream(uri)?.use { output ->
          FileInputStream(source).use { input -> input.copyTo(output) }
        } ?: throw IllegalStateException("downloads stream failed")
        values.clear()
        values.put(MediaStore.MediaColumns.IS_PENDING, 0)
        resolver.update(uri, values, null, null)
        promise.resolve(uri.toString())
        return
      }
      val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
      if (!dir.exists()) dir.mkdirs()
      val dest = File(dir, name)
      source.copyTo(dest, overwrite = true)
      promise.resolve(dest.absolutePath)
    } catch (error: Exception) {
      promise.reject("download", error.message, error)
    }
  }

  @ReactMethod
  fun exitApp() {
    val activity = reactApplicationContext.currentActivity ?: return
    GranthBack.allowExit = true
    activity.runOnUiThread { activity.finishAffinity() }
  }
}

class DownloadsPackage : ReactPackage {
  override fun createNativeModules(reactContext: ReactApplicationContext): List<NativeModule> =
    listOf(DownloadsModule(reactContext))

  override fun createViewManagers(reactContext: ReactApplicationContext): List<ViewManager<*, *>> =
    emptyList()
}
