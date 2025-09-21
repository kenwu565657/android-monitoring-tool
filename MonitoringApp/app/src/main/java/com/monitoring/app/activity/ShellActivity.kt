package com.monitoring.app.activity

import com.monitoring.app.databinding.ActivityShellBinding

class ShellActivity : BaseActivity() {
    private lateinit var viewBinding: ActivityShellBinding

    override fun initViewBinding() {
        viewBinding = ActivityShellBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
    }
}
