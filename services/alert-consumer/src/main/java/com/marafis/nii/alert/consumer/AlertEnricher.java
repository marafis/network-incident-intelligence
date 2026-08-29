package com.marafis.nii.alert.consumer;

import com.marafis.nii.alert.consumer.domain.EnrichmentProperties;
import com.marafis.nii.alert.consumer.domain.NetworkAlert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.marafis.nii.alert.consumer.domain.Incident;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;

@Component
@Slf4j
public class AlertEnricher {

    private final ComponentMetadataService componentMetadataService;
    private final EnrichmentProperties props;

    public AlertEnricher(ComponentMetadataService componentMetadataService, EnrichmentProperties props) {
        this.componentMetadataService = componentMetadataService;
        this.props = props;
    }

    public EnrichedAlert enrich(NetworkAlert alert) {
        if (!props.enabled() || alert == null) {
            log.debug("Enrichment disabled or alert null for {}", alert == null ? "<unknown>" : alert.getAlertId());
            return new EnrichedAlert(alert, null, calculateBaselineSeverityScore(alert), null);
        }

        try {
            Map<String, Object> metadata = props.lookupMetadata()
                    ? componentMetadataService.getComponentMetadata(alert.getComponentId())
                    : null;

            Integer severityScore = props.calculateSeverity()

                    ? calculateSeverityScore(alert, metadata)
                    : calculateBaselineSeverityScore(alert);

            String runbookReference = props.addRunbookReference()
                    ? assignRunbookReference(alert, metadata)
                    : null;

            log.debug("Enriched alert {} with severity score {} and runbook {}",
                    alert.getAlertId(), severityScore, runbookReference);
            return new EnrichedAlert(alert, metadata, severityScore, runbookReference);
        } catch (Exception e) {
            log.error("Failed to enrich alert {}. Falling back to partial enrichment.", alert.getAlertId(), e);
            return new EnrichedAlert(alert, null, calculateBaselineSeverityScore(alert), null);
        }
    }

    public Integer calculateSeverityScore(NetworkAlert alert, Map<String, Object> metadata) {
        if (alert == null) {
            return 0;
        }

        int score = calculateBaselineSeverityScore(alert);

        if (metadata != null && !metadata.isEmpty()) {
            String criticality = (String) metadata.getOrDefault("criticality", "LOW");
            score += switch (criticality.toUpperCase(Locale.ROOT)) {
                case "CRITICAL" -> 30;
                case "HIGH" -> 20;
                case "MEDIUM" -> 10;
                default -> 0;
            };
        }

        String region = alert.getRegion() == null ? "" : alert.getRegion();
        if (region.startsWith("us-east") || region.startsWith("us-west") || region.startsWith("us-")) {
            score += 5;
        } else if (region.startsWith("eu-")) {
            score += 3;
        } else if (region.startsWith("ap-")) {
            score += 2;
        }

        if (alert.getTimestamp() != null && Duration.between(alert.getTimestamp(), Instant.now()).toMinutes() > 10) {
            score += 5;
        }

        return Math.min(score, 100);
    }

    public Integer calculateBaselineSeverityScore(NetworkAlert alert) {
        if (alert == null || alert.getSeverity() == null) {
            return 5;
        }

        return switch (alert.getSeverity().toUpperCase(Locale.ROOT)) {
            case "CRITICAL" -> 30;
            case "WARNING" -> 20;
            case "INFO" -> 10;
            default -> 5;
        };
    }

    public String assignRunbookReference(NetworkAlert alert, Map<String, Object> metadata) {
        if (alert == null) {
            return null;
        }

        String component = alert.getComponentId() != null ? alert.getComponentId() : alert.getComponent();
        String runbookId = metadata != null && metadata.get("runbook_id") != null
                ? metadata.get("runbook_id").toString()
                : extractAlertType(alert.getMessage());

        if (component == null || component.isBlank()) {
            return null;
        }

        return "runbooks/" + component + "/" + runbookId;
    }

    public String extractAlertType(String message) {
        if (message == null || message.isBlank()) {
            return "GENERIC";
        }

        String normalized = message.toUpperCase(Locale.ROOT);

        if (normalized.contains("CPU") || normalized.contains("UTILIZATION")) {
            return "HIGH_CPU";
        }
        if (normalized.contains("MEMORY")) {
            return "HIGH_MEMORY";
        }
        if (normalized.contains("LATENCY")) {
            return "LATENCY";
        }
        if (normalized.contains("BGP")) {
            return "BGP_FLAP";
        }
        if (normalized.contains("INTERFACE") || normalized.contains("ERROR")) {
            return "INTERFACE_ERROR";
        }
        if (normalized.contains("LOSS")) {
            return "PACKET_LOSS";
        }
        if (normalized.contains("TIMEOUT")) {
            return "TIMEOUT";
        }
        if (normalized.contains("FAILURE")) {
            return "FAILURE";
        }
        if (normalized.contains("CHANGE")) {
            return "CONFIG_CHANGE";
        }
        if (normalized.contains("UNREACHABLE")) {
            return "UNREACHABLE";
        }
        return "GENERIC";
    }

    public record EnrichedAlert(
            NetworkAlert originalAlert,
            Map<String, Object> componentMetadata,
            Integer severityScore,
            String runbookReference
    ) {
        public Incident toIncident() {
            if (originalAlert == null) {
                return null;
            }

            return Incident.builder()
                    .alertId(originalAlert.getAlertId())
                    .alertTimestamp(originalAlert.getTimestamp())
                    .severity(originalAlert.getSeverity())
                    .severityScore(severityScore == null ? 0 : severityScore)
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
}