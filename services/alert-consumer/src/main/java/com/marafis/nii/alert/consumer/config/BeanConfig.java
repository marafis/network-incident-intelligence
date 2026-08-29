package com.marafis.nii.alert.consumer.config;

import com.marafis.nii.alert.consumer.domain.EnrichmentProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(EnrichmentProperties.class)
public class BeanConfig {
}
