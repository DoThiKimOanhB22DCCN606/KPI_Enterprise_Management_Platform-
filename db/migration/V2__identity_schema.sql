CREATE TABLE tenants (
    id UUID PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    logo_url TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    timezone VARCHAR(50),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE users (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash TEXT,
    full_name VARCHAR(255),
    avatar_url TEXT,
    phone VARCHAR(50),
    employee_code VARCHAR(100),
    status VARCHAR(30) DEFAULT 'ACTIVE',
    last_login_at TIMESTAMPTZ,
    organization_unit_id UUID,
    mfa_secret TEXT,
    mfa_enabled BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    updated_by UUID,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE roles (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    code VARCHAR(100) NOT NULL,
    name VARCHAR(255),
    description TEXT,
    system_role BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version BIGINT DEFAULT 0
);

CREATE TABLE permissions (
    id UUID PRIMARY KEY,
    code VARCHAR(255) UNIQUE NOT NULL,
    resource VARCHAR(100),
    action VARCHAR(100),
    description TEXT
);

CREATE TABLE user_roles (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    assigned_at TIMESTAMPTZ,
    assigned_by UUID,
    PRIMARY KEY(user_id, role_id)
);

CREATE TABLE role_permissions (
    role_id UUID NOT NULL,
    permission_id UUID NOT NULL,
    PRIMARY KEY(role_id, permission_id)
);

CREATE TABLE api_tokens (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    user_id UUID NOT NULL,
    token_hash TEXT NOT NULL,
    name VARCHAR(255),
    expired_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ
);

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    token_hash TEXT NOT NULL,
    device_name VARCHAR(255),
    ip_address VARCHAR(100),
    expired_at TIMESTAMPTZ,
    revoked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ
);

CREATE TABLE tenant_subscriptions (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    plan_type VARCHAR(50) NOT NULL DEFAULT 'FREE',
    max_users INTEGER NOT NULL DEFAULT 10,
    max_kpis INTEGER NOT NULL DEFAULT 50,
    expires_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(tenant_id)
);

CREATE TABLE tenant_themes (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    primary_color VARCHAR(20) DEFAULT '#1976D2',
    secondary_color VARCHAR(20) DEFAULT '#424242',
    logo_url TEXT,
    favicon_url TEXT,
    font_family VARCHAR(100) DEFAULT 'Inter',
    company_name VARCHAR(255),
    tagline VARCHAR(255),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(tenant_id)
);

CREATE UNIQUE INDEX uk_users_email ON users(tenant_id, email);
CREATE INDEX idx_users_org ON users(organization_unit_id);
CREATE INDEX idx_users_status ON users(status);
