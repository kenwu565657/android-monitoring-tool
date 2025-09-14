package lib.self.permission.manager

import androidx.fragment.app.FragmentActivity
import lib.self.permission.fragment.PermissionCallback
import lib.self.permission.fragment.RequestPermissionInvisibleFragment

object PermissionManager {
    fun request(activity: FragmentActivity, callback: PermissionCallback, vararg permissions: String) {
        val tag = RequestPermissionInvisibleFragment::class.java.simpleName
        val fragmentManager = activity.supportFragmentManager
        val existedFragment = fragmentManager.findFragmentByTag(tag)
        val fragment = if (existedFragment != null) {
            existedFragment as RequestPermissionInvisibleFragment
        } else {
            val newFragment = RequestPermissionInvisibleFragment()
            fragmentManager.beginTransaction().add(newFragment, tag).commitNow()
            newFragment
        }
        fragment.requestPermission(callback, *permissions)
    }
}
