package com.android.monitoring.handler.impl.info;

import com.android.monitoring.WebsocketConnectionMap;
import com.android.monitoring.handler.WebSocketMessageHandler;
import com.android.monitoring.helper.ObjectMapperInstance;
import com.android.monitoring.pojo.WebSocketMessage;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public class AdminQueryDeviceInfoHandler implements WebSocketMessageHandler {

    @Override
    public WebSocketMessage handleAndResponse(ChannelHandlerContext ctx, WebSocketMessage incomingMessageFromClient) {
        String adminConnectionId = ctx.channel().id().asLongText();
        String clientConnectionId = WebsocketConnectionMap.getAdminClientIdMap().get(adminConnectionId);
        Channel clientConnectionChannel = WebsocketConnectionMap.getWebSocketClientChannelMap().get(clientConnectionId);

        if (clientConnectionChannel != null && clientConnectionChannel.isActive()) {
            WebSocketMessage responseMessage = new WebSocketMessage();
            responseMessage.setHeader(incomingMessageFromClient.getHeader());
            responseMessage.setBody(incomingMessageFromClient.getBody());
            String jsonString;
            try {
                jsonString = ObjectMapperInstance.INSTANCE.getObjectMapper().writeValueAsString(responseMessage);
            } catch (Exception e) {
                throw new RuntimeException("Failed to convert WebsocketMessage to JSON", e);
            }
            clientConnectionChannel.writeAndFlush(new TextWebSocketFrame(jsonString));
        }
        return null;
    }
}
