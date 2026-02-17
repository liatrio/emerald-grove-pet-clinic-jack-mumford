# 07-spec-upcoming-visits

## Introduction/Overview

The Upcoming Visits page provides clinic staff with a centralized view of all scheduled future visits across all pets in the Emerald Grove Veterinary Clinic. Currently, visits are only viewable within the context of individual owner/pet pages, making it difficult to see the overall clinic schedule. This feature creates a dedicated page that displays all upcoming appointments in chronological order with filtering capabilities, enabling staff to efficiently manage and view the clinic's schedule.

## Goals

- Provide a comprehensive view of all future visits scheduled in the clinic (visits with date >= today)
- Enable clinic staff to filter visits by date range, pet type, and owner name for quick scheduling insights
- Create an accessible, mobile-responsive interface following existing application patterns
- Display essential visit information (date, pet, owner, description) in an easy-to-scan format
- Integrate seamlessly into the existing navigation structure alongside "Find Owners" and "Veterinarians"

## User Stories

- **As a clinic receptionist**, I want to view all upcoming visits in one place so that I can quickly answer patient scheduling questions without navigating through multiple owner pages.

- **As a veterinarian**, I want to see which pets are scheduled for visits in the coming days so that I can prepare for upcoming appointments and review relevant medical histories.

- **As a clinic administrator**, I want to filter upcoming visits by date range and pet type so that I can analyze scheduling patterns and resource allocation needs.

- **As a mobile user**, I want to view the upcoming visits page on my phone or tablet so that I can check the schedule while away from my desk.

## Demoable Units of Work

### Unit 1: Basic Upcoming Visits Display

**Purpose:** Create a functional page that displays all future visits in chronological order, accessible from the main navigation.

**Functional Requirements:**
- The system shall create a new page at `/visits/upcoming` that displays all visits with dates >= today
- The system shall sort visits by date in ascending order (nearest visits first)
- The system shall display for each visit: visit date, pet name, owner name, and visit description
- The system shall add a "Upcoming Visits" link to the main navigation bar between "Veterinarians" and the language selector
- The system shall display "No upcoming visits scheduled" when no future visits exist
- The page shall use the existing Thymeleaf layout template and Liatrio branding styles
- The page shall be fully responsive using Bootstrap responsive classes

**Proof Artifacts:**
- **URL**: `http://localhost:8080/visits/upcoming` accessible and loads demonstrates page exists
- **Screenshot**: Navigation bar showing "Upcoming Visits" link demonstrates navigation integration
- **Screenshot**: Page displaying list of upcoming visits sorted by date demonstrates core functionality
- **Screenshot**: Empty state message when no visits exist demonstrates edge case handling
- **Screenshot**: Mobile view showing responsive layout demonstrates mobile compatibility

### Unit 2: Visit Filtering System

**Purpose:** Enable users to filter the upcoming visits list by date range, pet type, and owner name for quick data access.

**Functional Requirements:**
- The system shall provide a date range filter with "From Date" and "To Date" fields
- The system shall provide a pet type dropdown filter populated with all pet types in the database (cat, dog, bird, etc.)
- The system shall provide an owner name text filter that searches by owner last name
- The system shall allow filters to be applied independently or in combination
- The system shall preserve filter selections when the page refreshes after applying filters
- The system shall display a "Clear Filters" button that resets all filters to default state
- The system shall update the visit list dynamically when filters are applied without full page reload if using AJAX, or with page refresh if using standard form submission

**Proof Artifacts:**
- **Screenshot**: Filter panel showing date range, pet type dropdown, and owner name search demonstrates filter UI
- **CLI**: Filter form submission with query parameters demonstrates filter functionality
- **Screenshot**: Filtered results showing only visits matching criteria demonstrates filter logic works
- **Screenshot**: "Clear Filters" button resetting to full list demonstrates reset functionality

### Unit 3: Repository Query and Data Access Layer

**Purpose:** Implement the backend data access logic to retrieve and filter upcoming visits from the database.

**Functional Requirements:**
- The system shall create a `VisitRepository` interface extending Spring Data JPA Repository
- The system shall implement a query method `findUpcomingVisits(LocalDate fromDate)` that returns visits with date >= fromDate
- The system shall implement a query method `findUpcomingVisitsWithFilters(LocalDate fromDate, LocalDate toDate, String petType, String ownerLastName)` supporting optional filter parameters
- The system shall ensure visits are joined with Pet and Owner entities to avoid N+1 query issues
- The system shall return visits sorted by date in ascending order from the repository layer
- The system shall handle null filter parameters gracefully (treat as "no filter applied")

**Proof Artifacts:**
- **Test**: `VisitRepositoryTests.java` passing demonstrates repository query methods work
- **Test**: Integration test showing visits filtered by date range demonstrates date filtering
- **Test**: Integration test showing visits filtered by pet type demonstrates pet type filtering
- **Test**: Integration test showing visits filtered by owner name demonstrates owner filtering
- **CLI**: SQL query logs showing JOIN operations demonstrates N+1 prevention

### Unit 4: Controller and View Integration

**Purpose:** Connect the repository layer to the Thymeleaf view with proper MVC architecture and error handling.

**Functional Requirements:**
- The system shall create a `VisitController` with a `@GetMapping` for `/visits/upcoming`
- The system shall inject `VisitRepository` into the controller via constructor dependency injection
- The system shall handle filter parameters from query string (`fromDate`, `toDate`, `petType`, `ownerLastName`)
- The system shall provide default filter values (fromDate = today, toDate/petType/ownerLastName = null)
- The system shall pass the filtered visit list to the Thymeleaf template as a model attribute
- The system shall pass filter values back to the view for form pre-population
- The system shall handle date parsing errors gracefully with user-friendly error messages
- The system shall create a Thymeleaf template `upcomingVisits.html` that renders the visit list and filters

**Proof Artifacts:**
- **Test**: `VisitControllerTests.java` passing with 8+ test scenarios demonstrates controller logic
- **Screenshot**: Page with pre-populated filter values after submission demonstrates filter preservation
- **Test**: Controller test for invalid date format returns error demonstrates error handling
- **CLI**: `curl http://localhost:8080/visits/upcoming?fromDate=2026-02-15` returns filtered HTML demonstrates endpoint works

### Unit 5: Internationalization and Documentation

**Purpose:** Add English message keys for the upcoming visits page and prepare for future multi-language support.

**Functional Requirements:**
- The system shall add message keys to `messages_en.properties` for all page labels and text
- Message keys shall include: page title, column headers, filter labels, button text, empty state message
- The Thymeleaf template shall use `th:text="#{key}"` for all user-visible text
- The system shall document the message keys in comments for future translation
- The system shall add a comment noting that translations for other languages (DE, ES, KO, FA, PT, RU, TR, ZH) can be added in a future enhancement

**Proof Artifacts:**
- **Code**: `messages_en.properties` contains all required keys demonstrates i18n setup
- **Screenshot**: Page displays all text using message keys (no hardcoded English) demonstrates proper i18n usage
- **Test**: Language selector still works and page respects locale demonstrates i18n integration

### Unit 6: End-to-End Testing and Validation

**Purpose:** Validate the complete feature with automated Playwright tests and manual testing scenarios.

**Functional Requirements:**
- The system shall include Playwright E2E tests covering: page navigation, visit display, filtering, empty state, responsive layout
- The system shall verify that clicking "Upcoming Visits" in navigation loads the page
- The system shall verify that visits are displayed in chronological order
- The system shall verify that filters correctly reduce the displayed visits
- The system shall verify that the empty state appears when no visits match filters
- The system shall verify that the page is accessible via keyboard navigation
- The system shall include manual testing scenarios documented in proof artifacts

**Proof Artifacts:**
- **Test**: `e2e-tests/tests/features/upcoming-visits.spec.ts` passes with 8+ scenarios demonstrates E2E coverage
- **Playwright Report**: HTML report showing all tests passing demonstrates test execution
- **Screenshot**: Keyboard navigation highlighting demonstrates accessibility
- **Document**: Manual test checklist with results demonstrates comprehensive validation

## Non-Goals (Out of Scope)

1. **Visit Editing or Cancellation**: This feature is read-only; users cannot edit or delete visits from this page (must navigate to owner page for visit management)
2. **Pagination**: All matching visits will be displayed on a single page without pagination (future enhancement if performance issues arise)
3. **Real-Time Updates**: The page will not automatically refresh to show new visits; users must manually refresh the page
4. **Advanced Scheduling Features**: No drag-and-drop rescheduling, no calendar view, no appointment conflicts detection
5. **Full Multi-Language Support**: Initial release includes English only; other languages (DE, ES, KO, FA, PT, RU, TR, ZH) can be added in Phase 2
6. **Export to Calendar**: No export to iCal, Google Calendar, or other calendar formats
7. **Visit Notifications**: No email or SMS notifications for upcoming visits
8. **Veterinarian Assignment**: No display of which veterinarian is assigned to each visit

## Design Considerations

The Upcoming Visits page should follow the established Liatrio branding and design patterns already present in the application:

- **Layout**: Use the existing `fragments/layout.html` Thymeleaf template with consistent header/footer
- **Tables**: Use Bootstrap `table` and `table-striped` classes with `liatrio-table` custom class for consistency
- **Colors**: Follow the Liatrio color scheme (dark blue `#1A365D` for primary, grays for backgrounds)
- **Typography**: Use existing font stack and sizing from `petclinic.css`
- **Spacing**: Maintain consistent padding and margins with other pages (use Bootstrap spacing utilities)
- **Buttons**: Use Bootstrap `btn` classes with `btn-primary` for main actions, `btn-secondary` for filters
- **Forms**: Use Bootstrap form controls (`form-control`, `form-select`) for filter inputs
- **Empty State**: Display centered message in muted text (`text-muted`) similar to other empty states
- **Responsive Breakpoints**: Follow Bootstrap's responsive grid system (xs, sm, md, lg, xl)
- **Mobile Layout**: Stack filters vertically on mobile, allow horizontal scrolling for table if needed

**UI Mockup Description** (no formal mockup provided):
- Page title: "Upcoming Visits" as `<h2>` at top
- Filter panel: Horizontal row of filter inputs (date range, pet type dropdown, owner search) with "Apply Filters" and "Clear Filters" buttons
- Results table: 4 columns (Visit Date, Pet Name, Owner Name, Description), sortable by date
- Empty state: Centered message "No upcoming visits scheduled" when no results

## Repository Standards

Implementation should follow existing patterns established in the Emerald Grove Pet Clinic codebase:

**Architecture Patterns:**
- **Spring MVC**: Use `@Controller` annotations, `@GetMapping`, and `ModelAndView`/model attributes
- **Spring Data JPA**: Extend `Repository<Visit, Integer>` for data access, use method name conventions or `@Query`
- **Dependency Injection**: Constructor-based injection (no field injection)
- **Layered Architecture**: Controller → Repository → Entity (no separate service layer needed for this feature)

**Code Organization:**
- Place `VisitRepository.java` in `src/main/java/org/springframework/samples/petclinic/owner/` package
- Place `VisitController.java` in the same package (visits are closely related to pets/owners)
- Place `upcomingVisits.html` template in `src/main/resources/templates/visits/`
- Follow existing file naming conventions (camelCase for Java, lowercase with hyphens for templates)

**Testing Standards:**
- Unit tests: `VisitRepositoryTests.java` and `VisitControllerTests.java` in `src/test/java/` mirroring source structure
- Use JUnit 5 with AssertJ for assertions
- Use `@DataJpaTest` for repository tests, `@WebMvcTest` for controller tests
- Follow Arrange-Act-Assert pattern
- Aim for >90% line coverage on new code
- E2E tests: `upcoming-visits.spec.ts` in `e2e-tests/tests/features/`

**Coding Standards:**
- Follow existing Google Java Style conventions (2-space indentation, braces on same line)
- Use JavaDoc comments for public methods
- Keep methods small (<30 lines)
- Use `Optional<>` for nullable returns
- Follow Spring Boot naming conventions for query methods (e.g., `findByDateGreaterThanEqual`)

**Commit Conventions:**
- Use conventional commits format: `feat:`, `test:`, `docs:`, `fix:`
- Include "Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>" in commit messages
- Make atomic commits (one feature/task per commit)

## Technical Considerations

**Database Access:**
- Visits are currently accessed through the Pet entity's `visits` relationship (no separate VisitRepository exists)
- Will need to create a `VisitRepository` interface to query visits independently
- The `visits` table has columns: `id`, `pet_id`, `visit_date`, `description`
- To get owner information, must join through: `visits` → `pets` → `owners`
- Use Spring Data JPA's `@Query` or method naming convention to join tables efficiently

**Query Performance:**
- Current Pet entity uses `FetchType.EAGER` for visits which could cause N+1 queries
- Repository query should use explicit JOIN FETCH to load Pet and Owner in single query
- For typical clinic size (hundreds of visits), no additional indexing needed
- If performance issues arise, consider adding database index on `visits.visit_date`

**Date Handling:**
- Use `LocalDate.now()` for "today" comparison (no time component needed)
- Filter dates are inclusive: `fromDate <= visit.date <= toDate`
- Default `fromDate` to `LocalDate.now()` (today)
- Handle timezone considerations: server timezone vs. database timezone

**Technology Stack:**
- Spring Boot 4.0.0
- Spring Data JPA with Hibernate
- Thymeleaf 3.x templating
- Bootstrap 5 CSS framework
- H2 database (development), MySQL/PostgreSQL (production support)
- Playwright for E2E testing
- JUnit 5 + Mockito for unit tests

**Error Handling:**
- Invalid date format in query params → return 400 Bad Request with error message
- Empty result set → display empty state message (not an error)
- Database connection issues → return 500 with user-friendly error page
- Invalid pet type filter → ignore filter, log warning

**Browser Support:**
- Modern browsers (Chrome, Firefox, Safari, Edge) - last 2 versions
- Bootstrap 5 provides responsive layout support
- No IE11 support required

## Security Considerations

**Authentication/Authorization:**
- Inherits existing application security (if any authentication is configured)
- If authentication is added in future, upcoming visits page should be accessible to all authenticated users
- No role-based access control needed (all clinic staff can view visits)

**Data Privacy:**
- Upcoming visits page displays owner names, pet names, and visit descriptions
- This is existing data already visible on owner detail pages, so no new privacy concerns
- Follow existing patterns for data display (no change in privacy posture)

**Input Validation:**
- Validate date format for `fromDate` and `toDate` parameters (ISO 8601: YYYY-MM-DD)
- Sanitize owner name search input to prevent SQL injection (use parameterized queries)
- Pet type filter should validate against known pet types or use parameterized queries
- No user-generated content is stored by this feature (read-only)

**Proof Artifacts Security:**
- Screenshots may contain simulated/test data (not real patient information)
- Ensure test data generation creates realistic but fictional owner/pet names
- Do not commit any real clinic data to version control
- E2E tests should use test fixtures, not production data

## Success Metrics

1. **Functional Completeness**: Page displays all upcoming visits correctly with visit date, pet name, owner name, and description (verified via E2E tests and manual testing)

2. **Usability**: Clinic staff can find and use the page without training (accessible via main navigation, intuitive filter controls)

3. **Performance**: Page loads within standard timeframe (<2 seconds for typical visit volume of 100-500 visits)

4. **Test Coverage**: >90% line coverage on new code (VisitRepository, VisitController), comprehensive E2E test suite

5. **Responsive Design**: Page functions correctly on desktop (1920x1080), tablet (768x1024), and mobile (375x667) viewports

## Open Questions

No open questions at this time. All requirements have been clarified through the questions process.
