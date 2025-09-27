package com.android.monitoring;

import io.netty.channel.Channel;

import java.util.concurrent.ConcurrentHashMap;

public enum WebRtcConnectionMap {
    INSTANCE;

    private final ConcurrentHashMap<String, Channel> userNameConnectionMap = new ConcurrentHashMap<>();

    public Channel getConnection(String userName) {
        return userNameConnectionMap.get(userName);
    }

    public void addConnection(String userName, Channel channel) {
        userNameConnectionMap.put(userName, channel);
    }

    public void removeConnection(String userName) {
        userNameConnectionMap.remove(userName);
    }
}
