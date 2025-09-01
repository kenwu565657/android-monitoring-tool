package com.monitoring.app.service.websocket

import com.monitoring.app.BuildConfig
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelPipeline
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.DefaultHttpHeaders
import io.netty.handler.codec.http.HttpClientCodec
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler
import io.netty.handler.codec.http.websocketx.WebSocketVersion
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import io.netty.handler.timeout.IdleStateHandler
import java.net.URI
import java.util.concurrent.TimeUnit


class MyNettyClientInitializer : ChannelInitializer<SocketChannel>() {
    override fun initChannel(socketChannel: SocketChannel) {
        val uri = URI(BuildConfig.WEB_SOCKET_URL)
        val pipeline: ChannelPipeline = socketChannel.pipeline()
        val handshake = WebSocketClientHandshakerFactory.newHandshaker(
            uri, WebSocketVersion.V13, null, true, DefaultHttpHeaders()
        )

        pipeline.addLast(LoggingHandler(LogLevel.WARN))
        pipeline.addLast(IdleStateHandler(60, 0, 0, TimeUnit.SECONDS))
        pipeline.addLast(HttpClientCodec())
        pipeline.addLast(HttpObjectAggregator(8192))
        pipeline.addLast(WebSocketClientProtocolHandler(handshake))
        pipeline.addLast(MyWebsocketClientHandler.getInstance())
    }
}
