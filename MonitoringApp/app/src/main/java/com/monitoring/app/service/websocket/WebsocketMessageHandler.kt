package com.monitoring.app.service.websocket

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.monitoring.app.model.WebSocketMessage
import com.monitoring.app.model.WebSocketMessageHeader
import com.monitoring.app.service.screen.WebRtcClient
import com.monitoring.app.system.admin.MyDevicePolicyManager
import com.monitoring.app.utils.JsonUtils
import com.monitoring.app.utils.LogUtils
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import java.io.BufferedReader
import java.io.InputStreamReader

class WebsocketMessageHandler private constructor() {
    private val gson = Gson()
    private var target: String? = null

    companion object {
        private const val TAG = "WebsocketMessageHandler"

        // Singleton
        @Volatile
        private var INSTANCE: WebsocketMessageHandler? = null

        fun getInstance(): WebsocketMessageHandler {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: WebsocketMessageHandler().also { INSTANCE = it }
            }
        }
    }

    fun buildResponseMessage(message: WebSocketMessage): String {
        LogUtils.i(TAG, "handling message with header: ${message.header} and body: ${message.body}")

        return when (message.header) {
            // connection
            WebSocketMessageHeader.REQUEST_MY_CONNECTION_ID_ON_SERVER -> saveMyConnectionIdInServer(message)

            // device info
            WebSocketMessageHeader.ADMIN_REQUEST_CLIENT_LOCATION -> getLocation()
            WebSocketMessageHeader.ADMIN_REQUEST_CLIENT_DEVICE_INFO -> handleDeviceInfo(message.body.toString())

            // logging
            WebSocketMessageHeader.ADMIN_REQUEST_CLIENT_DEVICE_START_REAL_TIME_LOGS -> handleRealTimeLogs()
            WebSocketMessageHeader.ADMIN_REQUEST_CLIENT_DEVICE_END_REAL_TIME_LOGS -> handleRealTimeLogs()

            // device control
            WebSocketMessageHeader.ADMIN_REQUEST_CLIENT_DEVICE_REBOOT -> handleReboot()
            WebSocketMessageHeader.ADMIN_REQUEST_CLIENT_DEVICE_LOCK_SCREEN -> lockScreen()
            WebSocketMessageHeader.ADMIN_REQUEST_CLIENT_DEVICE_DISABLE_STATUS_BAR -> disableStatusBar()

            // webRtc
            WebSocketMessageHeader.ADMIN_REQUEST_WEBRTC_START_STREAMING -> sendOfferToAdmin(message)
            // WebSocketMessageHeader.WEBRTC_OFFER -> handleWebRtcOffer(message.body.toString())
            WebSocketMessageHeader.WEBRTC_ANSWER -> handleWebRtcAnswer(message.body)
            WebSocketMessageHeader.WEBRTC_ICE_CANDIDATE -> handleWebRtcIceCandidate(message.body)

            else -> JsonUtils.EMPTY_JSON_STRING // Return an empty JSON object for unrecognized headers
        }
    }

    private fun saveMyConnectionIdInServer(message: WebSocketMessage): String {
        LogUtils.i(TAG, "My ConnectionId On Server: ${message.body}")
        WebsocketContextHolder.saveMyConnectionIdOnServer(message.body.toString())
        return JsonUtils.EMPTY_JSON_STRING
    }

    private fun handleReboot(): String {
        LogUtils.i(TAG, "Handling reboot command")
        MyDevicePolicyManager.reboot()

        val webSocketMessage = WebSocketMessage(
            header = WebSocketMessageHeader.ADMIN_REQUEST_CLIENT_DEVICE_REBOOT,
            destinationConnectionId = WebsocketContextHolder.getDestinationConnectionId(),
            sourceConnectionId = WebsocketContextHolder.getSourceConnectionId(),
            body = gson.toJsonTree("Reboot command executed"))
        return gson.toJson(webSocketMessage)
    }

    private fun disableStatusBar(): String {
        LogUtils.i(TAG, "Handling disable status bar command")
        MyDevicePolicyManager.disableStatusBar()

        val webSocketMessage = WebSocketMessage(
            header = WebSocketMessageHeader.ADMIN_REQUEST_CLIENT_DEVICE_DISABLE_STATUS_BAR,
            destinationConnectionId = WebsocketContextHolder.getDestinationConnectionId(),
            sourceConnectionId = WebsocketContextHolder.getSourceConnectionId(),
            body = gson.toJsonTree("Status bar disabled"))

        return gson.toJson(webSocketMessage)
    }

    private fun lockScreen(): String {
        LogUtils.i(TAG, "Handling lock screen command")
        MyDevicePolicyManager.lockScreen()

        val webSocketMessage = WebSocketMessage(
            header = WebSocketMessageHeader.ADMIN_REQUEST_CLIENT_DEVICE_LOCK_SCREEN,
            destinationConnectionId = WebsocketContextHolder.getDestinationConnectionId(),
            sourceConnectionId = WebsocketContextHolder.getSourceConnectionId(),
            body = gson.toJsonTree("Lock screen command executed"))

        return gson.toJson(webSocketMessage)
    }

    private fun handleRealTimeLogs(): String {
        LogUtils.i(TAG, "Handling real-time logs command")
        if (!isLogging) {
            WebsocketContextHolder.getContext()?.let { startLogThread(it) }
        } else {
            LogUtils.i(TAG, "Real-time logging is already active")
            stopLogThread()
        }

        val webSocketMessage = WebSocketMessage(
            header = WebSocketMessageHeader.ADMIN_REQUEST_CLIENT_DEVICE_START_REAL_TIME_LOGS,
            destinationConnectionId = WebsocketContextHolder.getDestinationConnectionId(),
            sourceConnectionId = WebsocketContextHolder.getSourceConnectionId(),
            body = gson.toJsonTree("Real-time logging started"))

        return gson.toJson(webSocketMessage)
    }

    @Volatile
    private var isLogging = false
    private var logJob: Job? = null

    private var count: Int = 0

    private fun startLogThread(ctx: ChannelHandlerContext) {
        isLogging = true
        logJob = CoroutineScope(Dispatchers.IO).launch {
            val process = ProcessBuilder("logcat").redirectErrorStream(true).start()
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String? = null
            while (isLogging && reader.readLine().also { line = it } != null) {
                val websocketMessage = line?.let {
                    WebSocketMessage(
                        header = WebSocketMessageHeader.CLIENT_DEVICE_LOG_RESPONSE,
                        destinationConnectionId = WebsocketContextHolder.getDestinationConnectionId(),
                        sourceConnectionId = WebsocketContextHolder.getSourceConnectionId(),
                        body = gson.toJsonTree(it)
                    )
                }
                if (websocketMessage == null) {
                    LogUtils.e(TAG, "Received null log line")
                    continue
                }
                val jsonString = gson.toJson(websocketMessage)
                ctx.channel().writeAndFlush(TextWebSocketFrame(jsonString))
                count++
                if (count >= 100) {
                    Thread.sleep(2000)
                    count = 0
                }
            }
            process.destroy()
        }
    }

    private fun stopLogThread() {
        isLogging = false
        logJob?.cancel()
    }

    private fun getLocation(): String {
        LogUtils.i(TAG, "Handling get location command")
        val location = MyDevicePolicyManager.getLocation()
        LogUtils.i(TAG, "Location: $location")

        val locationString = if (location != null) {
            "{\"latitude\": ${location.latitude}, \"longitude\": ${location.longitude}}"
        } else {
            JsonUtils.EMPTY_JSON_STRING
        }

        val webSocketMessage = WebSocketMessage(
            header = WebSocketMessageHeader.CLIENT_DEVICE_LOCATION_RESPONSE,
            destinationConnectionId = WebsocketContextHolder.getDestinationConnectionId(),
            sourceConnectionId = WebsocketContextHolder.getSourceConnectionId(),
            body = gson.toJsonTree(locationString)
        )

        return gson.toJson(webSocketMessage)
    }

    private fun handleDeviceInfo(body: String): String {
        LogUtils.i(TAG, "Handling device info with body: $body")
        val info = MyDevicePolicyManager.getDeviceInfo()

        val webSocketMessage = WebSocketMessage(
            header = WebSocketMessageHeader.CLIENT_DEVICE_INFO_RESPONSE,
            destinationConnectionId = WebsocketContextHolder.getDestinationConnectionId(),
            sourceConnectionId = WebsocketContextHolder.getSourceConnectionId(),
            body = gson.toJsonTree(info))

        return gson.toJson(webSocketMessage)
    }

    private fun sendOfferToAdmin(webSocketMessage: WebSocketMessage): String {
        LogUtils.i(TAG, "Handling admin request start WebRTC with message: $webSocketMessage")

        val webRtcClient = WebRtcClient.getInstance()
        val target = if (webSocketMessage.body.isJsonPrimitive) {
            webSocketMessage.body.asString
        } else {
            webSocketMessage.body.toString()
        }
        webRtcClient.createOffer(target)

        return JsonUtils.EMPTY_JSON_STRING
    }

    private fun handleWebRtcAnswer(body: JsonElement): String {
        LogUtils.i(TAG, "Handling WebRTC answer with body: $body")

        val answerSdp = if (body.isJsonPrimitive) {
            body.asString
        } else {
            body.toString()
        }

        val jsonObject = body.asJsonObject
        val sdp = jsonObject.get("sdp").asString
        val type = jsonObject.get("type").asString
        val sessionDescription = SessionDescription(SessionDescription.Type.fromCanonicalForm(type), sdp)


        val webRtcClient = WebRtcClient.getInstance()
        webRtcClient.onRemoteSessionReceived(sessionDescription)
        // webRtcClient.onRemoteSessionReceived(SessionDescription(SessionDescription.Type.ANSWER, answerSdp))
        return JsonUtils.EMPTY_JSON_STRING // Return an empty JSON object for now
    }

    private fun handleWebRtcIceCandidate(body: JsonElement): String {
        LogUtils.i(TAG, "Handling WebRTC ICE candidate with body: $body")
        val webRtcClient = WebRtcClient.getInstance()

        try {
            if (body.isJsonObject) {
                val jsonObject = body.asJsonObject

                if (jsonObject.has("candidate") && jsonObject.has("sdpMid") && jsonObject.has("sdpMLineIndex")) {
                    val candidate = jsonObject.get("candidate").asString
                    val sdpMid = jsonObject.get("sdpMid").asString
                    val sdpMLineIndex = jsonObject.get("sdpMLineIndex").asInt

                    val iceCandidate = IceCandidate(sdpMid, sdpMLineIndex, candidate)
                    LogUtils.i(TAG, "Add ice candidate: $iceCandidate")
                    webRtcClient.addIceCandidate(iceCandidate)
                } else {
                    LogUtils.e(TAG, "ice candidate missing information")
                }
            } else {
                LogUtils.e(TAG, "invalid ice candidate format")
            }
        } catch (e: Exception) {
            LogUtils.e(TAG, "error when processing ice candidate: ${e.message}")
            e.printStackTrace()
        }

        return JsonUtils.EMPTY_JSON_STRING
    }
}
