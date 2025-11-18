package com.android.monitoring.controller;

import com.android.monitoring.constant.WebSocketMessageHeader;
import com.android.monitoring.constant.WebsocketMessageConstant;
import com.android.monitoring.dto.request.WebsocketMessageRequest;
import com.android.monitoring.helper.ObjectMapperInstance;
import com.android.monitoring.pojo.json.admin.AdminMonitoringRequestBody;
import com.android.monitoring.service.IWebsocketService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RequestMapping
@RestController
@RequiredArgsConstructor
public class WebsocketController {
    private final IWebsocketService websocketService;

    @GetMapping("/websocket/connections")
    public List<String> findAllConnectionId() {
        return websocketService.findAllConnectionId();
    }

    @GetMapping("/websocket/admin-connections")
    public List<String> findAllAdminConnectionId() {
        return websocketService.findAllAdminConnectionId();
    }

    @GetMapping("/websocket/client-admin-connections")
    public Map<String, String> findAllClientAdminConnectionIdPair() {
        return websocketService.findAllClientAdminConnectionPair();
    }

    @GetMapping("/websocket/admin-client-connections")
    public Map<String, String> findAllAdminClientConnectionIdPair() {
        return websocketService.findAllAdminClientConnectionPair();
    }

    @PostMapping("{connectionId}/reboot")
    public void reboot(@PathVariable(value = "connectionId") String connectionId) {
        websocketService.send(
                connectionId,
                WebSocketMessageHeader.ADMIN_REQUEST_CLIENT_DEVICE_REBOOT,
                WebsocketMessageConstant.EMPTY_BODY
        );
    }

    @PostMapping("{connectionId}/disable-status-bar")
    public void disableStatusBar(@PathVariable(value = "connectionId") String connectionId) {
        websocketService.send(
                connectionId,
                WebSocketMessageHeader.ADMIN_REQUEST_CLIENT_DEVICE_DISABLE_STATUS_BAR,
                WebsocketMessageConstant.EMPTY_BODY
        );
    }

    @PostMapping("{connectionId}/lock")
    public void lockScreen(@PathVariable(value = "connectionId") String connectionId) {
        websocketService.send(
                connectionId,
                WebSocketMessageHeader.ADMIN_REQUEST_CLIENT_DEVICE_LOCK_SCREEN,
                WebsocketMessageConstant.EMPTY_BODY
        );
    }

    @PostMapping("/websocket/message")
    public void sendMessage(@RequestBody WebsocketMessageRequest request) {
        websocketService.send(
                request.getConnectionId(),
                request.getHeader(),
                request.getBody()
        );
    }
}
