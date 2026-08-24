package com.marafis.nii.alert.simulator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Alert Simulator Service
 * <p>
 * Generates synthetic network alerts and sends them to the Kafka 'network-alerts' topic.
 * Can be controlled via REST API to start/stop alert generation.
 */
@SpringBootApplication
@EnableScheduling
public class SimulatorApplication {
    public static void main(String[] args) {
        SpringApplication.run(SimulatorApplication.class, args);
    }
}