package com.android.monitoring.handler;

import com.android.monitoring.pojo.WebSocketMessage;
import io.netty.channel.ChannelHandlerContext;

public interface WebSocketMessageHandler {
    WebSocketMessage handleAndResponse(ChannelHandlerContext ctx, WebSocketMessage incomingMessageFromClient);
}
