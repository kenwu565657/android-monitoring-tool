package com.android.monitoring.pojo;

import com.android.monitoring.constant.WebSocketMessageHeader;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WebSocketMessage<T> {
    private WebSocketMessageHeader header;
    private String destinationConnectionId;
    private String sourceConnectionId;
    private T body;
}
