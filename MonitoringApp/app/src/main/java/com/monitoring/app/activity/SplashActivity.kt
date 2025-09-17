package com.monitoring.app.activity

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.monitoring.app.databinding.ActivitySplashBinding
import com.monitoring.app.utils.LogUtils
import com.monitoring.app.utils.PermissionUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : BaseActivity() {
    private lateinit var viewBinding: ActivitySplashBinding

    override fun initViewBinding() {
        viewBinding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            delay(1000)
            checkPermissionsAndNavigate()
        }
    }

    private fun checkPermissionsAndNavigate() {
        if (PermissionUtils.hasAllPermissions(this)) {
            navigateToMain()
        } else {
            PermissionUtils.requestPermissions(this)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PermissionUtils.CAMERA_PERMISSION_REQUEST) {
            if (PermissionUtils.hasAllPermissions(this)) {
                LogUtils.i("SplashActivity", "get permission success")
                navigateToMain()
            } else {
                LogUtils.w("SplashActivity", "get permission failed")
                navigateToMain()
            }
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
