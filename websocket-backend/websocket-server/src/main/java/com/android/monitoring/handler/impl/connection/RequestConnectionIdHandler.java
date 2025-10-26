package com.android.monitoring.handler.impl.connection;

import com.android.monitoring.handler.WebSocketMessageHandler;
import com.android.monitoring.pojo.WebSocketMessage;
import io.netty.channel.ChannelHandlerContext;

public class RequestConnectionIdHandler implements WebSocketMessageHandler {

    @Override
    public WebSocketMessage handleAndResponse(ChannelHandlerContext ctx, WebSocketMessage incomingMessageFromClient) {
        String clientConnectionIdOnServer = ctx.channel().id().asLongText();
        WebSocketMessage<String> response = new WebSocketMessage<>();
        response.setHeader(incomingMessageFromClient.getHeader());
        response.setSourceConnectionId(clientConnectionIdOnServer);
        response.setDestinationConnectionId(clientConnectionIdOnServer);
        response.setBody(clientConnectionIdOnServer);

        return response;
    }
}
