package com.marafis.nii.alert.consumer.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(
        name = "incidents",
        indexes = {
                @Index(name = "idx_alert_id", columnList = "alert_id"),
                @Index(name = "idx_severity", columnList = "severity"),
                @Index(name = "idx_component", columnList = "component"),
                @Index(name = "idx_status", columnList = "status"),
                @Index(name = "idx_created_at", columnList = "created_at DESC")
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "alert_id", nullable = false, unique = true)
    private String alertId;

    @Column(name = "alert_timestamp", nullable = false)
    private Instant alertTimestamp;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "severity", nullable = false)
    private String severity;

    @Column(name = "severity_score", nullable = false)
    private Integer severityScore;

    @Column(name = "component", nullable = false)
    private String component;

    @Column(name = "component_id")
    private String componentId;

    @Column(name = "region")
    private String region;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "source")
    private String source;

    @Column(name = "device_ip")
    private String deviceIp;

    @Column(name = "service")
    private String service;

    @Column(name = "status", nullable = false)
    @Builder.Default
    private String status = "OPEN";

    @Column(name = "correlation_id")
    private String correlationId;

    @Column(name = "runbook_reference", columnDefinition = "TEXT")
    private String runbookReference;

    @Column(name = "component_metadata", columnDefinition = "TEXT")
    private String componentMetadata;

    @Column(name = "enriched", nullable = false)
    @Builder.Default
    private Boolean enriched = true;

    @Column(name = "has_runbook", nullable = false)
    @Builder.Default
    private Boolean hasRunbook = false;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}

