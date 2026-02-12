# Clarifying Questions - Issue #11: Export owners search results as CSV

## Endpoint Design

1. **Endpoint URL Pattern**: Which approach do you prefer?
   - Option A: `/owners.csv` (separate endpoint)
   - Option B: `/owners?format=csv` (query parameter on existing endpoint)
   - Option C: `/owners/export?format=csv` (dedicated export sub-path)

2. **HTTP Method**: Should this be GET (simple download) or POST (for larger query payloads)?

## CSV Format

3. **Column Selection**: The issue mentions "minimal columns". Should we include:
   - Core fields only: First Name, Last Name, Address, City, Telephone
   - Add pets column: Should we include a pets column showing pet names (comma-separated)?
   - Add owner ID for reference?

4. **CSV Headers**: What column header names should we use?
   - Option A: Internationalized headers using current locale (e.g., "Telefon" for German)
   - Option B: English-only headers for consistency
   - Option C: Make it configurable via query parameter

5. **Empty Results**: What should happen if the search returns no results?
   - Return empty CSV with headers only
   - Return 404 or specific error
   - Return CSV with a message row

## Search Parameters

6. **Search Scope**: Should the CSV export:
   - Respect the `lastName` search parameter (filter like HTML view)
   - Include all owners regardless of search (full export option)
   - Support both filtered and full export via parameter?

7. **Pagination**: How should we handle pagination?
   - Option A: Export ALL matching results (ignore pagination)
   - Option B: Export only current page results
   - Option C: Add `limit` parameter to control export size

## Security & Performance

8. **Rate Limiting**: Should we add any restrictions?
   - Maximum number of records per export
   - Rate limiting for frequent exports
   - No restrictions initially

9. **Authorization**: Should CSV export require any special permissions?
   - Available to all users (same as HTML view)
   - Log export actions for audita

## File Naming

10. **Download Filename**: What should the downloaded file be named?
    - Static: `owners.csv`
    - Dynamic with timestamp: `owners-export-2026-02-12.csv`
    - Include search criteria: `owners-smith-2026-02-12.csv`

## Testing Requirements

11. **Test Coverage**: Beyond the basic acceptance criteria, should we test:
    - Special characters in owner data (quotes, commas, unicode)
    - Large result sets (100+ owners)
    - Empty/null field handling
    - Concurrent export requests

12. **Playwright E2E**: Should we implement:
    - Basic download verification (file exists)
    - CSV content validation (parse and verify data)
    - Both or just basic?

---

**Please answer these questions to proceed with spec generation. You can provide short answers like "3A, 4B, 5A" for efficiency.**
