package com.android.monitoring.handler.impl.log;

import com.android.monitoring.WebsocketConnectionMap;
import com.android.monitoring.constant.WebSocketMessageHeader;
import com.android.monitoring.constant.WebsocketMessageConstant;
import com.android.monitoring.handler.WebSocketMessageHandler;
import com.android.monitoring.helper.ObjectMapperInstance;
import com.android.monitoring.pojo.WebSocketMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceLogHandler implements WebSocketMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(DeviceLogHandler.class);

    @Override
    public WebSocketMessage handleAndResponse(ChannelHandlerContext ctx, WebSocketMessage incomingMessageFromClient) {

        if (incomingMessageFromClient.getHeader() == WebSocketMessageHeader.ADMIN_REQUEST_CLIENT_DEVICE_START_REAL_TIME_LOGS) {
            String adminConnectionId = ctx.channel().id().asLongText();
            String clientConnectionId = WebsocketConnectionMap.getAdminClientIdMap().get(adminConnectionId);
            if (null == clientConnectionId) {
                logger.info("Client connection id is null.");
                return null;
            }
            Channel clientConnectionChannel = WebsocketConnectionMap.getWebSocketClientChannelMap().get(clientConnectionId);
            if (clientConnectionChannel != null && clientConnectionChannel.isActive()) {
                WebSocketMessage responseMessage = new WebSocketMessage();
                responseMessage.setHeader(incomingMessageFromClient.getHeader());
                responseMessage.setBody(WebsocketMessageConstant.EMPTY_BODY);

                ObjectMapper objectMapper = ObjectMapperInstance.INSTANCE.getObjectMapper();
                String jsonString;
                try {
                    jsonString = objectMapper.writeValueAsString(responseMessage);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to convert WebsocketMessage to JSON", e);
                }
                clientConnectionChannel.writeAndFlush(new TextWebSocketFrame(jsonString));
            }
            return null;
        } else {
            String clientConnectionId = ctx.channel().id().asLongText();
            String adminConnectionId = WebsocketConnectionMap.getClientAdminIdMap().get(clientConnectionId);
            if (null == adminConnectionId) {
                logger.info("Admin connection id is null.");
                return null;
            }

            for (String adminId : WebsocketConnectionMap.getWebSocketAdminChannelMap().keySet()) {
                Channel adminConnectionChannel = WebsocketConnectionMap.getWebSocketAdminChannelMap().get(adminId);

                if (adminConnectionChannel != null && adminConnectionChannel.isActive()) {
                    WebSocketMessage responseMessage = new WebSocketMessage();
                    responseMessage.setHeader(incomingMessageFromClient.getHeader());
                    responseMessage.setBody(incomingMessageFromClient.getBody());

                    ObjectMapper objectMapper = ObjectMapperInstance.INSTANCE.getObjectMapper();
                    String jsonString;
                    try {
                        jsonString = objectMapper.writeValueAsString(responseMessage);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to convert WebsocketMessage to JSON", e);
                    }
                    adminConnectionChannel.writeAndFlush(new TextWebSocketFrame(jsonString));
                }
            }

            return null;
        }
        }
}
