package com.android.monitoring.handler;

import com.android.monitoring.pojo.WebSocketMessage;
import io.netty.channel.ChannelHandlerContext;

public class EmptyHandler implements WebSocketMessageHandler {
    @Override
    public WebSocketMessage handleAndResponse(ChannelHandlerContext ctx, WebSocketMessage incomingMessageFromClient) {
        return null;
    }
}
