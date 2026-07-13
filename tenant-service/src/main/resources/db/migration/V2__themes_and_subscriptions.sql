CREATE TABLE IF NOT EXISTS tenant_themes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL UNIQUE,
    primary_color VARCHAR(50) DEFAULT '#3B82F6',
    secondary_color VARCHAR(50) DEFAULT '#1E40AF',
    logo_url TEXT,
    favicon_url TEXT,
    custom_css TEXT,
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS tenant_subscriptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    plan VARCHAR(50) NOT NULL DEFAULT 'FREE',
    max_users INTEGER DEFAULT 10,
    max_kpis INTEGER DEFAULT 50,
    features JSONB DEFAULT '{}',
    started_at TIMESTAMP DEFAULT NOW(),
    expires_at TIMESTAMP,
    active BOOLEAN DEFAULT TRUE
);
CREATE INDEX IF NOT EXISTS idx_tenant_subscriptions_tenant ON tenant_subscriptions(tenant_id);
