package com.android.monitoring.dto.request;

import com.android.monitoring.constant.WebSocketMessageHeader;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WebsocketMessageRequest {
    private String connectionId;
    private WebSocketMessageHeader header;
    private String body;
}
