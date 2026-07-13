import argparse
import uuid
import random
from datetime import datetime, timedelta
import math
import json
import os

parser = argparse.ArgumentParser()
parser.add_argument('--stores', type=int, default=5)
parser.add_argument('--users', type=int, default=50)
parser.add_argument('--months', type=int, default=6)
parser.add_argument('--seed', type=int, default=42)
args = parser.parse_args()

random.seed(args.seed)

# Ensure directory exists
out_dir = '/db/migration' if os.path.exists('/db') else 'd:/prj_vdt/db/migration'
if not os.path.exists(out_dir):
    os.makedirs(out_dir)

out_file = f'{out_dir}/V999__demo_data_seeder.sql'
f = open(out_file, 'w', encoding='utf-8')

def gen_uuid():
    return str(uuid.uuid4())

def sql_escape(val):
    if val is None:
        return 'NULL'
    if isinstance(val, bool):
        return 'true' if val else 'false'
    if isinstance(val, (int, float)):
        return str(val)
    if isinstance(val, str) and val.startswith("JSON:"):
        return "'" + val[5:].replace("'", "''") + "'"
    return "'" + str(val).replace("'", "''") + "'"

def write(sql):
    f.write(sql + "\n")

write("-- V999 LARGE DEMO SEED SCRIPT")
write("-- DO NOT RUN IN PRODUCTION! Bypasses all domain constraints.")
write("DELETE FROM dashboard_widgets;")
write("DELETE FROM dashboards;")
write("DELETE FROM ai_messages;")
write("DELETE FROM ai_conversations;")
write("DELETE FROM alert_rules;")
write("DELETE FROM kpi_attachments;")
write("DELETE FROM kpi_values;")
write("DELETE FROM kpi_approvals;")
write("DROP TABLE IF EXISTS goals CASCADE;")
write("CREATE TABLE goals (id UUID PRIMARY KEY, tenant_id UUID NOT NULL, parent_goal_id UUID REFERENCES goals(id) ON DELETE SET NULL, kpi_id UUID, owner_type VARCHAR(50), owner_id UUID, name VARCHAR(255) NOT NULL, description TEXT, target_value NUMERIC(20,4) NOT NULL, current_value NUMERIC(20,4) DEFAULT 0, overall_progress NUMERIC(8,2) DEFAULT 0, weight DECIMAL(5,4) DEFAULT 1.0 NOT NULL, status VARCHAR(50) DEFAULT 'ACTIVE', expected_date TIMESTAMPTZ NOT NULL, created_at TIMESTAMPTZ DEFAULT NOW() NOT NULL, updated_at TIMESTAMPTZ DEFAULT NOW() NOT NULL, created_by UUID, updated_by UUID, version BIGINT DEFAULT 0);")
write("DELETE FROM kpis;")
write("DELETE FROM kpi_templates;")
write("DELETE FROM user_roles;")
write("DELETE FROM users;")
write("DELETE FROM roles;")
write("DELETE FROM organization_units;")
write("DELETE FROM tenants;")

TENANT_ID = '00000000-0000-0000-0000-000000000001'
MAIN_USER_ID = '00000000-0000-0000-0000-000000000002'
ADMIN_ROLE = '00000000-0000-0000-0000-000000000003'
HR_ROLE = '00000000-0000-0000-0000-000000000004'
MANAGER_ROLE = '00000000-0000-0000-0000-000000000005'
EMPLOYEE_ROLE = '00000000-0000-0000-0000-000000000006'
DEFAULT_PASS = '$2a$10$irAD4mqsdBY886W69EBexOYqv8LeAP9ViX6c0RFKlGRtzgoDmWpRq'

# 1. Tenant & Roles
write(f"INSERT INTO tenants (id, code, name, status) VALUES ({sql_escape(TENANT_ID)}, 'SYS_TENANT', 'Acme Retail', 'ACTIVE');")

roles = [
    (ADMIN_ROLE, 'TENANT_ADMIN', 'Tenant Admin'),
    (HR_ROLE, 'HR_ADMIN', 'HR Admin'),
    (MANAGER_ROLE, 'STORE_MANAGER', 'Store Manager'),
    (EMPLOYEE_ROLE, 'EMPLOYEE', 'Employee')
]
write(f"INSERT INTO roles (id, tenant_id, code, name, system_role) VALUES ")
for i, r in enumerate(roles):
    end = ";" if i == len(roles)-1 else ","
    write(f"({sql_escape(r[0])}, {sql_escape(TENANT_ID)}, {sql_escape(r[1])}, {sql_escape(r[2])}, true){end}")

# 2. Org Hierarchy
org_units = []

company_id = gen_uuid()
org_units.append((company_id, 'COMPANY', 'ACME', 'Acme Retail', 'ACME', 0, None))

regions = []
for code, name in [('NORTH', 'North Region'), ('SOUTH', 'South Region')]:
    r_id = gen_uuid()
    org_units.append((r_id, 'REGION', code, name, f'ACME.{code}', 1, company_id))
    regions.append((r_id, f'ACME.{code}'))

stores = []
store_profiles = ['BEST', 'WORST', 'GROWTH', 'STABLE', 'STABLE'] 
while len(store_profiles) < args.stores:
    store_profiles.append(random.choice(['STABLE', 'GROWTH']))

for i in range(1, args.stores + 1):
    r_id, r_path = random.choice(regions)
    s_id = gen_uuid()
    path = f"{r_path}.STORE_{i}"
    name = f"Store {i} ({store_profiles[i-1]})"
    org_units.append((s_id, 'STORE', f"STORE_{i}", name, path, 2, r_id))
    stores.append((s_id, path, store_profiles[i-1]))

departments = []
for s_id, s_path, s_profile in stores:
    for code, name in [('SALES', 'Sales Dept'), ('OPS', 'Operations'), ('CSAT', 'Customer Success')]:
        d_id = gen_uuid()
        path = f"{s_path}.{code}"
        org_units.append((d_id, 'DEPARTMENT', f"DEPT_{code}", name, path, 3, s_id))
        departments.append((d_id, path, s_profile))

teams = []
for d_id, d_path, s_profile in departments:
    t_id = gen_uuid()
    path = f"{d_path}.TEAM_1"
    org_units.append((t_id, 'TEAM', 'TEAM_1', 'Team Alpha', path, 4, d_id))
    teams.append((t_id, s_profile))

write("INSERT INTO organization_units (id, tenant_id, type, code, name, path, level, parent_id) VALUES")
for i, o in enumerate(org_units):
    end = ";" if i == len(org_units)-1 else ","
    write(f"({sql_escape(o[0])}, {sql_escape(TENANT_ID)}, {sql_escape(o[1])}, {sql_escape(o[2])}, {sql_escape(o[3])}, {sql_escape(o[4])}, {o[5]}, {sql_escape(o[6])}){end}")

# 3. Users
users = []
user_roles = []
first_names = ['John', 'Jane', 'Michael', 'Sarah', 'David', 'Emma', 'Chris', 'Laura', 'Robert', 'Anna', 'James', 'Emily', 'William', 'Jessica', 'Joseph']
last_names = ['Smith', 'Johnson', 'Williams', 'Brown', 'Jones', 'Garcia', 'Miller', 'Davis', 'Rodriguez', 'Martinez', 'Hernandez', 'Lopez']

# Main Test User
users.append((MAIN_USER_ID, 'admin@kemp.com', 'Acme Admin', company_id))
user_roles.append((MAIN_USER_ID, ADMIN_ROLE))

emp_per_team = max(1, args.users // len(teams))
user_store_profiles = {}

user_counter = 1
for t_id, s_profile in teams:
    mgr_id = gen_uuid()
    fn, ln = random.choice(first_names), random.choice(last_names)
    users.append((mgr_id, f"mgr.{user_counter}@kemp.com", f"{fn} {ln} (Mgr)", t_id))
    user_roles.append((mgr_id, MANAGER_ROLE))
    user_store_profiles[mgr_id] = s_profile
    user_counter += 1
    
    for _ in range(emp_per_team):
        e_id = gen_uuid()
        fn, ln = random.choice(first_names), random.choice(last_names)
        users.append((e_id, f"emp.{user_counter}@kemp.com", f"{fn} {ln}", t_id))
        user_roles.append((e_id, EMPLOYEE_ROLE))
        user_store_profiles[e_id] = s_profile
        user_counter += 1

# To handle chunking if needed, but for ~100 rows, a single insert is fine.
# We'll write one by one to be safe from syntax limits on very large args
for u in users:
    write(f"INSERT INTO users (id, tenant_id, email, password_hash, full_name, status, organization_unit_id) VALUES ({sql_escape(u[0])}, {sql_escape(TENANT_ID)}, {sql_escape(u[1])}, {sql_escape(DEFAULT_PASS)}, {sql_escape(u[2])}, 'ACTIVE', {sql_escape(u[3])});")

for ur in user_roles:
    write(f"INSERT INTO user_roles (user_id, role_id) VALUES ({sql_escape(ur[0])}, {sql_escape(ur[1])});")

# 4. KPI Templates
templates = [
    (gen_uuid(), 'REV', 'Revenue', 'FINANCIAL', 'MONTHLY', 'actual / target'),
    (gen_uuid(), 'PROFIT', 'Profit Margin', 'FINANCIAL', 'MONTHLY', 'actual / target'),
    (gen_uuid(), 'CSAT', 'CSAT Score', 'QUALITY', 'MONTHLY', 'actual / target'),
    (gen_uuid(), 'NPS', 'Net Promoter Score', 'QUALITY', 'MONTHLY', 'actual / target'),
    (gen_uuid(), 'INV', 'Inventory Turnover', 'OPERATION', 'MONTHLY', 'actual / target'),
    (gen_uuid(), 'ATT', 'Attendance Rate', 'HR', 'MONTHLY', 'actual / target'),
    (gen_uuid(), 'CONV', 'Sales Conversion Rate', 'SALES', 'MONTHLY', 'actual / target'),
    (gen_uuid(), 'RET', 'Return Rate', 'OPERATION', 'MONTHLY', '1 - (actual / target)') # lower is better
]
for t in templates:
    write(f"INSERT INTO kpi_templates (id, tenant_id, code, name, category, default_frequency, formula, active) VALUES ({sql_escape(t[0])}, {sql_escape(TENANT_ID)}, {sql_escape(t[1])}, {sql_escape(t[2])}, {sql_escape(t[3])}, {sql_escape(t[4])}, {sql_escape(t[5])}, true);")

# 5. Cascading Goals & KPIs
company_goal_id = gen_uuid()
write(f"INSERT INTO goals (id, tenant_id, parent_goal_id, kpi_id, owner_type, owner_id, name, target_value, current_value, overall_progress, expected_date) VALUES ({sql_escape(company_goal_id)}, {sql_escape(TENANT_ID)}, NULL, NULL, 'COMPANY', {sql_escape(company_id)}, 'Annual Revenue $10M', 10000000, 0, 0, '2026-12-31');")

today = datetime.now()
months = args.months
start_dates = []
for m in range(months, 0, -1):
    d = (today.replace(day=1) - timedelta(days=30 * m)).replace(day=1)
    start_dates.append(d)

for u_id, s_profile in user_store_profiles.items():
    emp_goal_id = gen_uuid()
    write(f"INSERT INTO goals (id, tenant_id, parent_goal_id, kpi_id, owner_type, owner_id, name, target_value, current_value, overall_progress, expected_date) VALUES ({sql_escape(emp_goal_id)}, {sql_escape(TENANT_ID)}, {sql_escape(company_goal_id)}, NULL, 'USER', {sql_escape(u_id)}, 'Individual Quota', 100000, 0, 0, '2026-12-31');")
    
    for k in range(2):
        tpl = random.choice(templates)
        k_id = gen_uuid()
        target = random.randint(1000, 50000) if tpl[1] in ['REV', 'PROFIT'] else random.randint(70, 100)
        
        write(f"INSERT INTO kpis (id, tenant_id, template_id, name, owner_type, owner_id, frequency, target_value, status, start_date, end_date, current_progress) VALUES ({sql_escape(k_id)}, {sql_escape(TENANT_ID)}, {sql_escape(tpl[0])}, {sql_escape(tpl[2] + ' - Q3')}, 'USER', {sql_escape(u_id)}, 'MONTHLY', {target}, 'ACTIVE', '{start_dates[0].strftime('%Y-%m-%d')}', '{(today + timedelta(days=30)).strftime('%Y-%m-%d')}', 0);")
        
        actual_cumulative = 0
        for i, dt in enumerate(start_dates):
            if s_profile == 'BEST': current_trend = 1.1 + (i * 0.05)
            elif s_profile == 'WORST': current_trend = 0.8 - (i * 0.05)
            elif s_profile == 'GROWTH': current_trend = 0.9 + (i * 0.1)
            else: current_trend = random.uniform(0.9, 1.1)
            
            chunk_target = target / months
            actual_val = chunk_target * current_trend
            actual_cumulative += actual_val
            progress = min(100, max(0, (actual_cumulative / target) * 100))
            
            v_id = gen_uuid()
            period_end = (dt + timedelta(days=28)).strftime('%Y-%m-%d')
            write(f"INSERT INTO kpi_values (id, tenant_id, kpi_id, period_start, period_end, actual_value, progress_percent, comment) VALUES ({sql_escape(v_id)}, {sql_escape(TENANT_ID)}, {sql_escape(k_id)}, '{dt.strftime('%Y-%m-%d')}', '{period_end}', {actual_cumulative:.2f}, {progress:.2f}, 'Automated update');")

# 6. Alerts & Dashboards
write("INSERT INTO alert_rules (id, tenant_id, name, description, condition_type, threshold_value, comparison_operator, severity, enabled) VALUES")
write(f"({sql_escape(gen_uuid())}, {sql_escape(TENANT_ID)}, 'Low KPI Alert', 'Triggers when KPI < 40%', 'PROGRESS', 40.0, 'LESS_THAN', 'CRITICAL', true);")

d_id = gen_uuid()
write(f"INSERT INTO dashboards (id, tenant_id, name, description) VALUES ({sql_escape(d_id)}, {sql_escape(TENANT_ID)}, 'Executive Overview', 'Main analytics dashboard');")

w_config = 'JSON:{"chartType": "bar", "dataSource": "leaderboard"}'
write(f"INSERT INTO dashboard_widgets (id, dashboard_id, widget_type, title, config_json) VALUES ({sql_escape(gen_uuid())}, {sql_escape(d_id)}, 'CHART', 'Store Leaderboard', {sql_escape(w_config)});")

f.close()
print(f"Generated V999 seed file successfully at {out_file}")
