package com.monitoring.app.system.admin

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context

class MyDevicePolicyManager {
    fun reboot(context: Context) {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        dpm.reboot(ComponentName(context, MyDeviceAdminReceiver::class.java))
    }
}