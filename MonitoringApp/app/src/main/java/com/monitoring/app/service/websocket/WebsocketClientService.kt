package com.monitoring.app.service.websocket

import android.app.Service
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.IBinder
import com.monitoring.app.BuildConfig
import com.monitoring.app.event.StartMonitoringEvent
import com.monitoring.app.event.StopMonitoringEvent
import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import org.greenrobot.eventbus.EventBus
import java.net.URI

class WebsocketClientService : Service() {
    companion object {
        const val CHANNEL_ID = "WebSocketServiceChannel"
    }

    private var group: EventLoopGroup? = null
    private var channel: Channel? = null

    override fun onBind(intent: Intent): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(1, createNotification())
        connectWebSocket()
    }

    override fun onDestroy() {
        channel?.close()
        group?.shutdownGracefully()
        super.onDestroy()
    }

    private fun connectWebSocket() {
        Thread {
            val uri = URI(BuildConfig.WEB_SOCKET_URL)
            val group = NioEventLoopGroup()
            try {
                val bootstrap = Bootstrap()
                bootstrap.group(group)
                    .channel(NioSocketChannel::class.java)
                    .handler(MyNettyClientInitializer())
                val channel = bootstrap.connect(uri.host, uri.port).sync().channel()
                EventBus.getDefault().post(StartMonitoringEvent(channel.id().asLongText()))
                WebsocketContextHolder.requestMyConnectionIdOnServer()
                channel.closeFuture().sync()
            } finally {
                group.shutdownGracefully()
                EventBus.getDefault().post(StopMonitoringEvent())
            }
        }.start()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "WebSocket Foreground Service",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("WebSocket Service Running")
            .setContentText("Maintaining WebSocket connection")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()
    }
}
