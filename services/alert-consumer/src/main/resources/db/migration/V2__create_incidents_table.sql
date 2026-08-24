-- V2__create_component_metadata_table.sql
-- Creates the components table for storing network component metadata
-- Used by alert-consumer enrichment (Phase 2+) for contextual lookups

CREATE TABLE IF NOT EXISTS components (
    id BIGSERIAL PRIMARY KEY,
    component_id VARCHAR(50) NOT NULL UNIQUE,
    component_name VARCHAR(100) NOT NULL,
    component_type VARCHAR(50),
    criticality VARCHAR(20),
    region VARCHAR(50),
    owner_team VARCHAR(100),
    sla_response_minutes INTEGER DEFAULT 60,
    runbook_id VARCHAR(100),
    runbook_url TEXT,
    keywords TEXT,
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX idx_component_id ON components(component_id);
CREATE INDEX idx_component_type ON components(component_type);
CREATE INDEX idx_criticality ON components(criticality);
CREATE INDEX idx_region ON components(region);
CREATE INDEX idx_owner_team ON components(owner_team);

-- Insert reference data for Phase 1 simulation
INSERT INTO components (component_id, component_name, component_type, criticality, region, owner_team, sla_response_minutes, runbook_id, keywords)
VALUES
    ('router-01', 'Core Router 01', 'ROUTER', 'CRITICAL', 'us-east-1', 'network-team', 15, 'router-troubleshooting', 'BGP configuration, interface monitoring'),
    ('switch-01', 'Switch 01', 'SWITCH', 'HIGH', 'us-east-1', 'network-team', 30, 'switch-troubleshooting', 'VLAN configuration, spanning-tree'),
    ('firewall-01', 'Firewall 01', 'FIREWALL', 'CRITICAL', 'us-east-1', 'security-team', 15, 'firewall-troubleshooting', 'Access lists, NAT rules, VPN'),
    ('db-01', 'Database Server 01', 'DATABASE', 'CRITICAL', 'us-east-2', 'database-team', 15, 'database-troubleshooting', 'Connection pooling, replication lag'),
    ('lb-01', 'Load Balancer 01', 'LOAD_BALANCER', 'HIGH', 'us-east-1', 'infrastructure-team', 30, 'lb-troubleshooting', 'Health checks, backend status')
ON CONFLICT (component_id) DO NOTHING;

-- Create comments
COMMENT ON TABLE components IS 'Network component reference data for alert enrichment';
COMMENT ON COLUMN components.criticality IS 'Component criticality: CRITICAL, HIGH, MEDIUM, LOW';
COMMENT ON COLUMN components.sla_response_minutes IS 'SLA response time in minutes based on criticality';
COMMENT ON COLUMN components.runbook_id IS 'Reference to runbook for Phase 4 RAG retrieval';

-- Grant permissions
GRANT SELECT, INSERT, UPDATE, DELETE ON components TO incident_db;
GRANT USAGE, SELECT ON SEQUENCE components_id_seq TO incident_db;