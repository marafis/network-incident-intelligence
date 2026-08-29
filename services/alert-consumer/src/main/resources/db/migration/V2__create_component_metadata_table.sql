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
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO components (component_id, component_name, component_type, criticality, region, owner_team, sla_response_minutes, runbook_id)
VALUES
    ('router-01', 'Core Router 01', 'ROUTER', 'CRITICAL', 'us-east-1', 'network-team', 15, 'router-troubleshooting'),
    ('switch-01', 'Switch 01', 'SWITCH', 'HIGH', 'us-east-1', 'network-team', 30, 'switch-troubleshooting'),
    ('firewall-01', 'Firewall 01', 'FIREWALL', 'CRITICAL', 'us-east-1', 'security-team', 15, 'firewall-troubleshooting'),
    ('db-01', 'Database Server 01', 'DATABASE', 'CRITICAL', 'us-east-2', 'database-team', 15, 'database-troubleshooting'),
    ('lb-01', 'Load Balancer 01', 'LOAD_BALANCER', 'HIGH', 'us-east-1', 'infrastructure-team', 30, 'lb-troubleshooting')
ON CONFLICT (component_id) DO NOTHING;

GRANT SELECT, INSERT, UPDATE, DELETE ON components TO incident_db;
GRANT USAGE, SELECT ON SEQUENCE components_id_seq TO incident_db;

