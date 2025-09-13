# Monitoring App

# Android app to monitor device usage and send data to a server.
# Features
# - remote control of device
# - remote screen mirroring
# - remote real-time logs
# - remote update
# - remote shell

# Start
# 1. Set app to be device owner by adb
```shell
adb shell dpm set-device-owner com.monitoring.app/com.monitoring.app.system.admin.MyDeviceAdminReceiver
```

# End
# 1. Unset app to be device owner by adb
```shell
adb shell dpm remove-active-admin com.monitoring.app/com.monitoring.app.system.admin.MyDeviceAdminReceiver
```

# 2. Uninstall app
```shell
adb uninstall com.monitoring.app
```
