describe('Dashboard Builder', () => {
  beforeEach(() => {
    // ==========================================
    // ARRANGE: Mocking Authentication and APIs
    // ==========================================
    
    // 1. Mock Authentication with standard JWT payload
    const mockJwtPayload = 'eyJzdWIiOiJhZG1pbiIsICJyb2xlcyI6WyJURU5BTlRfQURNSU4iXSwgImZ1bGxOYW1lIjogIkFkbWluIFVzZXIifQ==';
    const fakeJwt = `fakeHeader.${mockJwtPayload}.fakeSignature`;

    cy.intercept('POST', '/v1/auth/login', {
      statusCode: 200,
      body: {
        accessToken: fakeJwt,
        refreshToken: 'fake-refresh-token'
      }
    }).as('loginApi');

    // 2. Mock GET API for available widgets list
    cy.intercept('GET', '/v1/widgets*', {
      statusCode: 200,
      body: [
        { id: 'widget-1', type: 'BAR_CHART', name: 'Revenue Bar Chart' },
        { id: 'widget-2', type: 'PIE_CHART', name: 'KPI Distribution' }
      ]
    }).as('getWidgets');

    // 3. Mock POST API to save the new dashboard
    cy.intercept('POST', '/v1/dashboards', {
      statusCode: 201,
      body: {
        id: 'dashboard-new-1',
        name: 'Executive Dashboard',
        widgets: [
          { widgetId: 'widget-1', x: 0, y: 0, w: 6, h: 4 }
        ]
      }
    }).as('saveDashboard');

    // Execute Login before the test
    cy.visit('/login');
    cy.get('input[type="email"]').type('admin@example.com');
    cy.get('input[type="password"]').type('password123');
    cy.contains('button', /sign in/i).click();
    
    cy.wait('@loginApi');
  });

  it('should allow user to drag and drop widgets, input a name, and save the dashboard', () => {
    // ==========================================
    // ACT: UI Interactions
    // ==========================================

    // 1. Navigate to Dashboard Builder screen
    cy.visit('/dashboard/builder');
    // We don't need to wait for getWidgets because widgets are hardcoded in the UI palette right now.

    // 3. Simulate Drag and Drop of a Widget into the dropzone
    const dataTransfer = new DataTransfer();
    
    cy.contains('.cursor-grab', 'BAR CHART')
      .trigger('dragstart', { dataTransfer });
      
    cy.get('.react-grid-layout')
      .trigger('drop', { dataTransfer, clientX: 100, clientY: 100 })
      .trigger('dragend');

    // 4. Click Save button to open the modal
    cy.contains('button', 'Save').click();

    // 5. Input Dashboard Name in the modal
    cy.get('input[placeholder="e.g. Q3 Sales Overview"]').type('Executive Dashboard');

    // 6. Click Save Layout
    cy.contains('button', 'Save Layout').click();

    // ==========================================
    // ASSERT: Validation
    // ==========================================

    // 1. Assert the Save Dashboard API was called with the correct payload
    cy.wait('@saveDashboard').then((interception) => {
      expect(interception.request.body).to.have.property('name', 'Executive Dashboard');
      const layoutData = JSON.parse(interception.request.body.layoutJson);
      expect(layoutData.widgets).to.be.an('array').that.is.not.empty;
    });

    // 2. Assert success toast/message is displayed to the user
    cy.contains(/Dashboard saved successfully/i).should('be.visible');
  });
});
