# Specification: Duplicate Owner Prevention

**Feature ID**: Issue #6
**Status**: Draft
**Created**: 2026-02-12
**TDD Required**: Yes (Strict RED-GREEN-REFACTOR)

---

## 1. Feature Overview

### Summary
Prevent creating duplicate owner records by detecting existing owners with matching first name, last name, and telephone number during owner creation.

### Business Value
- Maintains data integrity and prevents duplicate records
- Improves data quality for reporting and analytics
- Reduces user confusion from multiple records for same owner
- Prevents accidental re-registration of existing clients

### Scope
- **In Scope**: Duplicate detection during creation, case-insensitive matching, form-level error display, database index
- **Out of Scope**: Fuzzy matching, phonetic matching, duplicate detection during updates, merging duplicate records

---

## 2. Acceptance Criteria

From GitHub Issue #6:
- [x] Attempting to create a duplicate owner is blocked
- [x] The UI shows a clear, actionable error message
- [x] The duplicate attempt does not create a second owner record
- [x] Duplicate defined as: same first name + last name + telephone (case-insensitive, exact match)
- [x] Validation occurs before save at repository layer
- [x] Updates allow self-match (exclude current owner from duplicate check)
- [x] Error displayed at top of form (form-level error)
- [x] Database index added for query performance

---

## 3. Functional Requirements

### FR-1: Duplicate Detection Rule
**Requirement**: Detect duplicates using first name, last name, and telephone as composite key.

**Specification**:
- **Fields Used**: `firstName`, `lastName`, `telephone`
- **Match Type**: Exact match (case-insensitive)
- **Comparison**:
  - Normalize: Convert to lowercase, trim whitespace
  - Telephone: Strip spaces/dashes before comparison (despite @Pattern validation)
  - Compare: All three fields must match for duplicate

**Examples**:
- ✅ Duplicate: `{John, Smith, 1234567890}` vs `{john, smith, 1234567890}` (case difference)
- ✅ Duplicate: `{John, Smith, 1234567890}` vs `{  John, Smith  , 1234567890}` (whitespace)
- ❌ Not Duplicate: `{John, Smith, 1234567890}` vs `{John, Smith, 9876543210}` (different phone)
- ❌ Not Duplicate: `{John, Smith, 1234567890}` vs `{Jane, Smith, 1234567890}` (different first name)

### FR-2: Validation Timing
**Requirement**: Check for duplicates before saving new owner to database.

**Behavior**:
- Trigger: POST to `/owners/new` (create owner form submission)
- Timing: After @Valid bean validation passes, before `owners.save()`
- Flow:
  1. @Valid validates @NotBlank, @Pattern constraints
  2. Check for duplicates via repository query
  3. If duplicate found → add error to BindingResult → return form
  4. If no duplicate → proceed with save

### FR-3: Owner Update Behavior
**Requirement**: Allow updates without false duplicate detection (self-match).

**Behavior**:
- During update (`/owners/{ownerId}/edit`): Skip duplicate check
- Rationale: Owner may update fields that happen to match their own record
- Implementation: Duplicate check only in `processCreationForm()`, not `processUpdateOwnerForm()`

**Alternative** (if update detection needed later):
- Exclude current owner ID from duplicate search query
- Out of scope for initial implementation

### FR-4: Error Message
**Requirement**: Display clear, actionable error when duplicate detected.

**Message Key**: `owner.alreadyExists`

**Message Content** (English): "An owner with this information already exists"

**Error Location**: Top of form (form-level error, not field-level)

**Translation**: English only initially

**UI Behavior**:
- Error appears in red alert box above form
- Form retains user input (first name, last name, etc.)
- User can correct and resubmit or navigate away

### FR-5: Null/Empty Field Handling
**Requirement**: Handle edge cases defensively.

**Behavior**:
- All three fields (@NotBlank) should be present due to bean validation
- Add defensive null checks in duplicate detection logic
- If any field is null/empty → skip duplicate check (let bean validation handle)

### FR-6: Multiple Duplicates
**Requirement**: Handle scenario where multiple duplicates exist.

**Behavior**:
- Repository query returns all matching owners (List<Owner>)
- If list is not empty → duplicate exists
- Return all matches in error context (for future enhancement: show links)
- Initial implementation: Just detect presence of duplicates (count > 0)

---

## 4. Technical Design

### 4.1 Architecture

**Component**: Repository query method + Controller validation logic

**Flow**:
```
User Submits Form → OwnerController.processCreationForm()
                     ↓
                     @Valid validates bean constraints
                     ↓
                     If BindingResult.hasErrors() → return form
                     ↓
                     Check duplicates: ownerRepository.findByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndTelephone()
                     ↓
                     If duplicates found → rejectValue("owner.alreadyExists") → return form
                     ↓
                     If no duplicates → owners.save(owner) → redirect
```

### 4.2 Repository Method

**Location**: `src/main/java/org/springframework/samples/petclinic/owner/OwnerRepository.java`

**New Method**:
```java
/**
 * Find owners by first name, last name, and telephone (case-insensitive).
 * Used for duplicate detection during owner creation.
 *
 * @param firstName the owner's first name (case-insensitive)
 * @param lastName the owner's last name (case-insensitive)
 * @param telephone the owner's telephone number
 * @return list of owners matching all three fields (empty if none found)
 */
List<Owner> findByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndTelephone(
    String firstName,
    String lastName,
    String telephone
);
```

**Implementation**: Spring Data JPA generates query automatically

**Generated SQL** (approximate):
```sql
SELECT * FROM owners
WHERE LOWER(first_name) = LOWER(?)
  AND LOWER(last_name) = LOWER(?)
  AND telephone = ?
```

### 4.3 Controller Validation Logic

**Location**: `src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java`

**Modified Method**: `processCreationForm()`

**Code Changes**:
```java
@PostMapping("/owners/new")
public String processCreationForm(@Valid Owner owner, BindingResult result, RedirectAttributes redirectAttributes) {
    if (result.hasErrors()) {
        redirectAttributes.addFlashAttribute("error", "There was an error in creating the owner.");
        return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
    }

    // NEW: Check for duplicates
    String firstName = owner.getFirstName() != null ? owner.getFirstName().trim() : "";
    String lastName = owner.getLastName() != null ? owner.getLastName().trim() : "";
    String telephone = normalizeTelephone(owner.getTelephone());

    List<Owner> duplicates = this.owners.findByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndTelephone(
        firstName, lastName, telephone);

    if (!duplicates.isEmpty()) {
        result.reject("owner.alreadyExists", "An owner with this information already exists");
        return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
    }
    // END NEW

    this.owners.save(owner);
    redirectAttributes.addFlashAttribute("message", "New Owner Created");
    return "redirect:/owners/" + owner.getId();
}

// NEW: Helper method for telephone normalization
private String normalizeTelephone(String telephone) {
    if (telephone == null) return "";
    return telephone.replaceAll("[\\s-]", ""); // Strip spaces and dashes
}
```

**Changes**:
1. Extract and normalize firstName, lastName, telephone
2. Query repository for duplicates
3. If duplicates exist → add form-level error → return form
4. Otherwise → proceed with save

### 4.4 Database Index

**Requirement**: Add composite index for duplicate query performance

**Migration** (if using Flyway/Liquibase):
```sql
CREATE INDEX idx_owner_duplicate_check
ON owners (LOWER(first_name), LOWER(last_name), telephone);
```

**Schema File** (if using schema.sql):
Add to `src/main/resources/db/h2/schema.sql` (and mysql/postgres equivalents):
```sql
CREATE INDEX IF NOT EXISTS idx_owner_duplicate_check
ON owners (first_name, last_name, telephone);
```

**Note**: Case-insensitive index syntax varies by database. H2/PostgreSQL support functional indexes; MySQL may need different approach.

### 4.5 Internationalization (i18n)

**Files to Update**: `messages.properties` (English only initially)

**Addition**:
```properties
owner.alreadyExists=An owner with this information already exists
```

**Future**: Translate to all 9 languages when internationalization expands

---

## 5. TDD Approach (RED-GREEN-REFACTOR)

### Phase 1: Repository Tests

#### RED 1: Test Repository Query Method Exists
**Test**: `OwnerRepositoryTests.shouldFindOwnerByFirstLastAndTelephone()`
```java
@Test
void shouldFindOwnerByFirstLastAndTelephone() {
    // Assuming test data includes George Franklin with phone 6085551023
    List<Owner> owners = this.ownerRepository.findByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndTelephone(
        "George", "Franklin", "6085551023");

    assertThat(owners).hasSize(1);
    assertThat(owners.get(0).getFirstName()).isEqualTo("George");
}
```
**Expected**: FAIL (method doesn't exist)

#### GREEN 1: Add Repository Method
```java
List<Owner> findByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndTelephone(
    String firstName, String lastName, String telephone);
```
**Expected**: PASS (Spring Data generates implementation)

#### RED 2: Test Case-Insensitive Matching
**Test**: `OwnerRepositoryTests.shouldFindOwnerCaseInsensitive()`
```java
@Test
void shouldFindOwnerCaseInsensitive() {
    List<Owner> owners = this.ownerRepository.findByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndTelephone(
        "george", "franklin", "6085551023"); // lowercase

    assertThat(owners).hasSize(1);
    assertThat(owners.get(0).getFirstName()).isEqualToIgnoringCase("George");
}
```
**Expected**: PASS (IgnoreCase handles this)

#### RED 3: Test No Match Returns Empty
**Test**: `OwnerRepositoryTests.shouldReturnEmptyListWhenNoMatch()`
```java
@Test
void shouldReturnEmptyListWhenNoMatch() {
    List<Owner> owners = this.ownerRepository.findByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndTelephone(
        "NonExistent", "Owner", "0000000000");

    assertThat(owners).isEmpty();
}
```
**Expected**: PASS

### Phase 2: Controller Logic Tests

#### RED 4: Test Duplicate Blocked on Creation
**Test**: `OwnerControllerTests.shouldRejectDuplicateOwnerCreation()`
```java
@Test
void shouldRejectDuplicateOwnerCreation() throws Exception {
    Owner george = george(); // Existing owner
    given(this.owners.findByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndTelephone(
        eq("George"), eq("Franklin"), eq("6085551023")))
        .willReturn(List.of(george)); // Duplicate found

    mockMvc.perform(post("/owners/new")
        .param("firstName", "George")
        .param("lastName", "Franklin")
        .param("address", "110 W. Liberty St.")
        .param("city", "Madison")
        .param("telephone", "6085551023"))
        .andExpect(status().isOk()) // Returns form, not redirect
        .andExpect(model().attributeHasErrors("owner"))
        .andExpect(model().errorCount(1))
        .andExpect(view().name(VIEWS_OWNER_CREATE_OR_UPDATE_FORM));
}
```
**Expected**: FAIL (duplicate check not implemented)

#### GREEN 4: Implement Duplicate Check in Controller
Add duplicate checking logic to `processCreationForm()` as shown in Section 4.3.
**Expected**: PASS

#### RED 5: Test Case-Insensitive Duplicate Detection
**Test**: `OwnerControllerTests.shouldRejectDuplicateWithDifferentCase()`
```java
@Test
void shouldRejectDuplicateWithDifferentCase() throws Exception {
    Owner george = george();
    given(this.owners.findByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndTelephone(
        eq("george"), eq("franklin"), eq("6085551023")))
        .willReturn(List.of(george));

    mockMvc.perform(post("/owners/new")
        .param("firstName", "george") // lowercase
        .param("lastName", "franklin") // lowercase
        .param("address", "110 W. Liberty St.")
        .param("city", "Madison")
        .param("telephone", "6085551023"))
        .andExpect(status().isOk())
        .andExpect(model().attributeHasErrors("owner"));
}
```
**Expected**: PASS (case-insensitive query handles this)

#### RED 6: Test Non-Duplicate Allowed
**Test**: `OwnerControllerTests.shouldAllowNonDuplicateOwnerCreation()`
```java
@Test
void shouldAllowNonDuplicateOwnerCreation() throws Exception {
    given(this.owners.findByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndTelephone(
        any(), any(), any()))
        .willReturn(Collections.emptyList()); // No duplicates

    mockMvc.perform(post("/owners/new")
        .param("firstName", "Jane")
        .param("lastName", "Doe")
        .param("address", "456 Elm St.")
        .param("city", "Springfield")
        .param("telephone", "5551234567"))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name(startsWith("redirect:/owners/")));
}
```
**Expected**: PASS

#### RED 7: Test Different Phone Allowed
**Test**: `OwnerControllerTests.shouldAllowOwnerWithSameNameDifferentPhone()`
```java
@Test
void shouldAllowOwnerWithSameNameDifferentPhone() throws Exception {
    given(this.owners.findByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndTelephone(
        eq("George"), eq("Franklin"), eq("9999999999")))
        .willReturn(Collections.emptyList()); // No duplicates (different phone)

    mockMvc.perform(post("/owners/new")
        .param("firstName", "George")
        .param("lastName", "Franklin")
        .param("address", "Different Address")
        .param("city", "Different City")
        .param("telephone", "9999999999")) // Different phone
        .andExpect(status().is3xxRedirection());
}
```
**Expected**: PASS

#### RED 8: Test Telephone Normalization
**Test**: `OwnerControllerTests.shouldNormalizeTelephoneBeforeCheck()`
```java
@Test
void shouldNormalizeTelephoneBeforeCheck() throws Exception {
    // Assuming normalization strips spaces/dashes
    Owner george = george();
    given(this.owners.findByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndTelephone(
        eq("George"), eq("Franklin"), eq("6085551023"))) // normalized
        .willReturn(List.of(george));

    mockMvc.perform(post("/owners/new")
        .param("firstName", "George")
        .param("lastName", "Franklin")
        .param("address", "110 W. Liberty St.")
        .param("city", "Madison")
        .param("telephone", "608 555 1023")) // With spaces (if validation allows)
        .andExpect(status().isOk())
        .andExpect(model().attributeHasErrors("owner"));
}
```
**Expected**: FAIL (normalization not implemented) or PASS (if @Pattern already enforces 10 digits only)

**Note**: Current @Pattern validation requires exactly 10 digits (`\\d{10}`), so spaces/dashes shouldn't reach controller. Normalization is defensive.

#### GREEN 8: Add Normalization Helper
```java
private String normalizeTelephone(String telephone) {
    if (telephone == null) return "";
    return telephone.replaceAll("[\\s-]", "");
}
```
**Expected**: PASS

#### REFACTOR 1: Extract Duplicate Check Method
```java
private boolean isDuplicate(Owner owner) {
    String firstName = owner.getFirstName() != null ? owner.getFirstName().trim() : "";
    String lastName = owner.getLastName() != null ? owner.getLastName().trim() : "";
    String telephone = normalizeTelephone(owner.getTelephone());

    List<Owner> duplicates = this.owners.findByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndTelephone(
        firstName, lastName, telephone);

    return !duplicates.isEmpty();
}
```

### Phase 3: End-to-End Tests (Playwright)

#### RED 9: Test Duplicate Detection E2E
**Test**: `e2e-tests/tests/owner-duplicate-prevention.spec.ts`
```typescript
test('should prevent creating duplicate owner', async ({ page }) => {
    // Create first owner
    await page.goto('/owners/new');
    await page.fill('input[name="firstName"]', 'John');
    await page.fill('input[name="lastName"]', 'Doe');
    await page.fill('input[name="address"]', '123 Main St');
    await page.fill('input[name="city"]', 'Springfield');
    await page.fill('input[name="telephone"]', '5551234567');
    await page.click('button[type="submit"]');

    await expect(page).toHaveURL(/\/owners\/\d+/);

    // Attempt to create duplicate
    await page.goto('/owners/new');
    await page.fill('input[name="firstName"]', 'John');
    await page.fill('input[name="lastName"]', 'Doe');
    await page.fill('input[name="address"]', '456 Elm St'); // Different address
    await page.fill('input[name="city"]', 'Springfield');
    await page.fill('input[name="telephone"]', '5551234567'); // Same phone
    await page.click('button[type="submit"]');

    // Should stay on form with error
    await expect(page).toHaveURL(/\/owners\/new/);
    await expect(page.locator('.alert-danger, .error')).toContainText('An owner with this information already exists');
});
```
**Expected**: FAIL (feature not integrated)

#### GREEN 9: Complete Integration
All previous steps complete the integration.
**Expected**: PASS

#### RED 10: Test Non-Duplicate Allowed E2E
**Test**: `e2e-tests/tests/owner-duplicate-prevention.spec.ts`
```typescript
test('should allow creating owner with similar but different info', async ({ page }) => {
    // Create first owner
    await page.goto('/owners/new');
    await page.fill('input[name="firstName"]', 'John');
    await page.fill('input[name="lastName"]', 'Doe');
    await page.fill('input[name="address"]', '123 Main St');
    await page.fill('input[name="city"]', 'Springfield');
    await page.fill('input[name="telephone"]', '5551234567');
    await page.click('button[type="submit"]');

    // Create similar owner with different phone
    await page.goto('/owners/new');
    await page.fill('input[name="firstName"]', 'John');
    await page.fill('input[name="lastName"]', 'Doe');
    await page.fill('input[name="address"]', '123 Main St');
    await page.fill('input[name="city"]', 'Springfield');
    await page.fill('input[name="telephone"]', '9999999999'); // Different phone
    await page.click('button[type="submit"]');

    // Should succeed
    await expect(page).toHaveURL(/\/owners\/\d+/);
    await expect(page.locator('.alert-success, .message')).toContainText('New Owner Created');
});
```
**Expected**: PASS

---

## 6. Test Scenarios

### 6.1 Repository Tests (OwnerRepositoryTests.java)

| Test Case | Description | Expected Result |
|-----------|-------------|-----------------|
| `shouldFindOwnerByFirstLastAndTelephone()` | Query existing owner | Owner found |
| `shouldFindOwnerCaseInsensitive()` | Query with lowercase name | Owner found |
| `shouldReturnEmptyListWhenNoMatch()` | Query non-existent owner | Empty list |
| `shouldFindMultipleDuplicates()` | Multiple owners match | List with all matches |

### 6.2 Controller Integration Tests (OwnerControllerTests.java)

| Test Case | Description | Expected Result |
|-----------|-------------|-----------------|
| `shouldRejectDuplicateOwnerCreation()` | Exact duplicate (same case) | Form returned with error |
| `shouldRejectDuplicateWithDifferentCase()` | Case-insensitive duplicate | Form returned with error |
| `shouldAllowNonDuplicateOwnerCreation()` | Unique owner | Redirect to owner page |
| `shouldAllowOwnerWithSameNameDifferentPhone()` | Same name, different phone | Redirect to owner page |
| `shouldAllowOwnerWithDifferentNameSamePhone()` | Different name, same phone | Redirect to owner page |
| `shouldDisplayErrorMessageOnForm()` | Duplicate detected | Error visible in model |
| `shouldRetainFormDataAfterDuplicateError()` | Duplicate error | Form fields populated |

### 6.3 End-to-End Tests (Playwright)

| Test Case | Description | Expected Result |
|-----------|-------------|-----------------|
| `shouldPreventCreatingDuplicateOwner()` | Create duplicate via UI | Error message shown |
| `shouldAllowCreatingNonDuplicate()` | Create similar but unique owner | Success message |
| `shouldHandleCaseVariations()` | Create with different case | Duplicate detected |

---

## 7. Implementation Plan

### Step 1: Add Repository Query Method
**Files**: `OwnerRepository.java`
- Add `findByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndTelephone()` method

**Tests**: `OwnerRepositoryTests.java` (add tests)

### Step 2: Implement Controller Duplicate Check
**Files**: `OwnerController.java`
- Modify `processCreationForm()` to check for duplicates
- Add `normalizeTelephone()` helper method
- Add `isDuplicate()` helper method (optional refactor)

**Tests**: `OwnerControllerTests.java` (add tests)

### Step 3: Add Internationalization Message
**Files**: `messages.properties`
- Add `owner.alreadyExists` key

**Tests**: Manual verification or i18n sync test

### Step 4: Add Database Index
**Files**: `schema.sql` (H2, MySQL, PostgreSQL variants)
- Add composite index on (firstName, lastName, telephone)

**Tests**: Performance testing (optional)

### Step 5: Add End-to-End Tests
**Files**: `e2e-tests/tests/owner-duplicate-prevention.spec.ts` (new)
- Write Playwright tests for duplicate scenarios

**Tests**: Run via `npm test`

---

## 8. Dependencies

### Internal
- **OwnerRepository**: Requires new query method
- **Owner Entity**: No changes needed
- **OwnerController**: Requires validation logic addition
- **Message Properties**: Requires new message key

### External
- None (uses standard Spring Data JPA)

### Database
- **Index**: Composite index for query performance
- **Schema Migration**: Update schema files for all DB variants (H2, MySQL, PostgreSQL)

---

## 9. Non-Functional Requirements

### Performance
- **Target**: Duplicate check completes in < 50ms
- **Index**: Composite index on (firstName, lastName, telephone) to optimize query
- **Query Complexity**: Simple SELECT with 3-field match (O(log n) with index)

### Security
- **SQL Injection**: Prevented by parameterized queries (Spring Data JPA)
- **Data Exposure**: No sensitive data exposed in error message

### Usability
- **Error Clarity**: Message clearly states the issue
- **Form Retention**: User input preserved after error (Spring MVC default)
- **Actionable Feedback**: User knows to search for existing owner or use different info

### Data Integrity
- **Race Condition**: Low risk (duplicate check + save not atomic)
- **Mitigation** (future): Add unique constraint at database level
- **Scope**: Initial implementation relies on application-level check

---

## 10. Risk Assessment

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| Race condition (concurrent creates) | Low | Medium | Add DB unique constraint in Phase 2 |
| False positives (legitimate twins) | Very Low | Low | Document as known limitation |
| Telephone normalization inconsistency | Low | Medium | Enforce 10-digit pattern via @Pattern |
| Performance degradation | Low | Low | Add composite index |
| Case-insensitive index compatibility | Medium | Low | Test on all DB variants (H2, MySQL, PostgreSQL) |

---

## 11. Future Enhancements (Out of Scope)

- **Database Unique Constraint**: Add unique index at DB level for atomic enforcement
- **Fuzzy Matching**: Detect similar names (e.g., "John" vs "Jon", "Smith" vs "Smyth")
- **Phonetic Matching**: Use Soundex or Metaphone for name variations
- **Duplicate Merge Tool**: Admin feature to merge duplicate records
- **Search Before Create**: Show potential matches before allowing creation
- **Duplicate Detection on Update**: Prevent updates that create duplicates
- **Duplicate Detection API**: Expose duplicate check as reusable service
- **Audit Log**: Track duplicate attempts for analysis

---

## 12. Acceptance Testing Checklist

- [ ] Repository query method created and returns correct results
- [ ] Case-insensitive matching works correctly
- [ ] Controller rejects duplicate owner creation
- [ ] Controller allows non-duplicate creation
- [ ] Error message displays at top of form
- [ ] Form retains user input after duplicate error
- [ ] Message key added to messages.properties
- [ ] Database index added to all schema files (H2, MySQL, PostgreSQL)
- [ ] Unit tests for repository achieve 100% coverage
- [ ] Controller integration tests achieve 90%+ coverage
- [ ] Playwright E2E test verifies duplicate prevention
- [ ] Playwright E2E test verifies non-duplicate creation
- [ ] Manual testing confirms UX behavior

---

## 13. Definition of Done

- [ ] All TDD cycles completed (RED-GREEN-REFACTOR)
- [ ] Repository query method implemented and tested
- [ ] Controller duplicate check logic implemented
- [ ] Unit tests written and passing (>90% coverage)
- [ ] Integration tests written and passing
- [ ] Playwright E2E tests written and passing
- [ ] Internationalization message added
- [ ] Database index added to schema files
- [ ] Code review completed
- [ ] No Checkstyle/SpotBugs violations
- [ ] Feature tested manually in dev environment
- [ ] Merged to main branch via PR

---

**End of Specification**
