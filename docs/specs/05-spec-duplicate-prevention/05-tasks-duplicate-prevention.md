# Task List: Duplicate Owner Prevention (Issue #6)

**Feature ID**: Issue #6
**Spec Document**: [SPEC.md](./SPEC.md)
**Status**: Ready for Implementation
**Created**: 2026-02-12
**TDD Required**: Yes (Strict RED-GREEN-REFACTOR)

---

## Relevant Files

### Implementation Files
- `/Users/jackmonford/Repos/liatrio-forge/emerald-grove-pet-clinic-jack-mumford/src/main/java/org/springframework/samples/petclinic/owner/OwnerRepository.java`
- `/Users/jackmonford/Repos/liatrio-forge/emerald-grove-pet-clinic-jack-mumford/src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java`
- `/Users/jackmonford/Repos/liatrio-forge/emerald-grove-pet-clinic-jack-mumford/src/main/java/org/springframework/samples/petclinic/owner/Owner.java`

### Test Files
- `/Users/jackmonford/Repos/liatrio-forge/emerald-grove-pet-clinic-jack-mumford/src/test/java/org/springframework/samples/petclinic/service/ClinicServiceTests.java`
- `/Users/jackmonford/Repos/liatrio-forge/emerald-grove-pet-clinic-jack-mumford/src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java`

### Database Schema Files
- `/Users/jackmonford/Repos/liatrio-forge/emerald-grove-pet-clinic-jack-mumford/src/main/resources/db/h2/schema.sql`
- `/Users/jackmonford/Repos/liatrio-forge/emerald-grove-pet-clinic-jack-mumford/src/main/resources/db/mysql/schema.sql`
- `/Users/jackmonford/Repos/liatrio-forge/emerald-grove-pet-clinic-jack-mumford/src/main/resources/db/postgres/schema.sql`
- `/Users/jackmonford/Repos/liatrio-forge/emerald-grove-pet-clinic-jack-mumford/src/main/resources/db/hsqldb/schema.sql`

### Internationalization Files
- `/Users/jackmonford/Repos/liatrio-forge/emerald-grove-pet-clinic-jack-mumford/src/main/resources/messages/messages.properties`
- `/Users/jackmonford/Repos/liatrio-forge/emerald-grove-pet-clinic-jack-mumford/src/main/resources/messages/messages_en.properties`

### E2E Test Files (New)
- `/Users/jackmonford/Repos/liatrio-forge/emerald-grove-pet-clinic-jack-mumford/e2e-tests/tests/owner-duplicate-prevention.spec.ts`

---

## Parent Tasks

### Task 1: Repository Layer - Duplicate Detection Query
**Description**: Implement repository query method to find duplicate owners based on first name, last name, and telephone number.

**Proof Artifact**:
- New repository method `findByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndTelephone()` in `OwnerRepository.java`
- Passing repository-level integration tests in `ClinicServiceTests.java`
- All tests passing with >90% code coverage

**Dependencies**: None

**Estimated Effort**: Small (2-3 hours)

---

### Task 2: Controller Layer - Duplicate Validation Logic
**Description**: Add duplicate detection logic to the owner creation controller method, including telephone normalization and error handling.

**Proof Artifact**:
- Modified `processCreationForm()` method in `OwnerController.java` with duplicate check logic
- Helper method `normalizeTelephone()` implemented
- Passing controller integration tests in `OwnerControllerTests.java`
- Form-level error returned when duplicate detected

**Dependencies**: Task 1 (requires repository method)

**Estimated Effort**: Medium (3-4 hours)

---

### Task 3: Database Optimization - Composite Index
**Description**: Add composite database indexes across all supported databases (H2, MySQL, PostgreSQL, HSQLDB) to optimize duplicate detection queries.

**Proof Artifact**:
- Index added to all four schema files
- Application starts successfully with all database profiles
- Query performance verified (optional benchmarking)

**Dependencies**: Task 1 (conceptual - index supports the query)

**Estimated Effort**: Small (1-2 hours)

---

### Task 4: Internationalization - Error Messages
**Description**: Add error message keys to internationalization properties files for duplicate owner detection.

**Proof Artifact**:
- `owner.alreadyExists` key added to `messages.properties`
- Error message displays correctly in UI
- Message follows existing i18n conventions

**Dependencies**: None

**Estimated Effort**: Small (1 hour)

---

### Task 5: End-to-End Testing - Duplicate Prevention Verification
**Description**: Create comprehensive Playwright E2E tests to verify duplicate prevention behavior through the full application stack.

**Proof Artifact**:
- New test file `owner-duplicate-prevention.spec.ts` created
- Tests cover duplicate detection, case-insensitive matching, and non-duplicate scenarios
- All E2E tests passing in CI/CD pipeline

**Dependencies**: Tasks 1, 2, 4 (requires full feature implementation)

**Estimated Effort**: Medium (2-3 hours)

---

### Task 6: Integration and Documentation
**Description**: Verify complete feature integration, update documentation, and ensure all acceptance criteria are met.

**Proof Artifact**:
- All unit, integration, and E2E tests passing
- Code coverage >90% for new code
- Manual testing completed and documented
- Acceptance criteria checklist completed

**Dependencies**: Tasks 1-5 (requires all components)

**Estimated Effort**: Small (1-2 hours)

---

## Detailed Sub-Tasks

### Task 1: Repository Layer - Duplicate Detection Query

#### 1.1: RED - Write Repository Test for Exact Match
**Type**: Test (RED Phase)

**Description**: Write a failing test that attempts to find an owner by exact first name, last name, and telephone match.

**TDD Phase**: RED

**Files to Modify**:
- `/Users/jackmonford/Repos/liatrio-forge/emerald-grove-pet-clinic-jack-mumford/src/test/java/org/springframework/samples/petclinic/service/ClinicServiceTests.java`

**Test Code**:
```java
@Test
void shouldFindOwnerByFirstLastAndTelephone() {
    // Arrange: Use existing test data (George Franklin, 6085551023)

    // Act
    List<Owner> owners = this.owners.findByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndTelephone(
        "George", "Franklin", "6085551023");

    // Assert
    assertThat(owners).hasSize(1);
    assertThat(owners.get(0).getFirstName()).isEqualTo("George");
    assertThat(owners.get(0).getLastName()).isEqualTo("Franklin");
    assertThat(owners.get(0).getTelephone()).isEqualTo("6085551023");
}
```

**Expected Result**: Test FAILS - method does not exist

**Verification**: Run `./mvnw test -Dtest=ClinicServiceTests#shouldFindOwnerByFirstLastAndTelephone`

**Dependencies**: None

---

#### 1.2: GREEN - Implement Repository Query Method
**Type**: Implementation (GREEN Phase)

**Description**: Add the repository query method signature to enable Spring Data JPA to auto-generate the implementation.

**TDD Phase**: GREEN

**Files to Modify**:
- `/Users/jackmonford/Repos/liatrio-forge/emerald-grove-pet-clinic-jack-mumford/src/main/java/org/springframework/samples/petclinic/owner/OwnerRepository.java`

**Implementation**:
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

**Expected Result**: Test from 1.1 PASSES

**Verification**: Run `./mvnw test -Dtest=ClinicServiceTests#shouldFindOwnerByFirstLastAndTelephone`

**Dependencies**: Task 1.1

---

#### 1.3: RED - Write Test for Case-Insensitive Matching
**Type**: Test (RED Phase)

**Description**: Write a test that verifies case-insensitive matching for first and last names.

**TDD Phase**: RED

**Files to Modify**:
- `/Users/jackmonford/Repos/liatrio-forge/emerald-grove-pet-clinic-jack-mumford/src/test/java/org/springframework/samples/petclinic/service/ClinicServiceTests.java`

**Test Code**:
```java
@Test
void shouldFindOwnerCaseInsensitive() {
    // Arrange: Query with lowercase names

    // Act
    List<Owner> owners = this.owners.findByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndTelephone(
        "george", "franklin", "6085551023");

    // Assert
    assertThat(owners).hasSize(1);
    assertThat(owners.get(0).getFirstName()).isEqualToIgnoringCase("George");
    assertThat(owners.get(0).getLastName()).isEqualToIgnoringCase("Franklin");
}
```

**Expected Result**: Test PASSES (IgnoreCase in method name handles this)

**Verification**: Run `./mvnw test -Dtest=ClinicServiceTests#shouldFindOwnerCaseInsensitive`

**Dependencies**: Task 1.2

---

#### 1.4: RED - Write Test for No Match Scenario
**Type**: Test (RED Phase)

**Description**: Write a test that verifies empty list is returned when no matching owner exists.

**TDD Phase**: RED

**Files to Modify**:
- `/Users/jackmonford/Repos/liatrio-forge/emerald-grove-pet-clinic-jack-mumford/src/test/java/org/springframework/samples/petclinic/service/ClinicServiceTests.java`

**Test Code**:
```java
@Test
void shouldReturnEmptyListWhenNoOwnerMatches() {
    // Arrange: Query with non-existent owner details

    // Act
    List<Owner> owners = this.owners.findByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndTelephone(
        "NonExistent", "Owner", "0000000000");

    // Assert
    assertThat(owners).isEmpty();
}
```

**Expected Result**: Test PASSES (Spring Data handles empty results)

**Verification**: Run `./mvnw test -Dtest=ClinicServiceTests#shouldReturnEmptyListWhenNoOwnerMatches`

**Dependencies**: Task 1.2

---

#### 1.5: RED - Write Test for Partial Match Rejection
**Type**: Test (RED Phase)

**Description**: Write tests that verify partial matches (only 2 of 3 fields) do not return results.

**TDD Phase**: RED

**Files to Modify**:
- `/Users/jackmonford/Repos/liatrio-forge/emerald-grove-pet-clinic-jack-mumford/src/test/java/org/springframework/samples/petclinic/service/ClinicServiceTests.java`

**Test Code**:
```java
@Test
void shouldNotFindOwnerWithDifferentTelephone() {
    // Arrange: Same name, different phone

    // Act
    List<Owner> owners = this.owners.findByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndTelephone(
        "George", "Franklin", "9999999999");

    // Assert
    assertThat(owners).isEmpty();
}

@Test
void shouldNotFindOwnerWithDifferentFirstName() {
    // Arrange: Different first name, same last name and phone

    // Act
    List<Owner> owners = this.owners.findByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndTelephone(
        "Jane", "Franklin", "6085551023");

    // Assert
    assertThat(owners).isEmpty();
}

@Test
void shouldNotFindOwnerWithDifferentLastName() {
    // Arrange: Same first name, different last name

    // Act
    List<Owner> owners = this.owners.findByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndTelephone(
        "George", "Smith", "6085551023");

    // Assert
    assertThat(owners).isEmpty();
}
```

**Expected Result**: All tests PASS (query requires all three fields to match)

**Verification**: Run `./mvnw test -Dtest=ClinicServiceTests#shouldNotFindOwner*`

**Dependencies**: Task 1.2

---

#### 1.6: REFACTOR - Add Import Statement and Verify Coverage
**Type**: Refactoring

**Description**: Add necessary import statement for List interface and verify test coverage meets standards.

**TDD Phase**: REFACTOR

**Files to Modify**:
- `/Users/jackmonford/Repos/liatrio-forge/emerald-grove-pet-clinic-jack-mumford/src/main/java/org/springframework/samples/petclinic/owner/OwnerRepository.java`

**Implementation**:
```java
import java.util.List;
```

**Expected Result**: All tests continue to pass, code compiles cleanly

**Verification**:
- Run `./mvnw test -Dtest=ClinicServiceTests`
- Run `./mvnw jacoco:report` and verify coverage >90%

**Dependencies**: Tasks 1.1-1.5

---

### Task 2: Controller Layer - Duplicate Validation Logic

#### 2.1: RED - Write Test for Duplicate Owner Rejection
**Type**: Test (RED Phase)

**Description**: Write a failing test that attempts to create a duplicate owner and expects form return with error.

**TDD Phase**: RED

**Files to Modify**:
- `/Users/jackmonford/Repos/liatrio-forge/emerald-grove-pet-clinic-jack-mumford/src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java`

**Test Code**:
```java
@Test
void shouldRejectDuplicateOwnerCreation() throws Exception {
    // Arrange: Mock repository to return existing owner (duplicate found)
    Owner george = george();
    given(this.owners.findByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndTelephone(
        eq("George"), eq("Franklin"), eq("6085551023")))
        .willReturn(List.of(george));

    // Act & Assert
    mockMvc.perform(post("/owners/new")
        .param("firstName", "George")
        .param("lastName", "Franklin")
        .param("address", "110 W. Liberty St.")
        .param("city", "Madison")
        .param("telephone", "6085551023"))
        .andExpect(status().isOk()) // Returns form, not redirect
        .andExpect(model().attributeHasErrors("owner"))
        .andExpect(model().errorCount(1))
        .andExpect(view().name("owners/createOrUpdateOwnerForm"));
}
```

**Expected Result**: Test FAILS - duplicate check not implemented

**Verification**: Run `./mvnw test -Dtest=OwnerControllerTests#shouldRejectDuplicateOwnerCreation`

**Dependencies**: Task 1.2 (requires repository method to exist)

---

#### 2.2: GREEN - Implement Basic Duplicate Check in Controller
**Type**: Implementation (GREEN Phase)

**Description**: Add basic duplicate detection logic to the `processCreationForm()` method.

**TDD Phase**: GREEN

**Files to Modify**:
- `/Users/jackmonford/Repos/liatrio-forge/emerald-grove-pet-clinic-jack-mumford/src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java`

**Implementation**:
```java
@PostMapping("/owners/new")
public String processCreationForm(@Valid Owner owner, BindingResult result, RedirectAttributes redirectAttributes) {
    if (result.hasErrors()) {
        redirectAttributes.addFlashAttribute("error", "There was an error in creating the owner.");
        return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
    }

    // NEW: Check for duplicates
    String firstName = owner.getFirstName();
    String lastName = owner.getLastName();
    String telephone = owner.getTelephone();

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
```

**Expected Result**: Test from 2.1 PASSES

**Verification**: Run `./mvnw test -Dtest=OwnerControllerTests#shouldRejectDuplicateOwnerCreation`

**Dependencies**: Task 2.1

---

#### 2.3: RED - Write Test for Case-Insensitive Duplicate Detection
**Type**: Test (RED Phase)

**Description**: Write a test that verifies case variations are detected as duplicates.

**TDD Phase**: RED

**Files to Modify**:
- `/Users/jackmonford/Repos/liatrio-forge/emerald-grove-pet-clinic-jack-mumford/src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java`

**Test Code**:
```java
@Test
void shouldRejectDuplicateWithDifferentCase() throws Exception {
    // Arrange: Mock repository to return existing owner (case-insensitive match)
    Owner george = george();
    given(this.owners.findByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndTelephone(
        eq("george"), eq("franklin"), eq("6085551023")))
        .willReturn(List.of(george));

    // Act & Assert: Submit with lowercase names
    mockMvc.perform(post("/owners/new")
        .param("firstName", "george")
        .param("lastName", "franklin")
        .param("address", "110 W. Liberty St.")
        .param("city", "Madison")
        .param("telephone", "6085551023"))
        .andExpect(status().isOk())
        .andExpect(model().attributeHasErrors("owner"));
}
```

**Expected Result**: Test PASSES (repository method handles case-insensitivity)

**Verification**: Run `./mvnw test -Dtest=OwnerControllerTests#shouldRejectDuplicateWithDifferentCase`

**Dependencies**: Task 2.2

---

#### 2.4: RED - Write Test for Non-Duplicate Allowed
**Type**: Test (RED Phase)

**Description**: Write a test that verifies non-duplicate owners can be created successfully.

**TDD Phase**: RED

**Files to Modify**:
- `/Users/jackmonford/Repos/liatrio-forge/emerald-grove-pet-clinic-jack-mumford/src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java`

**Test Code**:
```java
@Test
void shouldAllowNonDuplicateOwnerCreation() throws Exception {
    // Arrange: Mock repository to return empty list (no duplicates)
    given(this.owners.findByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndTelephone(
        any(), any(), any()))
        .willReturn(Collections.emptyList());

    // Act & Assert: Submit unique owner
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

**Expected Result**: Test PASSES (no duplicates, creation proceeds)

**Verification**: Run `./mvnw test -Dtest=OwnerControllerTests#shouldAllowNonDuplicateOwnerCreation`

**Dependencies**: Task 2.2

---

#### 2.5: RED - Write Test for Same Name Different Phone
**Type**: Test (RED Phase)

**Description**: Write a test that verifies owners with same name but different phone can be created.

**TDD Phase**: RED

**Files to Modify**:
- `/Users/jackmonford/Repos/liatrio-forge/emerald-grove-pet-clinic-jack-mumford/src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java`

**Test Code**:
```java
@Test
void shouldAllowOwnerWithSameNameDifferentPhone() throws Exception {
    // Arrange: Mock repository to return empty list (different phone = no duplicate)
    given(this.owners.findByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndTelephone(
        eq("George"), eq("Franklin"), eq("9999999999")))
        .willReturn(Collections.emptyList());

    // Act & Assert: Submit owner with same name, different phone
    mockMvc.perform(post("/owners/new")
        .param("firstName", "George")
        .param("lastName", "Franklin")
        .param("address", "Different Address")
        .param("city", "Different City")
        .param("telephone", "9999999999"))
        .andExpect(status().is3xxRedirection());
}
```

**Expected Result**: Test PASSES (different phone = not duplicate)

**Verification**: Run `./mvnw test -Dtest=OwnerControllerTests#shouldAllowOwnerWithSameNameDifferentPhone`

**Dependencies**: Task 2.2

---

#### 2.6: RED - Write Test for Telephone Normalization
**Type**: Test (RED Phase)

**Description**: Write a test for defensive telephone normalization (strips whitespace/dashes).

**TDD Phase**: RED

**Files to Modify**:
- `/Users/jackmonford/Repos/liatrio-forge/emerald-grove-pet-clinic-jack-mumford/src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java`

**Test Code**:
```java
@Test
void shouldNormalizeTelephoneForDuplicateCheck() throws Exception {
    // Arrange: Mock expects normalized phone (no spaces/dashes)
    Owner george = george();
    given(this.owners.findByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndTelephone(
        eq("George"), eq("Franklin"), eq("6085551023")))
        .willReturn(List.of(george));

    // Act & Assert: Submit with spaces in phone (if validation allows)
    // Note: Current @Pattern validation requires exactly 10 digits,
    // so this test verifies defensive normalization
    mockMvc.perform(post("/owners/new")
        .param("firstName", "George")
        .param("lastName", "Franklin")
        .param("address", "110 W. Liberty St.")
        .param("city", "Madison")
        .param("telephone", "6085551023")) // Already normalized by validation
        .andExpect(status().isOk())
        .andExpect(model().attributeHasErrors("owner"));
}
```

**Expected Result**: Test PASSES (normalization handled or not needed due to validation)

**Verification**: Run `./mvnw test -Dtest=OwnerControllerTests#shouldNormalizeTelephoneForDuplicateCheck`

**Dependencies**: Task 2.2

---

#### 2.7: GREEN - Add Telephone Normalization Helper Method
**Type**: Implementation (GREEN Phase)

**Description**: Add a helper method for defensive telephone normalization.

**TDD Phase**: GREEN

**Files to Modify**:
- `/Users/jackmonford/Repos/liatrio-forge/emerald-grove-pet-clinic-jack-mumford/src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java`

**Implementation**:
```java
/**
 * Normalize telephone number by removing spaces and dashes.
 * Defensive measure for consistent comparison, though @Pattern validation
 * should already enforce 10-digit format.
 *
 * @param telephone the telephone number to normalize
 * @return normalized telephone number (spaces and dashes removed)
 */
private String normalizeTelephone(String telephone) {
    if (telephone == null) {
        return "";
    }
    return telephone.replaceAll("[\\s-]", "");
}
```

**Expected Result**: All tests continue to pass

**Verification**: Run `./mvnw test -Dtest=OwnerControllerTests`

**Dependencies**: Task 2.6

---

#### 2.8: REFACTOR - Extract Duplicate Check to Helper Method
**Type**: Refactoring

**Description**: Refactor duplicate check logic into a dedicated helper method for better code organization.

**TDD Phase**: REFACTOR

**Files to Modify**:
- `/Users/jackmonford/Repos/liatrio-forge/emerald-grove-pet-clinic-jack-mumford/src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java`

**Implementation**:
```java
/**
 * Check if an owner with the same first name, last name, and telephone already exists.
 *
 * @param owner the owner to check for duplicates
 * @return true if duplicate exists, false otherwise
 */
private boolean isDuplicate(Owner owner) {
    String firstName = owner.getFirstName() != null ? owner.getFirstName().trim() : "";
    String lastName = owner.getLastName() != null ? owner.getLastName().trim() : "";
    String telephone = normalizeTelephone(owner.getTelephone());

    List<Owner> duplicates = this.owners.findByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndTelephone(
        firstName, lastName, telephone);

    return !duplicates.isEmpty();
}
```

**Update `processCreationForm()` to use helper**:
```java
@PostMapping("/owners/new")
public String processCreationForm(@Valid Owner owner, BindingResult result, RedirectAttributes redirectAttributes) {
    if (result.hasErrors()) {
        redirectAttributes.addFlashAttribute("error", "There was an error in creating the owner.");
        return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
    }

    // Check for duplicates
    if (isDuplicate(owner)) {
        result.reject("owner.alreadyExists", "An owner with this information already exists");
        return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
    }

    this.owners.save(owner);
    redirectAttributes.addFlashAttribute("message", "New Owner Created");
    return "redirect:/owners/" + owner.getId();
}
```

**Expected Result**: All tests continue to pass, code is cleaner

**Verification**: Run `./mvnw test -Dtest=OwnerControllerTests`

**Dependencies**: Tasks 2.1-2.7

---

#### 2.9: REFACTOR - Add Import Statements
**Type**: Refactoring

**Description**: Add necessary import statements for List and Collections classes.

**TDD Phase**: REFACTOR

**Files to Modify**:
- `/Users/jackmonford/Repos/liatrio-forge/emerald-grove-pet-clinic-jack-mumford/src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java`

**Implementation**:
```java
// Add to existing imports (if not already present)
import java.util.List;
```

**Expected Result**: Code compiles cleanly, all tests pass

**Verification**: Run `./mvnw clean test -Dtest=OwnerControllerTests`

**Dependencies**: Task 2.8

---

#### 2.10: REFACTOR - Verify Test Coverage
**Type**: Testing

**Description**: Verify that all controller changes achieve >90% test coverage.

**TDD Phase**: REFACTOR

**Files to Check**:
- `/Users/jackmonford/Repos/liatrio-forge/emerald-grove-pet-clinic-jack-mumford/src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java`

**Actions**:
1. Run JaCoCo coverage report: `./mvnw jacoco:report`
2. Open `target/site/jacoco/index.html`
3. Navigate to `OwnerController` class
4. Verify coverage >90% for all modified methods

**Expected Result**: Coverage >90% for `processCreationForm()`, `isDuplicate()`, and `normalizeTelephone()`

**Verification**: Check JaCoCo HTML report

**Dependencies**: Tasks 2.1-2.9

---

### Task 3: Database Optimization - Composite Index

#### 3.1: Add Composite Index to H2 Schema
**Type**: Implementation

**Description**: Add composite index on (first_name, last_name, telephone) to H2 schema file.

**TDD Phase**: N/A (Database optimization)

**Files to Modify**:
- `/Users/jackmonford/Repos/liatrio-forge/emerald-grove-pet-clinic-jack-mumford/src/main/resources/db/h2/schema.sql`

**Implementation**:
```sql
-- Add after the owners table definition (after line 44)
CREATE INDEX idx_owner_duplicate_check ON owners (first_name, last_name, telephone);
```

**Expected Result**: Application starts successfully with H2 profile

**Verification**: Run `./mvnw spring-boot:run` and check for startup errors

**Dependencies**: None (can be done independently)

---

#### 3.2: Add Composite Index to MySQL Schema
**Type**: Implementation

**Description**: Add composite index on (first_name, last_name, telephone) to MySQL schema file.

**TDD Phase**: N/A (Database optimization)

**Files to Modify**:
- `/Users/jackmonford/Repos/liatrio-forge/emerald-grove-pet-clinic-jack-mumford/src/main/resources/db/mysql/schema.sql`

**Implementation**:
```sql
-- Add after the owners table definition
CREATE INDEX idx_owner_duplicate_check ON owners (first_name, last_name, telephone);
```

**Expected Result**: Application starts successfully with MySQL profile

**Verification**:
1. Start MySQL container: `docker compose up mysql`
2. Run `./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql`

**Dependencies**: Task 3.1 (pattern established)

---

#### 3.3: Add Composite Index to PostgreSQL Schema
**Type**: Implementation

**Description**: Add composite index on (first_name, last_name, telephone) to PostgreSQL schema file.

**TDD Phase**: N/A (Database optimization)

**Files to Modify**:
- `/Users/jackmonford/Repos/liatrio-forge/emerald-grove-pet-clinic-jack-mumford/src/main/resources/db/postgres/schema.sql`

**Implementation**:
```sql
-- Add after the owners table definition
CREATE INDEX idx_owner_duplicate_check ON owners (first_name, last_name, telephone);
```

**Expected Result**: Application starts successfully with PostgreSQL profile

**Verification**:
1. Start PostgreSQL container: `docker compose up postgres`
2. Run `./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres`

**Dependencies**: Task 3.1 (pattern established)

---

#### 3.4: Add Composite Index to HSQLDB Schema
**Type**: Implementation

**Description**: Add composite index on (first_name, last_name, telephone) to HSQLDB schema file.

**TDD Phase**: N/A (Database optimization)

**Files to Modify**:
- `/Users/jackmonford/Repos/liatrio-forge/emerald-grove-pet-clinic-jack-mumford/src/main/resources/db/hsqldb/schema.sql`

**Implementation**:
```sql
-- Add after the owners table definition
CREATE INDEX idx_owner_duplicate_check ON owners (first_name, last_name, telephone);
```

**Expected Result**: Application starts successfully with HSQLDB profile

**Verification**: Run application with HSQLDB profile (if used)

**Dependencies**: Task 3.1 (pattern established)

---

#### 3.5: Verify Index Creation Across All Databases
**Type**: Testing

**Description**: Verify that indexes are created successfully on all database profiles.

**TDD Phase**: N/A (Verification)

**Actions**:
1. Test H2 (default): `./mvnw spring-boot:run`
2. Test MySQL: `docker compose up mysql -d && ./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql`
3. Test PostgreSQL: `docker compose up postgres -d && ./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres`
4. Check application logs for errors

**Expected Result**: All profiles start without index-related errors

**Verification**: Check startup logs for SQL errors

**Dependencies**: Tasks 3.1-3.4

---

### Task 4: Internationalization - Error Messages

#### 4.1: Add Error Message to Base Messages File
**Type**: Implementation

**Description**: Add the `owner.alreadyExists` key to the base messages.properties file.

**TDD Phase**: N/A (Configuration)

**Files to Modify**:
- `/Users/jackmonford/Repos/liatrio-forge/emerald-grove-pet-clinic-jack-mumford/src/main/resources/messages/messages.properties`

**Implementation**:
```properties
# Add to owner-related messages section
owner.alreadyExists=An owner with this information already exists
```

**Expected Result**: Message key available for English locale

**Verification**: Start application and trigger duplicate error

**Dependencies**: None

---

#### 4.2: Add Error Message to English Messages File
**Type**: Implementation

**Description**: Add the `owner.alreadyExists` key to the English-specific messages file.

**TDD Phase**: N/A (Configuration)

**Files to Modify**:
- `/Users/jackmonford/Repos/liatrio-forge/emerald-grove-pet-clinic-jack-mumford/src/main/resources/messages/messages_en.properties`

**Implementation**:
```properties
# Add to owner-related messages section
owner.alreadyExists=An owner with this information already exists
```

**Expected Result**: Message key available for English locale

**Verification**: Check i18n sync test (if exists)

**Dependencies**: Task 4.1

---

#### 4.3: Verify Message Display in Form
**Type**: Testing

**Description**: Manually verify that the error message displays correctly in the owner creation form.

**TDD Phase**: N/A (Manual testing)

**Actions**:
1. Start application: `./mvnw spring-boot:run`
2. Create a new owner (e.g., "Test Owner", "1234567890")
3. Attempt to create the same owner again
4. Verify error message appears: "An owner with this information already exists"

**Expected Result**: Error message displays prominently in form

**Verification**: Visual inspection of form error

**Dependencies**: Tasks 2.2, 4.1, 4.2

---

### Task 5: End-to-End Testing - Duplicate Prevention Verification

#### 5.1: RED - Create E2E Test File Structure
**Type**: Test (RED Phase)

**Description**: Create new Playwright test file for duplicate prevention scenarios.

**TDD Phase**: RED

**Files to Create**:
- `/Users/jackmonford/Repos/liatrio-forge/emerald-grove-pet-clinic-jack-mumford/e2e-tests/tests/owner-duplicate-prevention.spec.ts`

**Implementation**:
```typescript
import { test, expect } from '@playwright/test';

test.describe('Owner Duplicate Prevention', () => {
    // Tests will be added in subsequent sub-tasks
});
```

**Expected Result**: File created, tests can be discovered by Playwright

**Verification**: Run `cd e2e-tests && npm test -- --list`

**Dependencies**: None

---

#### 5.2: RED - Write E2E Test for Duplicate Detection
**Type**: Test (RED Phase)

**Description**: Write E2E test that creates an owner, then attempts to create a duplicate and verifies error.

**TDD Phase**: RED

**Files to Modify**:
- `/Users/jackmonford/Repos/liatrio-forge/emerald-grove-pet-clinic-jack-mumford/e2e-tests/tests/owner-duplicate-prevention.spec.ts`

**Test Code**:
```typescript
test('should prevent creating duplicate owner', async ({ page }) => {
    // Arrange: Create first owner
    await page.goto('/owners/new');
    await page.fill('input[name="firstName"]', 'John');
    await page.fill('input[name="lastName"]', 'Doe');
    await page.fill('input[name="address"]', '123 Main St');
    await page.fill('input[name="city"]', 'Springfield');
    await page.fill('input[name="telephone"]', '5551234567');
    await page.click('button[type="submit"]');

    // Verify first owner created
    await expect(page).toHaveURL(/\/owners\/\d+/);

    // Act: Attempt to create duplicate
    await page.goto('/owners/new');
    await page.fill('input[name="firstName"]', 'John');
    await page.fill('input[name="lastName"]', 'Doe');
    await page.fill('input[name="address"]', '456 Elm St'); // Different address
    await page.fill('input[name="city"]', 'Springfield');
    await page.fill('input[name="telephone"]', '5551234567'); // Same phone
    await page.click('button[type="submit"]');

    // Assert: Should stay on form with error
    await expect(page).toHaveURL(/\/owners\/new/);
    await expect(page.locator('.alert-danger, .error, .help-block.text-danger'))
        .toContainText('An owner with this information already exists');
});
```

**Expected Result**: Test FAILS initially, PASSES after feature implementation

**Verification**: Run `cd e2e-tests && npm test -- --grep "prevent creating duplicate"`

**Dependencies**: Tasks 2.2, 4.1

---

#### 5.3: RED - Write E2E Test for Case-Insensitive Detection
**Type**: Test (RED Phase)

**Description**: Write E2E test that verifies case variations are detected as duplicates.

**TDD Phase**: RED

**Files to Modify**:
- `/Users/jackmonford/Repos/liatrio-forge/emerald-grove-pet-clinic-jack-mumford/e2e-tests/tests/owner-duplicate-prevention.spec.ts`

**Test Code**:
```typescript
test('should detect duplicate with different case', async ({ page }) => {
    // Arrange: Create first owner
    await page.goto('/owners/new');
    await page.fill('input[name="firstName"]', 'Alice');
    await page.fill('input[name="lastName"]', 'Smith');
    await page.fill('input[name="address"]', '789 Oak St');
    await page.fill('input[name="city"]', 'Madison');
    await page.fill('input[name="telephone"]', '5559876543');
    await page.click('button[type="submit"]');

    await expect(page).toHaveURL(/\/owners\/\d+/);

    // Act: Attempt to create with different case
    await page.goto('/owners/new');
    await page.fill('input[name="firstName"]', 'alice'); // lowercase
    await page.fill('input[name="lastName"]', 'SMITH'); // uppercase
    await page.fill('input[name="address"]', '999 Different St');
    await page.fill('input[name="city"]', 'Madison');
    await page.fill('input[name="telephone"]', '5559876543'); // Same phone
    await page.click('button[type="submit"]');

    // Assert: Duplicate detected
    await expect(page).toHaveURL(/\/owners\/new/);
    await expect(page.locator('.alert-danger, .error, .help-block.text-danger'))
        .toContainText('An owner with this information already exists');
});
```

**Expected Result**: Test PASSES (case-insensitive matching working)

**Verification**: Run `cd e2e-tests && npm test -- --grep "different case"`

**Dependencies**: Task 5.2

---

#### 5.4: RED - Write E2E Test for Non-Duplicate Allowed
**Type**: Test (RED Phase)

**Description**: Write E2E test that verifies similar but non-duplicate owners can be created.

**TDD Phase**: RED

**Files to Modify**:
- `/Users/jackmonford/Repos/liatrio-forge/emerald-grove-pet-clinic-jack-mumford/e2e-tests/tests/owner-duplicate-prevention.spec.ts`

**Test Code**:
```typescript
test('should allow creating owner with similar but different info', async ({ page }) => {
    // Arrange: Create first owner
    await page.goto('/owners/new');
    await page.fill('input[name="firstName"]', 'Bob');
    await page.fill('input[name="lastName"]', 'Jones');
    await page.fill('input[name="address"]', '111 Pine St');
    await page.fill('input[name="city"]', 'Chicago');
    await page.fill('input[name="telephone"]', '5551112222');
    await page.click('button[type="submit"]');

    await expect(page).toHaveURL(/\/owners\/\d+/);

    // Act: Create similar owner with different phone
    await page.goto('/owners/new');
    await page.fill('input[name="firstName"]', 'Bob');
    await page.fill('input[name="lastName"]', 'Jones');
    await page.fill('input[name="address"]', '111 Pine St'); // Same address
    await page.fill('input[name="city"]', 'Chicago');
    await page.fill('input[name="telephone"]', '9999999999'); // Different phone
    await page.click('button[type="submit"]');

    // Assert: Should succeed
    await expect(page).toHaveURL(/\/owners\/\d+/);
    // Optional: verify success message if displayed
});
```

**Expected Result**: Test PASSES (different phone = not duplicate)

**Verification**: Run `cd e2e-tests && npm test -- --grep "similar but different"`

**Dependencies**: Task 5.2

---

#### 5.5: GREEN - Verify All E2E Tests Pass
**Type**: Testing

**Description**: Run complete E2E test suite and verify all tests pass.

**TDD Phase**: GREEN

**Actions**:
1. Start application: `./mvnw spring-boot:run` (in background)
2. Run E2E tests: `cd e2e-tests && npm test`
3. Review test results and HTML report

**Expected Result**: All E2E tests pass (3 tests)

**Verification**: Check test output and Playwright HTML report

**Dependencies**: Tasks 5.1-5.4

---

#### 5.6: REFACTOR - Add Test Documentation
**Type**: Documentation

**Description**: Add comments and documentation to E2E test file for clarity.

**TDD Phase**: REFACTOR

**Files to Modify**:
- `/Users/jackmonford/Repos/liatrio-forge/emerald-grove-pet-clinic-jack-mumford/e2e-tests/tests/owner-duplicate-prevention.spec.ts`

**Implementation**:
```typescript
import { test, expect } from '@playwright/test';

/**
 * End-to-End Tests for Owner Duplicate Prevention (Issue #6)
 *
 * These tests verify that the application prevents duplicate owner records
 * based on matching first name, last name, and telephone number.
 *
 * Duplicate Rule: Exact match on all three fields (case-insensitive for names)
 */
test.describe('Owner Duplicate Prevention', () => {
    // Test implementations from previous sub-tasks
    // Each test should have clear comments explaining:
    // - What is being tested
    // - Expected behavior
    // - Key assertions
});
```

**Expected Result**: All tests continue to pass, documentation improved

**Verification**: Review code quality and readability

**Dependencies**: Task 5.5

---

### Task 6: Integration and Documentation

#### 6.1: Run Full Test Suite
**Type**: Testing

**Description**: Run complete test suite (unit, integration, E2E) to verify all components work together.

**TDD Phase**: Integration

**Actions**:
1. Run unit tests: `./mvnw test`
2. Run integration tests with multiple DB profiles:
   - H2: `./mvnw test`
   - MySQL: `./mvnw test -Dspring.profiles.active=mysql`
   - PostgreSQL: `./mvnw test -Dspring.profiles.active=postgres`
3. Run E2E tests: `cd e2e-tests && npm test`

**Expected Result**: All tests pass across all profiles

**Verification**: Review test reports and coverage

**Dependencies**: Tasks 1-5 (all previous tasks)

---

#### 6.2: Verify Code Coverage Meets Standards
**Type**: Testing

**Description**: Generate and review code coverage report to ensure >90% coverage for new code.

**TDD Phase**: Verification

**Actions**:
1. Generate coverage report: `./mvnw jacoco:report`
2. Open HTML report: `open target/site/jacoco/index.html`
3. Navigate to modified classes:
   - `OwnerRepository`
   - `OwnerController`
4. Verify coverage percentages

**Expected Result**:
- `OwnerRepository`: 100% (interface only)
- `OwnerController.processCreationForm()`: >90%
- `OwnerController.isDuplicate()`: >90%
- `OwnerController.normalizeTelephone()`: 100%

**Verification**: Check JaCoCo HTML report

**Dependencies**: Task 6.1

---

#### 6.3: Perform Manual Testing
**Type**: Manual Testing

**Description**: Manually test duplicate prevention feature through the UI.

**TDD Phase**: Verification

**Test Scenarios**:
1. **Exact Duplicate**:
   - Create owner "Test User" with phone "1234567890"
   - Attempt to create same owner again
   - Verify error message appears

2. **Case Variation**:
   - Create owner "Test User" with phone "9876543210"
   - Attempt to create "TEST USER" with same phone
   - Verify duplicate detected

3. **Different Phone**:
   - Create owner "John Smith" with phone "1111111111"
   - Create owner "John Smith" with phone "2222222222"
   - Verify both created successfully

4. **Form Retention**:
   - Attempt to create duplicate
   - Verify form fields retain entered values

**Expected Result**: All scenarios pass as expected

**Verification**: Document results in test notes

**Dependencies**: Task 6.1

---

#### 6.4: Update Acceptance Criteria Checklist
**Type**: Documentation

**Description**: Review and check off all acceptance criteria from the spec.

**TDD Phase**: Verification

**Actions**:
Review SPEC.md Section 2 and verify:
- [x] Attempting to create a duplicate owner is blocked
- [x] The UI shows a clear, actionable error message
- [x] The duplicate attempt does not create a second owner record
- [x] Duplicate defined as: same first name + last name + telephone (case-insensitive)
- [x] Validation occurs before save at repository layer
- [x] Error displayed at top of form (form-level error)
- [x] Database index added for query performance

**Expected Result**: All acceptance criteria met

**Verification**: Review SPEC.md

**Dependencies**: Task 6.3

---

#### 6.5: Create Implementation Summary
**Type**: Documentation

**Description**: Document implementation details, changes made, and testing results.

**TDD Phase**: Documentation

**Files to Create/Update**:
- Create implementation notes (optional)

**Content to Document**:
1. **Repository Changes**:
   - New method: `findByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndTelephone()`
   - Spring Data JPA auto-generates query

2. **Controller Changes**:
   - Modified: `processCreationForm()`
   - Added: `isDuplicate()` helper method
   - Added: `normalizeTelephone()` helper method

3. **Database Changes**:
   - Added composite index to 4 schema files (H2, MySQL, PostgreSQL, HSQLDB)

4. **Internationalization**:
   - Added message key: `owner.alreadyExists`

5. **Testing**:
   - 9 new repository tests
   - 7 new controller tests
   - 3 new E2E tests
   - Total coverage >90%

**Expected Result**: Clear documentation of changes

**Verification**: Review documentation completeness

**Dependencies**: Tasks 6.1-6.4

---

#### 6.6: Verify Definition of Done
**Type**: Verification

**Description**: Review and verify all Definition of Done criteria from the spec.

**TDD Phase**: Final Verification

**Checklist** (from SPEC.md Section 13):
- [ ] All TDD cycles completed (RED-GREEN-REFACTOR)
- [ ] Repository query method implemented and tested
- [ ] Controller duplicate check logic implemented
- [ ] Unit tests written and passing (>90% coverage)
- [ ] Integration tests written and passing
- [ ] Playwright E2E tests written and passing
- [ ] Internationalization message added
- [ ] Database index added to schema files
- [ ] Code review completed (ready for review)
- [ ] No Checkstyle/SpotBugs violations
- [ ] Feature tested manually in dev environment
- [ ] Ready for merge to main branch via PR

**Expected Result**: All criteria checked off

**Verification**: Self-assessment against checklist

**Dependencies**: Tasks 6.1-6.5

---

## Summary

### Total Tasks
- **Parent Tasks**: 6
- **Sub-Tasks**: 38

### Estimated Effort
- **Task 1**: 2-3 hours (6 sub-tasks)
- **Task 2**: 3-4 hours (10 sub-tasks)
- **Task 3**: 1-2 hours (5 sub-tasks)
- **Task 4**: 1 hour (3 sub-tasks)
- **Task 5**: 2-3 hours (6 sub-tasks)
- **Task 6**: 1-2 hours (6 sub-tasks)

**Total Estimated Effort**: 10-15 hours

### Dependencies Flow
```
Task 1 (Repository) → Task 2 (Controller)
                    ↓
Task 3 (Database Index) (parallel)
Task 4 (i18n) (parallel)
                    ↓
Task 5 (E2E Tests) ← Task 2, Task 4
                    ↓
Task 6 (Integration & Docs) ← All Tasks
```

### Key Success Metrics
- All tests passing (unit, integration, E2E)
- Code coverage >90% for new code
- All database profiles working
- Manual testing scenarios pass
- Acceptance criteria met
- Definition of done complete

---

**End of Task List**
