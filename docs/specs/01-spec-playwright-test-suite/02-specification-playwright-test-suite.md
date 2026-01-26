# 01 Specification - Playwright Test Suite

## Overview

This specification defines the implementation of a comprehensive Playwright test suite for the Emerald Grove Veterinary Clinic application. The test suite will provide end-to-end browser automation testing that complements the existing unit and integration test infrastructure while following the project's strict TDD methodology. **Technology Choice: JavaScript/TypeScript Playwright** for maximum feature access and tooling support.

## Context

- **Application**: Emerald Grove Veterinary Clinic - Veterinary clinic management system
- **Technology Stack**: Spring Boot 4.0.0, Maven, Java 17, Thymeleaf, Bootstrap 5
- **Existing Testing**: JUnit 5, Mockito, TestContainers, JaCoCo with 90%+ coverage requirements
- **Testing Philosophy**: Strict TDD with Red-Green-Refactor cycle
- **E2E Testing**: JavaScript/TypeScript Playwright (separate from Java backend testing)

## Functional Requirements

### User Stories

#### US-01: Critical User Journey Coverage

**As a** developer
**I want** automated tests for critical user journeys
**So that** I can ensure core functionality works end-to-end

**Acceptance Criteria:**

- Owner registration workflow (find → add → edit → view details)
- Pet management workflow (add → edit → schedule visit)
- Veterinarian lookup and browsing
- Navigation between all major pages
- Form validation and error handling

#### US-02: Full Application Coverage

**As a** quality assurance engineer
**I want** comprehensive coverage of all application pages
**So that** I can detect regressions across the entire UI

**Acceptance Criteria:**

- All pages accessible and functional
- All forms submit and validate correctly
- Error scenarios handled gracefully
- Responsive design works on different viewports

#### US-03: Core Business Flow Testing

**As a** product owner
**I want** the complete Owner-Pet-Visit workflow tested
**So that** I can ensure the core business process functions correctly

**Acceptance Criteria:**

- Complete owner registration with valid data
- Pet addition to existing owners
- Visit scheduling with proper data validation
- Data persistence and retrieval verification

## Technical Requirements

### Architecture Integration

#### Test Suite Structure

```text
e2e-tests/
├── playwright.config.ts          # Playwright configuration
├── package.json                   # Node.js dependencies
├── tsconfig.json                  # TypeScript configuration
├── tests/                         # Test files (default testDir)
│   ├── fixtures/                  # Test data and setup
│   │   └── base-test.ts
│   ├── pages/                     # Page Object Model
│   │   ├── base-page.ts
│   │   ├── home-page.ts
│   │   ├── owner-page.ts
│   │   ├── pet-page.ts
│   │   ├── vet-page.ts
│   │   └── visit-page.ts
│   ├── features/                  # Feature-based test organization
│   │   ├── owner-management.spec.ts
│   │   ├── pet-management.spec.ts
│   │   ├── vet-directory.spec.ts
│   │   └── visit-scheduling.spec.ts
│   └── global-setup.ts             # Global test setup/teardown
├── utils/                         # Utility classes
│   ├── test-helpers.ts
│   └── data-factory.ts
└── test-results/                  # Test reports and screenshots (default output)
```

#### Dependencies

```json
{
  "name": "petclinic-e2e-tests",
  "version": "1.0.0",
  "devDependencies": {
    "@playwright/test": "^1.48.0",
    "@types/node": "^20.0.0",
    "typescript": "^5.0.0"
  },
  "scripts": {
    "test": "playwright test",
    "test:headed": "playwright test --headed",
    "test:debug": "playwright test --debug",
    "report": "playwright show-report"
  }
}
```

### Test Environment Strategy

#### Standalone E2E Suite

- Separate from existing unit/integration tests
- Independent Node.js project in `e2e-tests/` directory
- Tests running Spring Boot application on port 8080
- Leverages existing H2 in-memory database with sample data
- Executable via npm scripts: `npm test`

#### Configuration

```typescript
// playwright.config.ts
import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './tests',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  reporter: [
    ['html', { outputFolder: 'test-results/html-report' }],
    ['json', { outputFile: 'test-results/results.json' }],
    ['junit', { outputFile: 'test-results/results.xml' }],
  ],
  use: {
    baseURL: 'http://localhost:8080',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
  },
  projects: [
    {
      name: 'setup',
      testMatch: /global\.setup\.ts/,
    },
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
      dependencies: ['setup'],
    },
  ],
  webServer: {
    command: 'cd .. && ./mvnw spring-boot:run',
    port: 8080,
    reuseExistingServer: !process.env.CI,
    timeout: 120 * 1000,
  },
  outputDir: 'test-results',
  snapshotPathTemplate: '{testDir}/{testFileDir}/{testName}-{arg}{ext}',
});
```

### Data Management

#### Existing H2 Sample Data Strategy

- Leverage current test database fixtures and sample data
- No additional test data factories required initially
- Application handles database reset between test runs
- Predictable test data for consistent test results

#### Test Data Access

```typescript
// utils/data-factory.ts
export class TestDataFactory {
  static getTestOwner() {
    return {
      firstName: 'George',
      lastName: 'Franklin',
      address: '110 W. Liberty St.',
      city: 'Madison',
      telephone: '6085551023',
    };
  }

  static getTestPet() {
    return {
      name: 'Max',
      birthDate: '2020-09-04',
      type: 'dog',
    };
  }

  static getTestVisit() {
    return {
      date: new Date().toISOString().split('T')[0],
      description: 'Regular checkup',
    };
  }
}
```

### Browser Testing Strategy

#### Desktop Chrome Focus

- Primary testing on Chromium/Chrome browser
- Headless mode for CI/CD execution
- Headed mode available for local debugging
- Standard desktop viewport (1920x1080)

#### Responsive Validation

- Mobile viewport emulation for critical flows
- Tablet viewport testing for form layouts
- Responsive design verification

### Performance Optimization

#### Parallel Execution

```typescript
// Tests run in parallel by default in Playwright
// Configuration in playwright.config.ts
fullyParallel: true,
workers: process.env.CI ? 1 : undefined,
```

#### Efficient Waits

- Playwright's auto-waiting capabilities
- Minimal explicit waits
- Smart selectors for reliable element detection
- Test isolation to prevent interference

## Implementation Details

### Page Object Model Implementation

#### Base Page Structure

```typescript
// pages/base-page.ts
import { expect, type Page, type Locator } from '@playwright/test';

export abstract class BasePage {
  readonly page: Page;
  readonly baseUrl: string;

  constructor(page: Page, baseUrl: string) {
    this.page = page;
    this.baseUrl = baseUrl;
  }

  async navigate(path: string): Promise<void> {
    await this.page.goto(this.baseUrl + path);
    await this.page.waitForLoadState('networkidle');
  }

  async getTitle(): Promise<string> {
    return await this.page.title();
  }

  async takeScreenshot(name: string): Promise<void> {
    await this.page.screenshot({
      path: `test-results/${name}.png`,
      fullPage: true
    });
  }

  async verifyElementVisible(locator: Locator): Promise<void> {
    await expect(locator).toBeVisible();
  }

  async verifyElementContainsText(locator: Locator, text: string): Promise<void> {
    await expect(locator).toContainText(text);
  }
}
```

#### Specific Page Implementation

```typescript
// pages/owner-page.ts
import { expect, type Page, type Locator } from '@playwright/test';
import { BasePage } from './base-page';

export class OwnerPage extends BasePage {
  readonly lastNameInput: Locator;
  readonly findButton: Locator;
  readonly addOwnerButton: Locator;
  readonly ownerTable: Locator;
  readonly successMessage: Locator;

  constructor(page: Page, baseUrl: string) {
    super(page, baseUrl);
    this.lastNameInput = page.getByLabel('Last Name');
    this.findButton = page.getByRole('button', { name: 'Find Owner' });
    this.addOwnerButton = page.getByRole('link', { name: 'Add Owner' });
    this.ownerTable = page.locator('table.table');
    this.successMessage = page.locator('.alert-success');
  }

  async searchOwner(lastName: string): Promise<void> {
    await this.lastNameInput.fill(lastName);
    await this.findButton.click();
  }

  async clickAddOwner(): Promise<void> {
    await this.addOwnerButton.click();
  }

  async isOwnerListDisplayed(): Promise<boolean> {
    return await this.ownerTable.isVisible();
  }

  async getOwnerByName(name: string): Promise<Locator> {
    return this.page.getByRole('row').filter({ hasText: name });
  }

  async verifyOwnerFound(ownerName: string): Promise<void> {
    await this.verifyElementVisible(this.ownerTable);
    await this.verifyElementContainsText(this.getOwnerByName(ownerName), ownerName);
  }
}
```

### Feature-Based Test Organization

#### Owner Management Tests

```typescript
// features/owner-management.spec.ts
import { test, expect } from '@playwright/test';
import { OwnerPage } from '../pages/owner-page';
import { TestDataFactory } from '../utils/data-factory';

test.describe('Owner Management', () => {
  let ownerPage: OwnerPage;

  test.beforeEach(async ({ page }) => {
    ownerPage = new OwnerPage(page, 'http://localhost:8080');
  });

  test('should find existing owner by last name', async ({ page }) => {
    // Arrange
    await ownerPage.navigate('/owners/find');

    // Act
    await ownerPage.searchOwner('Franklin');

    // Assert
    await ownerPage.verifyOwnerFound('Franklin');
    await expect(page.getByRole('cell', { name: 'Franklin' })).toBeVisible();
  });

  test('should add new owner with valid data', async ({ page }) => {
    // Arrange
    const ownerData = TestDataFactory.getTestOwner();
    await ownerPage.navigate('/owners/find');
    await ownerPage.clickAddOwner();

    // Act
    await page.getByLabel('First Name').fill(ownerData.firstName);
    await page.getByLabel('Last Name').fill(ownerData.lastName);
    await page.getByLabel('Address').fill(ownerData.address);
    await page.getByLabel('City').fill(ownerData.city);
    await page.getByLabel('Telephone').fill(ownerData.telephone);
    await page.getByRole('button', { name: 'Add Owner' }).click();

    // Assert
    await expect(page.getByRole('heading', { name: 'Owner Information' })).toBeVisible();
    await expect(page.getByText(`${ownerData.firstName} ${ownerData.lastName}`)).toBeVisible();
  });

  test('should show validation error for invalid telephone number', async ({ page }) => {
    // Arrange
    await ownerPage.navigate('/owners/find');
    await ownerPage.clickAddOwner();

    // Act
    await page.getByLabel('First Name').fill('Test');
    await page.getByLabel('Last Name').fill('User');
    await page.getByLabel('Telephone').fill('invalid');
    await page.getByRole('button', { name: 'Add Owner' }).click();

    // Assert
    await expect(page.getByText('numeric value out of bounds')).toBeVisible();
  });
});
```

### Test Configuration and Utilities

#### TypeScript Configuration

```json
// tsconfig.json
{
  "compilerOptions": {
    "target": "ES2020",
    "module": "ESNext",
    "moduleResolution": "node",
    "strict": true,
    "esModuleInterop": true,
    "skipLibCheck": true,
    "forceConsistentCasingInFileNames": true,
    "baseUrl": ".",
    "paths": {
      "@/*": ["./tests/*"],
      "@/pages/*": ["./tests/pages/*"],
      "@/utils/*": ["./tests/utils/*"]
    }
  },
  "include": ["tests/**/*"],
  "exclude": ["node_modules", "reports"]
}
```

#### Global Setup Configuration

```typescript
// tests/global-setup.ts
import { test as setup, expect } from '@playwright/test';

setup('global setup', async ({ page }) => {
  // Ensure application is ready
  await page.goto('http://localhost:8080');
  await expect(page.getByRole('heading', { name: 'Welcome' })).toBeVisible();

  // Verify database is populated with test data
  await page.goto('http://localhost:8080/vets.html');
  await expect(page.locator('table.table')).toBeVisible();
});
```

### Playwright Best Practices

#### Locator Strategy

- **Prefer user-facing attributes**: Use `page.getByRole()`, `page.getByLabel()`, `page.getByText()` over CSS selectors
- **Avoid fragile selectors**: Don't rely on CSS classes or DOM structure that can change
- **Use chaining and filtering**: Chain locators to narrow down search scope
- **Leverage auto-waiting**: Playwright automatically waits for elements to be ready

#### Test Organization

- **Use describe blocks**: Group related tests in logical suites
- **Arrange-Act-Assert pattern**: Structure tests clearly with setup, action, and verification
- **Descriptive test names**: Use clear, human-readable test descriptions
- **Test isolation**: Each test should be independent and not rely on other tests

#### Error Handling

- **Use built-in assertions**: Playwright's `expect` provides auto-waiting and retry
- **Proper timeouts**: Configure appropriate timeouts for different scenarios
- **Trace on failure**: Enable traces for debugging failed tests
- **Screenshots on failure**: Capture visual evidence when tests fail

#### Performance Best Practices

- **Parallel execution**: Run tests in parallel where possible
- **Reuse browser contexts**: Share contexts between related tests
- **Efficient waits**: Use Playwright's auto-waiting instead of explicit waits
- **Proper cleanup**: Clean up resources after each test

## Non-Functional Requirements

### Performance Requirements

- Full test suite execution under 10 minutes
- Individual tests complete within 30 seconds
- Parallel execution reduces total runtime by 60%+
- Minimal resource consumption during execution

### Reliability Requirements

- 95%+ test success rate on stable builds
- Flaky test identification and resolution
- Consistent results across different environments
- Proper cleanup and isolation between tests

### Maintainability Requirements

- Clear test organization and naming conventions
- Reusable page objects and utility methods
- Comprehensive test documentation
- Easy addition of new test cases

## Success Metrics

### Code Coverage Metrics

- UI flow coverage: 100% of critical user journeys
- Page coverage: All application pages tested
- Form coverage: All forms and validation scenarios tested
- Navigation coverage: All navigation paths tested

### Quality Metrics

- Test execution time: < 10 minutes for full suite
- Test reliability: > 95% consistent pass rate
- Bug detection: Identify UI regressions before deployment
- Developer adoption: Easy to run and maintain locally

### Integration Metrics

- CI/CD integration: Automated execution on pull requests
- Reporting: Detailed HTML reports with screenshots on failure
- Debugging: Headed mode support for local development
- Documentation: Complete setup and usage instructions

## Implementation Plan

### Phase 1: Infrastructure Setup

1. Initialize Node.js project in `e2e-tests/` directory
2. Install Playwright and TypeScript dependencies
3. Create playwright.config.ts and tsconfig.json
4. Set up GitHub Actions workflow for E2E tests

### Phase 2: Page Object Implementation

1. Create BasePage with common functionality
2. Implement page objects for all major pages
3. Add utility methods for common interactions
4. Create test data providers

### Phase 3: Feature Test Implementation

1. Owner management workflow tests
2. Pet management workflow tests
3. Veterinarian directory tests
4. Visit scheduling tests

### Phase 4: Advanced Features

1. Error scenario testing
2. Responsive design validation
3. Performance optimization
4. Reporting and debugging enhancements

## Dependencies and Constraints

### Technical Dependencies

- Spring Boot 4.0.0 application running on port 8080
- H2 in-memory database with sample data
- Node.js 18+ and npm for E2E test execution
- Existing test infrastructure (separate from E2E tests)

### Constraints

- Single developer maintenance overhead
- Chrome browser focus (no cross-browser complexity)
- Standalone test suite (separate from existing tests)
- Manual trigger + CI execution strategy

### Integration Points

- Spring Boot application startup/shutdown
- Current test database fixtures
- GitHub Actions CI/CD pipeline
- Independent from Maven build system

## Proof Artifacts

### Functional Proof

- **URL**: `http://localhost:8080` - Application home page
- **URL**: `http://localhost:8080/owners/find` - Owner search functionality
- **URL**: `http://localhost:8080/vets.html` - Veterinarian directory
- **CLI Output**: `npm test` - Test execution results
- **Screenshot**: Visual confirmation of successful test execution

### Technical Proof

- **Node.js Output**: Dependency installation and test compilation
- **HTML Report**: Playwright test results with screenshots
- **Coverage Report**: UI flow coverage metrics
- **CI Log**: GitHub Actions execution log

## Acceptance Criteria

### Must Have

- [ ] Playwright tests execute successfully against running application
- [ ] All critical user journeys automated and passing
- [ ] Page Object Model implemented for maintainability
- [ ] Tests organized by feature/user stories
- [ ] Independent Node.js project structure
- [ ] CI/CD pipeline execution with reporting

### Should Have

- [ ] Responsive design validation
- [ ] Error scenario testing
- [ ] Performance optimization with parallel execution
- [ ] Detailed debugging support with screenshots
- [ ] Comprehensive test documentation

### Could Have

- [ ] Cross-browser testing framework
- [ ] Advanced visual regression testing
- [ ] Performance benchmarking
- [ ] Accessibility testing integration

### Won't Have (Initial Release)

- [ ] Mobile device testing
- [ ] API testing integration
- [ ] Load testing capabilities
- [ ] Internationalization testing

---

This specification provides the foundation for implementing a comprehensive Playwright test suite using JavaScript/TypeScript that enhances the Emerald Grove Veterinary Clinic application's testing capabilities while maintaining alignment with existing development practices and TDD methodology.
