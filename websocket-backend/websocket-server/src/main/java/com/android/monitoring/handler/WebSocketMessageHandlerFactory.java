package com.android.monitoring.handler;

import com.android.monitoring.constant.WebSocketMessageHeader;
import com.android.monitoring.handler.impl.admin.AdminMonitoringHandler;
import com.android.monitoring.handler.impl.connection.RequestConnectionIdHandler;
import com.android.monitoring.handler.impl.info.AdminQueryDeviceInfoHandler;
import com.android.monitoring.handler.impl.info.DeviceInfoHandler;
import com.android.monitoring.handler.impl.log.DeviceLogHandler;
import com.android.monitoring.handler.impl.webrtc.WebRtcHandler;
import com.android.monitoring.pojo.WebSocketMessage;

public enum WebSocketMessageHandlerFactory {
    INSTANCE;

    public WebSocketMessageHandler getWebSocketMessageHandler(WebSocketMessage incomingMessageFromClient) {
        if (null == incomingMessageFromClient) {
            return null;
        }

        return switch (incomingMessageFromClient.getHeader()) {
            // connection
            case REQUEST_MY_CONNECTION_ID_ON_SERVER -> new RequestConnectionIdHandler();

            // admin
            case WebSocketMessageHeader.ADMIN_REQUEST_MONITORING -> new AdminMonitoringHandler();

            // device info
            case WebSocketMessageHeader.ADMIN_REQUEST_CLIENT_LOCATION -> new AdminQueryDeviceInfoHandler();
            case WebSocketMessageHeader.ADMIN_REQUEST_CLIENT_DEVICE_INFO -> new AdminQueryDeviceInfoHandler();

            case WebSocketMessageHeader.CLIENT_DEVICE_LOCATION_RESPONSE -> new DeviceInfoHandler();
            case WebSocketMessageHeader.CLIENT_DEVICE_INFO_RESPONSE -> new DeviceInfoHandler();

            // device control
            case WebSocketMessageHeader.ADMIN_REQUEST_CLIENT_DEVICE_REBOOT -> new EmptyHandler();
            case WebSocketMessageHeader.ADMIN_REQUEST_CLIENT_DEVICE_DISABLE_STATUS_BAR -> new EmptyHandler();
            case WebSocketMessageHeader.ADMIN_REQUEST_CLIENT_DEVICE_LOCK_SCREEN -> new EmptyHandler();

            // logging
            case WebSocketMessageHeader.ADMIN_REQUEST_CLIENT_DEVICE_START_REAL_TIME_LOGS -> new DeviceLogHandler();
            case WebSocketMessageHeader.ADMIN_REQUEST_CLIENT_DEVICE_END_REAL_TIME_LOGS -> new DeviceLogHandler();
            case WebSocketMessageHeader.CLIENT_DEVICE_LOG_RESPONSE -> new DeviceLogHandler();

            // web rtc screen sharing
            case WebSocketMessageHeader.WEBRTC_SIGN_ON -> new WebRtcHandler();
            case WebSocketMessageHeader.WEBRTC_START_STREAMING -> new WebRtcHandler();
            case WebSocketMessageHeader.WEBRTC_END_STREAMING -> new WebRtcHandler();
            case WebSocketMessageHeader.WEBRTC_OFFER -> new WebRtcHandler();
            case WebSocketMessageHeader.WEBRTC_ANSWER -> new WebRtcHandler();
            case WebSocketMessageHeader.WEBRTC_ICE_CANDIDATE -> new WebRtcHandler();
            case ADMIN_REQUEST_WEBRTC_START_STREAMING -> new WebRtcHandler();

            // todo
            case WebSocketMessageHeader.SCREEN_MIRRORING -> new EmptyHandler();
            case WebSocketMessageHeader.REMOTE_UPDATE -> new EmptyHandler();
            case WebSocketMessageHeader.REMOTE_SHELL -> new EmptyHandler();

            // default
            case null, default -> new EmptyHandler();
        };
    }
}
