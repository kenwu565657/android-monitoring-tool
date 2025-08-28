package com.monitoring.app.system.admin

import android.annotation.SuppressLint
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import com.monitoring.app.MyApplication
import com.monitoring.app.utils.LogUtils
import androidx.core.net.toUri

class MyDevicePolicyManager {

    fun uninstall(): Boolean {
        return try {
            val context = getApplicationContext()
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val adminComponent = ComponentName(context, MyDeviceAdminReceiver::class.java)

            // Check if we have device owner privileges
            if (!dpm.isDeviceOwnerApp(context.packageName)) {
                LogUtils.e("MyDevicePolicyManager", "App is not device owner, cannot uninstall applications")
                return false
            }

            // Uninstall application using Intent for broader compatibility
            val success = uninstallPackageCompat(context, context.packageName)
            if (success) {
                LogUtils.i("MyDevicePolicyManager", "Successfully uninstalled application: $context.packageName")
            } else {
                LogUtils.e("MyDevicePolicyManager", "Failed to uninstall application: $context.packageName")
            }
            success
        } catch (e: SecurityException) {
            LogUtils.e("MyDevicePolicyManager", "No permission to uninstall application: ${e.message}")
            false
        } catch (e: Exception) {
            LogUtils.e("MyDevicePolicyManager", "Failed to uninstall application: ${e.message}")
            false
        }
    }

    private fun uninstallPackageCompat(context: Context, packageName: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_UNINSTALL_PACKAGE).apply {
                data = "package:$packageName".toUri()
                putExtra(Intent.EXTRA_RETURN_RESULT, true)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            LogUtils.e("MyDevicePolicyManager", "Uninstall compatibility method failed: ${e.message}")
            false
        }
    }

    fun reboot() {
        val context = getApplicationContext()
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        dpm.reboot(ComponentName(context, MyDeviceAdminReceiver::class.java))
    }

    fun lockScreen() {
        val context = getApplicationContext()
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        dpm.lockNow()
    }

    fun disableStatusBar() {
        val context = getApplicationContext()
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = ComponentName(context, MyDeviceAdminReceiver::class.java)
        dpm.setStatusBarDisabled(adminComponent, true)
    }

    fun getLocation(): Location? {
        val context = getApplicationContext()
        var currentLocation: Location? = null
        getCurrentLocation(context) { location ->
            if (location != null) {
                // Handle location
                println("Latitude: ${location.latitude}, Longitude: ${location.longitude}")
                currentLocation = location
            } else {
                // Handle case when location is not available
                println("Unable to get location")
            }
        }
        return currentLocation
    }

    fun getDeviceInfo(): String {
        val wifiManager = MyApplication.context.applicationContext.getSystemService(Context.WIFI_SERVICE) as android.net.wifi.WifiManager
        val macAddress = wifiManager.connectionInfo.macAddress ?: "00:00:00:00:00:00"

        val brand = android.os.Build.BRAND
        val model = android.os.Build.MODEL
        val version = android.os.Build.VERSION.RELEASE
        val sdkInt = android.os.Build.VERSION.SDK_INT
        return "{\"brand\": \"$brand\", \"model\": \"$model\", \"version\": $version, \"sdk\": $sdkInt, \"macAddress\": \"$macAddress\"}"
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation(context: Context, onResult: (Location?) -> Unit) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        when {
            isNetworkEnabled -> {
                val location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                LogUtils.i("MyDevicePolicyManager", "Network location: $location")
                onResult(location)
            }
            isGPSEnabled -> {
                val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

                if (null == location) {
                    val mockLocation = Location(LocationManager.GPS_PROVIDER).apply {
                        latitude = 37.7749
                        longitude = -122.4194
                        accuracy = 1.0f
                        time = System.currentTimeMillis()
                        elapsedRealtimeNanos = System.nanoTime()
                        altitude = 0.0
                        speed = 0.0f
                        bearing = 0.0f
                    }
                    onResult(mockLocation)
                } else {
                    LogUtils.i("MyDevicePolicyManager", "GPS location: $location")
                    onResult(location)
                }
            }
            else -> {
                LogUtils.i("MyDevicePolicyManager", "Location providers are not enabled")
                onResult(null)
            }
        }
    }

    private fun getApplicationContext(): Context {
        return MyApplication.context
    }
}