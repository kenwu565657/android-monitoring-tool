package com.android.monitoring;

import io.netty.channel.Channel;
import java.util.concurrent.ConcurrentHashMap;

public enum WebsocketConnectionMap {
    INSTANCE;

    private static volatile ConcurrentHashMap<String, Channel> webSocketClientChannelMap = null;
    private static volatile ConcurrentHashMap<String, Channel> webSocketAdminChannelMap = null;
    private static volatile ConcurrentHashMap<String, String> clientAdminIdMap = null;
    private static volatile ConcurrentHashMap<String, String> adminClientIdMap = null;

    private static final Object webSocketClientChannelMapLock = new Object();
    private static final Object webSocketAdminChannelMapLock = new Object();

    public static void map(String clientId, String adminId) {
        if (null == clientAdminIdMap) {
            synchronized (WebsocketConnectionMap.class) {
                if (null == clientAdminIdMap) {
                    clientAdminIdMap = new ConcurrentHashMap<>();
                }
            }
        }
        if (null == adminClientIdMap) {
            synchronized (WebsocketConnectionMap.class) {
                if (null == adminClientIdMap) {
                    adminClientIdMap = new ConcurrentHashMap<>();
                }
            }
        }
        clientAdminIdMap.put(clientId, adminId);
        adminClientIdMap.put(adminId, clientId);
    }

    public static void unmap(String clientId, String adminId) {
        if (null != clientAdminIdMap) {
            clientAdminIdMap.remove(clientId);
        }
        if (null != adminClientIdMap) {
            adminClientIdMap.remove(adminId);
        }
    }

    public static Channel getWebSocketChannel(String channelId) {
        if (getWebSocketClientChannelMap().containsKey(channelId)) {
            return getWebSocketClientChannelMap().get(channelId);
        }
        if (getWebSocketAdminChannelMap().containsKey(channelId)) {
            return  getWebSocketAdminChannelMap().get(channelId);
        }
        return null;
    }

    public static ConcurrentHashMap<String, Channel> getWebSocketClientChannelMap() {
        if (null == webSocketClientChannelMap) {
            synchronized (webSocketClientChannelMapLock) {
                if (null == webSocketClientChannelMap) {
                    webSocketClientChannelMap = new ConcurrentHashMap<>();
                }
            }
        }
        return webSocketClientChannelMap;
    }

    public static void addWebSocketClientChannel(String userId, Channel channel) {
        getWebSocketClientChannelMap().put(userId, channel);
    }

    public static ConcurrentHashMap<String, Channel> getWebSocketAdminChannelMap() {
        if (null == webSocketAdminChannelMap) {
            synchronized (webSocketAdminChannelMapLock) {
                if (null == webSocketAdminChannelMap) {
                    webSocketAdminChannelMap = new ConcurrentHashMap<>();
                }
            }
        }
        return webSocketAdminChannelMap;
    }

    public static ConcurrentHashMap<String, String> getAdminClientIdMap() {
        if (null == adminClientIdMap) {
            synchronized (WebsocketConnectionMap.class) {
                if (null == adminClientIdMap) {
                    adminClientIdMap = new ConcurrentHashMap<>();
                }
            }
        }
        return adminClientIdMap;
    }

    public static ConcurrentHashMap<String, String> getClientAdminIdMap() {
        if (null == clientAdminIdMap) {
            synchronized (WebsocketConnectionMap.class) {
                if (null == clientAdminIdMap) {
                    clientAdminIdMap = new ConcurrentHashMap<>();
                }
            }
        }
        return clientAdminIdMap;
    }

    public static void addWebSocketAdminChannel(String userId, Channel channel) {
        getWebSocketAdminChannelMap().put(userId, channel);
    }

    public static void removeChannel(String userId) {
        if (getWebSocketClientChannelMap().containsKey(userId)) {
            getWebSocketClientChannelMap().remove(userId);
            var adminId = getClientAdminIdMap().get(userId);
            if (null != adminId) {
                unmap(userId, adminId);
            }
        }

        if (getWebSocketAdminChannelMap().containsKey(userId)) {
            getWebSocketAdminChannelMap().remove(userId);
            var clientId = getAdminClientIdMap().get(userId);
            if (null != clientId) {
                unmap(clientId, userId);
            }
        }
    }
}
