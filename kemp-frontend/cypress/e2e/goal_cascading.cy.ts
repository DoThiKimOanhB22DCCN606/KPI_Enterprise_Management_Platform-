describe('Goal Cascading Flow', () => {
  beforeEach(() => {
    // ==========================================
    // ARRANGE: Mocking Authentication and APIs
    // ==========================================
    
    // 1. Mock Authentication using standard JWT payload
    const mockJwtPayload = 'eyJzdWIiOiJhZG1pbiIsICJyb2xlcyI6WyJURU5BTlRfQURNSU4iXSwgImZ1bGxOYW1lIjogIkFkbWluIFVzZXIifQ==';
    const fakeJwt = `fakeHeader.${mockJwtPayload}.fakeSignature`;

    cy.intercept('POST', '/v1/auth/login', {
      statusCode: 200,
      body: {
        accessToken: fakeJwt,
        refreshToken: 'fake-refresh-token'
      }
    }).as('loginApi');

    // 2. Mock POST API to create a Company-level Goal
    cy.intercept('POST', '/v1/goals', (req) => {
      if (req.body.level === 'COMPANY') {
        req.reply({
          statusCode: 201,
          body: {
            id: 'goal-company-1',
            title: 'Increase Q3 Revenue by 20%',
            level: 'COMPANY'
          }
        });
      } else {
        // Mock POST API for Department-level Goal (Cascaded)
        req.reply({
          statusCode: 201,
          body: {
            id: 'goal-dept-1',
            title: 'Launch New Marketing Campaign',
            level: 'DEPARTMENT',
            parentGoalId: 'goal-company-1'
          }
        });
      }
    }).as('createGoal');

    // 3. Mock GET API to fetch Company Goals (Used for the parent goal dropdown selection)
    cy.intercept('GET', '/v1/goals*', {
      statusCode: 200,
      body: {
        content: [
          { id: 'goal-company-1', title: 'Increase Q3 Revenue by 20%', level: 'COMPANY' }
        ],
        totalElements: 1
      }
    }).as('getCompanyGoals');

    // Execute Login before the test
    cy.visit('/login');
    cy.get('input[type="email"]').type('admin@example.com');
    cy.get('input[type="password"]').type('password123');
    cy.contains('button', /sign in/i).click();
    cy.wait('@loginApi');
  });

  it('should create a Company Goal and then create a Department Goal linked to it', () => {
    // ==========================================
    // ACT: UI Interactions - Create Company Goal
    // ==========================================

    // Navigate to Goals page
    cy.visit('/goals');
    cy.wait('@getCompanyGoals'); // initial load
    
    // Click 'Create Goal' to open the modal
    cy.contains('button', 'Create Goal').click();

    // Select goal level: COMPANY
    cy.get('select[name="level"]').select('COMPANY');

    // Fill Company Goal form
    cy.get('input[name="title"]').type('Increase Q3 Revenue by 20%');
    cy.get('textarea[name="description"]').type('Focus on upselling to existing enterprise clients.');
    cy.contains('button', 'Save Goal').click();
    
    // Wait and assert Company Goal was saved
    cy.wait('@createGoal').then((interception) => {
      expect(interception.request.body).to.have.property('level', 'COMPANY');
    });
    // Modal closes automatically, assume success

    // ==========================================
    // ACT: UI Interactions - Create Department Goal (Cascaded)
    // ==========================================

    // Click 'Create Goal' to open the modal again
    cy.contains('button', 'Create Goal').click();
    
    // Select goal level: DEPARTMENT
    cy.get('select[name="level"]').select('DEPARTMENT');

    // Fill Department Goal form
    cy.get('input[name="title"]').type('Launch New Marketing Campaign');
    
    // Link to the Parent Company Goal
    cy.get('select[name="parentGoalId"]').select('goal-company-1');

    cy.contains('button', 'Save Goal').click();

    // ==========================================
    // ASSERT: Validation
    // ==========================================

    // Assert the Department Goal creation API was called with the parent link
    cy.wait('@createGoal').then((interception) => {
      expect(interception.request.body).to.have.property('level', 'DEPARTMENT');
      expect(interception.request.body).to.have.property('parentGoalId', 'goal-company-1');
    });
  });
});
