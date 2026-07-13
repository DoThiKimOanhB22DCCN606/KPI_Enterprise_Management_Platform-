-- V16__analytics_views.sql

-- 1. Store Leaderboard View
CREATE OR REPLACE VIEW vw_store_leaderboard AS
SELECT 
    store.tenant_id,
    store.name AS store_name,
    SUM(COALESCE(k.target_value, 0)) AS total_revenue,
    AVG(COALESCE(k.current_progress, 0)) AS kpi_score
FROM organization_units store
LEFT JOIN organization_units sub_ou ON sub_ou.path <@ store.path
LEFT JOIN users u ON u.organization_unit_id = sub_ou.id
LEFT JOIN kpis k ON k.owner_id = u.id AND k.owner_type = 'USER'
WHERE store.type = 'STORE'
GROUP BY store.tenant_id, store.name, store.id;

-- 2. KPI Summary View
CREATE OR REPLACE VIEW vw_kpi_summary AS
SELECT 
    tenant_id,
    name,
    status,
    target_value AS target
FROM kpis;

-- 3. Employee Performance View
CREATE OR REPLACE VIEW vw_employee_performance AS
SELECT 
    u.tenant_id,
    u.full_name AS employee_name,
    ou.name AS department,
    AVG(COALESCE(k.current_progress, 0)) AS score
FROM users u
LEFT JOIN organization_units ou ON u.organization_unit_id = ou.id
LEFT JOIN kpis k ON k.owner_id = u.id AND k.owner_type = 'USER'
GROUP BY u.tenant_id, u.full_name, ou.name, u.id;
