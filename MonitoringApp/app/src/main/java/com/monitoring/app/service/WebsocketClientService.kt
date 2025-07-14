package com.monitoring.app.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

class WebsocketClientService : Service() {

    override fun onBind(intent: Intent) : IBinder {
        TODO()
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
       return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}