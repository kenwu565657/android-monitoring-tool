package com.monitoring.app.model

import com.google.gson.JsonElement

enum class WebSocketMessageHeader {

    // connection
    REQUEST_MY_CONNECTION_ID_ON_SERVER,

    // device info
    ADMIN_REQUEST_CLIENT_LOCATION,
    ADMIN_REQUEST_CLIENT_DEVICE_INFO,
    CLIENT_DEVICE_LOCATION_RESPONSE,
    CLIENT_DEVICE_INFO_RESPONSE,

    // device control
    ADMIN_REQUEST_CLIENT_DEVICE_REBOOT,
    ADMIN_REQUEST_CLIENT_DEVICE_DISABLE_STATUS_BAR,
    ADMIN_REQUEST_CLIENT_DEVICE_LOCK_SCREEN,

    // logging
    ADMIN_REQUEST_CLIENT_DEVICE_START_REAL_TIME_LOGS,
    ADMIN_REQUEST_CLIENT_DEVICE_END_REAL_TIME_LOGS,
    CLIENT_DEVICE_LOG_RESPONSE,

    // webRtc
    WEBRTC_SIGN_ON,
    WEBRTC_START_STREAMING,
    WEBRTC_END_STREAMING,
    ADMIN_REQUEST_WEBRTC_START_STREAMING,
    WEBRTC_OFFER,
    WEBRTC_ANSWER,
    WEBRTC_ICE_CANDIDATE
}

data class WebSocketMessage(
    val header: WebSocketMessageHeader,
    val destinationConnectionId: String? = null,
    val sourceConnectionId: String,
    val body: JsonElement
)
