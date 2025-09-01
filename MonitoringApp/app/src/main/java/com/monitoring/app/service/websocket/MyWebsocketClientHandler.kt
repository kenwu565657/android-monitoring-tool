package com.monitoring.app.service.websocket

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.monitoring.app.model.WebSocketMessage
import com.monitoring.app.utils.JsonUtils
import com.monitoring.app.utils.LogUtils
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame
import io.netty.handler.timeout.IdleState
import io.netty.handler.timeout.IdleStateEvent

@Sharable
class MyWebsocketClientHandler private constructor() : SimpleChannelInboundHandler<TextWebSocketFrame>() {
    override fun handlerAdded(ctx: ChannelHandlerContext) {
        LogUtils.i(TAG, "new client connected: ${ctx.channel().id().asLongText()}")
        WebsocketContextHolder.setContext(ctx)
    }

    override fun channelRead0(ctx: ChannelHandlerContext, msg: TextWebSocketFrame) {
        val text = msg.text()
        if (JsonUtils.EMPTY_JSON_STRING == text) {
            LogUtils.i(TAG, "received empty message: $text")
            return
        }
        try {
            val objectMapper = Gson()
            val type = object : TypeToken<WebSocketMessage>() {}.type
            val websocketMessage: WebSocketMessage = objectMapper.fromJson(text, type)
            LogUtils.i(TAG, "received: header=${websocketMessage.header}, body=${websocketMessage.body}")

            val websocketMessageHandler = WebsocketMessageHandler.getInstance()
            val responseMessage = websocketMessageHandler.buildResponseMessage(websocketMessage)

            if (JsonUtils.EMPTY_JSON_STRING == responseMessage) {
                return
            }

            ctx.channel().write(TextWebSocketFrame(responseMessage))
        } catch (e: Exception) {
            e.printStackTrace()
            LogUtils.e(TAG, "deserialize fail: $text")
        }
    }

    override fun channelReadComplete(channelHandlerContext: ChannelHandlerContext) {
        channelHandlerContext.flush()
        channelHandlerContext.fireChannelReadComplete()
    }

    override fun channelRegistered(ctx: ChannelHandlerContext) {
        super.channelRegistered(ctx)
    }

    override fun handlerRemoved(ctx: ChannelHandlerContext) {
        WebsocketContextHolder.removeContext(ctx)
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        WebsocketContextHolder.removeContext(ctx)
        super.channelInactive(ctx)
    }

    override fun userEventTriggered(ctx: ChannelHandlerContext, evt: Any) {
        if (evt is IdleStateEvent) {
            if (evt.state() == IdleState.ALL_IDLE) {
            }
        }

        super.userEventTriggered(ctx, evt)
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace()
        LogUtils.e(TAG, "Connection error ${ctx.channel().id().asLongText()}, cause: ${cause.message}")
        // ctx.close()
    }

    companion object {
        const val TAG = "WebsocketClientHandler"

        @Volatile
        private var INSTANCE: MyWebsocketClientHandler? = null

        fun getInstance(): MyWebsocketClientHandler {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MyWebsocketClientHandler().also { INSTANCE = it }
            }
        }
    }
}
