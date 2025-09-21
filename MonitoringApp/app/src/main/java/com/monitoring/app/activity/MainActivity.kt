package com.monitoring.app.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.monitoring.app.databinding.ActivityMainBinding
import com.monitoring.app.layout.GridViewAdapter
import com.monitoring.app.manager.ActivityManager
import com.monitoring.app.system.admin.MyDevicePolicyManager

class MainActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpViewBinding()
        setUpGridView()
    }

    private fun setUpViewBinding() {
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
    }

    private fun setUpGridView() {
        val gridView = viewBinding.gridView
        val adapter = GridViewAdapter(this)
        gridView.adapter = adapter
        gridView.setOnItemClickListener { parent, view, position, id ->
            handleGridItemClick(position)
        }
    }

    private fun handleGridItemClick(position: Int) {
        when (position) {
            0 -> {}
            1 -> MyDevicePolicyManager.uninstall()
            4 -> ActivityManager.switchToCameraActivity(this)
            5 -> ActivityManager.switchToShellActivity(this)
        }
    }
}
