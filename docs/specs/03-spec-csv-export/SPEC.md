# Specification: CSV Export for Owner Search Results

**Feature ID**: Issue #11
**Status**: Draft
**Created**: 2026-02-12
**TDD Required**: Yes (Strict RED-GREEN-REFACTOR)

---

## 1. Feature Overview

### Summary
Add a CSV export endpoint for owner search results, allowing users to download filtered owner data in CSV format for external use (spreadsheets, analysis, reporting).

### Business Value
- Enables data portability for administrative tasks
- Supports offline analysis and reporting workflows
- Maintains consistency with existing search/filter functionality

### Scope
- **In Scope**: CSV export endpoint respecting lastName search parameter, timestamp-based filenames, rate limiting
- **Out of Scope**: Excel/XLSX format, custom column selection, scheduling exports, multi-format support

---

## 2. Acceptance Criteria

From GitHub Issue #11:
- [x] A CSV endpoint exists at `/owners.csv`
- [x] Respects existing owner search parameters (e.g., `lastName`)
- [x] Response has `text/csv` content type and includes a header row
- [x] Returns 404 for empty search results
- [x] Exports ALL matching results (ignores pagination)
- [x] Filename includes timestamp: `owners-export-2026-02-12.csv`
- [x] Rate limiting applied for frequent exports
- [x] Available to all users (same authorization as HTML view)

---

## 3. Functional Requirements

### FR-1: CSV Endpoint
**Requirement**: Create a new GET endpoint at `/owners.csv` that returns CSV-formatted owner data.

**Behavior**:
- URL: `/owners.csv`
- HTTP Method: GET
- Query Parameters:
  - `lastName` (optional, string): Filter by last name prefix (same as `/owners`)
- Response Content-Type: `text/csv; charset=UTF-8`
- Response Disposition: `attachment; filename="owners-export-YYYY-MM-DD.csv"`

### FR-2: CSV Format
**Requirement**: CSV must include header row with English column names and core owner fields.

**Columns** (in order):
1. First Name
2. Last Name
3. Address
4. City
5. Telephone

**CSV Specification**:
- Header row: `First Name,Last Name,Address,City,Telephone`
- Field delimiter: comma (`,`)
- Text qualifier: double quotes (`"`) for fields containing commas, quotes, or newlines
- Line terminator: `\r\n` (CRLF, standard CSV)
- Character encoding: UTF-8
- Escape quotes: double-quote (`""`) within quoted fields

**Example**:
```csv
First Name,Last Name,Address,City,Telephone
George,Franklin,110 W. Liberty St.,Madison,6085551023
Betty,Davis,"638 Cardinal Ave., Apt 2B",Sun Prairie,6085551749
Eduardo,Rodriquez,2693 Commerce St.,McFarland,6085558763
```

### FR-3: Search Filtering
**Requirement**: Respect the `lastName` query parameter to filter results.

**Behavior**:
- `/owners.csv` → Export ALL owners (empty lastName = "")
- `/owners.csv?lastName=Smith` → Export only owners with lastName starting with "Smith" (case-insensitive)
- Use existing `OwnerRepository.findByLastNameStartingWith()` method
- Export ALL matching results, ignoring pagination (no page/size limits)

### FR-4: Empty Results Handling
**Requirement**: Return HTTP 404 when search yields no results.

**Behavior**:
- If no owners match the search criteria, return:
  - HTTP Status: 404 Not Found
  - Content-Type: `text/html` (use existing error page)
  - Redirect to `/notFound` or return error view

### FR-5: Filename Generation
**Requirement**: Generate dynamic filename with current date.

**Format**: `owners-export-YYYY-MM-DD.csv`

**Examples**:
- `owners-export-2026-02-12.csv`
- `owners-export-2026-12-31.csv`

**Implementation**: Use `LocalDate.now()` formatted as `yyyy-MM-dd`

### FR-6: Rate Limiting
**Requirement**: Apply rate limiting to prevent abuse.

**Specification**:
- Rate Limit: 10 requests per IP address per minute
- Enforcement: Use Spring rate limiting or custom interceptor
- Response on Exceed:
  - HTTP Status: 429 Too Many Requests
  - Error message: "Export rate limit exceeded. Please try again later."
  - Retry-After header: 60 seconds

**Note**: Initial implementation may defer rate limiting to Phase 2 if complexity is high.

---

## 4. Technical Design

### 4.1 Architecture

**Component**: New controller method in `OwnerController`

**Flow**:
```
Client Request → OwnerController.exportOwnersCsv()
                 ↓
                 OwnerRepository.findByLastNameStartingWith()
                 ↓
                 CsvBuilder.buildCsv(owners)
                 ↓
                 Response with CSV content
```

### 4.2 Controller Method

**Location**: `src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java`

**Method Signature**:
```java
@GetMapping("/owners.csv")
public ResponseEntity<String> exportOwnersCsv(
    @RequestParam(defaultValue = "") String lastName,
    HttpServletResponse response
) throws IOException
```

**Responsibilities**:
1. Extract `lastName` query parameter
2. Query repository for matching owners (unpaginated)
3. Check if results are empty → return 404 if true
4. Build CSV string from owner list
5. Set response headers (Content-Type, Content-Disposition, filename)
6. Return CSV as ResponseEntity

### 4.3 CSV Builder Utility

**Location**: `src/main/java/org/springframework/samples/petclinic/owner/CsvBuilder.java` (new file)

**Method**:
```java
public class CsvBuilder {
    private static final String HEADER = "First Name,Last Name,Address,City,Telephone\n";

    public static String buildOwnersCsv(List<Owner> owners) {
        StringBuilder csv = new StringBuilder(HEADER);
        for (Owner owner : owners) {
            csv.append(formatCsvRow(
                owner.getFirstName(),
                owner.getLastName(),
                owner.getAddress(),
                owner.getCity(),
                owner.getTelephone()
            ));
        }
        return csv.toString();
    }

    private static String formatCsvRow(String... fields) {
        // Escape and quote fields containing commas, quotes, or newlines
        // Join with commas, append CRLF
    }

    private static String escapeCsvField(String field) {
        // Handle null, escape quotes, wrap in quotes if needed
    }
}
```

**Responsibilities**:
1. Generate CSV header row
2. Iterate over owners and format each as CSV row
3. Properly escape special characters (quotes, commas, newlines)
4. Handle null/empty fields gracefully

### 4.4 Repository Query

**Use Existing**: `OwnerRepository.findByLastNameStartingWith(String lastName, Pageable pageable)`

**Modification Needed**: Create unpaginated variant for CSV export

**New Method**:
```java
List<Owner> findByLastNameStartingWith(String lastName);
```

**Rationale**: Export needs ALL results, not paginated subset.

### 4.5 Response Headers

**Required Headers**:
- `Content-Type: text/csv; charset=UTF-8`
- `Content-Disposition: attachment; filename="owners-export-2026-02-12.csv"`
- `Cache-Control: no-cache, no-store, must-revalidate`
- `Pragma: no-cache`
- `Expires: 0`

---

## 5. TDD Approach (RED-GREEN-REFACTOR)

### Phase 1: CSV Builder Unit Tests

#### RED 1: Test CSV Header Generation
**Test**: `CsvBuilderTests.shouldGenerateHeaderRow()`
```java
@Test
void shouldGenerateHeaderRow() {
    String csv = CsvBuilder.buildOwnersCsv(Collections.emptyList());
    assertThat(csv).startsWith("First Name,Last Name,Address,City,Telephone\n");
}
```
**Expected**: FAIL (CsvBuilder doesn't exist)

#### GREEN 1: Implement Minimal Header
```java
public class CsvBuilder {
    public static String buildOwnersCsv(List<Owner> owners) {
        return "First Name,Last Name,Address,City,Telephone\n";
    }
}
```
**Expected**: PASS

#### RED 2: Test Single Owner Row
**Test**: `CsvBuilderTests.shouldFormatSingleOwnerRow()`
```java
@Test
void shouldFormatSingleOwnerRow() {
    Owner owner = createTestOwner("George", "Franklin", "110 W. Liberty St.", "Madison", "6085551023");
    String csv = CsvBuilder.buildOwnersCsv(List.of(owner));
    assertThat(csv).contains("George,Franklin,110 W. Liberty St.,Madison,6085551023");
}
```
**Expected**: FAIL (no row formatting logic)

#### GREEN 2: Implement Basic Row Formatting
```java
public static String buildOwnersCsv(List<Owner> owners) {
    StringBuilder csv = new StringBuilder("First Name,Last Name,Address,City,Telephone\n");
    for (Owner owner : owners) {
        csv.append(owner.getFirstName()).append(",")
           .append(owner.getLastName()).append(",")
           .append(owner.getAddress()).append(",")
           .append(owner.getCity()).append(",")
           .append(owner.getTelephone()).append("\n");
    }
    return csv.toString();
}
```
**Expected**: PASS

#### RED 3: Test CSV Escaping (Commas, Quotes)
**Test**: `CsvBuilderTests.shouldEscapeFieldsWithCommas()`
```java
@Test
void shouldEscapeFieldsWithCommas() {
    Owner owner = createTestOwner("Betty", "Davis", "638 Cardinal Ave., Apt 2B", "Sun Prairie", "6085551749");
    String csv = CsvBuilder.buildOwnersCsv(List.of(owner));
    assertThat(csv).contains("\"638 Cardinal Ave., Apt 2B\"");
}
```
**Expected**: FAIL (no escaping logic)

#### GREEN 3: Implement CSV Field Escaping
```java
private static String escapeCsvField(String field) {
    if (field == null) return "";
    if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
        return "\"" + field.replace("\"", "\"\"") + "\"";
    }
    return field;
}
```
**Expected**: PASS

#### REFACTOR 1: Extract Row Formatting Method
- Extract `formatCsvRow(String... fields)` helper
- Remove duplication in row building logic
- Add null safety checks

### Phase 2: Controller Integration Tests

#### RED 4: Test CSV Endpoint Returns CSV Content
**Test**: `OwnerControllerTests.shouldReturnCsvFormatWhenAccessingCsvEndpoint()`
```java
@Test
void shouldReturnCsvFormatWhenAccessingCsvEndpoint() throws Exception {
    mockMvc.perform(get("/owners.csv"))
        .andExpect(status().isOk())
        .andExpect(content().contentType("text/csv; charset=UTF-8"))
        .andExpect(header().string("Content-Disposition", containsString("attachment")))
        .andExpect(content().string(containsString("First Name,Last Name,Address,City,Telephone")));
}
```
**Expected**: FAIL (endpoint doesn't exist)

#### GREEN 4: Implement Basic CSV Endpoint
```java
@GetMapping("/owners.csv")
public ResponseEntity<String> exportOwnersCsv(@RequestParam(defaultValue = "") String lastName) {
    List<Owner> owners = this.owners.findByLastNameStartingWith(lastName);
    String csv = CsvBuilder.buildOwnersCsv(owners);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
    headers.setContentDispositionFormData("attachment", "owners-export-" + LocalDate.now() + ".csv");
    return ResponseEntity.ok().headers(headers).body(csv);
}
```
**Expected**: PASS

#### RED 5: Test Search Filter Respected
**Test**: `OwnerControllerTests.shouldFilterCsvByLastNameParameter()`
```java
@Test
void shouldFilterCsvByLastNameParameter() throws Exception {
    given(this.owners.findByLastNameStartingWith(eq("Franklin")))
        .willReturn(List.of(george()));

    mockMvc.perform(get("/owners.csv?lastName=Franklin"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("George,Franklin")))
        .andExpect(content().string(not(containsString("Betty,Davis"))));
}
```
**Expected**: FAIL (filtering not implemented with unpaginated query)

#### GREEN 5: Add Unpaginated Repository Method
```java
// OwnerRepository.java
List<Owner> findByLastNameStartingWith(String lastName);
```
**Expected**: PASS

#### RED 6: Test 404 on Empty Results
**Test**: `OwnerControllerTests.shouldReturn404WhenNoCsvResultsFound()`
```java
@Test
void shouldReturn404WhenNoCsvResultsFound() throws Exception {
    given(this.owners.findByLastNameStartingWith(eq("NonExistent")))
        .willReturn(Collections.emptyList());

    mockMvc.perform(get("/owners.csv?lastName=NonExistent"))
        .andExpect(status().isNotFound());
}
```
**Expected**: FAIL (returns empty CSV instead of 404)

#### GREEN 6: Add Empty Check
```java
@GetMapping("/owners.csv")
public ResponseEntity<String> exportOwnersCsv(@RequestParam(defaultValue = "") String lastName) {
    List<Owner> owners = this.owners.findByLastNameStartingWith(lastName);
    if (owners.isEmpty()) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No owners found matching the search criteria");
    }
    // ... rest of implementation
}
```
**Expected**: PASS

#### REFACTOR 2: Extract CSV Response Builder
- Create helper method `buildCsvResponse(String csv, String filename)`
- Reduce duplication in header setting

### Phase 3: End-to-End Tests (Playwright)

#### RED 7: Test CSV Download (Basic)
**Test**: `e2e-tests/tests/csv-export.spec.ts`
```typescript
test('should download CSV file when clicking export link', async ({ page }) => {
    await page.goto('/owners?lastName=Franklin');

    const downloadPromise = page.waitForEvent('download');
    await page.click('a[href="/owners.csv?lastName=Franklin"]');
    const download = await downloadPromise;

    expect(download.suggestedFilename()).toMatch(/owners-export-\d{4}-\d{2}-\d{2}\.csv/);
});
```
**Expected**: FAIL (no export link in UI yet)

#### GREEN 7: Add CSV Export Link to UI
**Template**: `src/main/resources/templates/owners/ownersList.html`
```html
<a th:href="@{/owners.csv(lastName=${lastName})}" class="btn btn-secondary">
    Export to CSV
</a>
```
**Expected**: PASS

---

## 6. Test Scenarios

### 6.1 Unit Tests (CsvBuilderTests.java)

| Test Case | Description | Expected Result |
|-----------|-------------|-----------------|
| `shouldGenerateHeaderRow()` | Empty list generates header only | CSV contains header row |
| `shouldFormatSingleOwnerRow()` | Single owner formatted correctly | CSV contains 1 data row |
| `shouldFormatMultipleOwnerRows()` | Multiple owners formatted | CSV contains N data rows |
| `shouldEscapeFieldsWithCommas()` | Address with comma is quoted | Field wrapped in quotes |
| `shouldEscapeFieldsWithQuotes()` | Name with quote is escaped | Quote doubled within quotes |
| `shouldEscapeFieldsWithNewlines()` | Address with newline is quoted | Field wrapped in quotes |
| `shouldHandleNullFields()` | Null fields become empty | Empty string in CSV |
| `shouldHandleUnicodeCharacters()` | Unicode in name preserved | UTF-8 encoded correctly |

### 6.2 Controller Integration Tests (OwnerControllerTests.java)

| Test Case | Description | Expected Result |
|-----------|-------------|-----------------|
| `shouldReturnCsvFormatWhenAccessingCsvEndpoint()` | GET /owners.csv returns CSV | 200 OK, text/csv |
| `shouldIncludeHeaderRowInCsv()` | CSV has English headers | Header present |
| `shouldFilterCsvByLastNameParameter()` | ?lastName=Smith filters results | Only Smith* rows |
| `shouldExportAllOwnersWhenNoFilter()` | No lastName param = all owners | All owners in CSV |
| `shouldReturn404WhenNoCsvResultsFound()` | Empty search returns 404 | 404 Not Found |
| `shouldSetContentDispositionHeader()` | Response has attachment header | Download triggered |
| `shouldGenerateFilenameWithCurrentDate()` | Filename includes today's date | Format: owners-export-YYYY-MM-DD.csv |
| `shouldExportAllResultsIgnoringPagination()` | Export not limited to page size | All 100+ owners returned |

### 6.3 Repository Tests (OwnerRepositoryTests.java)

| Test Case | Description | Expected Result |
|-----------|-------------|-----------------|
| `shouldFindAllOwnersUnpaginatedByLastName()` | Unpaginated query works | All matching owners returned |
| `shouldReturnEmptyListWhenNoMatch()` | No matches = empty list | List.isEmpty() == true |

### 6.4 End-to-End Tests (Playwright)

| Test Case | Description | Expected Result |
|-----------|-------------|-----------------|
| `shouldDownloadCsvFile()` | Click export link downloads CSV | File downloaded |
| `shouldHaveCorrectFilename()` | Downloaded file has timestamp | Filename matches pattern |

---

## 7. Implementation Plan

### Step 1: Add Unpaginated Repository Method
**Files**: `OwnerRepository.java`
- Add `List<Owner> findByLastNameStartingWith(String lastName);`
- Spring Data JPA generates implementation automatically

**Tests**: Repository integration test

### Step 2: Implement CSV Builder Utility
**Files**: `CsvBuilder.java` (new)
- Create `buildOwnersCsv(List<Owner>)` method
- Implement `escapeCsvField(String)` helper
- Follow TDD RED-GREEN-REFACTOR cycles from Section 5

**Tests**: `CsvBuilderTests.java` (new)

### Step 3: Add CSV Export Controller Method
**Files**: `OwnerController.java`
- Add `@GetMapping("/owners.csv")` method
- Implement search filtering, empty result handling
- Set CSV response headers with dynamic filename

**Tests**: `OwnerControllerTests.java` (add tests)

### Step 4: Add Export Link to UI (Optional for MVP)
**Files**: `ownersList.html`
- Add "Export to CSV" button/link above or below owners table
- Pass current `lastName` parameter to CSV endpoint

**Tests**: Manual verification or Playwright

### Step 5: Add Rate Limiting (Deferred to Phase 2)
**Files**: Rate limiting configuration (if implemented)
- Configure rate limiter (Spring or custom)
- Return 429 on exceed

**Tests**: Rate limiting tests

---

## 8. Dependencies

### Internal
- **OwnerRepository**: Requires new unpaginated `findByLastNameStartingWith()` method
- **Owner Entity**: No changes needed
- **Thymeleaf Templates**: Optional UI link addition

### External
- None (uses standard Java libraries for CSV generation)

### Spring Configuration
- No additional dependencies required
- Uses existing Spring MVC `ResponseEntity` and `HttpHeaders`

---

## 9. Non-Functional Requirements

### Performance
- **Target**: Export up to 1,000 owners in < 2 seconds
- **Constraint**: Unpaginated query may be slow for large datasets (10,000+ owners)
- **Mitigation**: Add index on `last_name` column if not present

### Security
- **Authorization**: No additional authentication required (same as HTML view)
- **Rate Limiting**: 10 requests/minute per IP to prevent abuse
- **Data Exposure**: CSV contains all owner data visible in HTML view (no sensitive data)

### Scalability
- **Concern**: Large datasets (100,000+ owners) may cause memory issues
- **Mitigation**: Consider streaming CSV generation in Phase 2 if needed

### Compatibility
- **Browsers**: All modern browsers support CSV download
- **CSV Format**: Standard RFC 4180 compliance
- **Character Encoding**: UTF-8 for international characters

---

## 10. Risk Assessment

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| Large dataset OOM | Medium | High | Add streaming in Phase 2, document limits |
| CSV injection attacks | Low | Medium | Escape special characters, validate data |
| Rate limiting complexity | Medium | Low | Defer to Phase 2, document as future work |
| Special character handling | Medium | Medium | Thorough unit tests, follow RFC 4180 |
| Empty result UX confusion | Low | Low | Clear 404 error message |

---

## 11. Future Enhancements (Out of Scope)

- **Multi-format support**: Excel (.xlsx), JSON, XML exports
- **Custom column selection**: Allow users to choose which fields to export
- **Streaming CSV generation**: For very large datasets (10,000+ owners)
- **Scheduled exports**: Daily/weekly automated exports
- **Export history**: Track who exported what and when
- **Advanced filtering**: Export by city, telephone, registration date
- **Include pets data**: Add pet information to export

---

## 12. Acceptance Testing Checklist

- [ ] `/owners.csv` endpoint returns 200 OK with CSV content
- [ ] CSV has correct header: `First Name,Last Name,Address,City,Telephone`
- [ ] Filtering by `?lastName=Smith` returns only matching owners
- [ ] Empty search results return 404 Not Found
- [ ] All matching results exported (ignoring pagination)
- [ ] Filename format: `owners-export-YYYY-MM-DD.csv`
- [ ] Content-Type header: `text/csv; charset=UTF-8`
- [ ] Content-Disposition header: `attachment`
- [ ] Fields with commas are properly quoted and escaped
- [ ] Fields with quotes are properly escaped (doubled quotes)
- [ ] Unicode characters (e.g., José, Müller) render correctly
- [ ] Null/empty fields handled gracefully (empty string)
- [ ] Unit tests for CsvBuilder achieve 90%+ coverage
- [ ] Controller integration tests pass
- [ ] Playwright E2E test verifies basic download

---

## 13. Definition of Done

- [ ] All TDD cycles completed (RED-GREEN-REFACTOR)
- [ ] Unit tests written and passing (>90% coverage)
- [ ] Integration tests written and passing
- [ ] Playwright E2E test written and passing (basic download)
- [ ] Code review completed
- [ ] No Checkstyle/SpotBugs violations
- [ ] Documentation updated (if needed)
- [ ] Feature tested manually in dev environment
- [ ] Merged to main branch via PR

---

**End of Specification**
