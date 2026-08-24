package com.marafis.nii.alert.simulator.records;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "simulator")
public record SimulatorProperties(
        boolean enabled,
        Generation generation,
        Alerts alerts
) {
    public record Alerts(
            List<String> components,
            Map<String, Double> severityDistribution,
            List<String> messages,
            List<String> regions
    ) {}
}
