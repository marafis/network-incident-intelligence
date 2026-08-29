package com.marafis.nii.alert.consumer.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NetworkAlert {

    @JsonProperty("alert_id")
    private String alertId;

    @JsonProperty("timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private Instant timestamp;

    @JsonProperty("severity")
    private String severity;

    @JsonProperty("component")
    private String component;

    @JsonProperty("component_id")
    private String componentId;

    @JsonProperty("region")
    private String region;

    @JsonProperty("message")
    private String message;

    @JsonProperty("source")
    private String source;

    @JsonProperty("device_ip")
    private String deviceIp;

    @JsonProperty("service")
    private String service;

    @JsonProperty("is_duplicate")
    private Boolean isDuplicate;

    @JsonProperty("correlation_id")
    private String correlationId;

    @Override
    public String toString() {
        return "NetworkAlert{" +
                "alertId='" + alertId + '\'' +
                ", timestamp=" + timestamp +
                ", severity='" + severity + '\'' +
                ", component='" + component + '\'' +
                ", componentId='" + componentId + '\'' +
                ", region='" + region + '\'' +
                ", message='" + message + '\'' +
                ", source='" + source + '\'' +
                ", deviceIp='" + deviceIp + '\'' +
                ", service='" + service + '\'' +
                ", isDuplicate=" + isDuplicate +
                ", correlationId='" + correlationId + '\'' +
                '}';
    }
}

