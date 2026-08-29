package com.marafis.nii.alert.consumer.domain;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "consumer.enrichment")
public record EnrichmentProperties (
        boolean enabled,
        boolean lookupMetadata,
        boolean calculateSeverity,
        boolean addRunbookReference
) {}
