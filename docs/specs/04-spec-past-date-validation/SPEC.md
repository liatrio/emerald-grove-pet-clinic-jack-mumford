# Specification: Past Date Validation for Visit Scheduling

**Feature ID**: Issue #8
**Status**: Draft
**Created**: 2026-02-12
**TDD Required**: Yes (Strict RED-GREEN-REFACTOR)

---

## 1. Feature Overview

### Summary
Add validation to prevent scheduling veterinary visits in the past, ensuring visits can only be scheduled for today or future dates.

### Business Value
- Prevents data entry errors (accidentally backdated visits)
- Maintains data integrity for visit scheduling
- Improves user experience with clear validation feedback

### Scope
- **In Scope**: Date validation for new visits, custom validator, i18n error messages, HTML5 date picker constraints
- **Out of Scope**: Editing existing past visits, admin override capability, timezone handling

---

## 2. Acceptance Criteria

From GitHub Issue #8:
- [x] Visit form rejects dates earlier than today
- [x] A clear validation message is displayed when a past date is submitted
- [x] Visits for today and future dates still work
- [x] Validation rule: Date must be >= today (visits can be scheduled for today)
- [x] Error message translates to all 9 supported languages
- [x] HTML5 date input has `min="today"` attribute for client-side prevention
- [x] Validation occurs via custom `VisitValidator` class
- [x] Form displays error at top of form (global error message)

---

## 3. Functional Requirements

### FR-1: Validation Rule
**Requirement**: Visit date must be greater than or equal to today's date.

**Specification**:
- **Valid**: Date >= current system date (`LocalDate.now()`)
- **Invalid**: Date < current system date
- **Boundary**: Today's date is VALID (visits can be scheduled for today)

**Examples** (assuming today is 2026-02-12):
- ✅ Valid: `2026-02-12` (today)
- ✅ Valid: `2026-02-13` (tomorrow)
- ✅ Valid: `2026-03-01` (future)
- ❌ Invalid: `2026-02-11` (yesterday)
- ❌ Invalid: `2025-12-31` (past year)

### FR-2: Validation Error Message
**Requirement**: Display clear, actionable error message when validation fails.

**Message Key**: `typeMismatch.visitDate`

**Message Content** (English): "Visit date cannot be in the past"

**Error Location**: Top of form (global error message)

**Translations Required**: All 9 supported languages:
- **English (en)**: "Visit date cannot be in the past"
- **German (de)**: "Besuchsdatum darf nicht in der Vergangenheit liegen"
- **Spanish (es)**: "La fecha de la visita no puede estar en el pasado"
- **Korean (ko)**: "방문 날짜는 과거일 수 없습니다"
- **Persian (fa)**: "تاریخ ویزیت نمی‌تواند در گذشته باشد"
- **Portuguese (pt)**: "A data da visita não pode estar no passado"
- **Russian (ru)**: "Дата визита не может быть в прошлом"
- **Turkish (tr)**: "Ziyaret tarihi geçmişte olamaz"
- **Chinese (zh)**: "访问日期不能是过去的日期" *(assuming 9th language is Chinese)*

### FR-3: Client-Side Prevention
**Requirement**: Add HTML5 date input constraint to prevent past date selection in UI.

**Implementation**: Add `min` attribute to date input field

**Template Change** (`createOrUpdateVisitForm.html`):
```html
<input type="date" th:field="*{date}" th:min="${#temporals.format(#temporals.createToday(), 'yyyy-MM-dd')}" />
```

**Behavior**:
- Date picker disables past dates
- User cannot select dates before today in calendar UI
- Server-side validation still required (defense in depth)

### FR-4: Form Behavior
**Requirement**: Maintain current default date behavior.

**Default Date**: `LocalDate.now()` (today) - **no change**

**Rationale**: Default to today ensures valid input by default

### FR-5: Existing Data Handling
**Requirement**: Do not modify or validate existing visits with past dates.

**Behavior**:
- Validation applies only to new visit creation
- Editing existing visits not affected by this validation (out of scope)
- Historical data remains untouched

---

## 4. Technical Design

### 4.1 Architecture

**Component**: Custom validator following existing `PetValidator` pattern

**Validation Flow**:
```
User Submits Form → VisitController.processNewVisitForm()
                     ↓
                     Spring calls VisitValidator.validate()
                     ↓
                     Check: visit.date >= LocalDate.now()
                     ↓
                     If invalid: errors.rejectValue("date", "typeMismatch.visitDate")
                     ↓
                     BindingResult.hasErrors() → return form view with error
```

### 4.2 VisitValidator Class

**Location**: `src/main/java/org/springframework/samples/petclinic/owner/VisitValidator.java` (new file)

**Implementation**:
```java
package org.springframework.samples.petclinic.owner;

import java.time.LocalDate;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validator for Visit forms.
 * Ensures visit dates are not in the past.
 */
public class VisitValidator implements Validator {

    private static final String DATE_FIELD = "date";
    private static final String DATE_IN_PAST_ERROR = "typeMismatch.visitDate";

    @Override
    public void validate(Object obj, Errors errors) {
        Visit visit = (Visit) obj;
        LocalDate visitDate = visit.getDate();

        // Validate date is not in the past
        if (visitDate != null && visitDate.isBefore(LocalDate.now())) {
            errors.rejectValue(DATE_FIELD, DATE_IN_PAST_ERROR, "Visit date cannot be in the past");
        }
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return Visit.class.isAssignableFrom(clazz);
    }
}
```

### 4.3 Controller Integration

**Location**: `src/main/java/org/springframework/samples/petclinic/owner/VisitController.java`

**Changes**:
1. Inject `VisitValidator` as a field
2. Register validator in `@InitBinder` method

**Code Addition**:
```java
@Controller
class VisitController {

    private final OwnerRepository owners;
    private final VisitValidator visitValidator;  // NEW

    public VisitController(OwnerRepository owners, VisitValidator visitValidator) {  // MODIFIED
        this.owners = owners;
        this.visitValidator = visitValidator;  // NEW
    }

    @InitBinder("visit")  // NEW METHOD
    public void initVisitBinder(WebDataBinder dataBinder) {
        dataBinder.addValidators(visitValidator);
    }

    // Existing methods unchanged
}
```

**Validator Registration**: Spring will automatically call `VisitValidator.validate()` before `processNewVisitForm()` due to `@Valid` annotation

### 4.4 Internationalization (i18n)

**Files to Update**: All 9 message properties files in `src/main/resources/messages/`

**Files**:
- `messages.properties` (English - default)
- `messages_de.properties` (German)
- `messages_es.properties` (Spanish)
- `messages_ko.properties` (Korean)
- `messages_fa.properties` (Persian)
- `messages_pt.properties` (Portuguese)
- `messages_ru.properties` (Russian)
- `messages_tr.properties` (Turkish)
- `messages_zh.properties` (Chinese - if 9th language)

**Addition to Each File**:
```properties
typeMismatch.visitDate=Visit date cannot be in the past
```
*(Translated appropriately per language)*

### 4.5 Template Update

**Location**: `src/main/resources/templates/pets/createOrUpdateVisitForm.html`

**Current Date Input**:
```html
<input type="date" th:field="*{date}" class="form-control" />
```

**Updated Date Input**:
```html
<input type="date" th:field="*{date}" class="form-control"
       th:min="${#temporals.format(#temporals.createToday(), 'yyyy-MM-dd')}" />
```

**Effect**: Browser date picker will disable past dates

---

## 5. TDD Approach (RED-GREEN-REFACTOR)

### Phase 1: Validator Unit Tests

#### RED 1: Test Past Date Rejection
**Test**: `VisitValidatorTests.shouldRejectPastDate()`
```java
@Test
void shouldRejectPastDate() {
    Visit visit = new Visit();
    visit.setDate(LocalDate.now().minusDays(1)); // Yesterday
    visit.setDescription("Checkup");

    Validator validator = new VisitValidator();
    BeanPropertyBindingResult errors = new BeanPropertyBindingResult(visit, "visit");
    validator.validate(visit, errors);

    assertThat(errors.hasErrors()).isTrue();
    assertThat(errors.getFieldError("date")).isNotNull();
    assertThat(errors.getFieldError("date").getCode()).isEqualTo("typeMismatch.visitDate");
}
```
**Expected**: FAIL (VisitValidator doesn't exist)

#### GREEN 1: Create VisitValidator with Past Date Check
```java
public class VisitValidator implements Validator {
    @Override
    public void validate(Object obj, Errors errors) {
        Visit visit = (Visit) obj;
        if (visit.getDate() != null && visit.getDate().isBefore(LocalDate.now())) {
            errors.rejectValue("date", "typeMismatch.visitDate");
        }
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return Visit.class.isAssignableFrom(clazz);
    }
}
```
**Expected**: PASS

#### RED 2: Test Today's Date is Valid
**Test**: `VisitValidatorTests.shouldAllowTodayDate()`
```java
@Test
void shouldAllowTodayDate() {
    Visit visit = new Visit();
    visit.setDate(LocalDate.now()); // Today
    visit.setDescription("Checkup");

    Validator validator = new VisitValidator();
    BeanPropertyBindingResult errors = new BeanPropertyBindingResult(visit, "visit");
    validator.validate(visit, errors);

    assertThat(errors.hasErrors()).isFalse();
}
```
**Expected**: PASS (implementation already handles >= today)

#### RED 3: Test Future Date is Valid
**Test**: `VisitValidatorTests.shouldAllowFutureDate()`
```java
@Test
void shouldAllowFutureDate() {
    Visit visit = new Visit();
    visit.setDate(LocalDate.now().plusDays(7)); // Next week
    visit.setDescription("Vaccination");

    Validator validator = new VisitValidator();
    BeanPropertyBindingResult errors = new BeanPropertyBindingResult(visit, "visit");
    validator.validate(visit, errors);

    assertThat(errors.hasErrors()).isFalse();
}
```
**Expected**: PASS

#### RED 4: Test Null Date Handling
**Test**: `VisitValidatorTests.shouldNotFailOnNullDate()`
```java
@Test
void shouldNotFailOnNullDate() {
    Visit visit = new Visit();
    visit.setDate(null);
    visit.setDescription("Checkup");

    Validator validator = new VisitValidator();
    BeanPropertyBindingResult errors = new BeanPropertyBindingResult(visit, "visit");
    validator.validate(visit, errors);

    // Null date is handled by @NotNull on Visit entity, not by VisitValidator
    assertThat(errors.getFieldErrorCount("date")).isZero();
}
```
**Expected**: PASS (null check already in validator)

#### REFACTOR 1: Extract Constants
- Extract `DATE_FIELD = "date"` constant
- Extract `DATE_IN_PAST_ERROR = "typeMismatch.visitDate"` constant
- Add JavaDoc comments

### Phase 2: Controller Integration Tests

#### RED 5: Test Past Date Rejected via Controller
**Test**: `VisitControllerTests.shouldRejectVisitWithPastDate()`
```java
@Test
void shouldRejectVisitWithPastDate() throws Exception {
    LocalDate yesterday = LocalDate.now().minusDays(1);

    mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID)
        .param("date", yesterday.toString())
        .param("description", "Checkup"))
        .andExpect(status().isOk()) // Returns form with errors, not redirect
        .andExpect(model().attributeHasFieldErrors("visit", "date"))
        .andExpect(view().name("pets/createOrUpdateVisitForm"));
}
```
**Expected**: FAIL (validator not registered in controller)

#### GREEN 5: Register VisitValidator in Controller
```java
@Controller
class VisitController {
    private final VisitValidator visitValidator;

    public VisitController(OwnerRepository owners, VisitValidator visitValidator) {
        this.owners = owners;
        this.visitValidator = visitValidator;
    }

    @InitBinder("visit")
    public void initVisitBinder(WebDataBinder dataBinder) {
        dataBinder.addValidators(visitValidator);
    }
}
```
**Expected**: PASS

#### RED 6: Test Today's Date Accepted
**Test**: `VisitControllerTests.shouldAcceptVisitWithTodayDate()`
```java
@Test
void shouldAcceptVisitWithTodayDate() throws Exception {
    LocalDate today = LocalDate.now();

    mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID)
        .param("date", today.toString())
        .param("description", "Checkup"))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/owners/{ownerId}"));
}
```
**Expected**: PASS

#### RED 7: Test Future Date Accepted
**Test**: `VisitControllerTests.shouldAcceptVisitWithFutureDate()`
```java
@Test
void shouldAcceptVisitWithFutureDate() throws Exception {
    LocalDate nextWeek = LocalDate.now().plusWeeks(1);

    mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID)
        .param("date", nextWeek.toString())
        .param("description", "Vaccination"))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/owners/{ownerId}"));
}
```
**Expected**: PASS

#### REFACTOR 2: Extract Test Data Builders
- Create `createValidVisit()` test helper
- Create `createVisitWithDate(LocalDate date)` helper
- Reduce duplication in test setup

### Phase 3: Internationalization Tests

#### RED 8: Test Error Message Translation
**Test**: `VisitValidatorTests.shouldShowLocalizedErrorMessage()`
```java
@Test
void shouldShowLocalizedErrorMessageInGerman() {
    LocaleContextHolder.setLocale(Locale.GERMAN);

    Visit visit = new Visit();
    visit.setDate(LocalDate.now().minusDays(1));
    visit.setDescription("Checkup");

    Validator validator = new VisitValidator();
    BeanPropertyBindingResult errors = new BeanPropertyBindingResult(visit, "visit");
    validator.validate(visit, errors);

    MessageSource messageSource = createMessageSource();
    String errorMessage = messageSource.getMessage(
        errors.getFieldError("date"), Locale.GERMAN);

    assertThat(errorMessage).isEqualTo("Besuchsdatum darf nicht in der Vergangenheit liegen");
}
```
**Expected**: FAIL (message key not in messages_de.properties)

#### GREEN 8: Add i18n Messages
Add `typeMismatch.visitDate` to all 9 language files with translations.
**Expected**: PASS

### Phase 4: End-to-End Tests (Playwright)

#### RED 9: Test Past Date Validation E2E
**Test**: `e2e-tests/tests/visit-validation.spec.ts`
```typescript
test('should show error when scheduling visit with past date', async ({ page }) => {
    await page.goto('/owners/1');
    await page.click('text=Add New Visit');

    const yesterday = new Date();
    yesterday.setDate(yesterday.getDate() - 1);
    const yesterdayStr = yesterday.toISOString().split('T')[0];

    await page.fill('input[name="date"]', yesterdayStr);
    await page.fill('textarea[name="description"]', 'Checkup');
    await page.click('button[type="submit"]');

    await expect(page.locator('.alert-danger, .error')).toContainText('Visit date cannot be in the past');
});
```
**Expected**: FAIL (validation not integrated)

#### GREEN 9: Complete Integration
All previous steps complete the integration.
**Expected**: PASS

#### RED 10: Test Future Date E2E
**Test**: `e2e-tests/tests/visit-validation.spec.ts`
```typescript
test('should successfully schedule visit with future date', async ({ page }) => {
    await page.goto('/owners/1');
    await page.click('text=Add New Visit');

    const nextWeek = new Date();
    nextWeek.setDate(nextWeek.getDate() + 7);
    const nextWeekStr = nextWeek.toISOString().split('T')[0];

    await page.fill('input[name="date"]', nextWeekStr);
    await page.fill('textarea[name="description"]', 'Vaccination');
    await page.click('button[type="submit"]');

    await expect(page).toHaveURL(/\/owners\/\d+/);
    await expect(page.locator('.alert-success, .message')).toContainText('Your visit has been booked');
});
```
**Expected**: PASS

---

## 6. Test Scenarios

### 6.1 Unit Tests (VisitValidatorTests.java)

| Test Case | Description | Expected Result |
|-----------|-------------|-----------------|
| `shouldRejectPastDate()` | Date = yesterday | Validation error on "date" field |
| `shouldAllowTodayDate()` | Date = today | No validation errors |
| `shouldAllowFutureDate()` | Date = next week | No validation errors |
| `shouldNotFailOnNullDate()` | Date = null | No NPE, null handled gracefully |
| `shouldRejectDateOneYearAgo()` | Date = 1 year ago | Validation error |
| `shouldAllowDateOneYearAhead()` | Date = 1 year ahead | No validation errors |

### 6.2 Controller Integration Tests (VisitControllerTests.java)

| Test Case | Description | Expected Result |
|-----------|-------------|-----------------|
| `shouldRejectVisitWithPastDate()` | POST with yesterday's date | 200 OK, returns form with errors |
| `shouldAcceptVisitWithTodayDate()` | POST with today's date | 302 redirect to owner page |
| `shouldAcceptVisitWithFutureDate()` | POST with future date | 302 redirect to owner page |
| `shouldShowErrorMessageOnForm()` | Past date submitted | Error message visible in model |

### 6.3 Internationalization Tests

| Test Case | Description | Expected Result |
|-----------|-------------|-----------------|
| `shouldShowErrorInEnglish()` | Locale = en | "Visit date cannot be in the past" |
| `shouldShowErrorInGerman()` | Locale = de | German translation shown |
| `shouldShowErrorInSpanish()` | Locale = es | Spanish translation shown |
| `shouldShowErrorInAllLanguages()` | Loop through all 9 locales | Appropriate translation for each |

### 6.4 End-to-End Tests (Playwright)

| Test Case | Description | Expected Result |
|-----------|-------------|-----------------|
| `shouldShowErrorForPastDate()` | Submit form with yesterday | Error message appears |
| `shouldAcceptFutureDate()` | Submit form with next week | Success message, visit created |
| `shouldAcceptTodayDate()` | Submit form with today | Success message, visit created |
| `shouldDisablePastDatesInPicker()` | Check date input min attribute | Past dates disabled in UI |

---

## 7. Implementation Plan

### Step 1: Create VisitValidator Class
**Files**: `VisitValidator.java` (new)
- Implement `Validator` interface
- Add `validate()` method with date check
- Add `supports()` method

**Tests**: `VisitValidatorTests.java` (new)
- Follow TDD RED-GREEN-REFACTOR from Section 5

### Step 2: Register Validator in VisitController
**Files**: `VisitController.java`
- Add `VisitValidator` field and constructor injection
- Add `@InitBinder("visit")` method to register validator

**Tests**: `VisitControllerTests.java` (add tests)

### Step 3: Add Internationalization Messages
**Files**: All 9 `messages*.properties` files
- Add `typeMismatch.visitDate` key with translated messages

**Tests**: `I18nPropertiesSyncTest.java` (should pass after additions)

### Step 4: Update Visit Form Template
**Files**: `createOrUpdateVisitForm.html`
- Add `th:min` attribute to date input

**Tests**: Manual verification or Playwright

### Step 5: Add End-to-End Tests
**Files**: `e2e-tests/tests/visit-validation.spec.ts` (new)
- Write Playwright tests for past/today/future date scenarios

**Tests**: Run via `npm test`

---

## 8. Dependencies

### Internal
- **Visit Entity**: No changes needed
- **VisitController**: Requires validator registration
- **OwnerRepository**: No changes needed
- **Message Properties**: Requires new message key in all 9 languages

### External
- None (uses standard Java `LocalDate` and Spring Validation)

### Spring Configuration
- **VisitValidator Bean**: Auto-discovered by Spring component scan (if annotated) or manually injected

---

## 9. Non-Functional Requirements

### Performance
- **Impact**: Minimal - single date comparison (`LocalDate.isBefore()`)
- **Target**: Validation completes in < 1ms

### Security
- **Client-Side Bypass**: HTML5 `min` attribute can be bypassed
- **Mitigation**: Server-side validation is authoritative (defense in depth)

### Usability
- **Error Clarity**: Message clearly states the issue and implies solution
- **Default Value**: Form defaults to today (valid date) to minimize errors

### Internationalization
- **Coverage**: All 9 supported languages receive translated error messages
- **Consistency**: Message key follows existing pattern (`typeMismatch.*`)

---

## 10. Risk Assessment

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| Timezone issues | Low | Medium | Use system date, document behavior |
| Existing past visits affected | Low | Low | Validation only for new visits |
| Translation quality | Medium | Low | Request review from native speakers |
| Browser date picker compatibility | Low | Low | Server-side validation is primary |
| Edge case at midnight | Very Low | Low | Document that date is compared, not time |

---

## 11. Future Enhancements (Out of Scope)

- **Admin override**: Allow authorized users to schedule past visits for data correction
- **Timezone awareness**: Use clinic timezone instead of system timezone
- **Edit validation**: Apply validation to visit edits (currently only creation)
- **Date range validation**: Prevent scheduling too far in future (e.g., > 1 year)
- **Business hours validation**: Only allow visits during clinic operating hours
- **Capacity checking**: Prevent overbooking on specific dates

---

## 12. Acceptance Testing Checklist

- [ ] VisitValidator created and implements Validator interface
- [ ] Validator rejects dates before today
- [ ] Validator accepts today's date
- [ ] Validator accepts future dates
- [ ] Validator handles null dates without NPE
- [ ] VisitController registers validator via @InitBinder
- [ ] All 9 message properties files have `typeMismatch.visitDate` key
- [ ] Error message translates correctly in all languages
- [ ] Visit form template has `min` attribute on date input
- [ ] Unit tests for VisitValidator achieve 100% coverage
- [ ] Controller integration tests pass
- [ ] Playwright E2E test verifies past date rejection
- [ ] Playwright E2E test verifies future date acceptance
- [ ] I18nPropertiesSyncTest passes with new message keys

---

## 13. Definition of Done

- [ ] All TDD cycles completed (RED-GREEN-REFACTOR)
- [ ] Unit tests written and passing (100% coverage for validator)
- [ ] Integration tests written and passing
- [ ] Internationalization complete (all 9 languages)
- [ ] Playwright E2E tests written and passing
- [ ] Code review completed
- [ ] No Checkstyle/SpotBugs violations
- [ ] Feature tested manually in dev environment
- [ ] Merged to main branch via PR

---

**End of Specification**
