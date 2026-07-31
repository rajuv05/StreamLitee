package com.streamlite.core

import android.util.Log

object AppLogger {

  private const val TAG = "StreamLite"

  fun info(message: String) {
    Log.i(TAG, message)
  }

  fun error(message: String, error: Throwable? = null) {
    Log.e(TAG, message, error)
  }

  fun debug(message: String) {
    Log.d(TAG, message)
  }

  fun warning(message: String) {
    Log.w(TAG, message)
  }
}