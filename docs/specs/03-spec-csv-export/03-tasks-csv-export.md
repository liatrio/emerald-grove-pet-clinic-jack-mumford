# Task List: CSV Export for Owner Search Results

**Feature ID**: Issue #11
**Spec**: `03-spec-csv-export/SPEC.md`
**Created**: 2026-02-12

---

## Relevant Files

### Files to Create
- `src/main/java/org/springframework/samples/petclinic/owner/CsvBuilder.java` - Utility class for building CSV content from owner data
- `src/test/java/org/springframework/samples/petclinic/owner/CsvBuilderTests.java` - Unit tests for CsvBuilder utility
- `e2e-tests/tests/csv-export.spec.ts` - End-to-end Playwright test for CSV export functionality

### Files to Modify
- `src/main/java/org/springframework/samples/petclinic/owner/OwnerRepository.java` - Add unpaginated `findByLastNameStartingWith(String)` method
- `src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java` - Add CSV export endpoint `exportOwnersCsv()`
- `src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java` - Add integration tests for CSV endpoint
- `src/test/java/org/springframework/samples/petclinic/service/ClinicServiceTests.java` - Add repository tests for unpaginated query
- `src/main/resources/templates/owners/ownersList.html` - Add "Export to CSV" button to UI

### Notes

- Unit tests should typically be placed alongside the code files they are testing
- Use Maven for running tests: `./mvnw test -Dtest=TestClassName`
- Follow the repository's TDD workflow: RED (failing test) → GREEN (minimal implementation) → REFACTOR
- Adhere to Spring Boot conventions and existing patterns in OwnerController
- Ensure 90%+ line coverage for new code (verified via JaCoCo)
- Follow existing Thymeleaf template patterns for UI modifications

---

## Tasks

### [ ] 1.0 Build CSV formatting utility with proper character escaping

**Description**: Create a `CsvBuilder` utility class that generates CSV content from a list of owners. Must handle special characters (commas, quotes, newlines) according to RFC 4180, produce proper header row, and handle edge cases (null fields, Unicode characters).

#### 1.0 Proof Artifact(s)

- Test: `CsvBuilderTests.java` all tests pass (8+ test cases) demonstrates CSV generation and escaping work correctly
- CLI: Manual inspection of generated CSV in tests shows proper formatting with commas escaped in "638 Cardinal Ave., Apt 2B" demonstrates field quoting works
- Coverage: JaCoCo report shows 90%+ line coverage for CsvBuilder demonstrates comprehensive testing

#### 1.0 Tasks

- [ ] 1.1 **RED**: Create `CsvBuilderTests.java` with test for header row generation (should fail)
- [ ] 1.2 **GREEN**: Create `CsvBuilder.java` with minimal `buildOwnersCsv()` method that returns header only
- [ ] 1.3 **RED**: Add test for single owner row formatting (should fail)
- [ ] 1.4 **GREEN**: Implement basic row formatting logic (append owner fields with commas)
- [ ] 1.5 **RED**: Add test for CSV field escaping - fields with commas should be quoted (should fail)
- [ ] 1.6 **GREEN**: Implement `escapeCsvField()` helper method to handle commas, quotes, newlines
- [ ] 1.7 **RED**: Add tests for edge cases (null fields, quotes in text, Unicode characters)
- [ ] 1.8 **GREEN**: Enhance `escapeCsvField()` to handle all edge cases
- [ ] 1.9 **REFACTOR**: Extract `formatCsvRow()` helper method, add JavaDoc comments, verify 90%+ coverage

---

### [ ] 2.0 Add unpaginated owner repository query method

**Description**: Extend `OwnerRepository` with an unpaginated version of `findByLastNameStartingWith()` that returns all matching owners (no page limit). This is needed because CSV export must return ALL results, not just one page.

#### 2.0 Proof Artifact(s)

- Test: `OwnerRepositoryTests.java` tests for unpaginated query pass demonstrates repository method works
- CLI: Run `./mvnw test -Dtest=OwnerRepositoryTests` shows passing tests demonstrates integration with Spring Data JPA
- Code: New repository method signature visible in `OwnerRepository.java` demonstrates API addition

#### 2.0 Tasks

- [ ] 2.1 **RED**: Add test in `ClinicServiceTests.java` for unpaginated `findByLastNameStartingWith(String)` (should fail - method doesn't exist)
- [ ] 2.2 **GREEN**: Add method signature to `OwnerRepository.java`: `List<Owner> findByLastNameStartingWith(String lastName);`
- [ ] 2.3 Verify Spring Data JPA generates implementation automatically (run test - should pass)
- [ ] 2.4 **RED**: Add test for case-insensitive matching (should pass due to existing behavior)
- [ ] 2.5 **RED**: Add test for empty result returns empty list (should pass)
- [ ] 2.6 Run full repository test suite to ensure no regressions: `./mvnw test -Dtest=ClinicServiceTests`

---

### [ ] 3.0 Implement CSV export controller endpoint with filtering

**Description**: Add `@GetMapping("/owners.csv")` method to `OwnerController` that returns CSV-formatted response. Must respect `lastName` query parameter, return 404 on empty results, set proper headers (Content-Type, Content-Disposition), and generate dynamic filename with timestamp.

#### 3.0 Proof Artifact(s)

- CLI: `curl http://localhost:8080/owners.csv` returns CSV with headers demonstrates endpoint works
- CLI: `curl http://localhost:8080/owners.csv?lastName=Franklin` returns filtered results demonstrates search parameter works
- CLI: `curl -I http://localhost:8080/owners.csv` shows `Content-Type: text/csv` and `Content-Disposition: attachment; filename="owners-export-2026-02-12.csv"` demonstrates proper headers
- Test: `OwnerControllerTests.java` integration tests pass (6+ test cases) demonstrates controller logic works

#### 3.0 Tasks

- [ ] 3.1 **RED**: Add test in `OwnerControllerTests.java` for GET `/owners.csv` endpoint returning CSV (should fail - endpoint doesn't exist)
- [ ] 3.2 **GREEN**: Add `@GetMapping("/owners.csv")` method to `OwnerController.java` with basic CSV response
- [ ] 3.3 Implement CSV response headers: Content-Type (`text/csv`), Content-Disposition with dynamic filename
- [ ] 3.4 Use `CsvBuilder.buildOwnersCsv()` to generate CSV content from owner list
- [ ] 3.5 **RED**: Add test for `lastName` parameter filtering (should fail - filtering not implemented)
- [ ] 3.6 **GREEN**: Implement filtering logic using `owners.findByLastNameStartingWith(lastName)`
- [ ] 3.7 **RED**: Add test for 404 on empty results (should fail - always returns 200)
- [ ] 3.8 **GREEN**: Add empty check, throw `ResponseStatusException(HttpStatus.NOT_FOUND)` when no owners found
- [ ] 3.9 **RED**: Add test for filename format with current date (should fail if format incorrect)
- [ ] 3.10 **GREEN**: Implement dynamic filename using `LocalDate.now()` formatted as `yyyy-MM-dd`
- [ ] 3.11 **REFACTOR**: Extract helper method for building CSV response headers
- [ ] 3.12 Run full controller test suite: `./mvnw test -Dtest=OwnerControllerTests`

---

### [ ] 4.0 Add CSV export button to owners list UI

**Description**: Add "Export to CSV" button to `ownersList.html` template that links to `/owners.csv` with current search parameters. Button should follow existing Liatrio styling and be placed above or below the owners table.

#### 4.0 Proof Artifact(s)

- Screenshot: `docs/specs/03-spec-csv-export/proof/owners-list-with-export-button.png` shows button on page demonstrates UI integration
- URL: http://localhost:8080/owners (manual test) shows export button demonstrates feature is accessible
- Manual Test: Click button triggers CSV download with correct filename demonstrates end-to-end flow works

#### 4.0 Tasks

- [ ] 4.1 Open `ownersList.html` and locate the owners table section
- [ ] 4.2 Add "Export to CSV" button above or below the table using Bootstrap `btn btn-secondary` class
- [ ] 4.3 Set button `th:href` to `@{/owners.csv(lastName=${param.lastName})}` to preserve search filter
- [ ] 4.4 Add Font Awesome icon to button (e.g., `fa-download`) for visual clarity
- [ ] 4.5 Start application: `./mvnw spring-boot:run`
- [ ] 4.6 Navigate to http://localhost:8080/owners and verify button appears
- [ ] 4.7 Test button click triggers CSV download
- [ ] 4.8 Test with search filter: search for "Franklin", click export, verify only Franklin results in CSV
- [ ] 4.9 Take screenshot of UI with button and save to `docs/specs/03-spec-csv-export/proof/owners-list-with-export-button.png`

---

### [ ] 5.0 Add end-to-end Playwright test for CSV export

**Description**: Create Playwright E2E test that verifies CSV download functionality. Test should navigate to owners page, trigger export, verify file download, and check filename format matches pattern `owners-export-YYYY-MM-DD.csv`.

#### 5.0 Proof Artifact(s)

- Test: `e2e-tests/tests/csv-export.spec.ts` passes demonstrates E2E flow works
- CLI: `cd e2e-tests && npm test -- csv-export.spec.ts` shows passing test demonstrates automation works
- Artifact: Playwright test report in `e2e-tests/test-results/` demonstrates test execution evidence

#### 5.0 Tasks

- [ ] 5.1 Create `e2e-tests/tests/csv-export.spec.ts` file
- [ ] 5.2 **RED**: Write test that navigates to `/owners?lastName=Franklin` and clicks export link (should fail - test doesn't exist)
- [ ] 5.3 **GREEN**: Implement test using `page.waitForEvent('download')` to capture download event
- [ ] 5.4 Add assertion for filename format: `expect(download.suggestedFilename()).toMatch(/owners-export-\d{4}-\d{2}-\d{2}\.csv/)`
- [ ] 5.5 **Optional**: Add test to verify CSV content by reading downloaded file
- [ ] 5.6 Run E2E test: `cd e2e-tests && npm test -- csv-export.spec.ts`
- [ ] 5.7 Verify test passes and Playwright generates test report
- [ ] 5.8 Review test artifacts in `e2e-tests/test-results/` directory

---

## Notes

- Follow strict TDD (RED-GREEN-REFACTOR) for all implementation
- Minimum 90% line coverage required for new code
- Rate limiting (FR-6) deferred to Phase 2 as noted in spec
- Tasks 1.0 and 2.0 can be implemented in parallel (no dependencies)
- Task 3.0 depends on 1.0 and 2.0 completion
- Task 4.0 depends on 3.0 completion
- Task 5.0 depends on 4.0 completion
