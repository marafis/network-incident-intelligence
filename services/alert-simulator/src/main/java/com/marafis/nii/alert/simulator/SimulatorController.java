package com.marafis.nii.alert.simulator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * REST Controller for simulator health and control
 */
@Slf4j
@RestController
public class SimulatorController {

    private final AlertProducer alertProducer;
    private final AtomicLong alertCount;

    public SimulatorController(AlertProducer alertProducer, AtomicLong alertCount) {
        this.alertProducer = alertProducer;
        this.alertCount = alertCount;
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health/simulator")
    public Map<String, Object> health() {
        return Map.of(
                "status", "UP",
                "service", "alert-simulator",
                "alerts_generated", alertCount.get(),
                "kafka_connected", alertProducer.isHealthy()
        );
    }

    /**
     * Send a manual alert (for testing)
     */
    @PostMapping("/api/v1/alerts/manual")
    public Map<String, Object> sendManualAlert(@RequestBody com.marafis.nii.alert.simulator.NetworkAlert alert) {
        // sendAlert will increment the shared counter on success; call send then read the counter
        alertProducer.sendAlert(alert);
        long count = alertCount.get();
        log.info("Manual alert sent: {} (total: {})", alert.getAlertId(), count);
        return Map.of(
                "status", "sent",
                "alertId", alert.getAlertId(),
                "total_sent", count
        );
    }

    /**
     * Get simulator metrics
     */
    @GetMapping("/api/v1/simulator/metrics")
    public Map<String, Object> getMetrics() {
        return Map.of(
                "total_alerts_sent", alertCount.get(),
                "service_status", alertProducer.isHealthy() ? "running" : "degraded"
        );
    }

    // Shared AtomicLong 'alertCount' is updated by AlertProducer when alerts are successfully sent.
}
