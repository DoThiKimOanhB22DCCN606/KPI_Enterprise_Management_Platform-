describe('Core KPI Workflow', () => {
  beforeEach(() => {
    // Intercept login API and return a mock user
    const mockJwtPayload = 'eyJzdWIiOiJhZG1pbiIsICJyb2xlcyI6WyJURU5BTlRfQURNSU4iXSwgImZ1bGxOYW1lIjogIkFkbWluIFVzZXIifQ==';
    const fakeJwt = `fakeHeader.${mockJwtPayload}.fakeSignature`;

    cy.intercept('POST', '/v1/auth/login', {
      statusCode: 200,
      body: {
        accessToken: fakeJwt,
        refreshToken: 'fake-refresh-token'
      }
    }).as('loginApi');

    // Intercept getting KPIs list
    cy.intercept('GET', '/v1/kpis*', {
      statusCode: 200,
      body: {
        content: [
          { id: 'kpi-1', name: 'Existing KPI', status: 'APPROVED', currentValue: 10, targetValue: 100 }
        ],
        totalElements: 1
      }
    }).as('getKpis');
    
    // Intercept creating KPI
    cy.intercept('POST', '/v1/kpis', {
      statusCode: 201,
      body: {
        id: 'kpi-new', name: 'New E2E KPI', status: 'DRAFT', currentValue: 0, targetValue: 100
      }
    }).as('createKpi');
    
    // Intercept getting specific KPI
    cy.intercept('GET', '/v1/kpis/kpi-new', {
      statusCode: 200,
      body: {
        id: 'kpi-new', name: 'New E2E KPI', status: 'DRAFT', currentValue: 0, targetValue: 100,
        owner: { username: 'admin' }, periodStart: '2026-01-01', periodEnd: '2026-12-31'
      }
    }).as('getKpiDetail');

    // Intercept progress update
    cy.intercept('PUT', '/v1/kpis/kpi-new/progress', {
      statusCode: 200,
      body: { success: true }
    }).as('updateProgress');

    // Intercept KPI approval
    cy.intercept('POST', '/v1/kpis/kpi-new/approvals', {
      statusCode: 200,
      body: { success: true }
    }).as('approveKpi');

  });

  it('should complete the KPI lifecycle: Login -> Create -> Update Progress -> Approve', () => {
    // 1. Đăng nhập
    cy.visit('/login');
    cy.get('input[type="email"]').type('admin@example.com');
    cy.get('input[type="password"]').type('password123');
    cy.contains('button', /sign in/i).click();
    
    cy.wait('@loginApi');
    cy.url().should('include', '/dashboard');

    // 2. Vào màn hình KPI
    cy.contains('nav a', /KPIs/i).click();
    cy.url().should('include', '/kpis');
    cy.wait('@getKpis');

    // 3. Tạo KPI mới
    cy.contains('button', /Create KPI/i).click();
    cy.url().should('include', '/kpis/new');
    
    // Điền form KPI
    cy.get('input[name="name"]').type('New E2E KPI');
    cy.get('input[name="targetValue"]').type('100');
    cy.contains('button', /Save KPI/i).click();
    
    cy.wait('@createKpi');
    // Cypress should assert redirect or success toast
    cy.contains(/successfully/i).should('be.visible');

    // 4. Vào màn hình chi tiết và Cập nhật tiến độ
    cy.visit('/kpis/kpi-new');
    cy.wait('@getKpiDetail');
    cy.contains('New E2E KPI').should('be.visible');

    // Mở form Update Progress (giả sử có nút Record Progress)
    cy.contains('button', /Update Progress|Record Progress/i).click();
    cy.get('input[name="value"]').type('50');
    cy.get('textarea[name="note"]').type('Halfway there');
    cy.contains('button', /Save Progress/i).click();
    
    cy.wait('@updateProgress');
    cy.contains(/Progress recorded/i).should('be.visible');

    // 5. Duyệt KPI
    // Giả sử có nút Submit for Approval hoặc Approve
    cy.contains('button', /Submit/i).click();
    cy.wait('@approveKpi');
    cy.contains(/Status updated/i).should('be.visible');
  });
});
