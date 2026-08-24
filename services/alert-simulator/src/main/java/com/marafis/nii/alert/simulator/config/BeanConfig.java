package com.marafis.nii.alert.simulator.config;

import com.marafis.nii.alert.simulator.AlertProducer;
import com.marafis.nii.alert.simulator.records.SimulatorProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.atomic.AtomicLong;

@Configuration
@EnableConfigurationProperties(SimulatorProperties.class)
public class BeanConfig {
    /**
     * Inject StreamBridge for dynamic message sending
     */
    @Bean
    public AtomicLong alertCounter() {
        return new AtomicLong(0);
    }

    @Bean
    public AlertProducer alertProducer(
            StreamBridge streamBridge,
            AtomicLong alertCounter,
            SimulatorProperties props
    ) {
        return new AlertProducer(
                streamBridge,
                alertCounter,
                props
        );
    }
}
