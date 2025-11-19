package com.monitoring.app

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import org.webrtc.PeerConnectionFactory


class MyApplication : Application() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        createPeerConnectionInitializationOption()
    }

    private fun createPeerConnectionInitializationOption() {
        val options = PeerConnectionFactory
            .InitializationOptions
            .builder(this)
            .setEnableInternalTracer(true)
            .setFieldTrials("WebRTC-H264HighProfile/Enabled/")
            .createInitializationOptions()

        PeerConnectionFactory.initialize(options)
    }

}
