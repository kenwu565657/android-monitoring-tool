package com.monitoring.app.service.websocket

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.monitoring.app.model.WebSocketMessage
import com.monitoring.app.model.WebSocketMessageHeader
import com.monitoring.app.utils.JsonUtils
import com.monitoring.app.utils.LogUtils
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame

/**
 * Save the socket connection context from this app to server.
 *
 */
object WebsocketContextHolder {
    val gson: Gson = Gson()

    @Volatile
    private var context: ChannelHandlerContext? = null

    @Synchronized
    fun setContext(ctx: ChannelHandlerContext) {
        context = ctx
    }

    @Synchronized
    fun getContext(): ChannelHandlerContext? {
        return context
    }

    @Synchronized
    fun removeContext(ctx: ChannelHandlerContext) {
        if (context == ctx) {
            context = null
        }
    }

    fun sendMessage(message: String): Boolean {
        val ctx = getContext() ?: return false
        if (ctx.channel().isActive) {
            ctx.channel().writeAndFlush(TextWebSocketFrame(message))
            return true
        }
        return false
    }

    fun sendMessage(message: WebSocketMessage): Boolean {
        val ctx = getContext() ?: return false
        if (ctx.channel().isActive) {
            val jsonMessage = gson.toJson(message)
            ctx.channel().writeAndFlush(TextWebSocketFrame(jsonMessage))
            return true
        }
        LogUtils.i(TAG, "Channel is inActive")
        return false
    }

    fun requestMyConnectionIdOnServer() {
        val webSocketMessage = WebSocketMessage(
            header = WebSocketMessageHeader.REQUEST_MY_CONNECTION_ID_ON_SERVER,
            destinationConnectionId = getSourceConnectionId(),
            sourceConnectionId = getSourceConnectionId(),
            body = gson.toJsonTree(JsonUtils.EMPTY_JSON_STRING)
        )
        sendMessage(webSocketMessage)
    }

    fun isActive(): Boolean {
        return context?.channel()?.isActive ?: false
    }

    fun getSourceConnectionId(): String {
        return myConnectionIdOnServer?: DEFAULT_SOURCE_CONNECTION_ID
    }

    fun saveMyConnectionIdOnServer(myConnectionIdOnServer: String) {
        this.myConnectionIdOnServer = myConnectionIdOnServer
    }

    fun getDestinationConnectionId(): String? {
        return getContext()?.channel()?.id()?.asLongText()
    }

    var myConnectionIdOnServer: String? = null
    const val DEFAULT_SOURCE_CONNECTION_ID = "mobile"
    const val TAG = "WebSocketContextHolder"
}
