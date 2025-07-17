package com.monitoring.app.utils

import com.monitoring.app.MyApplication

object ToastUtils {
    fun showShort(message: String) {
        android.widget.Toast.makeText(
            MyApplication.context,
            message,
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }

    fun showLong(message: String) {
        android.widget.Toast.makeText(
            MyApplication.context,
            message,
            android.widget.Toast.LENGTH_LONG
        ).show()
    }
}
