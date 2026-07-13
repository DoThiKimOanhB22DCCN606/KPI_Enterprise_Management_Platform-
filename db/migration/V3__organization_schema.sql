CREATE TABLE organization_units (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    parent_id UUID,
    type VARCHAR(50) NOT NULL,
    code VARCHAR(100),
    name VARCHAR(255) NOT NULL,
    manager_user_id UUID,
    path ltree,
    level INTEGER DEFAULT 0 NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version BIGINT DEFAULT 0,
    CONSTRAINT fk_org_parent FOREIGN KEY(parent_id) REFERENCES organization_units(id) ON DELETE RESTRICT
);

CREATE INDEX idx_org_parent ON organization_units(parent_id);
CREATE INDEX idx_org_path ON organization_units USING GiST(path);
CREATE INDEX idx_org_type ON organization_units(type);
CREATE INDEX idx_org_tenant ON organization_units(tenant_id);
