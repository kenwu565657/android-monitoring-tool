package com.monitoring.app.manager

import com.google.gson.Gson

object GsonManager {
    val gsonInstance: Gson by lazy { Gson() }
}
