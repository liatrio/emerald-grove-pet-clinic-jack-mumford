# Task List: Past Date Validation for Visit Scheduling

**Feature ID**: Issue #8
**Spec**: docs/specs/04-spec-past-date-validation/SPEC.md
**Status**: Ready for Implementation
**Created**: 2026-02-12

---

## Relevant Files

### Source Files (Production Code)
- **NEW** `src/main/java/org/springframework/samples/petclinic/owner/VisitValidator.java` - Custom validator for visit date validation
- **MODIFY** `src/main/java/org/springframework/samples/petclinic/owner/VisitController.java` - Register validator via @InitBinder
- **MODIFY** `src/main/resources/templates/pets/createOrUpdateVisitForm.html` - Add HTML5 date picker constraints

### Internationalization Files
- **MODIFY** `src/main/resources/messages/messages.properties` (English - default)
- **MODIFY** `src/main/resources/messages/messages_de.properties` (German)
- **MODIFY** `src/main/resources/messages/messages_es.properties` (Spanish)
- **MODIFY** `src/main/resources/messages/messages_ko.properties` (Korean)
- **MODIFY** `src/main/resources/messages/messages_fa.properties` (Persian)
- **MODIFY** `src/main/resources/messages/messages_pt.properties` (Portuguese)
- **MODIFY** `src/main/resources/messages/messages_ru.properties` (Russian)
- **MODIFY** `src/main/resources/messages/messages_tr.properties` (Turkish)
- **MODIFY** `src/main/resources/messages/messages_en.properties` (English explicit)

### Test Files
- **NEW** `src/test/java/org/springframework/samples/petclinic/owner/VisitValidatorTests.java` - Unit tests for VisitValidator
- **MODIFY** `src/test/java/org/springframework/samples/petclinic/owner/VisitControllerTests.java` - Integration tests for validation
- **NEW** `e2e-tests/tests/visit-validation.spec.ts` - End-to-end browser tests

### Reference Files (Patterns to Follow)
- `src/main/java/org/springframework/samples/petclinic/owner/PetValidator.java` - Validator pattern reference
- `src/main/java/org/springframework/samples/petclinic/owner/PetController.java` - @InitBinder pattern reference
- `src/test/java/org/springframework/samples/petclinic/owner/PetValidatorTests.java` - Test structure reference

---

## Parent Tasks Overview

This feature is divided into **4 demoable units** following TDD principles:

1. **[PT-1] Core Validation Logic** - VisitValidator class with date validation
2. **[PT-2] Controller Integration** - Register validator in VisitController
3. **[PT-3] Internationalization** - Error message translations for 9 languages
4. **[PT-4] Client-Side Enhancement & E2E** - HTML5 constraints and browser tests

**Estimated Total Effort**: 6-8 hours

---

## Parent Task 1: Core Validation Logic

**ID**: PT-1
**Title**: Implement VisitValidator with Past Date Validation
**Priority**: P0 (Critical)
**Estimated Effort**: 2 hours

### Description
Create a custom `VisitValidator` class following the existing `PetValidator` pattern. Implement date validation logic to reject visits scheduled in the past (date < today). Follow strict TDD RED-GREEN-REFACTOR methodology.

### Acceptance Criteria
- [ ] VisitValidator class created implementing Spring Validator interface
- [ ] Validates that visit date >= LocalDate.now()
- [ ] Rejects dates in the past with error code "typeMismatch.visitDate"
- [ ] Handles null dates gracefully (no NullPointerException)
- [ ] Unit tests achieve 100% code coverage
- [ ] All tests pass following TDD RED-GREEN-REFACTOR cycle

### Proof Artifacts
- `VisitValidator.java` implemented with validate() and supports() methods
- `VisitValidatorTests.java` with 6+ passing unit tests
- Test coverage report showing 100% line coverage for VisitValidator
- All tests green in Maven/Gradle output

### Dependencies
- None (foundational task)

### Sub-Tasks

#### [ST-1.1] RED Phase - Test Past Date Rejection
**Type**: Test-First TDD
**Effort**: 15 minutes

**Description**: Write a failing test that validates the core requirement - rejecting dates before today.

**Steps**:
1. Create new test file `src/test/java/org/springframework/samples/petclinic/owner/VisitValidatorTests.java`
2. Add test class structure with @ExtendWith(MockitoExtension.class)
3. Add @BeforeEach setup method initializing Visit, VisitValidator, and Errors objects
4. Write test method `shouldRejectPastDate()`:
   ```java
   @Test
   void shouldRejectPastDate() {
       Visit visit = new Visit();
       visit.setDate(LocalDate.now().minusDays(1)); // Yesterday
       visit.setDescription("Checkup");

       VisitValidator validator = new VisitValidator();
       Errors errors = new MapBindingResult(new HashMap<>(), "visit");
       validator.validate(visit, errors);

       assertTrue(errors.hasFieldErrors("date"));
       assertEquals("typeMismatch.visitDate",
           errors.getFieldError("date").getCode());
   }
   ```
5. Run test and verify it **FAILS** with compilation error (VisitValidator doesn't exist)
6. Commit with message: "RED: Add failing test for past date rejection"

**Verification**: Test fails with "cannot find symbol: class VisitValidator"

---

#### [ST-1.2] GREEN Phase - Create VisitValidator Skeleton
**Type**: Minimal Implementation
**Effort**: 15 minutes

**Description**: Create the minimum code to make the test compile and run (still failing).

**Steps**:
1. Create file `src/main/java/org/springframework/samples/petclinic/owner/VisitValidator.java`
2. Add package declaration and imports
3. Implement empty Validator interface:
   ```java
   public class VisitValidator implements Validator {
       @Override
       public void validate(Object obj, Errors errors) {
           // Empty - test will fail
       }

       @Override
       public boolean supports(Class<?> clazz) {
           return Visit.class.isAssignableFrom(clazz);
       }
   }
   ```
4. Run test - should now **FAIL** at assertion (no validation performed)
5. Commit with message: "GREEN: Create VisitValidator skeleton"

**Verification**: Test compiles and runs but fails at `assertTrue(errors.hasFieldErrors("date"))`

---

#### [ST-1.3] GREEN Phase - Implement Past Date Validation
**Type**: Implementation
**Effort**: 20 minutes

**Description**: Implement the minimum logic to pass the test.

**Steps**:
1. Modify `VisitValidator.validate()` method:
   ```java
   @Override
   public void validate(Object obj, Errors errors) {
       Visit visit = (Visit) obj;
       LocalDate visitDate = visit.getDate();

       if (visitDate != null && visitDate.isBefore(LocalDate.now())) {
           errors.rejectValue("date", "typeMismatch.visitDate",
               "Visit date cannot be in the past");
       }
   }
   ```
2. Run test - should now **PASS**
3. Verify Maven build succeeds: `./mvnw test -Dtest=VisitValidatorTests`
4. Commit with message: "GREEN: Implement past date validation logic"

**Verification**: Test `shouldRejectPastDate()` passes

---

#### [ST-1.4] RED Phase - Test Today's Date is Valid (Boundary Case)
**Type**: Test-First TDD
**Effort**: 10 minutes

**Description**: Write test to verify the boundary condition - today's date should be VALID.

**Steps**:
1. Add new test method in `VisitValidatorTests.java`:
   ```java
   @Test
   void shouldAllowTodayDate() {
       Visit visit = new Visit();
       visit.setDate(LocalDate.now()); // Today
       visit.setDescription("Checkup");

       VisitValidator validator = new VisitValidator();
       Errors errors = new MapBindingResult(new HashMap<>(), "visit");
       validator.validate(visit, errors);

       assertFalse(errors.hasErrors());
   }
   ```
2. Run test - should **PASS** (implementation already uses isBefore, not isBeforeOrEqual)
3. Commit with message: "RED: Add test for today's date boundary case"

**Verification**: Test passes immediately (confirms boundary logic is correct)

---

#### [ST-1.5] RED Phase - Test Future Date is Valid
**Type**: Test-First TDD
**Effort**: 10 minutes

**Description**: Write test to verify future dates are accepted.

**Steps**:
1. Add new test method:
   ```java
   @Test
   void shouldAllowFutureDate() {
       Visit visit = new Visit();
       visit.setDate(LocalDate.now().plusDays(7)); // Next week
       visit.setDescription("Vaccination");

       VisitValidator validator = new VisitValidator();
       Errors errors = new MapBindingResult(new HashMap<>(), "visit");
       validator.validate(visit, errors);

       assertFalse(errors.hasErrors());
   }
   ```
2. Run test - should **PASS** (implementation already handles future dates)
3. Commit with message: "RED: Add test for future date acceptance"

**Verification**: Test passes

---

#### [ST-1.6] RED Phase - Test Null Date Handling
**Type**: Test-First TDD
**Effort**: 10 minutes

**Description**: Write test to verify null dates don't cause NullPointerException.

**Steps**:
1. Add new test method:
   ```java
   @Test
   void shouldNotFailOnNullDate() {
       Visit visit = new Visit();
       visit.setDate(null);
       visit.setDescription("Checkup");

       VisitValidator validator = new VisitValidator();
       Errors errors = new MapBindingResult(new HashMap<>(), "visit");
       validator.validate(visit, errors);

       // Null handling is graceful, no validation error added by validator
       assertFalse(errors.hasFieldErrors("date"));
   }
   ```
2. Run test - should **PASS** (null check already in implementation)
3. Commit with message: "RED: Add test for null date handling"

**Verification**: Test passes

---

#### [ST-1.7] RED Phase - Test Edge Case (One Year Ago)
**Type**: Test-First TDD
**Effort**: 10 minutes

**Description**: Write test for significant past date (edge case coverage).

**Steps**:
1. Add new test method:
   ```java
   @Test
   void shouldRejectDateOneYearAgo() {
       Visit visit = new Visit();
       visit.setDate(LocalDate.now().minusYears(1)); // One year ago
       visit.setDescription("Historical data");

       VisitValidator validator = new VisitValidator();
       Errors errors = new MapBindingResult(new HashMap<>(), "visit");
       validator.validate(visit, errors);

       assertTrue(errors.hasFieldErrors("date"));
   }
   ```
2. Run test - should **PASS**
3. Commit with message: "RED: Add edge case test for date one year ago"

**Verification**: Test passes

---

#### [ST-1.8] RED Phase - Test Edge Case (One Year Ahead)
**Type**: Test-First TDD
**Effort**: 10 minutes

**Description**: Write test for far future date (edge case coverage).

**Steps**:
1. Add new test method:
   ```java
   @Test
   void shouldAllowDateOneYearAhead() {
       Visit visit = new Visit();
       visit.setDate(LocalDate.now().plusYears(1)); // One year ahead
       visit.setDescription("Future appointment");

       VisitValidator validator = new VisitValidator();
       Errors errors = new MapBindingResult(new HashMap<>(), "visit");
       validator.validate(visit, errors);

       assertFalse(errors.hasErrors());
   }
   ```
2. Run test - should **PASS**
3. Commit with message: "RED: Add edge case test for date one year ahead"

**Verification**: Test passes

---

#### [ST-1.9] REFACTOR Phase - Extract Constants and Add Documentation
**Type**: Code Quality
**Effort**: 15 minutes

**Description**: Improve code maintainability without changing behavior.

**Steps**:
1. Extract constants in `VisitValidator.java`:
   ```java
   private static final String DATE_FIELD = "date";
   private static final String DATE_IN_PAST_ERROR = "typeMismatch.visitDate";
   ```
2. Add JavaDoc comments:
   ```java
   /**
    * Validator for Visit forms.
    * Ensures visit dates are not in the past.
    *
    * @author [Your Name]
    */
   public class VisitValidator implements Validator {

       /**
        * Validates that the visit date is not in the past.
        * A null date is considered valid (handled by @NotNull constraint).
        *
        * @param obj the Visit object to validate
        * @param errors the Errors object to store validation errors
        */
       @Override
       public void validate(Object obj, Errors errors) {
           // ... implementation
       }
   }
   ```
3. Run all tests - should still **PASS**
4. Verify no behavior change with `./mvnw test -Dtest=VisitValidatorTests`
5. Commit with message: "REFACTOR: Extract constants and add JavaDoc to VisitValidator"

**Verification**: All tests pass, code coverage remains 100%

---

#### [ST-1.10] Add Nested Test Structure (Optional Enhancement)
**Type**: Test Organization
**Effort**: 15 minutes

**Description**: Improve test organization using @Nested classes (following PetValidatorTests pattern).

**Steps**:
1. Refactor `VisitValidatorTests.java` to use nested classes:
   ```java
   @ExtendWith(MockitoExtension.class)
   @DisabledInNativeImage
   public class VisitValidatorTests {

       private VisitValidator validator;
       private Errors errors;

       @BeforeEach
       void setUp() {
           validator = new VisitValidator();
           errors = new MapBindingResult(new HashMap<>(), "visit");
       }

       @Nested
       class ValidateRejectsPastDates {
           @Test
           void shouldRejectPastDate() { /* ... */ }

           @Test
           void shouldRejectDateOneYearAgo() { /* ... */ }
       }

       @Nested
       class ValidateAcceptsValidDates {
           @Test
           void shouldAllowTodayDate() { /* ... */ }

           @Test
           void shouldAllowFutureDate() { /* ... */ }

           @Test
           void shouldAllowDateOneYearAhead() { /* ... */ }
       }

       @Nested
       class ValidateHandlesEdgeCases {
           @Test
           void shouldNotFailOnNullDate() { /* ... */ }
       }
   }
   ```
2. Run all tests - should still **PASS**
3. Commit with message: "REFACTOR: Organize tests with nested classes"

**Verification**: All tests pass with improved organization

---

## Parent Task 2: Controller Integration

**ID**: PT-2
**Title**: Register VisitValidator in VisitController
**Priority**: P0 (Critical)
**Estimated Effort**: 1.5 hours

### Description
Integrate the VisitValidator into the VisitController using Spring's @InitBinder mechanism. Follow the existing pattern from PetController. Write integration tests using MockMvc to verify the validation is applied during form submission.

### Acceptance Criteria
- [ ] VisitController constructor accepts VisitValidator parameter
- [ ] @InitBinder method registers validator for "visit" model attribute
- [ ] Validator is automatically invoked on form submission
- [ ] Integration tests verify past dates are rejected via HTTP POST
- [ ] Integration tests verify valid dates are accepted
- [ ] All existing VisitController tests still pass

### Proof Artifacts
- Modified `VisitController.java` with validator registration
- Enhanced `VisitControllerTests.java` with validation tests
- All tests green showing validation works end-to-end
- Manual verification via `./mvnw spring-boot:run` and browser testing

### Dependencies
- **Depends on**: PT-1 (VisitValidator must exist)

### Sub-Tasks

#### [ST-2.1] RED Phase - Test Past Date Rejected via Controller
**Type**: Test-First TDD
**Effort**: 20 minutes

**Description**: Write integration test to verify controller rejects past dates via MockMvc.

**Steps**:
1. Open `src/test/java/org/springframework/samples/petclinic/owner/VisitControllerTests.java`
2. Check if file exists, if not, create it following `PetControllerTests.java` structure
3. Add test method:
   ```java
   @Test
   void shouldRejectVisitWithPastDate() throws Exception {
       LocalDate yesterday = LocalDate.now().minusDays(1);

       mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/visits/new",
               TEST_OWNER_ID, TEST_PET_ID)
           .param("date", yesterday.toString())
           .param("description", "Checkup"))
           .andExpect(status().isOk()) // Returns form with errors
           .andExpect(model().attributeHasFieldErrors("visit", "date"))
           .andExpect(view().name("pets/createOrUpdateVisitForm"));
   }
   ```
4. Add necessary mocks for OwnerRepository in @BeforeEach
5. Run test - should **FAIL** (validator not registered yet)
6. Commit with message: "RED: Add integration test for past date rejection"

**Verification**: Test fails with "expected field error on 'date' but got none"

---

#### [ST-2.2] GREEN Phase - Inject VisitValidator into Controller
**Type**: Implementation
**Effort**: 15 minutes

**Description**: Modify VisitController to accept VisitValidator via constructor injection.

**Steps**:
1. Open `src/main/java/org/springframework/samples/petclinic/owner/VisitController.java`
2. Add private field:
   ```java
   private final VisitValidator visitValidator;
   ```
3. Modify constructor:
   ```java
   public VisitController(OwnerRepository owners, VisitValidator visitValidator) {
       this.owners = owners;
       this.visitValidator = visitValidator;
   }
   ```
4. Add @Component annotation to VisitValidator.java to enable auto-wiring:
   ```java
   @Component
   public class VisitValidator implements Validator { /* ... */ }
   ```
5. Compile code - should succeed
6. Run test - should still **FAIL** (validator injected but not registered)
7. Commit with message: "GREEN: Inject VisitValidator into controller"

**Verification**: Code compiles, test still fails

---

#### [ST-2.3] GREEN Phase - Register Validator via @InitBinder
**Type**: Implementation
**Effort**: 15 minutes

**Description**: Register the validator using @InitBinder so Spring calls it automatically.

**Steps**:
1. Add @InitBinder method in `VisitController.java`:
   ```java
   @InitBinder("visit")
   public void initVisitBinder(WebDataBinder dataBinder) {
       dataBinder.addValidators(visitValidator);
   }
   ```
2. Ensure method is placed after constructor, before controller methods
3. Run test `shouldRejectVisitWithPastDate()` - should now **PASS**
4. Verify with `./mvnw test -Dtest=VisitControllerTests#shouldRejectVisitWithPastDate`
5. Commit with message: "GREEN: Register VisitValidator via @InitBinder"

**Verification**: Test `shouldRejectVisitWithPastDate()` passes

---

#### [ST-2.4] RED Phase - Test Today's Date Accepted via Controller
**Type**: Test-First TDD
**Effort**: 15 minutes

**Description**: Write test to verify controller accepts today's date.

**Steps**:
1. Add test method in `VisitControllerTests.java`:
   ```java
   @Test
   void shouldAcceptVisitWithTodayDate() throws Exception {
       LocalDate today = LocalDate.now();

       mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/visits/new",
               TEST_OWNER_ID, TEST_PET_ID)
           .param("date", today.toString())
           .param("description", "Checkup"))
           .andExpect(status().is3xxRedirection())
           .andExpect(view().name("redirect:/owners/{ownerId}"));
   }
   ```
2. Run test - should **PASS** (boundary case works correctly)
3. Commit with message: "RED: Add test for today's date acceptance via controller"

**Verification**: Test passes

---

#### [ST-2.5] RED Phase - Test Future Date Accepted via Controller
**Type**: Test-First TDD
**Effort**: 15 minutes

**Description**: Write test to verify controller accepts future dates.

**Steps**:
1. Add test method:
   ```java
   @Test
   void shouldAcceptVisitWithFutureDate() throws Exception {
       LocalDate nextWeek = LocalDate.now().plusWeeks(1);

       mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/visits/new",
               TEST_OWNER_ID, TEST_PET_ID)
           .param("date", nextWeek.toString())
           .param("description", "Vaccination"))
           .andExpect(status().is3xxRedirection())
           .andExpect(view().name("redirect:/owners/{ownerId}"));
   }
   ```
2. Run test - should **PASS**
3. Commit with message: "RED: Add test for future date acceptance via controller"

**Verification**: Test passes

---

#### [ST-2.6] REFACTOR Phase - Extract Test Data Builders
**Type**: Test Quality
**Effort**: 20 minutes

**Description**: Reduce duplication in test setup with helper methods.

**Steps**:
1. Add helper methods in `VisitControllerTests.java`:
   ```java
   private ResultActions performVisitCreation(LocalDate date, String description)
           throws Exception {
       return mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/visits/new",
               TEST_OWNER_ID, TEST_PET_ID)
           .param("date", date.toString())
           .param("description", description));
   }

   private Visit createVisitWithDate(LocalDate date) {
       Visit visit = new Visit();
       visit.setDate(date);
       visit.setDescription("Test visit");
       return visit;
   }
   ```
2. Refactor existing tests to use helper methods
3. Run all tests - should still **PASS**
4. Commit with message: "REFACTOR: Extract test data builders for visit creation"

**Verification**: All tests pass with reduced duplication

---

#### [ST-2.7] Verify Existing Tests Still Pass
**Type**: Regression Testing
**Effort**: 10 minutes

**Description**: Ensure controller changes don't break existing functionality.

**Steps**:
1. Run full VisitController test suite:
   ```bash
   ./mvnw test -Dtest=VisitControllerTests
   ```
2. Run full owner package test suite:
   ```bash
   ./mvnw test -Dtest="org.springframework.samples.petclinic.owner.*"
   ```
3. If any tests fail, investigate and fix without changing validation behavior
4. Verify all tests green
5. Commit any fixes with message: "FIX: Resolve regression issues in existing tests"

**Verification**: All existing tests pass

---

#### [ST-2.8] Manual Verification via Browser
**Type**: Manual Testing
**Effort**: 15 minutes

**Description**: Manually test the feature in a running application.

**Steps**:
1. Start application:
   ```bash
   ./mvnw spring-boot:run
   ```
2. Open browser to `http://localhost:8080`
3. Navigate to Owners > Find Owners > Select an owner
4. Click "Add New Visit" for any pet
5. **Test Case 1**: Select yesterday's date, submit form
   - **Expected**: Form redisplays with error message
6. **Test Case 2**: Select today's date, submit form
   - **Expected**: Success, redirects to owner page
7. **Test Case 3**: Select future date, submit form
   - **Expected**: Success, redirects to owner page
8. Document results in commit message
9. Commit with message: "MANUAL: Verify validation works in browser"

**Verification**: All manual tests pass

---

## Parent Task 3: Internationalization

**ID**: PT-3
**Title**: Add Error Message Translations for 9 Languages
**Priority**: P1 (High)
**Estimated Effort**: 1.5 hours

### Description
Add the validation error message key `typeMismatch.visitDate` to all 9 supported language files. Ensure translations are accurate and culturally appropriate. Verify message synchronization tests pass.

### Acceptance Criteria
- [ ] English message: "Visit date cannot be in the past"
- [ ] Translations added to all 9 language files
- [ ] Messages follow existing property file formatting conventions
- [ ] I18nPropertiesSyncTest passes (if exists)
- [ ] Test for each language verifies correct message is displayed

### Proof Artifacts
- All 9 messages*.properties files updated with new key
- I18nPropertiesSyncTest passing (or equivalent test)
- Optional: Localization test verifying each language displays correctly
- Git diff showing additions to all property files

### Dependencies
- **Depends on**: PT-1 (error code defined in validator)

### Sub-Tasks

#### [ST-3.1] Add English Message (Default)
**Type**: Internationalization
**Effort**: 5 minutes

**Description**: Add the error message to the default English properties file.

**Steps**:
1. Open `src/main/resources/messages/messages.properties`
2. Locate validation-related messages (search for "typeMismatch")
3. Add new line:
   ```properties
   typeMismatch.visitDate=Visit date cannot be in the past
   ```
4. Save file
5. Run tests to verify message is loaded: `./mvnw test -Dtest=VisitValidatorTests`
6. Commit with message: "I18N: Add English error message for past date validation"

**Verification**: Message appears in properties file

---

#### [ST-3.2] Add English Explicit Message
**Type**: Internationalization
**Effort**: 5 minutes

**Description**: Add to explicit English file for consistency.

**Steps**:
1. Open `src/main/resources/messages/messages_en.properties`
2. Add same message:
   ```properties
   typeMismatch.visitDate=Visit date cannot be in the past
   ```
3. Commit with message: "I18N: Add explicit English (en) message"

**Verification**: Message in messages_en.properties

---

#### [ST-3.3] Add German Translation
**Type**: Internationalization
**Effort**: 10 minutes

**Description**: Add German translation following language conventions.

**Steps**:
1. Open `src/main/resources/messages/messages_de.properties`
2. Add translated message:
   ```properties
   typeMismatch.visitDate=Besuchsdatum darf nicht in der Vergangenheit liegen
   ```
3. Verify encoding is UTF-8
4. Commit with message: "I18N: Add German (de) translation for visit date validation"

**Verification**: German message added

---

#### [ST-3.4] Add Spanish Translation
**Type**: Internationalization
**Effort**: 10 minutes

**Description**: Add Spanish translation.

**Steps**:
1. Open `src/main/resources/messages/messages_es.properties`
2. Add:
   ```properties
   typeMismatch.visitDate=La fecha de la visita no puede estar en el pasado
   ```
3. Commit with message: "I18N: Add Spanish (es) translation"

**Verification**: Spanish message added

---

#### [ST-3.5] Add Korean Translation
**Type**: Internationalization
**Effort**: 10 minutes

**Description**: Add Korean translation with proper Unicode encoding.

**Steps**:
1. Open `src/main/resources/messages/messages_ko.properties`
2. Add:
   ```properties
   typeMismatch.visitDate=방문 날짜는 과거일 수 없습니다
   ```
3. Ensure UTF-8 encoding (Korean uses Unicode characters)
4. Commit with message: "I18N: Add Korean (ko) translation"

**Verification**: Korean message added with correct encoding

---

#### [ST-3.6] Add Persian Translation
**Type**: Internationalization
**Effort**: 10 minutes

**Description**: Add Persian (Farsi) translation with RTL support.

**Steps**:
1. Open `src/main/resources/messages/messages_fa.properties`
2. Add:
   ```properties
   typeMismatch.visitDate=تاریخ ویزیت نمی‌تواند در گذشته باشد
   ```
3. Ensure UTF-8 encoding for Persian characters
4. Commit with message: "I18N: Add Persian (fa) translation"

**Verification**: Persian message added

---

#### [ST-3.7] Add Portuguese Translation
**Type**: Internationalization
**Effort**: 10 minutes

**Description**: Add Portuguese translation.

**Steps**:
1. Open `src/main/resources/messages/messages_pt.properties`
2. Add:
   ```properties
   typeMismatch.visitDate=A data da visita não pode estar no passado
   ```
3. Commit with message: "I18N: Add Portuguese (pt) translation"

**Verification**: Portuguese message added

---

#### [ST-3.8] Add Russian Translation
**Type**: Internationalization
**Effort**: 10 minutes

**Description**: Add Russian translation with Cyrillic characters.

**Steps**:
1. Open `src/main/resources/messages/messages_ru.properties`
2. Add:
   ```properties
   typeMismatch.visitDate=Дата визита не может быть в прошлом
   ```
3. Ensure UTF-8 encoding for Cyrillic script
4. Commit with message: "I18N: Add Russian (ru) translation"

**Verification**: Russian message added

---

#### [ST-3.9] Add Turkish Translation
**Type**: Internationalization
**Effort**: 10 minutes

**Description**: Add Turkish translation.

**Steps**:
1. Open `src/main/resources/messages/messages_tr.properties`
2. Add:
   ```properties
   typeMismatch.visitDate=Ziyaret tarihi geçmişte olamaz
   ```
3. Commit with message: "I18N: Add Turkish (tr) translation"

**Verification**: Turkish message added

---

#### [ST-3.10] Verify Message Synchronization
**Type**: Validation
**Effort**: 10 minutes

**Description**: Verify all property files are synchronized and consistent.

**Steps**:
1. Run i18n synchronization test (if exists):
   ```bash
   ./mvnw test -Dtest=I18nPropertiesSyncTest
   ```
2. If test fails, investigate missing or inconsistent keys
3. If no such test exists, manually verify all 9 files have the same keys
4. Create simple verification script:
   ```bash
   for file in src/main/resources/messages/messages*.properties; do
       echo "Checking $file"
       grep "typeMismatch.visitDate" $file || echo "MISSING KEY!"
   done
   ```
5. Commit with message: "I18N: Verify all translations are synchronized"

**Verification**: All files contain the new message key

---

#### [ST-3.11] Write Localization Test (Optional)
**Type**: Testing
**Effort**: 20 minutes

**Description**: Write test to verify messages display correctly for each locale.

**Steps**:
1. Add test method in `VisitValidatorTests.java`:
   ```java
   @ParameterizedTest
   @CsvSource({
       "en, Visit date cannot be in the past",
       "de, Besuchsdatum darf nicht in der Vergangenheit liegen",
       "es, La fecha de la visita no puede estar en el pasado",
       "ko, 방문 날짜는 과거일 수 없습니다",
       "fa, تاریخ ویزیت نمی‌تواند در گذشته باشد",
       "pt, A data da visita não pode estar no passado",
       "ru, Дата визита не может быть в прошлом",
       "tr, Ziyaret tarihi geçmişte olamaz"
   })
   void shouldShowLocalizedErrorMessage(String language, String expectedMessage) {
       Locale locale = Locale.forLanguageTag(language);
       LocaleContextHolder.setLocale(locale);

       Visit visit = new Visit();
       visit.setDate(LocalDate.now().minusDays(1));

       VisitValidator validator = new VisitValidator();
       Errors errors = new MapBindingResult(new HashMap<>(), "visit");
       validator.validate(visit, errors);

       MessageSource messageSource = createMessageSource();
       String errorMessage = messageSource.getMessage(
           errors.getFieldError("date"), locale);

       assertEquals(expectedMessage, errorMessage);
   }

   private MessageSource createMessageSource() {
       ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
       messageSource.setBasename("messages/messages");
       return messageSource;
   }
   ```
2. Run test - should **PASS** for all locales
3. Commit with message: "TEST: Add parameterized test for all locale translations"

**Verification**: Test passes for all 9 languages

---

## Parent Task 4: Client-Side Enhancement & E2E Testing

**ID**: PT-4
**Title**: Add HTML5 Date Constraints and End-to-End Tests
**Priority**: P2 (Medium)
**Estimated Effort**: 2 hours

### Description
Enhance the user experience by adding HTML5 date input constraints (min attribute) to prevent past date selection in the browser. Write comprehensive Playwright end-to-end tests to verify the complete feature works in a real browser environment.

### Acceptance Criteria
- [ ] Date input has `min` attribute set to today's date
- [ ] Browser date picker disables past dates in UI
- [ ] Server-side validation remains authoritative (defense in depth)
- [ ] E2E test verifies past date rejection with error message
- [ ] E2E test verifies future date acceptance with success
- [ ] E2E test verifies today's date acceptance
- [ ] All E2E tests pass in Playwright

### Proof Artifacts
- Modified `createOrUpdateVisitForm.html` with min attribute
- New file `e2e-tests/tests/visit-validation.spec.ts` with 3+ tests
- Playwright test report showing all tests green
- Screenshots/video of E2E tests passing

### Dependencies
- **Depends on**: PT-2 (controller validation must be working)
- **Depends on**: PT-3 (error messages must be translated)

### Sub-Tasks

#### [ST-4.1] RED Phase - Inspect Current Template Structure
**Type**: Analysis
**Effort**: 10 minutes

**Description**: Understand the current Thymeleaf template structure before modification.

**Steps**:
1. Open `src/main/resources/templates/pets/createOrUpdateVisitForm.html`
2. Locate the date input field:
   ```html
   <input th:replace="~{fragments/inputField :: input ('Date', 'date', 'date')}" />
   ```
3. Check if template uses fragment replacement pattern
4. Open `src/main/resources/templates/fragments/inputField.html` to understand fragment
5. Determine if modification should be in main template or fragment
6. Document findings in task notes

**Verification**: Template structure understood

---

#### [ST-4.2] Modify Date Input to Add Min Constraint
**Type**: Implementation
**Effort**: 15 minutes

**Description**: Add HTML5 min attribute to date input field.

**Steps**:
1. Open `src/main/resources/templates/pets/createOrUpdateVisitForm.html`
2. Modify the date input line to add `th:min` attribute:
   ```html
   <input th:replace="~{fragments/inputField :: input ('Date', 'date', 'date')}"
          th:min="${#temporals.format(#temporals.createToday(), 'yyyy-MM-dd')}" />
   ```
   **OR** if fragment doesn't support attributes, replace fragment call with direct input:
   ```html
   <div class="form-group">
       <label for="date" th:text="#{date}">Date</label>
       <input type="date"
              th:field="*{date}"
              class="form-control"
              th:min="${#temporals.format(#temporals.createToday(), 'yyyy-MM-dd')}" />
       <span th:if="${#fields.hasErrors('date')}"
             th:errors="*{date}"
             class="help-block">Error</span>
   </div>
   ```
3. Save file
4. Start application: `./mvnw spring-boot:run`
5. Test manually in browser - verify date picker doesn't allow past dates
6. Commit with message: "FEAT: Add HTML5 min constraint to visit date picker"

**Verification**: Date picker in browser disables past dates

---

#### [ST-4.3] RED Phase - Create E2E Test File Structure
**Type**: Test Setup
**Effort**: 15 minutes

**Description**: Create Playwright test file for visit validation scenarios.

**Steps**:
1. Create new file `e2e-tests/tests/visit-validation.spec.ts`
2. Add test structure:
   ```typescript
   import { test, expect } from '@playwright/test';

   test.describe('Visit Date Validation', () => {
       test.beforeEach(async ({ page }) => {
           // Navigate to application
           await page.goto('/');
       });

       // Tests will be added in subsequent sub-tasks
   });
   ```
3. Verify test file is recognized:
   ```bash
   cd e2e-tests
   npm test -- visit-validation.spec.ts
   ```
4. Should execute (but have no tests yet)
5. Commit with message: "RED: Create E2E test structure for visit validation"

**Verification**: Test file executes with no tests

---

#### [ST-4.4] RED Phase - E2E Test for Past Date Rejection
**Type**: Test-First TDD
**Effort**: 25 minutes

**Description**: Write failing E2E test that verifies past date shows error.

**Steps**:
1. Add test in `visit-validation.spec.ts`:
   ```typescript
   test('should show error when scheduling visit with past date', async ({ page }) => {
       // Navigate to owner details page
       await page.goto('/owners/1');

       // Find first pet's "Add New Visit" link
       await page.click('text=Add New Visit');

       // Wait for form to load
       await expect(page).toHaveURL(/\/owners\/\d+\/pets\/\d+\/visits\/new/);

       // Calculate yesterday's date
       const yesterday = new Date();
       yesterday.setDate(yesterday.getDate() - 1);
       const yesterdayStr = yesterday.toISOString().split('T')[0];

       // Fill form with past date
       await page.fill('input[name="date"]', yesterdayStr);
       await page.fill('textarea[name="description"]', 'Test checkup');

       // Submit form
       await page.click('button[type="submit"]');

       // Verify error message appears
       await expect(page.locator('.alert-danger, .error, .help-block'))
           .toContainText('Visit date cannot be in the past');

       // Verify still on form page (not redirected)
       await expect(page).toHaveURL(/\/owners\/\d+\/pets\/\d+\/visits\/new/);
   });
   ```
2. Run test:
   ```bash
   cd e2e-tests
   npm test -- visit-validation.spec.ts
   ```
3. If application is working, test should **PASS**
4. Commit with message: "RED: Add E2E test for past date rejection"

**Verification**: Test passes (feature already implemented in PT-2)

---

#### [ST-4.5] RED Phase - E2E Test for Future Date Acceptance
**Type**: Test-First TDD
**Effort**: 20 minutes

**Description**: Write E2E test that verifies future dates are accepted.

**Steps**:
1. Add test:
   ```typescript
   test('should successfully schedule visit with future date', async ({ page }) => {
       await page.goto('/owners/1');
       await page.click('text=Add New Visit');

       // Calculate next week's date
       const nextWeek = new Date();
       nextWeek.setDate(nextWeek.getDate() + 7);
       const nextWeekStr = nextWeek.toISOString().split('T')[0];

       // Fill form
       await page.fill('input[name="date"]', nextWeekStr);
       await page.fill('textarea[name="description"]', 'Vaccination appointment');

       // Submit form
       await page.click('button[type="submit"]');

       // Verify success message
       await expect(page.locator('.alert-success, .message'))
           .toContainText('Your visit has been booked');

       // Verify redirected to owner page
       await expect(page).toHaveURL(/\/owners\/\d+$/);
   });
   ```
2. Run test - should **PASS**
3. Commit with message: "RED: Add E2E test for future date acceptance"

**Verification**: Test passes

---

#### [ST-4.6] RED Phase - E2E Test for Today's Date Acceptance
**Type**: Test-First TDD
**Effort**: 20 minutes

**Description**: Write E2E test for boundary case - today's date.

**Steps**:
1. Add test:
   ```typescript
   test('should successfully schedule visit with today date', async ({ page }) => {
       await page.goto('/owners/1');
       await page.click('text=Add New Visit');

       // Get today's date
       const today = new Date();
       const todayStr = today.toISOString().split('T')[0];

       // Fill form
       await page.fill('input[name="date"]', todayStr);
       await page.fill('textarea[name="description"]', 'Emergency visit');

       // Submit form
       await page.click('button[type="submit"]');

       // Verify success
       await expect(page.locator('.alert-success, .message'))
           .toContainText('Your visit has been booked');
       await expect(page).toHaveURL(/\/owners\/\d+$/);
   });
   ```
2. Run test - should **PASS**
3. Commit with message: "RED: Add E2E test for today's date boundary case"

**Verification**: Test passes

---

#### [ST-4.7] E2E Test for Date Picker Constraint
**Type**: Testing
**Effort**: 20 minutes

**Description**: Verify HTML5 min attribute is present in DOM.

**Steps**:
1. Add test:
   ```typescript
   test('should have min attribute on date picker to prevent past dates', async ({ page }) => {
       await page.goto('/owners/1');
       await page.click('text=Add New Visit');

       // Get date input element
       const dateInput = page.locator('input[name="date"]');

       // Verify min attribute exists
       const minAttr = await dateInput.getAttribute('min');
       expect(minAttr).toBeTruthy();

       // Verify min is today or in the future
       const today = new Date().toISOString().split('T')[0];
       expect(minAttr).toEqual(today);
   });
   ```
2. Run test - should **PASS**
3. Commit with message: "TEST: Verify HTML5 min attribute on date picker"

**Verification**: Test passes

---

#### [ST-4.8] Run Full E2E Test Suite
**Type**: Integration Testing
**Effort**: 10 minutes

**Description**: Verify all E2E tests pass together.

**Steps**:
1. Run full test suite:
   ```bash
   cd e2e-tests
   npm test
   ```
2. Review test report in `test-results/html-report/index.html`
3. Verify all visit validation tests pass
4. Check for any flaky tests and investigate
5. Generate final report:
   ```bash
   npm run report
   ```
6. Commit with message: "TEST: Verify full E2E test suite passes"

**Verification**: All E2E tests green

---

#### [ST-4.9] REFACTOR Phase - Add Test Helper Functions
**Type**: Test Quality
**Effort**: 15 minutes

**Description**: Extract common test functionality to reduce duplication.

**Steps**:
1. Add helper functions at top of test file:
   ```typescript
   async function navigateToAddVisitForm(page: Page, ownerId: number = 1) {
       await page.goto(`/owners/${ownerId}`);
       await page.click('text=Add New Visit');
       await expect(page).toHaveURL(/\/owners\/\d+\/pets\/\d+\/visits\/new/);
   }

   function formatDateForInput(date: Date): string {
       return date.toISOString().split('T')[0];
   }

   async function submitVisitForm(page: Page, date: string, description: string) {
       await page.fill('input[name="date"]', date);
       await page.fill('textarea[name="description"]', description);
       await page.click('button[type="submit"]');
   }
   ```
2. Refactor existing tests to use helpers
3. Run tests - should still **PASS**
4. Commit with message: "REFACTOR: Extract E2E test helper functions"

**Verification**: All tests pass with improved readability

---

#### [ST-4.10] Generate Final Test Coverage Report
**Type**: Documentation
**Effort**: 10 minutes

**Description**: Generate comprehensive test coverage report for the feature.

**Steps**:
1. Run Java tests with coverage:
   ```bash
   ./mvnw clean test jacoco:report
   ```
2. Open coverage report: `target/site/jacoco/index.html`
3. Navigate to `VisitValidator` and verify 100% coverage
4. Navigate to `VisitController` and verify validator integration is covered
5. Run E2E tests and capture artifacts:
   ```bash
   cd e2e-tests
   npm test
   ```
6. Take screenshots of passing tests
7. Create summary document (optional)
8. Commit with message: "DOCS: Generate final test coverage report"

**Verification**: Coverage reports show comprehensive testing

---

## Task Dependencies Visualization

```
PT-1 (Core Validation)
  └─> PT-2 (Controller Integration)
        ├─> PT-3 (Internationalization)
        └─> PT-4 (Client-Side & E2E)
```

**Critical Path**: PT-1 → PT-2 → PT-4
**Parallel Work**: PT-3 can be done alongside PT-2

---

## Quality Gates

Before marking feature as COMPLETE, verify:

- [ ] All unit tests pass (VisitValidatorTests)
- [ ] All integration tests pass (VisitControllerTests)
- [ ] All E2E tests pass (visit-validation.spec.ts)
- [ ] Test coverage >= 90% for new code (target: 100% for VisitValidator)
- [ ] All 9 language files have the error message
- [ ] I18nPropertiesSyncTest passes
- [ ] Manual browser testing confirms validation works
- [ ] No regressions in existing tests
- [ ] Code follows project conventions (checkstyle, spotbugs)
- [ ] All commits follow TDD RED-GREEN-REFACTOR methodology

---

## Rollback Plan

If issues are discovered after merge:

1. **Revert controller changes**: Remove validator registration from VisitController
2. **Keep validator class**: Leave VisitValidator.java for future use
3. **Revert template changes**: Remove min attribute from date input
4. **Keep translations**: Leave message keys in properties files (no harm)

**Revert Command**:
```bash
git revert <commit-hash> --no-commit
git commit -m "Revert: Disable visit date validation due to [reason]"
```

---

## Post-Implementation Tasks (Optional)

After feature is deployed and verified in production:

- [ ] Monitor error logs for validation errors
- [ ] Track metrics: % of visits rejected due to past dates
- [ ] Gather user feedback on error message clarity
- [ ] Consider adding admin override capability (future enhancement)
- [ ] Evaluate need for timezone-aware validation
- [ ] Update user documentation with validation rules

---

## Notes

- **TDD Discipline**: Every sub-task follows RED-GREEN-REFACTOR
- **Test Coverage**: Aim for 100% coverage on VisitValidator, 90%+ on controller
- **Defense in Depth**: Client-side (HTML5) + Server-side (Spring Validator)
- **Internationalization**: Critical for global user base - all 9 languages required
- **Browser Compatibility**: HTML5 date input widely supported (fallback to text input on old browsers, but server validation is authoritative)

---

**Total Estimated Effort**: 7-9 hours
**Complexity**: Medium
**Risk Level**: Low (well-defined feature with clear acceptance criteria)

**End of Task List**
