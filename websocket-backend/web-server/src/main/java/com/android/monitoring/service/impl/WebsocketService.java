package com.android.monitoring.service.impl;

import com.android.monitoring.WebsocketConnectionMap;
import com.android.monitoring.constant.WebSocketMessageHeader;
import com.android.monitoring.helper.ObjectMapperInstance;
import com.android.monitoring.pojo.WebSocketMessage;
import com.android.monitoring.service.IWebsocketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class WebsocketService implements IWebsocketService {
    private static final Logger logger = LoggerFactory.getLogger(WebsocketService.class);

    @Override
    public List<String> findAllConnectionId() {
        return WebsocketConnectionMap.getWebSocketClientChannelMap().keySet().stream().toList();
    }

    @Override
    public List<String> findAllAdminConnectionId() {
        return WebsocketConnectionMap.getWebSocketAdminChannelMap().keySet().stream().toList();
    }

    @Override
    public Map<String, String> findAllClientAdminConnectionPair() {
        return WebsocketConnectionMap.getClientAdminIdMap();
    }

    @Override
    public Map<String, String> findAllAdminClientConnectionPair() {
        return WebsocketConnectionMap.getAdminClientIdMap();
    }

    @Override
    public void send(String connectionId, WebSocketMessageHeader header, String body) {
        Channel channel = WebsocketConnectionMap.getWebSocketClientChannelMap().get(connectionId);
        if (null == channel) {
            logger.info("No channel found for connectionId {}", connectionId);
            return;
        }
        WebSocketMessage websocketMessage = new WebSocketMessage();
        websocketMessage.setHeader(header);
        websocketMessage.setDestinationConnectionId(connectionId);
        websocketMessage.setBody(body);
        ObjectMapper objectMapper = ObjectMapperInstance.INSTANCE.getObjectMapper();
        String jsonString;
        try {
            jsonString = objectMapper.writeValueAsString(websocketMessage);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert WebsocketMessage to JSON", e);
        }
        channel.writeAndFlush(new TextWebSocketFrame(jsonString));
    }
}
