package com.android.monitoring.handler.impl.webrtc;

import com.android.monitoring.WebRtcConnectionMap;
import com.android.monitoring.WebsocketConnectionMap;
import com.android.monitoring.constant.WebSocketMessageHeader;
import com.android.monitoring.handler.WebSocketMessageHandler;
import com.android.monitoring.helper.ObjectMapperInstance;
import com.android.monitoring.pojo.WebSocketMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebRtcHandler implements WebSocketMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(WebRtcHandler.class);

    @Override
    public WebSocketMessage handleAndResponse(ChannelHandlerContext ctx, WebSocketMessage incomingMessageFromClient) {
        // Handle WebRTC messages here
        // For now, we return an empty response
        var type = incomingMessageFromClient.getHeader();
        return switch (type) {
            case WebSocketMessageHeader.ADMIN_REQUEST_WEBRTC_START_STREAMING -> handleAdminRequestWebRtcStartStreaming(ctx, incomingMessageFromClient);
            case WebSocketMessageHeader.WEBRTC_SIGN_ON -> handleWebRtcSignOn(ctx, incomingMessageFromClient);
            case WebSocketMessageHeader.WEBRTC_OFFER -> handleWebRtc(ctx, incomingMessageFromClient);
            case WebSocketMessageHeader.WEBRTC_ANSWER -> handleWebRtc(ctx, incomingMessageFromClient);
            case WebSocketMessageHeader.WEBRTC_ICE_CANDIDATE -> handleWebRtc(ctx, incomingMessageFromClient);

            default ->  null;
        };
    }

    private WebSocketMessage handleAdminRequestWebRtcStartStreaming(ChannelHandlerContext ctx, WebSocketMessage incomingMessageFromClient) {
        String sourceConnectionId = ctx.channel().id().asLongText();
        String targetConnectionId = incomingMessageFromClient.getDestinationConnectionId();

        WebSocketMessage<String> webSocketMessage = new WebSocketMessage<>();
        webSocketMessage.setHeader(WebSocketMessageHeader.ADMIN_REQUEST_WEBRTC_START_STREAMING);
        webSocketMessage.setSourceConnectionId(sourceConnectionId);
        webSocketMessage.setDestinationConnectionId(targetConnectionId);
        webSocketMessage.setBody(sourceConnectionId);
        String webSocketMessageInJsonString = null;
        try {
            webSocketMessageInJsonString = ObjectMapperInstance.INSTANCE.getObjectMapper().writeValueAsString(webSocketMessage);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }

        Channel channel = WebsocketConnectionMap.getWebSocketClientChannelMap().get(targetConnectionId);
        channel.writeAndFlush(new TextWebSocketFrame(webSocketMessageInJsonString));

        return null;
    }

    private WebSocketMessage handleWebRtcSignOn(ChannelHandlerContext ctx, WebSocketMessage incomingMessageFromClient) {
        String sourceConnectionId = ctx.channel().id().asLongText();
        WebRtcConnectionMap.INSTANCE.addConnection(sourceConnectionId, ctx.channel());
        return null;
    }

    private WebSocketMessage handleWebRtc(ChannelHandlerContext ctx, WebSocketMessage incomingMessageFromClient) {
        String sourceConnectionId = ctx.channel().id().asLongText();
        String targetConnectionId = incomingMessageFromClient.getDestinationConnectionId();

        WebSocketMessage<Object> webSocketMessage = new WebSocketMessage<>();
        webSocketMessage.setHeader(incomingMessageFromClient.getHeader());
        webSocketMessage.setSourceConnectionId(sourceConnectionId);
        webSocketMessage.setDestinationConnectionId(targetConnectionId);
        webSocketMessage.setBody(incomingMessageFromClient.getBody());
        String webSocketMessageInJsonString = null;
        try {
            webSocketMessageInJsonString = ObjectMapperInstance.INSTANCE.getObjectMapper().writeValueAsString(webSocketMessage);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }

        /*
        if (StringUtils.isBlank(targetConnectionId) ||
                StringUtils.isNotBlank(sourceConnectionId) ||
                "mobile".equals(incomingMessageFromClient.getSourceConnectionId())
        ) {
            targetConnectionId = WebsocketConnectionMap.getClientAdminIdMap().get(sourceConnectionId);
        }

         */

        if ((incomingMessageFromClient.getHeader() == WebSocketMessageHeader.WEBRTC_ANSWER
        || incomingMessageFromClient.getHeader() == WebSocketMessageHeader.WEBRTC_ICE_CANDIDATE)
        && !"mobile".equals(incomingMessageFromClient.getSourceConnectionId())) {

        }

        targetConnectionId = WebsocketConnectionMap.getClientAdminIdMap().get(sourceConnectionId);
        if (null == targetConnectionId) {
            targetConnectionId = WebsocketConnectionMap.getAdminClientIdMap().get(sourceConnectionId);
        }

        Channel channel = WebsocketConnectionMap.getWebSocketChannel(targetConnectionId);
        if (null != channel && channel.isActive()) {
            channel.writeAndFlush(new TextWebSocketFrame(webSocketMessageInJsonString));
        } else {
            logger.error("WebRTC channel is not active for target connection ID: {}", targetConnectionId);
        }
        return null;
    }
}
