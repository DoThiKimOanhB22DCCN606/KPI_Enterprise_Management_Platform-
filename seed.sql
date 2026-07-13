INSERT INTO tenants (id, code, name, status) 
VALUES ('00000000-0000-0000-0000-000000000001', 'SYS_TENANT', 'System Tenant', 'ACTIVE') 
ON CONFLICT (code) DO NOTHING;

INSERT INTO users (id, tenant_id, email, password_hash, full_name, status) 
VALUES ('00000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000001', 'admin@kemp.com', '$2a$10$6Fo9VUUYFeSJCtC9kFkQiu/MH41zo4StkYQ2iWtijIXGg98JmtcDa', 'Admin User', 'ACTIVE') 
ON CONFLICT (tenant_id, email) DO NOTHING;

INSERT INTO roles (id, tenant_id, code, name, system_role)
VALUES ('00000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000001', 'TENANT_ADMIN', 'Tenant Admin', true)
ON CONFLICT (tenant_id, code) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
VALUES ('00000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000003')
ON CONFLICT (user_id, role_id) DO NOTHING;
