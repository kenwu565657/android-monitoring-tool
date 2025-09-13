package com.monitoring.app.manager

import android.content.Context
import android.content.Intent
import com.monitoring.app.activity.CameraActivity

object ActivityManager {
    private val activityStack = mutableListOf<android.app.Activity>()

    fun switchToCameraActivity(context: Context) {
        val intent = Intent(context, CameraActivity::class.java)
        context.startActivity(intent)
    }

    fun addActivity(activity: android.app.Activity) {
        activityStack.add(activity)
    }

    fun removeActivity(activity: android.app.Activity) {
        activityStack.remove(activity)
    }

    fun finishAllActivity() {
        for (activity in activityStack) {
            if (!activity.isFinishing) {
                activity.finish()
            }
        }
        activityStack.clear()
    }
}
