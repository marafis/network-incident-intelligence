package com.marafis.nii.alert.consumer;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Incident - JPA entity representing an enriched network incident
 *
 * Persisted to PostgreSQL table: incidents
 * Maps raw network alerts to incident records with enrichment metadata
 */
@Entity
@Table(name = "incidents", indexes = {
        @Index(name = "idx_alert_id", columnList = "alert_id", unique = false),
        @Index(name = "idx_severity", columnList = "severity", unique = false),
        @Index(name = "idx_component", columnList = "component", unique = false),
        @Index(name = "idx_status", columnList = "status", unique = false),
        @Index(name = "idx_created_at", columnList = "created_at", unique = false)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Incident {

    /**
     * Primary key - auto-generated
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Reference to original Kafka alert ID
     */
    @Column(name = "alert_id", nullable = false, length = 50)
    private String alertId;

    /**
     * Original alert timestamp
     */
    @Column(name = "alert_timestamp", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant alertTimestamp;

    /**
     * When incident was created (enriched and stored)
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant createdAt = Instant.now();

    /**
     * Last update time
     */
    @Column(name = "updated_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant updatedAt = Instant.now();

    /**
     * Alert severity: CRITICAL, WARNING, INFO
     */
    @Column(name = "severity", nullable = false, length = 20)
    private String severity;

    /**
     * Composite severity score (0-100, calculated by enricher)
     */
    @Column(name = "severity_score", nullable = false)
    private Integer severityScore;

    /**
     * Component that generated alert
     */
    @Column(name = "component", nullable = false, length = 100)
    private String component;

    /**
     * Component ID for quick lookup
     */
    @Column(name = "component_id", length = 50)
    private String componentId;

    /**
     * Geographic region
     */
    @Column(name = "region", length = 50)
    private String region;

    /**
     * Alert message/description
     */
    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    /**
     * Alert source (SNMP, SYSLOG, API, etc.)
     */
    @Column(name = "source", length = 50)
    private String source;

    /**
     * Device IP address
     */
    @Column(name = "device_ip", length = 50)
    private String deviceIp;

    /**
     * Associated service/interface
     */
    @Column(name = "service", length = 100)
    private String service;

    /**
     * Incident status: OPEN, ACKNOWLEDGED, RESOLVED, CLOSED
     */
    @Column(name = "status", nullable = false, length = 20)
    private String status = "OPEN";

    /**
     * Correlation ID for grouping related incidents
     */
    @Column(name = "correlation_id", length = 50)
    private String correlationId;

    /**
     * Reference to runbook/documentation (populated by enricher for RAG Phase 4)
     */
    @Column(name = "runbook_reference", columnDefinition = "TEXT")
    private String runbookReference;

    /**
     * Component metadata JSON (enriched by lookup)
     */
    @Column(name = "component_metadata", columnDefinition = "TEXT")
    private String componentMetadata;

    /**
     * Enrichment status flags
     */
    @Column(name = "enriched", nullable = false)
    private Boolean enriched = true;

    @Column(name = "has_runbook", nullable = false)
    private Boolean hasRunbook = false;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}