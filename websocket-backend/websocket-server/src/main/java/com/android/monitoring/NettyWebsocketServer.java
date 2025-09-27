package com.android.monitoring;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyWebsocketServer {
    private final static Logger logger = LoggerFactory.getLogger(NettyWebsocketServer.class);
    private final int port;

    public NettyWebsocketServer(int port) {
        this.port = port;
    }

    public void start() {
        EventLoopGroup bossGroup;
        EventLoopGroup workerGroup;

        try {
            bossGroup = new NioEventLoopGroup();
            workerGroup = new NioEventLoopGroup();
        } catch (Exception e) {
            logger.error("Error starting Netty WebSocket server", e);
            throw e;
        }

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new MyNettyServerInitializer());
            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
            logger.info("Netty Websocket Server started on port {}", port);
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            logger.info("Netty Websocket Server stopped");
        }
    }
}
