package com.monitoring.app.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.monitoring.app.manager.ActivityManager
import com.monitoring.app.utils.LogUtils

abstract class BaseActivity : AppCompatActivity() {
    open fun getTag(): String = javaClass.simpleName
    abstract fun initViewBinding()
    open fun registerOnClickListener() {}

    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtils.d(getTag(), "Lifecycle: onCreate called")
        super.onCreate(savedInstanceState)
        initViewBinding()
        registerOnClickListener()
        ActivityManager.addActivity(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        ActivityManager.removeActivity(this)
    }
}
