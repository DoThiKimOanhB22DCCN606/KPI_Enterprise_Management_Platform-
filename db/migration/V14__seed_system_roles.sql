INSERT INTO roles (id, tenant_id, code, name, description, system_role) VALUES
(gen_random_uuid(), '00000000-0000-0000-0000-000000000001', 'SUPER_ADMIN', 'Super Admin', 'System administrator', TRUE),
(gen_random_uuid(), '00000000-0000-0000-0000-000000000001', 'TENANT_ADMIN', 'Tenant Admin', 'Tenant administrator', TRUE),
(gen_random_uuid(), '00000000-0000-0000-0000-000000000001', 'CEO', 'CEO', 'Chief Executive Officer', TRUE),
(gen_random_uuid(), '00000000-0000-0000-0000-000000000001', 'HR_ADMIN', 'HR Admin', 'HR Administrator', TRUE),
(gen_random_uuid(), '00000000-0000-0000-0000-000000000001', 'REGIONAL_MANAGER', 'Regional Manager', 'Regional Manager', TRUE),
(gen_random_uuid(), '00000000-0000-0000-0000-000000000001', 'STORE_MANAGER', 'Store Manager', 'Store Manager', TRUE),
(gen_random_uuid(), '00000000-0000-0000-0000-000000000001', 'TEAM_LEADER', 'Team Leader', 'Team Leader', TRUE),
(gen_random_uuid(), '00000000-0000-0000-0000-000000000001', 'EMPLOYEE', 'Employee', 'Standard Employee', TRUE),
(gen_random_uuid(), '00000000-0000-0000-0000-000000000001', 'DATA_ENTRY', 'Data Entry', 'Data Entry Clerk', TRUE),
(gen_random_uuid(), '00000000-0000-0000-0000-000000000001', 'VIEWER', 'Viewer', 'Read-only viewer', TRUE);

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p WHERE r.code = 'SUPER_ADMIN';

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p WHERE r.code = 'TENANT_ADMIN';

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p WHERE r.code = 'HR_ADMIN' 
AND p.code IN ('USER_READ', 'USER_WRITE', 'ORG_READ', 'ORG_WRITE', 'KPI_TEMPLATE_READ', 'KPI_TEMPLATE_WRITE', 'KPI_READ');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p WHERE r.code = 'CEO' 
AND p.code IN ('USER_READ', 'ORG_READ', 'KPI_READ', 'GOAL_READ', 'DASHBOARD_READ', 'REPORT_READ', 'REPORT_GENERATE', 'ALERT_READ', 'AUDIT_READ', 'AI_QUERY', 'LEADERBOARD_READ');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p WHERE r.code IN ('REGIONAL_MANAGER', 'STORE_MANAGER', 'TEAM_LEADER')
AND p.code IN ('USER_READ', 'ORG_READ', 'KPI_READ', 'KPI_WRITE', 'KPI_APPROVE', 'GOAL_READ', 'GOAL_WRITE', 'DASHBOARD_READ', 'REPORT_READ', 'REPORT_GENERATE', 'LEADERBOARD_READ');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p WHERE r.code = 'EMPLOYEE'
AND p.code IN ('USER_READ', 'ORG_READ', 'KPI_READ', 'KPI_VALUE_WRITE', 'GOAL_READ', 'DASHBOARD_READ', 'LEADERBOARD_READ');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p WHERE r.code = 'DATA_ENTRY'
AND p.code IN ('KPI_READ', 'KPI_VALUE_WRITE');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p WHERE r.code = 'VIEWER'
AND p.code IN ('USER_READ', 'ORG_READ', 'KPI_READ', 'GOAL_READ', 'DASHBOARD_READ', 'REPORT_READ', 'LEADERBOARD_READ');
