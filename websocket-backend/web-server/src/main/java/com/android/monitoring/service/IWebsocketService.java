package com.android.monitoring.service;

import com.android.monitoring.constant.WebSocketMessageHeader;

import java.util.List;
import java.util.Map;

public interface IWebsocketService {
    List<String> findAllConnectionId();
    List<String> findAllAdminConnectionId();
    Map<String, String> findAllClientAdminConnectionPair();
    Map<String, String> findAllAdminClientConnectionPair();
    void send(String connectionId, WebSocketMessageHeader header, String body);
}
