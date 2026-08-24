package com.marafis.nii.alert.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Alert Consumer Service
 *
 * Consumes raw network alerts from Kafka 'network-alerts' topic,
 * enriches them with metadata and context, and persists to PostgreSQL
 * as incident records for further processing by the incident-agent (Phase 5).
 */
@SpringBootApplication
@Slf4j
public class ConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConsumerApplication.class, args);
    }

    /**
     * Define the Kafka consumer function
     * Spring Cloud Stream will bind this to 'network-alerts' topic
     */
    @Bean
    public Consumer<NetworkAlert> alertConsumer(AlertEnricher enricher, IncidentRepository incidentRepository) {
        return alert -> {
            try {
                log.debug("Received alert from Kafka: {}", alert.getAlertId());

                // Enrich the alert with metadata and context
                EnrichedAlert enrichedAlert = enricher.enrich(alert);
                log.debug("Alert enriched: severity_score={}", enrichedAlert.getSeverityScore());

                // Convert to incident and persist
                Incident incident = enrichedAlert.toIncident();
                Incident saved = incidentRepository.save(incident);
                log.info("Incident persisted: id={}, severity={}", saved.getId(), saved.getSeverity());

            } catch (Exception e) {
                log.error("Error processing alert: {}", alert.getAlertId(), e);
                // In production: route to dead-letter queue
            }
        };
    }

    /**
     * REST Controller for consumer health and statistics
     */
    @RestController
    public static class ConsumerController {

        private final IncidentRepository incidentRepository;
        private final AtomicLong alertsProcessed = new AtomicLong(0);

        public ConsumerController(IncidentRepository incidentRepository) {
            this.incidentRepository = incidentRepository;
        }

        /**
         * Health check endpoint
         */
        @GetMapping("/health/consumer")
        public Map<String, Object> health() {
            long totalIncidents = incidentRepository.count();
            return Map.of(
                    "status", "UP",
                    "service", "alert-consumer",
                    "alerts_processed", alertsProcessed.get(),
                    "total_incidents_in_db", totalIncidents
            );
        }

        /**
         * Get consumer statistics
         */
        @GetMapping("/api/v1/consumer/stats")
        public Map<String, Object> getStats() {
            return Map.of(
                    "alerts_processed", alertsProcessed.get(),
                    "incidents_stored", incidentRepository.count()
            );
        }

        /**
         * Increment counter (called by consumer function)
         */
        public void incrementCounter() {
            alertsProcessed.incrementAndGet();
        }
    }
}