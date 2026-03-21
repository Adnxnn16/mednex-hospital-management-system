CREATE SCHEMA IF NOT EXISTS hospital_a;
CREATE SCHEMA IF NOT EXISTS hospital_b;

CREATE TABLE IF NOT EXISTS hospital_a.audit_log (
    id         UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    action     VARCHAR(255),
    entity_id  VARCHAR(255),
    entity_type VARCHAR(255),
    metadata   VARCHAR(4000),
    ip_address VARCHAR(255),
    occurred_at TIMESTAMP,
    patient_id  VARCHAR(36),
    tenant_id   VARCHAR(255) NOT NULL,
    user_id     VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS hospital_b.audit_log (
    id         UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    action     VARCHAR(255),
    entity_id  VARCHAR(255),
    entity_type VARCHAR(255),
    metadata   VARCHAR(4000),
    ip_address VARCHAR(255),
    occurred_at TIMESTAMP,
    patient_id  VARCHAR(36),
    tenant_id   VARCHAR(255) NOT NULL,
    user_id     VARCHAR(255)
);
