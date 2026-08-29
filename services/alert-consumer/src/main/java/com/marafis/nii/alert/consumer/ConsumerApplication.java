package com.marafis.nii.alert.consumer;

import com.marafis.nii.alert.consumer.domain.Incident;
import com.marafis.nii.alert.consumer.domain.NetworkAlert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

@SpringBootApplication
@Slf4j
public class ConsumerApplication {

    private static final AtomicLong ALERTS_PROCESSED = new AtomicLong(0);

    public static void main(String[] args) {
        SpringApplication.run(ConsumerApplication.class, args);
    }

    @Bean
    public Consumer<NetworkAlert> alertConsumer(AlertEnricher enricher, IncidentRepository repo) {
        return alert -> {
            try {
                log.debug("Received alert from Kafka: {}", alert.getAlertId());
                AlertEnricher.EnrichedAlert enriched = enricher.enrich(alert);
                log.debug("Alert enriched: severity_score={}, runbook={}",
                        enriched.severityScore(), enriched.runbookReference());

                Incident incident = enriched.toIncident();
                if (incident != null) {
                    Incident saved = repo.save(incident);
                    ALERTS_PROCESSED.incrementAndGet();
                    log.info("Incident persisted: id={}, severity={}, alertId={}",
                            saved.getId(), saved.getSeverity(), saved.getAlertId());
                } else {
                    log.warn("Alert {} could not be converted to an Incident record.", alert.getAlertId());
                }
            } catch (Exception e) {
                log.error("Error processing alert: {}", alert.getAlertId(), e);
                // Route to dead-letter queue in Phase 2.
            }
        };
    }

    @RestController
    public static class ConsumerController {

        private final IncidentRepository incidentRepository;
        private final AtomicLong alertsProcessed = new AtomicLong(0);

        public ConsumerController(IncidentRepository incidentRepository) {
            this.incidentRepository = incidentRepository;
        }

        @GetMapping("/health/consumer")
        public Map<String, Object> health() {
            return Map.of(
                    "status", "UP",
                    "service", "alert-consumer",
                    "alerts_processed", ALERTS_PROCESSED.get(),
                    "total_incidents_in_db", incidentRepository.count()
            );
        }

        @GetMapping("/api/v1/consumer/stats")
        public Map<String, Object> stats() {
            return Map.of(
                    "alerts_processed", ALERTS_PROCESSED.get(),
                    "incidents_stored", incidentRepository.count()
            );
        }

        public long getAlertCount() {
            return alertsProcessed.get();
        }
    }
}