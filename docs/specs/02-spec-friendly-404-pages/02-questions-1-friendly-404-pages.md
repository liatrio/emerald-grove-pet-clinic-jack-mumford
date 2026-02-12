# 02 Questions Round 1 - Friendly 404 Pages

Please answer each question below (select one or more options, or add your own notes). Feel free to add additional context under any question.

## 1. Exception Handling Strategy

How should we implement the 404 error handling for missing owners and pets?

- [x] (A) Create a `@ControllerAdvice` class to handle `IllegalArgumentException` globally and return 404 responses
- [ ] (B) Create a custom exception (e.g., `ResourceNotFoundException`) and handle it with `@ControllerAdvice`
- [ ] (C) Add `@ExceptionHandler` methods directly in each controller class
- [ ] (D) Replace the `orElseThrow()` calls with conditional logic that returns the error view directly
- [ ] (E) Other (describe)

**Notes:**

## 2. Error Page Customization

Should the 404 error page be customized specifically for owners/pets, or use the generic error template?

- [ ] (A) Use the existing generic `error.html` template (already supports 404 with i18n)
- [x] (B) Create a dedicated `notFound.html` template with more specific messaging
- [ ] (C) Enhance the existing `error.html` to show context-specific messages for owners vs pets
- [ ] (D) Create separate templates for owner-not-found and pet-not-found scenarios
- [ ] (E) Other (describe)

**Notes:**

## 3. Navigation Links

The issue mentions adding a link back to "Find Owners". What navigation should the 404 page provide?

- [x] (A) Single link: "Find Owners" only
- [ ] (B) Multiple links: "Find Owners", "Home", "All Vets"
- [ ] (C) Dynamic link based on context (if owner not found → "Find Owners", if pet not found → back to owner details)
- [ ] (D) Breadcrumb navigation showing the user's path
- [ ] (E) Other (describe)

**Notes:**

## 4. Error Messages

What level of detail should the error messages provide to users?

- [ ] (A) Generic message: "The requested resource was not found"
- [ ] (B) Specific message with ID: "Owner with ID 123 was not found"
- [x] (C) Helpful message with action: "We couldn't find that owner. Please search again or verify the ID"
- [ ] (D) Different messages for different scenarios (owner not found vs pet not found)
- [ ] (E) Other (describe)

**Notes:**

## 5. Internationalization (i18n)

Should we add new message keys for owner/pet-specific 404 errors?

- [ ] (A) Reuse existing generic 404 message (`error.404`) across all 8 languages
- [x] (B) Add new specific keys (`error.owner.notFound`, `error.pet.notFound`) to all language files
- [ ] (C) Add English-only specific messages first, extend to other languages later
- [ ] (D) Use the message from the exception as-is without i18n keys
- [ ] (E) Other (describe)

**Notes:**

## 6. Testing Coverage

Beyond the Playwright and JUnit tests mentioned in the issue, what else should be tested?

- [x] (A) Only what's specified: Playwright E2E test + JUnit MVC test for 404 status and view
- [ ] (B) Add unit tests for the exception handler logic itself
- [ ] (C) Add tests for both owner-not-found and pet-not-found scenarios separately
- [ ] (D) Add integration tests that verify the full request-response cycle
- [ ] (E) Other (describe)

**Notes:**

## 7. Edge Cases

Are there any edge cases or special scenarios we should explicitly handle or exclude?

- [x] (A) Only handle integer ID not found (e.g., `/owners/999`)
- [ ] (B) Also handle invalid ID formats (e.g., `/owners/abc`)
- [ ] (C) Handle both missing owner and missing pet on the same request (e.g., `/owners/1/pets/999`)
- [ ] (D) Consider authorization scenarios (owner exists but user shouldn't see it)
- [ ] (E) Other (describe)

**Notes:**

## 8. Existing Functionality

Should we modify any existing error handling behavior?

- [x] (A) Only add 404 handling, leave all other error handling unchanged
- [ ] (B) Consolidate all exception handling to use the new approach consistently
- [ ] (C) Add 404 handling but also improve generic error pages while we're at it
- [ ] (D) Replace all `IllegalArgumentException` usage with custom exceptions
- [ ] (E) Other (describe)

**Notes:**

## 9. Proof Artifacts

What specific proof artifacts should be committed to demonstrate this feature works?

- [ ] (A) Screenshots: Browser showing friendly 404 page for missing owner and pet
- [ ] (B) Playwright test report: HTML report showing E2E test passing
- [x] (C) Test output: Console output from JUnit tests showing 404 assertions
- [ ] (D) Video: Screen recording of the 404 flow in action
- [ ] (E) Other (describe)

**Notes:**

## Additional Questions or Context

Is there anything else we should consider for this feature?

