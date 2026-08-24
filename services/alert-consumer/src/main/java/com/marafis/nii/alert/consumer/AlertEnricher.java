package com.marafis.nii.alert.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * AlertEnricher - Enriches raw network alerts with context and metadata
 *
 * Performs:
 * - Component metadata lookup (from Phase 2 database)
 * - Composite severity score calculation
 * - Runbook reference assignment (for Phase 4 RAG)
 * - Deduplication checks (Phase 2+)
 */
@Component
@Slf4j
public class AlertEnricher {

    @Value("${consumer.enrichment.enabled:true}")
    private boolean enrichmentEnabled;

    @Value("${consumer.enrichment.lookup-metadata:true}")
    private boolean lookupMetadata;

    @Value("${consumer.enrichment.calculate-severity-score:true}")
    private boolean calculateSeverity;

    @Value("${consumer.enrichment.add-runbook-reference:true}")
    private boolean addRunbookReference;

    private final ComponentMetadataService componentMetadataService;

    public AlertEnricher(ComponentMetadataService componentMetadataService) {
        this.componentMetadataService = componentMetadataService;
    }

    /**
     * Enrich a raw network alert with metadata and context
     *
     * @param alert Raw NetworkAlert from Kafka
     * @return EnrichedAlert with added metadata and scores
     */
    public EnrichedAlert enrich(NetworkAlert alert) {
        if (!enrichmentEnabled) {
            log.warn("Enrichment disabled, returning minimal enrichment");
            return new EnrichedAlert(alert, null, calculateBaselineSeverityScore(alert), null);
        }

        try {
            // 1. Lookup component metadata
            Map<String, Object> componentMetadata = null;
            if (lookupMetadata) {
                componentMetadata = componentMetadataService.getComponentMetadata(alert.getComponentId());
                log.debug("Component metadata found for {}: {}", alert.getComponentId(), componentMetadata);
            }

            // 2. Calculate composite severity score
            int severityScore = 0;
            if (calculateSeverity) {
                severityScore = calculateSeverityScore(alert, componentMetadata);
                log.debug("Calculated severity score: {} for alert {}", severityScore, alert.getAlertId());
            }

            // 3. Add runbook reference (for Phase 4 RAG)
            String runbookReference = null;
            if (addRunbookReference) {
                runbookReference = assignRunbookReference(alert, componentMetadata);
                log.debug("Assigned runbook: {} for component {}", runbookReference, alert.getComponent());
            }

            return new EnrichedAlert(alert, componentMetadata, severityScore, runbookReference);

        } catch (Exception e) {
            log.error("Error enriching alert {}", alert.getAlertId(), e);
            // Fallback: return partially enriched
            return new EnrichedAlert(alert, null, calculateBaselineSeverityScore(alert), null);
        }
    }

    /**
     * Calculate composite severity score (0-100)
     *
     * Factors:
     * - Base severity (CRITICAL=30, WARNING=20, INFO=10)
     * - Component criticality (from metadata)
     * - Region/SLA impact
     * - Time-based escalation
     */
    private int calculateSeverityScore(NetworkAlert alert, Map<String, Object> metadata) {
        int score = calculateBaselineSeverityScore(alert);

        // Component criticality multiplier
        if (metadata != null) {
            String criticality = (String) metadata.getOrDefault("criticality", "LOW");
            switch (criticality) {
                case "CRITICAL" -> score += 30;
                case "HIGH" -> score += 20;
                case "MEDIUM" -> score += 10;
                case "LOW" -> score += 0;
            }
        }

        // Region SLA impact
        if (alert.getRegion() != null) {
            if (alert.getRegion().startsWith("us-east")) {
                score += 5;  // Primary region
            } else if (alert.getRegion().startsWith("eu")) {
                score += 3;  // Secondary region
            }
        }

        // Cap at 100
        return Math.min(score, 100);
    }

    /**
     * Baseline severity score based on alert severity level
     */
    private int calculateBaselineSeverityScore(NetworkAlert alert) {
        return switch (alert.getSeverity()) {
            case "CRITICAL" -> 30;
            case "WARNING" -> 20;
            case "INFO" -> 10;
            default -> 5;
        };
    }

    /**
     * Assign runbook reference based on alert type and component
     *
     * In Phase 4, the incident-agent will use this to fetch detailed runbooks via RAG
     * Format: "runbooks/<component>/<alert-type>"
     */
    private String assignRunbookReference(NetworkAlert alert, Map<String, Object> metadata) {
        String component = alert.getComponent();
        String alertMessage = alert.getMessage();

        // Simple pattern matching for common alerts
        String alertType = extractAlertType(alertMessage);

        if (metadata != null && metadata.containsKey("runbook_id")) {
            String runbookId = (String) metadata.get("runbook_id");
            return String.format("runbooks/%s/%s", component, runbookId);
        }

        // Fallback to generic runbook
        return String.format("runbooks/network/%s", alertType.toLowerCase());
    }

    /**
     * Extract alert type from message for runbook matching
     */
    private String extractAlertType(String message) {
        if (message == null) return "UNKNOWN";

        if (message.contains("CPU") || message.contains("cpu")) return "HIGH_CPU";
        if (message.contains("Memory") || message.contains("memory")) return "HIGH_MEMORY";
        if (message.contains("latency") || message.contains("Latency")) return "LATENCY";
        if (message.contains("BGP") || message.contains("bgp")) return "BGP_FLAP";
        if (message.contains("Interface") || message.contains("interface")) return "INTERFACE_ERROR";
        if (message.contains("loss") || message.contains("Loss")) return "PACKET_LOSS";
        if (message.contains("timeout") || message.contains("Timeout")) return "TIMEOUT";
        if (message.contains("failure") || message.contains("Failure")) return "FAILURE";
        if (message.contains("change") || message.contains("Change")) return "CONFIG_CHANGE";
        if (message.contains("unreachable") || message.contains("Unreachable")) return "UNREACHABLE";

        return "GENERIC";
    }
}

/**
 * EnrichedAlert - Wrapper for raw alert with enrichment data
 */
record EnrichedAlert(
        NetworkAlert originalAlert,
        Map<String, Object> componentMetadata,
        Integer severityScore,
        String runbookReference
) {
    public Incident toIncident() {
        return Incident.builder()
                .alertId(originalAlert.getAlertId())
                .alertTimestamp(originalAlert.getTimestamp())
                .severity(originalAlert.getSeverity())
                .severityScore(severityScore)
                .component(originalAlert.getComponent())
                .componentId(originalAlert.getComponentId())
                .region(originalAlert.getRegion())
                .message(originalAlert.getMessage())
                .source(originalAlert.getSource())
                .deviceIp(originalAlert.getDeviceIp())
                .service(originalAlert.getService())
                .status("OPEN")
                .correlationId(originalAlert.getCorrelationId())
                .runbookReference(runbookReference)
                .componentMetadata(componentMetadata != null ? componentMetadata.toString() : null)
                .enriched(true)
                .hasRunbook(runbookReference != null)
                .build();
    }
}