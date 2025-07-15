package com.monitoring.app.service

import android.app.Service
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.IBinder
import io.netty.bootstrap.Bootstrap
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.http.*
import io.netty.handler.codec.http.websocketx.*
import io.netty.handler.ssl.SslContext
import io.netty.handler.ssl.SslContextBuilder
import java.net.URI

class WebsocketClientService : Service() {
    private val CHANNEL_ID = "WebSocketServiceChannel"
    private var group: EventLoopGroup? = null
    private var channel: Channel? = null

    override fun onBind(intent: Intent): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(1, createNotification())
        connectWebSocket("wss://your.websocket.server")
    }

    override fun onDestroy() {
        channel?.close()
        group?.shutdownGracefully()
        super.onDestroy()
    }

    private fun connectWebSocket(url: String) {
        Thread {
            try {
                val uri = URI(url)
                val scheme = uri.scheme ?: "ws"
                val host = uri.host ?: "127.0.0.1"
                val port = if (uri.port == -1) (if (scheme == "wss") 443 else 80) else uri.port

                val sslCtx: SslContext? = if (scheme == "wss") SslContextBuilder.forClient().build() else null
                group = NioEventLoopGroup()
                val handshaker = WebSocketClientHandshakerFactory.newHandshaker(
                    uri, WebSocketVersion.V13, null, true, DefaultHttpHeaders()
                )

                val handler = object : SimpleChannelInboundHandler<Any>() {
                    override fun channelActive(ctx: ChannelHandlerContext) {
                        handshaker.handshake(ctx.channel())
                    }
                    override fun channelRead0(ctx: ChannelHandlerContext, msg: Any) {
                        if (msg is FullHttpResponse) {
                            // 握手响应
                        } else if (msg is WebSocketFrame) {
                            if (msg is TextWebSocketFrame) {
                                // 收到文本消息
                            } else if (msg is CloseWebSocketFrame) {
                                ctx.channel().close()
                            }
                        }
                    }
                    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
                        ctx.close()
                    }
                }

                val b = Bootstrap()
                b.group(group)
                    .channel(NioSocketChannel::class.java)
                    .handler(object : ChannelInitializer<NioSocketChannel>() {
                        override fun initChannel(ch: NioSocketChannel) {
                            val p = ch.pipeline()
                            if (sslCtx != null) p.addLast(sslCtx.newHandler(ch.alloc(), host, port))
                            p.addLast(HttpClientCodec(), HttpObjectAggregator(8192), handler)
                        }
                    })

                channel = b.connect(host, port).sync().channel()
                channel?.closeFuture()?.sync()
            } catch (e: Exception) {
                // 连接异常处理
            }
        }.start()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "WebSocket前台服务",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("WebSocket服务运行中")
                .setContentText("正在保持 WebSocket 连接")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .build()
        } else {
            Notification.Builder(this)
                .setContentTitle("WebSocket服务运行中")
                .setContentText("正在保持 WebSocket 连接")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .build()
        }
    }
}