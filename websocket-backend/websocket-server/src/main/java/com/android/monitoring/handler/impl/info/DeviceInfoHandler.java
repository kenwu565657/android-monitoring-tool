package com.android.monitoring.handler.impl.info;

import com.android.monitoring.WebsocketConnectionMap;
import com.android.monitoring.handler.WebSocketMessageHandler;
import com.android.monitoring.helper.ObjectMapperInstance;
import com.android.monitoring.pojo.WebSocketMessage;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public class DeviceInfoHandler implements WebSocketMessageHandler {

    @Override
    public WebSocketMessage handleAndResponse(ChannelHandlerContext ctx, WebSocketMessage incomingMessageFromClient) {
        String clientConnectionId = ctx.channel().id().asLongText();
        String adminConnectionId = WebsocketConnectionMap.getClientAdminIdMap().get(clientConnectionId);
        Channel adminConnectionChannel = WebsocketConnectionMap.getWebSocketAdminChannelMap().get(adminConnectionId);

        if (adminConnectionChannel != null && adminConnectionChannel.isActive()) {
            WebSocketMessage responseMessage = new WebSocketMessage();
            responseMessage.setHeader(incomingMessageFromClient.getHeader());
            responseMessage.setBody(incomingMessageFromClient.getBody());

            String jsonString;
            try {
                jsonString = ObjectMapperInstance.INSTANCE.getObjectMapper().writeValueAsString(responseMessage);
            } catch (Exception e) {
                throw new RuntimeException("Failed to convert WebsocketMessage to JSON", e);
            }
            adminConnectionChannel.writeAndFlush(new TextWebSocketFrame(jsonString));
        }
        return null;
    }
}
