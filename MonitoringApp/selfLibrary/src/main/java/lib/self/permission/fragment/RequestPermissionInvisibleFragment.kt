package lib.self.permission.fragment

import androidx.fragment.app.Fragment

typealias PermissionCallback = (granted: Boolean, permissionList: List<String>) -> Unit

class RequestPermissionInvisibleFragment : Fragment() {
    private var callback: PermissionCallback? = null

    fun requestPermission(callback: PermissionCallback, vararg permissionList: String) {
        this.callback = callback
        requestPermissions(permissionList, 1)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 1) {
            val deniedPermissions = mutableListOf<String>()
            for (i in permissions.indices) {
                if (grantResults[i] != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    deniedPermissions.add(permissions[i])
                }
            }
            callback?.invoke(deniedPermissions.isEmpty(), deniedPermissions)
        }
    }
}
