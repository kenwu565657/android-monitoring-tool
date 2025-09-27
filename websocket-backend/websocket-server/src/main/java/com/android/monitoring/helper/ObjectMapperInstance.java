package com.android.monitoring.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

@Getter
public enum ObjectMapperInstance {
    INSTANCE;

    private final ObjectMapper objectMapper;

    ObjectMapperInstance() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules();
    }
}
