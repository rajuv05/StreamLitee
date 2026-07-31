package com.streamlite.core

import android.util.Log

object AppLogger {
  private const val TAG = "StreamLite"
  fun info(message: String) = Log.i(TAG, message)
  fun error(message: String, error: Throwable? = null) = Log.e(TAG, message, error)
}
