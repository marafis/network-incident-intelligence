package com.marafis.nii.alert.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class ComponentMetadataService {

    public Map<String, Object> getComponentMetadata(String componentId) {
        if (componentId == null || componentId.isBlank()) {
            return null;
        }

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

    private Map<String, Object> buildMetadata(String name, String criticality, String region, String runbookId, String keywords) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("component_name", name);
        metadata.put("criticality", criticality);
        metadata.put("region", region);
        metadata.put("runbook_id", runbookId);
        metadata.put("keywords", keywords);
        metadata.put("owner", "platform-team");
        metadata.put("sla_response_minutes", computeSlaResponseMinutes(criticality));
        return metadata;
    }

    private int computeSlaResponseMinutes(String criticality) {
        if ("CRITICAL".equalsIgnoreCase(criticality)) {
            return 15;
        }
        if ("HIGH".equalsIgnoreCase(criticality)) {
            return 60;
        }
        if ("MEDIUM".equalsIgnoreCase(criticality)) {
            return 60;
        }
        return 120;
    }
}