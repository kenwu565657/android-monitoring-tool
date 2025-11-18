package com.android.monitoring.pojo.json;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeviceInfoResponseBody {
    private String brand;
    private String model;
    private String version;
    private String sdk;
}
