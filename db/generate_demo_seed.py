import uuid
import random
from datetime import datetime, timedelta

def gen_uuid():
    return str(uuid.uuid4())

def sql_escape(val):
    if val is None:
        return 'NULL'
    if isinstance(val, bool):
        return 'true' if val else 'false'
    if isinstance(val, (int, float)):
        return str(val)
    # string
    return "'" + str(val).replace("'", "''") + "'"

out_file = 'd:/prj_vdt/db/large_demo_seed.sql'
f = open(out_file, 'w', encoding='utf-8')

def write(sql):
    f.write(sql + "\n")

write("-- LARGE DEMO SEED SCRIPT")
write("-- DO NOT RUN IN PRODUCTION!")

write("DELETE FROM ai_messages;")
write("DELETE FROM alert_rules;")
write("DELETE FROM kpi_attachments;")
write("DELETE FROM kpi_values;")
write("DELETE FROM kpi_approvals;")
write("DELETE FROM kpis;")
write("DELETE FROM kpi_templates;")
write("DELETE FROM user_roles;")
write("DELETE FROM users;")
write("DELETE FROM roles;")
write("DELETE FROM organization_units;")
write("DELETE FROM tenants;")

TENANT_ID = '00000000-0000-0000-0000-000000000001'
ADMIN_ROLE = '00000000-0000-0000-0000-000000000003'
HR_ROLE = '00000000-0000-0000-0000-000000000004'
MANAGER_ROLE = '00000000-0000-0000-0000-000000000005'
EMPLOYEE_ROLE = '00000000-0000-0000-0000-000000000006'
DEFAULT_PASS = '$2a$10$irAD4mqsdBY886W69EBexOYqv8LeAP9ViX6c0RFKlGRtzgoDmWpRq'

write(f"INSERT INTO tenants (id, code, name, status) VALUES ({sql_escape(TENANT_ID)}, 'SYS_TENANT', 'System Tenant', 'ACTIVE');")

write(f"INSERT INTO roles (id, tenant_id, code, name, system_role) VALUES ")
write(f"({sql_escape(ADMIN_ROLE)}, {sql_escape(TENANT_ID)}, 'TENANT_ADMIN', 'Tenant Admin', true),")
write(f"({sql_escape(HR_ROLE)}, {sql_escape(TENANT_ID)}, 'HR_ADMIN', 'HR Admin', true),")
write(f"({sql_escape(MANAGER_ROLE)}, {sql_escape(TENANT_ID)}, 'STORE_MANAGER', 'Store Manager', true),")
write(f"({sql_escape(EMPLOYEE_ROLE)}, {sql_escape(TENANT_ID)}, 'EMPLOYEE', 'Employee', true);")

org_units = []
users = []
user_roles = []
kpi_templates = []
kpis = []
kpi_values = []

# Org Hierarchy
hq_id = gen_uuid()
org_units.append((hq_id, 'COMPANY', 'HQ', 'KEMP Corp HQ', 'HQ', 0, None))

regions = [
    (gen_uuid(), 'NORTH', 'North Region', f'HQ.NORTH'),
    (gen_uuid(), 'SOUTH', 'South Region', f'HQ.SOUTH')
]
for r in regions:
    org_units.append((r[0], 'REGION', r[1], r[2], r[3], 1, hq_id))

stores = []
for i in range(1, 11): # 10 stores
    reg = random.choice(regions)
    s_id = gen_uuid()
    path = f"{reg[3]}.STORE_{i}"
    name = f"Store {i} ({reg[2]})"
    stores.append((s_id, 'STORE', f"STORE_{i}", name, path, 2, reg[0]))
    org_units.append(stores[-1])

departments = []
dept_types = ['SALES', 'OPERATION', 'CSAT']
for s in stores:
    for dt in dept_types:
        d_id = gen_uuid()
        path = f"{s[4]}.{dt}"
        name = f"{dt} Dept - {s[3]}"
        departments.append((d_id, 'DEPARTMENT', f"{s[2]}_{dt}", name, path, 3, s[0]))
        org_units.append(departments[-1])

write("INSERT INTO organization_units (id, tenant_id, type, code, name, path, level, parent_id) VALUES")
for i, o in enumerate(org_units):
    end = ";" if i == len(org_units)-1 else ","
    write(f"({sql_escape(o[0])}, {sql_escape(TENANT_ID)}, {sql_escape(o[1])}, {sql_escape(o[2])}, {sql_escape(o[3])}, {sql_escape(o[4])}, {o[5]}, {sql_escape(o[6])}){end}")

first_names = ['John', 'Jane', 'Michael', 'Sarah', 'David', 'Emma', 'Chris', 'Laura', 'Robert', 'Anna', 'James', 'Emily', 'William', 'Jessica', 'Joseph', 'Olivia', 'Charles', 'Sophia', 'Thomas', 'Ava']
last_names = ['Smith', 'Johnson', 'Williams', 'Brown', 'Jones', 'Garcia', 'Miller', 'Davis', 'Rodriguez', 'Martinez', 'Hernandez', 'Lopez', 'Gonzalez', 'Wilson', 'Anderson', 'Thomas', 'Taylor', 'Moore', 'Jackson', 'Martin']

def generate_users(count, org_id, role_id):
    for _ in range(count):
        u_id = gen_uuid()
        fn = random.choice(first_names)
        ln = random.choice(last_names)
        email = f"{fn.lower()}.{ln.lower()}.{random.randint(100,999)}@kemp.com"
        users.append((u_id, email, f"{fn} {ln}", org_id))
        user_roles.append((u_id, role_id))
        return u_id

# Admin
admin_id = gen_uuid()
users.append((admin_id, 'admin@kemp.com', 'Admin User', hq_id))
user_roles.append((admin_id, ADMIN_ROLE))

# Managers and Employees
all_employee_ids = []
for d in departments:
    # 1 manager
    mgr_id = gen_uuid()
    fn = random.choice(first_names)
    ln = random.choice(last_names)
    users.append((mgr_id, f"{fn.lower()}.{ln.lower()}.mgr@kemp.com", f"{fn} {ln} (Mgr)", d[0]))
    user_roles.append((mgr_id, MANAGER_ROLE))
    
    # 3-5 employees
    emp_count = random.randint(3, 5)
    for _ in range(emp_count):
        e_id = gen_uuid()
        fn = random.choice(first_names)
        ln = random.choice(last_names)
        users.append((e_id, f"{fn.lower()}.{ln.lower()}.{random.randint(100,999)}@kemp.com", f"{fn} {ln}", d[0]))
        user_roles.append((e_id, EMPLOYEE_ROLE))
        all_employee_ids.append(e_id)
        
write("INSERT INTO users (id, tenant_id, email, password_hash, full_name, status, organization_unit_id) VALUES")
for i, u in enumerate(users):
    end = ";" if i == len(users)-1 else ","
    write(f"({sql_escape(u[0])}, {sql_escape(TENANT_ID)}, {sql_escape(u[1])}, {sql_escape(DEFAULT_PASS)}, {sql_escape(u[2])}, 'ACTIVE', {sql_escape(u[3])}){end}")

write("INSERT INTO user_roles (user_id, role_id) VALUES")
for i, ur in enumerate(user_roles):
    end = ";" if i == len(user_roles)-1 else ","
    write(f"({sql_escape(ur[0])}, {sql_escape(ur[1])}){end}")

# Templates
tpls = [
    (gen_uuid(), 'TPL_REV', 'Revenue Goal', 'SALES', 'MONTHLY', 'actual / target', True),
    (gen_uuid(), 'TPL_CSAT', 'CSAT Score', 'CSAT', 'MONTHLY', 'actual / target', True),
    (gen_uuid(), 'TPL_TKT', 'Tickets Resolved', 'OPERATION', 'WEEKLY', 'actual / target', True)
]
write("INSERT INTO kpi_templates (id, tenant_id, code, name, category, default_frequency, formula, active) VALUES")
for i, t in enumerate(tpls):
    end = ";" if i == len(tpls)-1 else ","
    write(f"({sql_escape(t[0])}, {sql_escape(TENANT_ID)}, {sql_escape(t[1])}, {sql_escape(t[2])}, {sql_escape(t[3])}, {sql_escape(t[4])}, {sql_escape(t[5])}, true){end}")

# Generate KPIs for Employees
today = datetime.now()
first_day_current_month = today.replace(day=1)
first_day_last_month = (first_day_current_month - timedelta(days=1)).replace(day=1)
first_day_two_months_ago = (first_day_last_month - timedelta(days=1)).replace(day=1)

periods = [
    (first_day_two_months_ago, first_day_last_month - timedelta(days=1), 'CLOSED', 'Month -2'),
    (first_day_last_month, first_day_current_month - timedelta(days=1), 'CLOSED', 'Month -1'),
    (first_day_current_month, (first_day_current_month + timedelta(days=31)).replace(day=1) - timedelta(days=1), 'ACTIVE', 'Current Month')
]

# Random breached user ids to ensure we have bad metrics
bad_performers = random.sample(all_employee_ids, max(1, len(all_employee_ids)//5))

kpis_chunk = []
values_chunk = []

for emp_id in all_employee_ids:
    for period in periods:
        start_dt = period[0]
        end_dt = period[1]
        status = period[2]
        
        target = random.randint(100, 1000)
        
        if emp_id in bad_performers and status == 'CLOSED':
            actual = target * random.uniform(0.3, 0.7) # Bad
        else:
            actual = target * random.uniform(0.7, 1.2) # Normal to Good
            
        progress = (actual / target) * 100
        
        k_id = gen_uuid()
        tpl = random.choice(tpls)
        
        kpis_chunk.append((k_id, tpl[0], f"{tpl[2]} - {period[3]}", emp_id, target, status, start_dt.strftime('%Y-%m-%d'), end_dt.strftime('%Y-%m-%d'), progress))
        
        # Insert 3 values per KPI to show history
        step = (end_dt - start_dt).days // 3
        curr_actual = 0
        for step_idx in range(1, 4):
            v_date = start_dt + timedelta(days=step*step_idx)
            if v_date > today:
                v_date = today
            
            chunk_actual = actual / 3
            curr_actual += chunk_actual
            curr_prog = (curr_actual / target) * 100
            
            v_id = gen_uuid()
            values_chunk.append((v_id, k_id, start_dt.strftime('%Y-%m-%d'), v_date.strftime('%Y-%m-%d'), curr_actual, curr_prog, 'Progress update'))

            if v_date == today:
                break

write("INSERT INTO kpis (id, tenant_id, template_id, name, owner_type, owner_id, frequency, target_value, status, start_date, end_date, current_progress) VALUES")
for i, k in enumerate(kpis_chunk):
    end = ";" if i == len(kpis_chunk)-1 else ","
    write(f"({sql_escape(k[0])}, {sql_escape(TENANT_ID)}, {sql_escape(k[1])}, {sql_escape(k[2])}, 'USER', {sql_escape(k[3])}, 'MONTHLY', {k[4]}, {sql_escape(k[5])}, '{k[6]}', '{k[7]}', {k[8]:.2f}){end}")

write("INSERT INTO kpi_values (id, tenant_id, kpi_id, period_start, period_end, actual_value, progress_percent, comment) VALUES")
for i, v in enumerate(values_chunk):
    end = ";" if i == len(values_chunk)-1 else ","
    write(f"({sql_escape(v[0])}, {sql_escape(TENANT_ID)}, {sql_escape(v[1])}, '{v[2]}', '{v[3]}', {v[4]:.2f}, {v[5]:.2f}, {sql_escape(v[6])}){end}")

write("INSERT INTO alert_rules (id, tenant_id, name, description, condition_type, threshold_value, comparison_operator, severity, enabled) VALUES")
write(f"({sql_escape(gen_uuid())}, {sql_escape(TENANT_ID)}, 'Low Progress Alert', 'Alerts when progress is below 50%', 'PROGRESS', 50.0, 'LESS_THAN', 'CRITICAL', true);")

f.close()
print(f"Generated seed file successfully at {out_file}")
