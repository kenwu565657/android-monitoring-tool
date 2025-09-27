package com.android.monitoring.websocket;

import com.android.monitoring.NettyWebsocketServer;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Order(1)
@Component
@Qualifier("nettyWebsocketServerBootstrapRunner")
public class NettyWebsocketServerBootstrapRunner implements CommandLineRunner, ApplicationListener<ContextClosedEvent> {
    private static final Logger logger = LoggerFactory.getLogger(NettyWebsocketServerBootstrapRunner.class);

    @Value("${netty.websocket.port}")
    private int port;
    private ApplicationContext applicationContext;
    private Channel serverChannel;

    @Override
    public void run(String... args) throws Exception {
        logger.info("Executing ApplicationRunner: {}", this.getClass().getSimpleName());
        NettyWebsocketServer nettyWebsocketServer = new NettyWebsocketServer(port);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            try {
                nettyWebsocketServer.start();
            } catch (Exception e) {
                logger.error("Error starting Netty WebSocket server", e);
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        if (this.serverChannel != null) {
            this.serverChannel.close();
        }

        logger.info("NettyBootstrapRunner -> onApplicationEvent(): stop websocket");
    }
}