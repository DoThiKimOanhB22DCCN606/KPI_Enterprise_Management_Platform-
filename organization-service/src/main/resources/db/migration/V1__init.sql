CREATE EXTENSION IF NOT EXISTS ltree;

CREATE TABLE IF NOT EXISTS organization_units (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    parent_id UUID,
    type VARCHAR(50) NOT NULL,
    code VARCHAR(100),
    name VARCHAR(255) NOT NULL,
    manager_user_id UUID,
    path ltree,
    level INTEGER DEFAULT 0,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    version BIGINT DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_org_parent ON organization_units(parent_id);
CREATE INDEX IF NOT EXISTS idx_org_path ON organization_units USING GIST(path);
CREATE INDEX IF NOT EXISTS idx_org_type ON organization_units(type);
CREATE INDEX IF NOT EXISTS idx_org_tenant ON organization_units(tenant_id);
