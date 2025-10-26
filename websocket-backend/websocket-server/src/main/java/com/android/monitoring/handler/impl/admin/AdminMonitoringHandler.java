package com.android.monitoring.handler.impl.admin;

import com.android.monitoring.WebsocketConnectionMap;
import com.android.monitoring.handler.WebSocketMessageHandler;
import com.android.monitoring.pojo.WebSocketMessage;
import io.netty.channel.ChannelHandlerContext;

public class AdminMonitoringHandler implements WebSocketMessageHandler {

    @Override
    public WebSocketMessage handleAndResponse(ChannelHandlerContext ctx, WebSocketMessage incomingMessageFromClient) {
        String adminConnectionId = ctx.channel().id().asLongText();
        WebsocketConnectionMap.getWebSocketClientChannelMap().remove(adminConnectionId);
        WebsocketConnectionMap.addWebSocketAdminChannel(adminConnectionId, ctx.channel());

        try {
            String clientConnectionId = incomingMessageFromClient.getBody().toString();
            WebsocketConnectionMap.map(clientConnectionId, adminConnectionId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return incomingMessageFromClient;
    }
}
