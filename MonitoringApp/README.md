# Monitoring App

# Start
# 1. Set app to be device owner by adb
```shell
adb shell dpm set-device-owner com.monitoring.app/system.admin.MyDeviceAdminReceiver
```

# End
# 1. Unset app to be device owner by adb
```shell
adb shell dpm remove-active-admin com.monitoring.app/system.admin.MyDeviceAdminReceiver
```

# 2. Uninstall app
```shell
adb uninstall com.monitoring.app
```
