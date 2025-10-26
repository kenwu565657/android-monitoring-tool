package com.android.monitoring.handler.impl.control;

import com.android.monitoring.handler.WebSocketMessageHandler;
import com.android.monitoring.pojo.WebSocketMessage;
import io.netty.channel.ChannelHandlerContext;

public class DeviceControlHandler implements WebSocketMessageHandler {
    @Override
    public WebSocketMessage handleAndResponse(ChannelHandlerContext ctx, WebSocketMessage incomingMessageFromClient) {
        return null;
    }
}
