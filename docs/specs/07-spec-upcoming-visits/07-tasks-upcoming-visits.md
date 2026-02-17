# Task List: Upcoming Visits Page

**Feature ID**: Issue #8
**Spec**: `07-spec-upcoming-visits/SPEC.md`
**Created**: 2026-02-12

---

## Relevant Files

### Files to Create

- `src/main/java/org/springframework/samples/petclinic/owner/VisitRepository.java` - Repository interface for querying visits with filtering support
- `src/test/java/org/springframework/samples/petclinic/owner/VisitRepositoryTests.java` - Unit tests for VisitRepository query methods
- `src/main/java/org/springframework/samples/petclinic/owner/UpcomingVisitsController.java` - Controller for handling `/visits/upcoming` endpoint
- `src/test/java/org/springframework/samples/petclinic/owner/UpcomingVisitsControllerTests.java` - Unit tests for UpcomingVisitsController
- `src/main/resources/templates/visits/upcomingVisits.html` - Thymeleaf template for displaying upcoming visits
- `e2e-tests/tests/features/upcoming-visits.spec.ts` - Playwright E2E tests for upcoming visits feature
- `docs/specs/07-spec-upcoming-visits/07-proofs/` - Directory for proof artifacts (screenshots, test outputs, etc.)

### Files to Modify

- `src/main/resources/templates/fragments/layout.html` - Add "Upcoming Visits" navigation link
- `src/main/resources/messages/messages_en.properties` - Add English message keys for upcoming visits page
- `src/main/resources/messages/messages.properties` - Add default message keys for upcoming visits page

### Notes

- Unit tests should be placed alongside the code files they are testing in the same package structure
- Use Maven for running tests: `./mvnw test -Dtest=TestClassName`
- Follow the repository's TDD workflow: RED (failing test) → GREEN (minimal implementation) → REFACTOR
- Adhere to Spring Boot conventions: constructor injection, method naming, `@Controller`, `@GetMapping`
- Ensure >90% line coverage for new code (verified via JaCoCo)
- Follow existing Thymeleaf template patterns and Liatrio branding styles
- Use Bootstrap 5 responsive classes for mobile compatibility

---

## Tasks

### [x] 1.0 Create VisitRepository with Query Methods for Upcoming Visits

**Description**: Implement the data access layer by creating a VisitRepository interface that can query visits independently of the Pet entity relationship. This repository will provide query methods for finding upcoming visits with optional filtering by date range, pet type, and owner name.

#### 1.0 Proof Artifact(s)

- **Test**: `VisitRepositoryTests.java` with 8+ passing tests demonstrates repository query methods work correctly
- **Test**: Test showing visits filtered by date range (fromDate to toDate) demonstrates date filtering works
- **Test**: Test showing visits filtered by pet type demonstrates pet type filtering works
- **Test**: Test showing visits filtered by owner last name demonstrates owner name search works
- **Test**: Test showing combined filters (date + pet type + owner) demonstrates multi-filter support works
- **CLI**: Maven test execution output shows all repository tests passing demonstrates test coverage
- **SQL Log**: Hibernate query log showing JOIN FETCH operations demonstrates N+1 prevention

#### 1.0 Tasks

- [x] 1.1 **RED**: Create `VisitRepositoryTests.java` with test for `findUpcomingVisits(LocalDate fromDate)` returning all visits with date >= fromDate (should fail - repository doesn't exist yet)
- [x] 1.2 **GREEN**: Create `VisitRepository.java` interface extending `Repository<Visit, Integer>` with `findUpcomingVisits()` method signature
- [x] 1.3 **GREEN**: Add Spring Data JPA query method using method naming convention: `List<Visit> findByDateGreaterThanEqualOrderByDateAsc(LocalDate date)`
- [x] 1.4 **RED**: Add test for visits sorted by date in ascending order (should pass if query method is correct)
- [x] 1.5 **RED**: Add test for `findUpcomingVisits()` that verifies Pet and Owner are loaded (JOIN FETCH) to prevent N+1 queries (should fail - need custom query)
- [x] 1.6 **GREEN**: Replace method naming convention with `@Query` annotation using JOIN FETCH: `@Query("SELECT v FROM Visit v JOIN FETCH v.pet p JOIN FETCH p.owner WHERE v.date >= :fromDate ORDER BY v.date ASC")`
- [x] 1.7 **RED**: Add test for filtering by date range (fromDate and toDate) - should fail, method doesn't exist
- [x] 1.8 **GREEN**: Add query method `findByDateBetweenOrderByDateAsc(LocalDate fromDate, LocalDate toDate)` with JOIN FETCH
- [x] 1.9 **RED**: Add test for filtering by pet type (e.g., "dog") - should fail, method doesn't exist
- [x] 1.10 **GREEN**: Add custom query method with pet type filter using `@Query` with JOIN and WHERE clause on `p.type.name`
- [x] 1.11 **RED**: Add test for filtering by owner last name (case-insensitive) - should fail, method doesn't exist
- [x] 1.12 **GREEN**: Add custom query method with owner name filter using `@Query` with LOWER() for case-insensitive matching
- [x] 1.13 **RED**: Add test for combined filters (date + pet type + owner name) with all parameters optional (null handling)
- [x] 1.14 **GREEN**: Create comprehensive query method `findUpcomingVisitsWithFilters()` with conditional WHERE clauses handling null parameters
- [x] 1.15 **REFACTOR**: Extract query strings to constants, add JavaDoc comments, ensure all tests pass
- [x] 1.16 Run full test suite: `./mvnw test -Dtest=VisitRepositoryTests` and verify >90% coverage

---

### [x] 2.0 Create Basic Upcoming Visits Page with Navigation Integration

**Description**: Create a functional page at `/visits/upcoming` that displays all future visits (date >= today) in chronological order. Integrate the page into the main navigation bar and implement the controller, view template, and basic styling following existing patterns.

#### 2.0 Proof Artifact(s)

- **URL**: `http://localhost:8080/visits/upcoming` accessible and displays visits demonstrates page exists and works
- **Screenshot**: Navigation bar showing "Upcoming Visits" link between "Veterinarians" and "Error" demonstrates navigation integration
- **Screenshot**: Page displaying list of upcoming visits with columns (Visit Date, Pet Name, Owner Name, Description) sorted by date demonstrates core display functionality
- **Screenshot**: Empty state showing "No upcoming visits scheduled" when no future visits exist demonstrates empty state handling
- **Screenshot**: Mobile view (375x667) showing responsive layout with stacked table demonstrates mobile compatibility
- **Test**: `UpcomingVisitsControllerTests.java` with 6+ passing tests demonstrates controller logic works

#### 2.0 Tasks

- [x] 2.1 **RED**: Create `UpcomingVisitsControllerTests.java` with test for GET `/visits/upcoming` endpoint returning 200 OK (should fail - controller doesn't exist)
- [x] 2.2 **GREEN**: Create `UpcomingVisitsController.java` with `@Controller` annotation and constructor injecting `VisitRepository`
- [x] 2.3 **GREEN**: Add `@GetMapping("/visits/upcoming")` method that returns "visits/upcomingVisits" view name
- [x] 2.4 **RED**: Add test verifying model contains "visits" attribute with list of Visit objects (should fail - not added to model yet)
- [x] 2.5 **GREEN**: In controller method, call `visitRepository.findByDateGreaterThanEqualOrderByDateAsc(LocalDate.now())` and add to model
- [x] 2.6 **RED**: Add test for empty state when no upcoming visits exist (should pass if model handling is correct)
- [x] 2.7 Create directory: `src/main/resources/templates/visits/`
- [x] 2.8 **GREEN**: Create `upcomingVisits.html` template using `fragments/layout.html` layout with placeholder content
- [x] 2.9 Add page title `<h2>Upcoming Visits</h2>` and basic table structure with Bootstrap classes
- [x] 2.10 Add Thymeleaf iteration: `<tr th:each="visit : ${visits}">` to display visit data in table rows
- [x] 2.11 Add table columns: Visit Date (`th:text="${#temporals.format(visit.date, 'yyyy-MM-dd')}"`), Pet Name, Owner Name, Description
- [x] 2.12 Add empty state: `<div th:if="${#lists.isEmpty(visits)}">No upcoming visits scheduled</div>`
- [x] 2.13 Apply Bootstrap responsive classes: `table`, `table-striped`, `liatrio-table`, `table-responsive` for mobile compatibility
- [x] 2.14 Modify `layout.html` to add navigation link: `<li th:replace="~{::menuItem ('/visits/upcoming','upcomingVisits','upcoming visits','calendar',#{upcomingVisits})}">`
- [x] 2.15 Insert navigation link after "Veterinarians" line (line ~135) and before "Error" line
- [x] 2.16 **RED**: Add controller test verifying default fromDate is `LocalDate.now()` when no parameter provided
- [ ] 2.17 Start application: `./mvnw spring-boot:run` and navigate to `http://localhost:8080/visits/upcoming`
- [ ] 2.18 Take screenshot of page showing visits table and save to `docs/specs/07-spec-upcoming-visits/07-proofs/basic-page-display.png`
- [ ] 2.19 Take screenshot of navigation bar with "Upcoming Visits" link and save to `docs/specs/07-spec-upcoming-visits/07-proofs/navigation-integration.png`
- [ ] 2.20 Test mobile responsiveness by resizing browser to 375x667 and take screenshot
- [x] 2.21 Run controller tests: `./mvnw test -Dtest=UpcomingVisitsControllerTests` and verify all pass

---

### [ ] 3.0 Implement Visit Filtering System with Date, Pet Type, and Owner Name Filters

**Description**: Add a filter panel to the Upcoming Visits page that allows users to filter visits by date range (from/to dates), pet type (dropdown), and owner last name (text search). Filters can be applied independently or in combination, with filter values preserved after submission.

#### 3.0 Proof Artifact(s)

- **Screenshot**: Filter panel showing date range inputs, pet type dropdown, and owner name search field demonstrates filter UI exists
- **Screenshot**: Filtered results showing only visits matching selected criteria demonstrates filter logic works
- **Screenshot**: Filter form with pre-populated values after submission demonstrates filter preservation works
- **Screenshot**: "Clear Filters" button returning to unfiltered view demonstrates reset functionality
- **CLI**: `curl "http://localhost:8080/visits/upcoming?fromDate=2026-02-15&toDate=2026-02-20&petType=dog"` returns filtered HTML demonstrates query parameter handling
- **Test**: Controller tests for filter scenarios (date only, pet type only, owner only, combined) demonstrates filter combinations work

#### 3.0 Tasks

- [ ] 3.1 **RED**: Add controller test for GET `/visits/upcoming?fromDate=2026-02-15` returning filtered visits (should fail - parameter not handled)
- [ ] 3.2 **GREEN**: Update controller method signature to accept `@RequestParam(required=false) @DateTimeFormat(pattern="yyyy-MM-dd") LocalDate fromDate` parameter
- [ ] 3.3 **GREEN**: Use fromDate if provided, otherwise default to `LocalDate.now()`: `LocalDate from = fromDate != null ? fromDate : LocalDate.now();`
- [ ] 3.4 **RED**: Add test for `toDate` parameter filtering visits within date range (should fail - not implemented)
- [ ] 3.5 **GREEN**: Add `@RequestParam(required=false) @DateTimeFormat(pattern="yyyy-MM-dd") LocalDate toDate` parameter to controller
- [ ] 3.6 **GREEN**: Call `visitRepository.findUpcomingVisitsWithFilters(from, toDate, null, null)` instead of simple query
- [ ] 3.7 **RED**: Add test for `petType` parameter filtering by pet type (should fail - parameter not handled)
- [ ] 3.8 **GREEN**: Add `@RequestParam(required=false) String petType` parameter to controller
- [ ] 3.9 **GREEN**: Pass petType to repository: `visitRepository.findUpcomingVisitsWithFilters(from, toDate, petType, null)`
- [ ] 3.10 **RED**: Add test for `ownerLastName` parameter filtering by owner name (should fail - parameter not handled)
- [ ] 3.11 **GREEN**: Add `@RequestParam(required=false) String ownerLastName` parameter to controller
- [ ] 3.12 **GREEN**: Pass ownerLastName to repository: `visitRepository.findUpcomingVisitsWithFilters(from, toDate, petType, ownerLastName)`
- [ ] 3.13 **RED**: Add test for combined filters (all parameters provided) returning correct subset
- [ ] 3.14 **GREEN**: Verify controller correctly passes all parameters to repository method
- [ ] 3.15 **RED**: Add test for invalid date format returning 400 Bad Request (should fail - no error handling)
- [ ] 3.16 **GREEN**: Add `@ExceptionHandler` for `DateTimeParseException` returning error view with message
- [ ] 3.17 Add filter form to `upcomingVisits.html` above the table with Bootstrap form classes
- [ ] 3.18 Add "From Date" input: `<input type="date" name="fromDate" th:value="${param.fromDate}" class="form-control">`
- [ ] 3.19 Add "To Date" input: `<input type="date" name="toDate" th:value="${param.toDate}" class="form-control">`
- [ ] 3.20 Add "Pet Type" dropdown: `<select name="petType" class="form-select">` with options from PetTypeRepository
- [ ] 3.21 Update controller to inject `PetTypeRepository` and add `petTypes` to model for dropdown population
- [ ] 3.22 Use Thymeleaf `th:each` to populate pet type options: `<option th:each="type : ${petTypes}" th:value="${type.name}" th:text="${type.name}" th:selected="${param.petType == type.name}">`
- [ ] 3.23 Add "Owner Name" search input: `<input type="text" name="ownerLastName" th:value="${param.ownerLastName}" class="form-control" placeholder="Search by last name">`
- [ ] 3.24 Add "Apply Filters" button: `<button type="submit" class="btn btn-primary">Apply Filters</button>`
- [ ] 3.25 Add "Clear Filters" link: `<a th:href="@{/visits/upcoming}" class="btn btn-secondary">Clear Filters</a>`
- [ ] 3.26 Arrange filter inputs in responsive grid layout using Bootstrap columns (col-md-3 for each input, col-md-12 for mobile)
- [ ] 3.27 Test filter form by submitting with various combinations and verify results are filtered correctly
- [ ] 3.28 Take screenshot of filter panel and save to `docs/specs/07-spec-upcoming-visits/07-proofs/filter-panel.png`
- [ ] 3.29 Take screenshot of filtered results and save to `docs/specs/07-spec-upcoming-visits/07-proofs/filtered-results.png`
- [ ] 3.30 Run `curl "http://localhost:8080/visits/upcoming?fromDate=2026-02-15&petType=dog"` and verify response
- [ ] 3.31 Run controller tests: `./mvnw test -Dtest=UpcomingVisitsControllerTests` and verify all filter tests pass

---

### [ ] 4.0 Add Internationalization Support for Upcoming Visits Page

**Description**: Add English message keys to `messages_en.properties` for all text on the Upcoming Visits page (page title, column headers, filter labels, button text, empty state message). Update the Thymeleaf template to use `th:text="#{key}"` for all user-visible text, preparing for future multi-language support.

#### 4.0 Proof Artifact(s)

- **Code**: `messages_en.properties` showing all required keys (upcomingVisits.title, upcomingVisits.column.*, upcomingVisits.filter.*, etc.) demonstrates i18n keys defined
- **Code**: `upcomingVisits.html` showing all text using `th:text="#{key}"` syntax (no hardcoded English) demonstrates proper i18n usage
- **Screenshot**: Page displaying with language selector still functional demonstrates i18n integration doesn't break existing functionality
- **Diff**: Git diff showing only message key additions and th:text changes demonstrates clean i18n implementation

#### 4.0 Tasks

- [ ] 4.1 Add message keys to `messages.properties` (default file):
  - `upcomingVisits=Upcoming Visits`
  - `upcomingVisits.title=Upcoming Visits`
  - `upcomingVisits.column.visitDate=Visit Date`
  - `upcomingVisits.column.petName=Pet Name`
  - `upcomingVisits.column.ownerName=Owner Name`
  - `upcomingVisits.column.description=Description`
  - `upcomingVisits.filter.fromDate=From Date`
  - `upcomingVisits.filter.toDate=To Date`
  - `upcomingVisits.filter.petType=Pet Type`
  - `upcomingVisits.filter.ownerName=Owner Last Name`
  - `upcomingVisits.filter.apply=Apply Filters`
  - `upcomingVisits.filter.clear=Clear Filters`
  - `upcomingVisits.filter.petType.all=All Types`
  - `upcomingVisits.empty=No upcoming visits scheduled`
- [ ] 4.2 Copy all message keys to `messages_en.properties` with comment noting English-only for now
- [ ] 4.3 Add comment to `messages_en.properties`: `# Future translations can be added to messages_de.properties, messages_es.properties, etc.`
- [ ] 4.4 Update `upcomingVisits.html` page title to use `<h2 th:text="#{upcomingVisits.title}">Upcoming Visits</h2>`
- [ ] 4.5 Update table column headers to use th:text with message keys (e.g., `<th th:text="#{upcomingVisits.column.visitDate}">Visit Date</th>`)
- [ ] 4.6 Update filter labels to use `<label th:text="#{upcomingVisits.filter.fromDate}">From Date</label>`
- [ ] 4.7 Update "Apply Filters" button to use `<button th:text="#{upcomingVisits.filter.apply}">Apply Filters</button>`
- [ ] 4.8 Update "Clear Filters" link to use `<a th:text="#{upcomingVisits.filter.clear}">Clear Filters</a>`
- [ ] 4.9 Update empty state div to use `<div th:text="#{upcomingVisits.empty}">No upcoming visits scheduled</div>`
- [ ] 4.10 Add "All Types" option to pet type dropdown: `<option value="" th:text="#{upcomingVisits.filter.petType.all}">All Types</option>`
- [ ] 4.11 Search template for any remaining hardcoded English text and replace with message keys
- [ ] 4.12 Start application and verify page displays all text correctly using message keys
- [ ] 4.13 Test language selector to ensure it still works (English should be the only available translation for this page)
- [ ] 4.14 Run `git diff` to review changes and verify only message keys were added and th:text changes made
- [ ] 4.15 Take screenshot of page showing proper i18n usage and save to `docs/specs/07-spec-upcoming-visits/07-proofs/i18n-integration.png`

---

### [ ] 5.0 Create End-to-End Playwright Tests for Upcoming Visits Feature

**Description**: Implement comprehensive Playwright E2E tests covering page navigation, visit display, filtering, empty state, responsive layout, and accessibility. Tests should validate the complete user journey from clicking the navigation link through applying filters and viewing results.

#### 5.0 Proof Artifact(s)

- **Test**: `e2e-tests/tests/features/upcoming-visits.spec.ts` with 10+ test scenarios passing demonstrates E2E coverage
- **Playwright Report**: HTML report (`test-results/html-report/index.html`) showing all tests passing demonstrates test execution success
- **Test**: Navigation test verifying "Upcoming Visits" link loads correct page demonstrates navigation works
- **Test**: Display test verifying visits sorted chronologically demonstrates sorting works
- **Test**: Filter test verifying date range reduces results correctly demonstrates filtering works
- **Test**: Empty state test verifying message appears when no visits match demonstrates empty state handling
- **Test**: Responsive test verifying mobile layout (viewport 375x667) demonstrates mobile support
- **Screenshot**: Keyboard navigation highlighting focus states demonstrates accessibility compliance

#### 5.0 Tasks

- [ ] 5.1 Create `e2e-tests/tests/features/upcoming-visits.spec.ts` file
- [ ] 5.2 Import Playwright test utilities: `import { test, expect } from '@playwright/test';`
- [ ] 5.3 Add test suite: `test.describe('Upcoming Visits Page', () => { ... });`
- [ ] 5.4 **Test 1**: Write test for navigation - click "Upcoming Visits" link and verify URL is `/visits/upcoming`
- [ ] 5.5 **Test 2**: Write test verifying page title is "Upcoming Visits"
- [ ] 5.6 **Test 3**: Write test verifying visits table is visible and contains expected columns (Visit Date, Pet Name, Owner Name, Description)
- [ ] 5.7 **Test 4**: Write test verifying visits are sorted chronologically (first visit has earliest date)
- [ ] 5.8 **Test 5**: Write test for date range filter - fill in from/to dates, click "Apply Filters", verify results are filtered
- [ ] 5.9 **Test 6**: Write test for pet type filter - select a pet type, click "Apply Filters", verify only that type appears
- [ ] 5.10 **Test 7**: Write test for owner name filter - enter last name, click "Apply Filters", verify only matching owners appear
- [ ] 5.11 **Test 8**: Write test for "Clear Filters" button - apply filters, click "Clear Filters", verify all visits shown again
- [ ] 5.12 **Test 9**: Write test for empty state - apply filters that match no visits, verify "No upcoming visits scheduled" message appears
- [ ] 5.13 **Test 10**: Write test for responsive layout - set viewport to 375x667 (mobile), verify table is responsive
- [ ] 5.14 **Test 11**: Write test for keyboard navigation - tab through filter inputs and buttons, verify focus states are visible
- [ ] 5.15 **Test 12**: Write test for filter preservation - apply filters, reload page, verify filter values are preserved in form inputs
- [ ] 5.16 Add `beforeEach` hook to navigate to base URL and ensure clean state for each test
- [ ] 5.17 Run E2E tests: `cd e2e-tests && npm test -- upcoming-visits.spec.ts`
- [ ] 5.18 Verify all tests pass and review any failures
- [ ] 5.19 Generate Playwright HTML report: `npx playwright show-report`
- [ ] 5.20 Take screenshot of passing Playwright report and save to `docs/specs/07-spec-upcoming-visits/07-proofs/playwright-report.png`
- [ ] 5.21 Take screenshot showing keyboard navigation focus states and save to `docs/specs/07-spec-upcoming-visits/07-proofs/accessibility-keyboard-nav.png`
- [ ] 5.22 Review test coverage and add any missing scenarios based on spec requirements

---

## Notes

- Follow strict TDD (Test-Driven Development) methodology: write tests before implementation
- Each parent task should be committed to git after completion with a conventional commit message
- Use JUnit 5 with AssertJ for assertions in Java tests
- Use `@DataJpaTest` for repository tests, `@WebMvcTest` for controller tests
- Follow existing Spring Boot patterns: constructor injection, method naming conventions
- Maintain >90% line coverage for all new code
- Use Bootstrap 5 responsive classes for mobile compatibility
- Follow Liatrio branding and styling patterns from existing pages
- Create proof artifacts directory: `mkdir -p docs/specs/07-spec-upcoming-visits/07-proofs/`
- Save all screenshots and test outputs to the proofs directory for validation
