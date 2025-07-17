package com.monitoring.app.utils

import android.util.Log

object LogUtils {
    private const val VERBOSE = 1
    private const val DEBUG = 2
    private const val INFO = 3
    private const val WARN = 4
    private const val ERROR = 5

    private var logLevel = VERBOSE

    fun v(tag: String, message: String) {
        if (logLevel <= VERBOSE) {
            Log.v(tag, message)
        }
    }

    fun d(tag: String, message: String) {
        if (logLevel <= DEBUG) {
            Log.d(tag, message)
        }
    }

    fun i(tag: String, message: String) {
        if (logLevel <= INFO) {
            Log.i(tag, message)
        }
    }

    fun w(tag: String, message: String) {
        if (logLevel <= WARN) {
            Log.w(tag, message)
        }
    }

    fun e(tag: String, message: String) {
        if (logLevel <= ERROR) {
            Log.e(tag, message)
        }
    }
}