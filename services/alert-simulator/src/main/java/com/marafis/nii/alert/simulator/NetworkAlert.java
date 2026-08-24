package com.marafis.nii.alert.simulator;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * NetworkAlert - Custom alert schema for NII platform
 *
 * Represents a raw network alert (SNMP trap, syslog, or custom format)
 * sent to Kafka topic: network-alerts
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NetworkAlert {

    /**
     * Unique alert identifier (UUID or timestamp-based)
     */
    @JsonProperty("alert_id")
    private UUID alertId;

    /**
     * Timestamp when alert was generated (ISO-8601 format)
     */
    @JsonProperty("timestamp")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant timestamp;

    /**
     * Alert severity level: CRITICAL, WARNING, INFO
     */
    @JsonProperty("severity")
    private Severity severity;

    /**
     * Component that generated the alert (router-01, switch-01, etc.)
     */
    @JsonProperty("component")
    private String component;

    /**
     * Component ID for quick lookup
     */
    @JsonProperty("component_id")
    private String componentId;

    /**
     * Geographic region where component is located
     */
    @JsonProperty("region")
    private String region;

    /**
     * Alert message/description
     */
    @JsonProperty("message")
    private String message;

    /**
     * Optional: Alert source (SNMP, syslog, API, etc.)
     */
    @JsonProperty("source")
    private String source;

    /**
     * Optional: Device IP address
     */
    @JsonProperty("device_ip")
    private String deviceIp;

    /**
     * Optional: Associated service/interface
     */
    @JsonProperty("service")
    private String service;

    /**
     * Indicates if this is a repeat/duplicate of a previous alert
     */
    @JsonProperty("is_duplicate")
    private boolean isDuplicate = false;

    /**
     * Correlation ID for grouping related alerts
     */
    @JsonProperty("correlation_id")
    private UUID correlationId;

    public enum Severity {
        CRITICAL, WARNING, INFO
    }

    @Override
    public String toString() {
        // Use a compact JSON representation for logs. If serialization fails, fall back to simple string.
        try {
            ObjectMapper om = new ObjectMapper();
            om.registerModule(new JavaTimeModule());
            // Ensure timestamps are serialized as ISO-8601 strings
            return om.writeValueAsString(this);
        } catch (Exception e) {
            return String.format(
                    "NetworkAlert{alertId='%s', timestamp=%s, severity='%s', component='%s', region='%s', message='%s'}",
                    alertId, timestamp, severity, component, region, message
            );
        }
    }
}