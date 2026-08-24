package com.marafis.nii.alert.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * ComponentMetadataService - Provides component metadata for enrichment
 *
 * Phase 1: In-memory stub data
 * Phase 2: Will query PostgreSQL components table
 */
@Service
@Slf4j
public class ComponentMetadataService {

    /**
     * Lookup component metadata by component ID
     *
     * Phase 1: Stub implementation with hardcoded data
     * Phase 2: Query from incidents_db.components table
     *
     * @param componentId Component identifier (e.g., "router-01")
     * @return Map of metadata or null if not found
     */
    public Map<String, Object> getComponentMetadata(String componentId) {
        // Phase 1: Hardcoded metadata (stub)
        // In Phase 2, this will be replaced with JPA repository query

        return switch (componentId) {
            case "router-01" -> buildMetadata(
                    "Core Router 01",
                    "CRITICAL",
                    "us-east-1",
                    "router-troubleshooting",
                    "BGP configuration, interface monitoring"
            );
            case "switch-01" -> buildMetadata(
                    "Switch 01",
                    "HIGH",
                    "us-east-1",
                    "switch-troubleshooting",
                    "VLAN configuration, spanning-tree"
            );
            case "firewall-01" -> buildMetadata(
                    "Firewall 01",
                    "CRITICAL",
                    "us-east-1",
                    "firewall-troubleshooting",
                    "Access lists, NAT rules, VPN"
            );
            case "db-01" -> buildMetadata(
                    "Database Server 01",
                    "CRITICAL",
                    "us-east-2",
                    "database-troubleshooting",
                    "Connection pooling, replication lag"
            );
            case "lb-01" -> buildMetadata(
                    "Load Balancer 01",
                    "HIGH",
                    "us-east-1",
                    "lb-troubleshooting",
                    "Health checks, backend status"
            );
            default -> {
                log.warn("No metadata found for component: {}", componentId);
                yield null;
            }
        };
    }

    /**
     * Build component metadata map
     */
    private Map<String, Object> buildMetadata(
            String name,
            String criticality,
            String region,
            String runbookId,
            String keywords
    ) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("component_name", name);
        metadata.put("criticality", criticality);
        metadata.put("region", region);
        metadata.put("runbook_id", runbookId);
        metadata.put("keywords", keywords);
        metadata.put("owner", "platform-team");
        metadata.put("sla_response_minutes", criticality.equals("CRITICAL") ? 15 : 60);
        return metadata;
    }
}