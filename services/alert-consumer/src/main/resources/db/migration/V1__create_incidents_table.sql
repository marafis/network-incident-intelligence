CREATE TABLE IF NOT EXISTS incidents (
    id BIGSERIAL PRIMARY KEY,
    alert_id VARCHAR(50) NOT NULL UNIQUE,
    alert_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    severity VARCHAR(20) NOT NULL,
    severity_score INTEGER NOT NULL,
    component VARCHAR(100) NOT NULL,
    component_id VARCHAR(50),
    region VARCHAR(50),
    message TEXT NOT NULL,
    source VARCHAR(50),
    device_ip VARCHAR(50),
    service VARCHAR(100),

    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    correlation_id VARCHAR(50),
    runbook_reference TEXT,
    component_metadata TEXT,
    enriched BOOLEAN NOT NULL DEFAULT TRUE,
    has_runbook BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_alert_id ON incidents(alert_id);
CREATE INDEX idx_severity ON incidents(severity);
CREATE INDEX idx_component ON incidents(component);
CREATE INDEX idx_status ON incidents(status);
CREATE INDEX idx_created_at ON incidents(created_at DESC);
CREATE INDEX idx_severity_score ON incidents(severity_score DESC);
CREATE INDEX idx_status_severity ON incidents(status, severity_score DESC, created_at DESC);

GRANT SELECT, INSERT, UPDATE, DELETE ON incidents TO incident_db;
GRANT USAGE, SELECT ON SEQUENCE incidents_id_seq TO incident_db;

