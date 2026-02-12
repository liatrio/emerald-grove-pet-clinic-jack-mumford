# 02-spec-friendly-404-pages.md

## Introduction/Overview

This feature improves error handling for missing owners and pets by replacing raw exception views with user-friendly 404 pages. Currently, when users navigate to a non-existent owner or pet, they see technical exception messages with stack traces. This enhancement provides clear, helpful error messages with actionable navigation options, improving the overall user experience and maintaining a professional appearance.

## Goals

- Replace raw exception/stack trace displays with user-friendly 404 error pages for missing owners and pets
- Provide clear, actionable error messages that guide users back to productive actions
- Implement proper HTTP 404 status codes for missing resources
- Maintain internationalization support across all 8 supported languages
- Ensure comprehensive test coverage with both E2E (Playwright) and unit (JUnit) tests

## User Stories

**As a pet clinic staff member**, I want to see a friendly error message when I navigate to a non-existent owner so that I understand what went wrong and can quickly search for the correct owner.

**As a pet clinic administrator**, I want missing pet pages to show helpful error messages instead of technical errors so that staff members don't get confused by stack traces and can maintain confidence in the system.

**As a system user**, I want a convenient link back to the "Find Owners" page on error pages so that I can immediately search for the correct owner without navigating through menus.

## Demoable Units of Work

### Unit 1: Global Exception Handler with 404 Response

**Purpose:** Implement a centralized exception handling mechanism that intercepts `IllegalArgumentException` exceptions thrown when owners or pets are not found, returning proper 404 HTTP status codes instead of exposing stack traces to end users.

**Functional Requirements:**
- The system shall create a `@ControllerAdvice` class that handles `IllegalArgumentException` globally across all controllers
- The system shall return HTTP 404 status code when an `IllegalArgumentException` is caught
- The system shall extract contextual information from the exception message to determine if it's an owner or pet not found scenario
- The system shall pass appropriate error context to the view layer (error message, resource type, status code)
- The system shall NOT modify existing exception throwing behavior in controllers (only intercept and handle)

**Proof Artifacts:**
- Test output: JUnit MVC test console output demonstrates 404 status code returned for missing owner ID
- Test output: JUnit MVC test console output demonstrates correct view name returned ("notFound")
- Test output: JUnit MVC test console output demonstrates model attributes contain expected error message

### Unit 2: Dedicated Not Found Template with Navigation

**Purpose:** Create a user-friendly error page that displays helpful, internationalized error messages and provides quick navigation back to the owner search functionality, serving users who encounter missing resources.

**Functional Requirements:**
- The system shall create a new `notFound.html` Thymeleaf template in `src/main/resources/templates/`
- The template shall display a user-friendly error message based on the context (owner vs pet not found)
- The template shall include a prominent "Find Owners" link that navigates to `/owners/find`
- The template shall follow the existing Liatrio branding and layout patterns from other templates
- The template shall use Thymeleaf expressions to render dynamic error messages from the model
- The template shall use the same layout fragment as other pages for consistency

**Proof Artifacts:**
- Test output: Playwright E2E test console output demonstrates navigation to non-existent owner shows friendly 404 page
- Test output: Playwright E2E test console output demonstrates "Find Owners" link is present and functional
- Test output: Browser screenshot of 404 page showing friendly message and navigation link

### Unit 3: Internationalization Support

**Purpose:** Extend error messaging to all supported languages, ensuring users in different locales receive helpful error messages in their preferred language.

**Functional Requirements:**
- The system shall add `error.owner.notFound` message key to all 8 language files (en, de, ko, fa, pt, es, tr, ru)
- The system shall add `error.pet.notFound` message key to all 8 language files
- The system shall add `error.notFound.action` message key to provide actionable guidance in all languages
- Each message shall convey the same meaning: "We couldn't find that [owner/pet]. Please search again or verify the ID"
- The template shall use `#{...}` Thymeleaf syntax to reference these i18n keys

**Proof Artifacts:**
- Test output: JUnit test console output demonstrates multiple locale messages resolve correctly
- Code review: All 8 message property files contain the new keys with appropriate translations

### Unit 4: Comprehensive Test Coverage

**Purpose:** Validate the 404 functionality works correctly through both end-to-end browser testing and unit-level controller testing, ensuring reliability and preventing regressions.

**Functional Requirements:**
- The system shall include a Playwright E2E test that navigates to a non-existent owner URL (e.g., `/owners/99999`)
- The Playwright test shall verify the page returns HTTP 404 status
- The Playwright test shall verify the page contains user-friendly error message (not stack trace)
- The Playwright test shall verify the "Find Owners" link is present
- The system shall include JUnit MVC tests in `OwnerControllerTests` that mock repository to return empty Optional
- The JUnit test shall assert 404 HTTP status code is returned
- The JUnit test shall assert the correct view name ("notFound") is returned
- The JUnit test shall verify model attributes contain appropriate error information

**Proof Artifacts:**
- Test output: Playwright test report shows E2E test passing with 404 verification
- Test output: JUnit test console output shows all MVC tests passing with 404 assertions
- Test output: Code coverage report demonstrates exception handler is fully tested

## Non-Goals (Out of Scope)

1. **Invalid ID format handling**: We will NOT handle non-integer IDs (e.g., `/owners/abc`). Spring's type conversion will handle these separately.
2. **Authorization/Security**: We will NOT implement any authentication or authorization logic for resource access. This spec only addresses missing resources, not forbidden ones.
3. **Other exception types**: We will NOT modify handling for other exceptions (validation errors, server errors, etc.). Only `IllegalArgumentException` for missing owners/pets is in scope.
4. **Cascading pet-owner scenarios**: We will NOT add special handling for `/owners/1/pets/999` where owner exists but pet doesn't. Current behavior is acceptable.
5. **General error page improvements**: We will NOT enhance the existing `error.html` template or improve other error scenarios beyond 404s.
6. **Custom exception classes**: We will NOT create custom exception types like `ResourceNotFoundException`. We'll work with existing `IllegalArgumentException`.

## Design Considerations

**Visual Design:**
- Follow existing Liatrio branding established in `error.html` template
- Use `.liatrio-section` and `.liatrio-error-card` CSS classes for consistency
- Include the pets image (`/resources/images/pets.png`) for visual continuity
- Maintain responsive layout using Bootstrap 5 grid system
- Use same color palette and typography as other pages

**User Experience:**
- Error message should be immediately visible and easy to understand
- "Find Owners" link should be prominent (button style, not just text link)
- Page should not feel like a system failure, but a helpful redirect
- Keep message concise (1-2 sentences) to avoid overwhelming users

**Template Structure:**
- Use Thymeleaf layout fragment: `th:replace="~{fragments/layout :: layout (~{::body},'error')}"`
- Include proper DOCTYPE and namespace declarations
- Structure content in semantic HTML5 sections

## Repository Standards

**Coding Standards:**
- Follow Spring Boot naming conventions for components (`@ControllerAdvice` named `*ExceptionHandler`)
- Use constructor injection for dependencies (no `@Autowired` field injection)
- Follow existing code formatting and style (indentation, bracing, etc.)
- Include Apache License header on all new Java files

**Testing Conventions:**
- JUnit 5 test classes named `*Tests` (not `*Test`)
- Use `@WebMvcTest` for controller layer testing
- Use MockMvc for HTTP request/response verification
- Use Mockito's `given().willReturn()` syntax for mocking
- Use AssertJ or Hamcrest matchers for assertions
- Include `@DisabledInNativeImage` and `@DisabledInAotMode` annotations where appropriate

**File Organization:**
- Place exception handler in appropriate package (consider `system` package alongside existing system utilities)
- Place template in `src/main/resources/templates/` (root level, not in subdirectory)
- Place test files in corresponding test package matching main package structure

**Commit Conventions:**
- Follow conventional commit format: `feat(error-handling): add friendly 404 pages for missing owners/pets`
- Include co-author tag: `Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>`

## Technical Considerations

**Spring Framework Integration:**
- Use Spring MVC's `@ControllerAdvice` for global exception handling
- Return `ModelAndView` from exception handler with appropriate HTTP status
- Use `HttpStatus.NOT_FOUND` constant for 404 status codes
- Leverage Spring's `Model` to pass error attributes to view

**Exception Handling Strategy:**
- Intercept `IllegalArgumentException` only (narrow scope)
- Parse exception message to determine context (owner vs pet)
- Consider message pattern: "Owner not found with id: X" or "Pet not found"
- Handle gracefully if message parsing fails (fallback to generic message)

**Thymeleaf Template Engine:**
- Use standard Thymeleaf dialect expressions for i18n (`#{key}`)
- Use `th:text` for internationalized content
- Use `th:href="@{/owners/find}"` for URL generation
- Leverage existing layout fragments for consistent page structure

**Testing Infrastructure:**
- Use TestContainers for database-dependent tests (if needed)
- Mock repository layer to return `Optional.empty()` for not-found scenarios
- Use `MockMvcRequestBuilders.get()` for simulating HTTP GET requests
- Assert using `MockMvcResultMatchers.status()`, `view()`, and `model()` matchers

**Dependencies:**
- No new dependencies required (all needed libraries already present)
- Leverage existing Spring Boot starter dependencies
- Use existing testing libraries (JUnit 5, Mockito, MockMvc, Playwright)

## Security Considerations

**Information Disclosure:**
- DO NOT include stack traces in error responses (handled by Spring Boot defaults in production)
- DO NOT expose internal exception details (database constraints, system paths, etc.)
- Error messages should be user-friendly but not reveal system internals
- Verify stack traces are disabled in production profiles (`server.error.include-stacktrace=never`)

**Input Validation:**
- ID validation already handled by Spring's path variable type conversion
- No additional input validation needed for this feature
- Do not parse or process raw exception messages in ways that could expose sensitive data

**Proof Artifact Security:**
- Test output (console logs) are safe to commit - no sensitive data
- Screenshots should only show the UI with sample IDs (e.g., 99999)
- DO NOT commit screenshots showing real production data or internal URLs
- Playwright test videos should be in `.gitignore` (not committed)

## Success Metrics

1. **User Experience**: Staff members can navigate to non-existent owner/pet without seeing stack traces - measured by zero stack trace reports in production logs after deployment
2. **Error Rate**: 404 errors return proper HTTP status codes - measured by monitoring tools showing 404 status instead of 500 for missing resources
3. **Navigation Efficiency**: Users can quickly return to owner search from error pages - measured by clickstream data showing "Find Owners" link usage
4. **Test Coverage**: 100% coverage of exception handler logic - measured by JaCoCo code coverage report
5. **Internationalization**: All 8 languages display appropriate error messages - verified by manual testing in each locale

## Open Questions

No open questions at this time.
