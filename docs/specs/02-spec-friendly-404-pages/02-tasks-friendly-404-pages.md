# 02-tasks-friendly-404-pages.md

## Relevant Files

### Files to Create
- `src/main/java/org/springframework/samples/petclinic/system/GlobalExceptionHandler.java` - @ControllerAdvice class to handle IllegalArgumentException globally and return 404 responses
- `src/main/resources/templates/notFound.html` - User-friendly 404 error page with Liatrio branding and "Find Owners" navigation link
- `e2e-tests/tests/404-error-handling.spec.ts` - Playwright E2E test verifying 404 page functionality and navigation

### Files to Modify
- `src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java` - Add JUnit test methods for missing owner scenarios (404 status, notFound view, model attributes)
- `src/test/java/org/springframework/samples/petclinic/owner/PetControllerTests.java` - Add JUnit test methods for missing pet scenarios
- `src/main/resources/messages/messages.properties` - Add error.owner.notFound, error.pet.notFound, error.notFound.action keys (default/English)
- `src/main/resources/messages/messages_de.properties` - Add German translations for error message keys
- `src/main/resources/messages/messages_en.properties` - Add explicit English translations for error message keys
- `src/main/resources/messages/messages_es.properties` - Add Spanish translations for error message keys
- `src/main/resources/messages/messages_fa.properties` - Add Persian/Farsi translations for error message keys
- `src/main/resources/messages/messages_ko.properties` - Add Korean translations for error message keys
- `src/main/resources/messages/messages_pt.properties` - Add Portuguese translations for error message keys
- `src/main/resources/messages/messages_ru.properties` - Add Russian translations for error message keys
- `src/main/resources/messages/messages_tr.properties` - Add Turkish translations for error message keys

### Notes

- Unit tests should be placed in the same package structure as the main code (e.g., `system` package tests in `src/test/.../system/`)
- Use the repository's testing commands: `./mvnw test` for JUnit tests, `cd e2e-tests && npm test` for Playwright
- Follow Spring Boot naming conventions: test classes named `*Tests`, @ControllerAdvice named `*ExceptionHandler`
- Include Apache License header on all new Java files
- Follow TDD strictly: write failing tests first (RED), implement to pass (GREEN), then refactor
- Use `@DisabledInNativeImage` and `@DisabledInAotMode` annotations on test classes where appropriate
- Commit after each major phase with conventional commit format: `feat(error-handling): <description>`

## Tasks

### [x] 1.0 Write Failing JUnit Tests for 404 Handling (RED Phase)

#### 1.0 Proof Artifact(s)

- Test output: `./mvnw test -Dtest=OwnerControllerTests` console output demonstrates new test methods exist and are failing with expected assertions (404 status, "notFound" view name)
- Test output: Console output shows tests expecting 404 status code but currently getting exception or different behavior

#### 1.0 Tasks

- [x] 1.1 Read existing `OwnerControllerTests.java` to understand test patterns (use MockMvc, Mockito mocking, Hamcrest matchers)
- [x] 1.2 Add test method `testShowOwnerNotFound()` that mocks repository to return `Optional.empty()` for owner ID 999
- [x] 1.3 In test method, assert HTTP status is 404 using `status().isNotFound()`
- [x] 1.4 Assert view name is "notFound" using `view().name("notFound")`
- [x] 1.5 Assert model contains error message attribute using `model().attributeExists("errorMessage")`
- [x] 1.6 Add similar test method `testShowOwnerNotFoundInEdit()` for edit endpoint `/owners/{ownerId}/edit`
- [x] 1.7 Read existing `PetControllerTests.java` to understand test patterns
- [x] 1.8 Add test method `testShowPetNotFound()` for missing pet scenario
- [x] 1.9 Run `./mvnw test -Dtest=OwnerControllerTests,PetControllerTests` and verify tests fail as expected (no exception handler exists yet)
- [x] 1.10 Review test output and confirm failure reasons align with expectations (likely seeing 500 error or exception instead of 404)
- [x] 1.11 Commit failing tests with message: `test(error-handling): add failing tests for 404 owner/pet scenarios`

### [x] 2.0 Implement Global Exception Handler (GREEN Phase)

#### 2.0 Proof Artifact(s)

- Test output: `./mvnw test -Dtest=OwnerControllerTests` console output demonstrates all 404 tests now pass
- Test output: Console output shows 404 HTTP status code returned for missing owner/pet scenarios
- Test output: JUnit test output demonstrates model attributes contain expected error context (error message, resource type)

#### 2.0 Tasks

- [x] 2.1 Create new file `GlobalExceptionHandler.java` in `src/main/java/org/springframework/samples/petclinic/system/` package
- [x] 2.2 Add Apache License header (copy from existing files like `CacheConfiguration.java`)
- [x] 2.3 Add class declaration with `@ControllerAdvice` annotation to enable global exception handling
- [x] 2.4 Add `@ExceptionHandler(IllegalArgumentException.class)` method with signature `public ModelAndView handleNotFound(IllegalArgumentException ex)`
- [x] 2.5 Inside handler, parse exception message using `ex.getMessage()` to determine if it contains "Owner" or "Pet"
- [x] 2.6 Create `ModelAndView` object with view name "notFound"
- [x] 2.7 Set HTTP status to `HttpStatus.NOT_FOUND` using `mav.setStatus(HttpStatus.NOT_FOUND)`
- [x] 2.8 Add error context to model: `mav.addObject("errorMessage", userFriendlyMessage)`, `mav.addObject("status", 404)`
- [x] 2.9 Add fallback logic if message parsing fails (generic "Resource not found" message)
- [x] 2.10 Run `./mvnw test -Dtest=OwnerControllerTests,PetControllerTests` and verify all 404 tests now pass
- [x] 2.11 Run `./mvnw test jacoco:report` to generate code coverage report
- [x] 2.12 Verify GlobalExceptionHandler has 100% line coverage in JaCoCo report at `target/site/jacoco/index.html`
- [x] 2.13 Commit exception handler with message: `feat(error-handling): add global exception handler for 404 responses`

### [x] 3.0 Create User-Friendly Not Found Template (GREEN Phase)

#### 3.0 Proof Artifact(s)

- Screenshot: Browser showing friendly 404 page at `http://localhost:8080/owners/99999` demonstrates user-friendly error message displays
- Screenshot: 404 page showing "Find Owners" button/link demonstrates navigation element is present and styled
- Manual verification: Clicking "Find Owners" link navigates to `/owners/find` demonstrates functional navigation

#### 3.0 Tasks

- [x] 3.1 Read existing `error.html` template to understand Liatrio branding patterns (liatrio-section, liatrio-error-card classes)
- [x] 3.2 Create new file `notFound.html` in `src/main/resources/templates/` directory
- [x] 3.3 Add DOCTYPE declaration: `<!DOCTYPE html>`
- [x] 3.4 Add HTML tag with Thymeleaf namespace: `<html xmlns:th="https://www.thymeleaf.org" th:replace="~{fragments/layout :: layout (~{::body},'error')}">`
- [x] 3.5 Create body section with `<section class="liatrio-section">` wrapper
- [x] 3.6 Add `<div class="liatrio-error-card">` container for error content
- [x] 3.7 Include pets image: `<img th:src="@{/resources/images/pets.png}" alt="Pets at the clinic" />`
- [x] 3.8 Add heading: `<h2>We couldn't find what you're looking for</h2>`
- [x] 3.9 Add dynamic error message paragraph: `<p th:text="${errorMessage}">Resource not found</p>`
- [x] 3.10 Add "Find Owners" button with proper styling: `<a th:href="@{/owners/find}" class="btn btn-primary">Find Owners</a>`
- [x] 3.11 Start application with `./mvnw spring-boot:run`
- [x] 3.12 Navigate to `http://localhost:8080/owners/99999` in browser
- [x] 3.13 Verify friendly error message displays (no stack trace)
- [x] 3.14 Verify "Find Owners" button/link is visible and styled
- [x] 3.15 Click "Find Owners" link and verify navigation to `/owners/find` works
- [x] 3.16 Take screenshot of 404 page and save to `docs/specs/02-spec-friendly-404-pages/proof-artifacts/404-page-screenshot.png`
- [x] 3.17 Commit template with message: `feat(error-handling): add user-friendly notFound template with navigation`

### [x] 4.0 Add Internationalization Support (GREEN Phase)

#### 4.0 Proof Artifact(s)

- Code review: All 9 message property files contain new keys (`error.owner.notFound`, `error.pet.notFound`, `error.notFound.action`) demonstrates i18n coverage
- Test output: Manual verification or test output demonstrates error messages display in multiple locales (e.g., English, German, Spanish)
- Diff: Git diff of message files demonstrates all required translations added

#### 4.0 Tasks

- [x] 4.1 Open `src/main/resources/messages/messages.properties` (default/English)
- [x] 4.2 Add key: `error.owner.notFound=We couldn't find that owner. Please search again or verify the ID.`
- [x] 4.3 Add key: `error.pet.notFound=We couldn't find that pet. Please search again or verify the ID.`
- [x] 4.4 Add key: `error.notFound.action=You can search for owners using the button below.`
- [x] 4.5 Open `src/main/resources/messages/messages_en.properties` and add same three keys
- [x] 4.6 Open `src/main/resources/messages/messages_de.properties` (German) and add: `error.owner.notFound=Wir konnten diesen Besitzer nicht finden. Bitte suchen Sie erneut oder überprüfen Sie die ID.`
- [x] 4.7 Add German keys: `error.pet.notFound=Wir konnten dieses Haustier nicht finden. Bitte suchen Sie erneut oder überprüfen Sie die ID.`
- [x] 4.8 Add German key: `error.notFound.action=Sie können mit der Schaltfläche unten nach Besitzern suchen.`
- [x] 4.9 Open `src/main/resources/messages/messages_es.properties` (Spanish) and add: `error.owner.notFound=No pudimos encontrar ese propietario. Por favor, busque nuevamente o verifique el ID.`
- [x] 4.10 Add Spanish keys: `error.pet.notFound=No pudimos encontrar esa mascota. Por favor, busque nuevamente o verifique el ID.`
- [x] 4.11 Add Spanish key: `error.notFound.action=Puede buscar propietarios usando el botón de abajo.`
- [x] 4.12 Open `src/main/resources/messages/messages_ko.properties` (Korean) and add: `error.owner.notFound=해당 소유자를 찾을 수 없습니다. 다시 검색하거나 ID를 확인해 주세요.`
- [x] 4.13 Add Korean keys: `error.pet.notFound=해당 반려동물을 찾을 수 없습니다. 다시 검색하거나 ID를 확인해 주세요.`
- [x] 4.14 Add Korean key: `error.notFound.action=아래 버튼을 사용하여 소유자를 검색할 수 있습니다.`
- [x] 4.15 Open `src/main/resources/messages/messages_fa.properties` (Persian) and add: `error.owner.notFound=ما نتوانستیم آن مالک را پیدا کنیم. لطفاً دوباره جستجو کنید یا شناسه را بررسی کنید.`
- [x] 4.16 Add Persian keys: `error.pet.notFound=ما نتوانستیم آن حیوان خانگی را پیدا کنیم. لطفاً دوباره جستجو کنید یا شناسه را بررسی کنید.`
- [x] 4.17 Add Persian key: `error.notFound.action=می‌توانید با استفاده از دکمه زیر مالکان را جستجو کنید.`
- [x] 4.18 Open `src/main/resources/messages/messages_pt.properties` (Portuguese) and add: `error.owner.notFound=Não conseguimos encontrar esse proprietário. Por favor, pesquise novamente ou verifique o ID.`
- [x] 4.19 Add Portuguese keys: `error.pet.notFound=Não conseguimos encontrar esse animal de estimação. Por favor, pesquise novamente ou verifique o ID.`
- [x] 4.20 Add Portuguese key: `error.notFound.action=Você pode pesquisar proprietários usando o botão abaixo.`
- [x] 4.21 Open `src/main/resources/messages/messages_ru.properties` (Russian) and add: `error.owner.notFound=Мы не смогли найти этого владельца. Пожалуйста, повторите поиск или проверьте ID.`
- [x] 4.22 Add Russian keys: `error.pet.notFound=Мы не смогли найти этого питомца. Пожалуйста, повторите поиск или проверьте ID.`
- [x] 4.23 Add Russian key: `error.notFound.action=Вы можете искать владельцев с помощью кнопки ниже.`
- [x] 4.24 Open `src/main/resources/messages/messages_tr.properties` (Turkish) and add: `error.owner.notFound=Bu sahip bulunamadı. Lütfen tekrar arayın veya ID'yi doğrulayın.`
- [x] 4.25 Add Turkish keys: `error.pet.notFound=Bu evcil hayvan bulunamadı. Lütfen tekrar arayın veya ID'yi doğrulayın.`
- [x] 4.26 Add Turkish key: `error.notFound.action=Aşağıdaki düğmeyi kullanarak sahipleri arayabilirsiniz.`
- [x] 4.27 Update `notFound.html` template to use i18n keys instead of hardcoded text
- [x] 4.28 Replace hardcoded error message with: `<p th:text="#{error.owner.notFound}">Default message</p>` (use conditional logic based on error type if needed)
- [x] 4.29 Add action message: `<p class="liatrio-muted" th:text="#{error.notFound.action}">Action guidance</p>`
- [x] 4.30 Test with English locale by navigating to `http://localhost:8080/owners/99999`
- [x] 4.31 Test with another locale (e.g., German) by adding `?lang=de` to URL or setting browser locale
- [x] 4.32 Verify error messages display in the correct language
- [x] 4.33 Run `git diff src/main/resources/messages/` to review all i18n changes
- [x] 4.34 Commit i18n changes with message: `feat(error-handling): add i18n support for 404 error messages in 8 languages`

### [x] 5.0 Implement Playwright End-to-End Tests

#### 5.0 Proof Artifact(s)

- Test output: `cd e2e-tests && npm test -- --grep "404"` console output demonstrates E2E test passes
- Test output: Playwright test report shows verification of 404 status code, friendly error message, and "Find Owners" link presence
- Screenshot: Playwright test artifacts showing 404 page captured during test execution demonstrates E2E flow validation

#### 5.0 Tasks

- [x] 5.1 Read existing `e2e-tests/tests/smoke.spec.ts` to understand Playwright test patterns and setup
- [x] 5.2 Create new file `404-error-handling.spec.ts` in `e2e-tests/tests/` directory
- [x] 5.3 Add import statements: `import { test, expect } from '@playwright/test';`
- [x] 5.4 Create test suite: `test.describe('404 Error Handling', () => { ... });`
- [x] 5.5 Add test case: `test('should show friendly 404 page for non-existent owner', async ({ page }) => { ... });`
- [x] 5.6 Navigate to non-existent owner: `await page.goto('/owners/99999');`
- [x] 5.7 Wait for page load and verify URL: `await page.waitForLoadState('networkidle');`
- [x] 5.8 Assert friendly error message is visible: `await expect(page.locator('text=couldn\\'t find')).toBeVisible();`
- [x] 5.9 Assert no stack trace is visible: `await expect(page.locator('text=Exception')).not.toBeVisible();`
- [x] 5.10 Assert "Find Owners" link exists: `await expect(page.locator('a:has-text("Find Owners")')).toBeVisible();`
- [x] 5.11 Add test case for clicking "Find Owners" link: `test('should navigate to owner search from 404 page', async ({ page }) => { ... });`
- [x] 5.12 In second test, navigate to 404 page then click link: `await page.click('a:has-text("Find Owners")');`
- [x] 5.13 Assert navigation to search page: `await expect(page).toHaveURL('/owners/find');`
- [x] 5.14 Add test case for missing pet: `test('should show friendly 404 page for non-existent pet', async ({ page }) => { ... });`
- [x] 5.15 Navigate to non-existent pet (use valid owner ID + invalid pet ID): `await page.goto('/owners/1/pets/99999/edit');`
- [x] 5.16 Verify friendly error message displays for pet scenario
- [x] 5.17 Run Playwright tests: `cd e2e-tests && npm test -- --grep "404"`
- [x] 5.18 Verify all 404 tests pass in console output
- [x] 5.19 Review Playwright HTML report: `cd e2e-tests && npm run report`
- [x] 5.20 Check test artifacts (screenshots/traces) in `e2e-tests/test-results/` directory
- [x] 5.21 Commit E2E tests with message: `test(error-handling): add Playwright E2E tests for 404 error handling`
- [x] 5.22 Create final proof artifact showing all tests passing: run `./mvnw test && cd e2e-tests && npm test` and capture output
- [x] 5.23 Update issue #12 with completion status and link to committed proof artifacts
