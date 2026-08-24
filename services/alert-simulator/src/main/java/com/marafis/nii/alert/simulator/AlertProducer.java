package com.marafis.nii.alert.simulator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marafis.nii.alert.simulator.records.SimulatorProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import java.time.Instant;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * AlertProducer - Generates and sends network alerts to Kafka
 * Features:
 * - Scheduled alert generation based on configurable interval
 * - Random alert distribution (severity, components, regions)
 * - Health checks for Kafka connectivity
 * - Metrics tracking
 */
@Slf4j
@ConditionalOnProperty(name = "simulator.enabled", havingValue = "true", matchIfMissing = true)
public class AlertProducer {

    private final StreamBridge streamBridge;
    private final SimulatorProperties props;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Random random = new Random();
    private final java.util.concurrent.atomic.AtomicBoolean healthy = new java.util.concurrent.atomic.AtomicBoolean(true);

    // Configurable fields (injected by SimulatorApplication bean factory)
    private final AtomicLong alertCounter;

    private static final String[] SOURCES = {"SNMP", "SYSLOG", "API", "PROBE"};

    /**
     * Construct AlertProducer with external configuration.
     * This constructor is intended to be called from a @Bean factory in the main application class.
     */
    public AlertProducer(StreamBridge streamBridge,
                         AtomicLong alertCounter,
                         SimulatorProperties props) {
        this.streamBridge = streamBridge;
        this.alertCounter = alertCounter;
        this.props = props;

        objectMapper.findAndRegisterModules(); // register JSR310 module for Instant

        log.info("AlertProducer initialized (enabled={}, batchSize={})", props.enabled(), props.generation().batchSize());
    }

    /**
     * Scheduled task: Generate alerts at configured interval
     * Default: every 5 seconds
     */
    @Scheduled(fixedDelayString = "${simulator.generation.interval-ms:5000}")
    public void generateAndSendAlerts() {
        if (!props.enabled()) {
            return;
        }

        try {
            for (int i = 0; i < props.generation().batchSize(); i++) {
                NetworkAlert alert = generateRandomAlert();
                sendAlert(alert);
            }
        } catch (Exception e) {
            log.error("Error generating alerts", e);
            healthy.set(false);
        }
    }

    /**
     * Generate a random network alert with realistic data
     */
    private NetworkAlert generateRandomAlert() {
        String component = props.alerts().components().get(random.nextInt(props.alerts().components().size()));;
        NetworkAlert.Severity severity = selectRandomSeverity();
        String region = props.alerts().regions().get(random.nextInt(props.alerts().regions().size()));
        String message = props.alerts().messages().get(random.nextInt(props.alerts().messages().size()));
        String source = SOURCES[random.nextInt(SOURCES.length)];

        String deviceIp = String.format("192.168.%d.%d", random.nextInt(256), random.nextInt(256));

        // Construct a component-specific id to act as partition key (preserves ordering per component)
        String componentId = component + "-" + Math.abs(random.nextInt(10000));

        return NetworkAlert.builder()
                .alertId(UUID.randomUUID())
                .timestamp(Instant.now())
                .severity(severity)
                .component(component)
                .componentId(componentId)
                .region(region)
                .message(message)
                .source(source)
                .deviceIp(deviceIp)
                .service("network-services")
                .isDuplicate(random.nextDouble() < 0.1) // small chance to be a duplicate
                .correlationId(UUID.randomUUID())
                .build();
    }

    /**
     * Select severity based on configured distribution
     * CRITICAL: 15%, WARNING: 35%, INFO: 50%
     */
    private NetworkAlert.Severity selectRandomSeverity() {
        // Use the configured severity distribution map (CRITICAL, WARNING, INFO)
        double r = random.nextDouble();
        double cumulative = 0.0;

        // Ensure deterministic order: CRITICAL -> WARNING -> INFO
        String[] order = new String[]{"CRITICAL", "WARNING", "INFO"};
        for (String key : order) {
            Double prob = props.alerts().severityDistribution().get(key);
            if (prob == null) continue;
            cumulative += prob;
            if (r <= cumulative) {
                return NetworkAlert.Severity.valueOf(key);
            }
        }

        // Fallback default
        return NetworkAlert.Severity.INFO;
    }

    /**
     * Send alert to Kafka topic 'network-alerts'
     *
     * @param alert NetworkAlert to send
     */
    public void sendAlert(NetworkAlert alert) {
        try {
            // Serialize alert to JSON payload
            String jsonPayload = objectMapper.writeValueAsString(alert);

            Message<String> message = MessageBuilder
                    .withPayload(jsonPayload)
                    .setHeader("partition-key", alert.getComponentId())
                    .setHeader("alert-severity", alert.getSeverity().name())
                    .setHeader("correlation-id", alert.getCorrelationId().toString())
                    .build();

            boolean sent = streamBridge.send("network-alerts-out-0", message);

            if (sent) {
                // Increment counter atomically
                if (alertCounter != null) {
                    alertCounter.incrementAndGet();
                }
                log.debug("Alert sent successfully: {} component={} severity={}",
                        alert.getAlertId(), alert.getComponent(), alert.getSeverity());
                healthy.set(true);
            } else {
                log.warn("Failed to send alert (streamBridge returned false): {}", alert.getAlertId());
                healthy.set(false);
            }
        } catch (Exception e) {
            log.error("Exception serializing/sending alert {}", alert.getAlertId(), e);
            healthy.set(false);
        }
    }

    /**
     * Health check: Is Kafka producer healthy?
     */
    public boolean isHealthy() {
        return healthy.get();
    }
}