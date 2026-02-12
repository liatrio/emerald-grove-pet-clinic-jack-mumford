# Clarifying Questions - Issue #6: Prevent duplicate owner creation

## Duplicate Detection Rule

1. **What Defines a Duplicate?**: Which fields should we use to detect duplicates?
   - Option A: First Name + Last Name + Telephone (exact match, case-insensitive)
   - Option B: Last Name + Telephone only
   - Option C: Telephone only (assuming unique phone numbers)
   - Option D: First Name + Last Name + Address + City (full match)
   - Option E: Other combination?

2. **Case Sensitivity**: How should we handle case differences?
   - Case-insensitive comparison (recommended)
   - Case-sensitive comparison
   - Normalize before comparison (trim, lowercase)

3. **Partial Matches**: Should we detect "similar" owners?
   - Exact match only (simpler)
   - Fuzzy matching (e.g., "John Smith" vs "Jon Smith")
   - Phonetic matching (Soundex, Metaphone)
   - Start with exact match only

## Validation Location

4. **Where to Implement**: Where should duplicate detection occur?
   - Option A: Repository layer (add `findByFirstNameAndLastNameAndTelephone()` method)
   - Option B: Service layer (create `OwnerService` with business logic)
   - Option C: Custom validator (like `PetValidator`, create `OwnerValidator`)
   - Option D: Controller logic in `processCreationForm()`

5. **Validation Timing**: When should we check?
   - Before save (on form submission)
   - After basic validation passes (after `@Valid` checks)
   - Both

## Update Scenario

6. **Owner Updates**: When editing an existing owner, should we:
   - Allow changes that might create duplicates (they're updating their own record)
   - Block updates that match other owners
   - Skip duplicate check on updates (only check on creation)

7. **Self-Match Handling**: If editing owner changes fields to match their own existing record:
   - Allow (they're the same owner)
   - Implement logic to exclude current owner from duplicate check

## Error Handling

8. **Error Message Key**: What i18n message key should we use?
   - `owner.duplicate` (new key)
   - `duplicate` (reuse existing generic key)
   - `owner.alreadyExists` (descriptive)

9. **Error Message Content**: What should the message say?
   - "An owner with this information already exists"
   - "Duplicate owner detected: [Name] with telephone [XXX]"
   - "This owner may already be registered. Please search before creating."
   - Include link to existing owner?

10. **Translation Requirements**: Should we translate to all 9 languages?
    - Yes, all 9 languages
    - English only initially
    - Top 3 languages (en, de, es)

## User Experience

11. **Error Display**: Where should the error appear?
    - Form-level error (top of form)
    - Field-level errors (under specific fields)
    - Flash message after redirect
    - Modal/alert dialog

12. **Next Steps for User**: After showing duplicate error, should we:
    - Show form again with error (current pattern)
    - Redirect to search page with pre-filled criteria
    - Show link to potentially duplicate owner
    - Offer to display existing owner record

## Database & Repository

13. **Query Efficiency**: For duplicate checking, should we:
    - Add database index on (firstName, lastName, telephone)
    - Use existing indices only
    - Consider performance later if needed

14. **Telephone Normalization**: Should we normalize telephone format?
    - Store and compare as-is (current: 10-digit pattern via validation)
    - Normalize: strip spaces/dashes before comparison
    - Already handled by @Pattern validation (10 digits only)

## Edge Cases

15. **Null/Empty Fields**: What if fields are null (shouldn't happen due to @NotBlank)?
    - Trust validation - all required fields present
    - Add defensive null checks
    - Not applicable

16. **Multiple Duplicates**: If somehow multiple duplicates exist:
    - Return first match as error
    - Return all matches
    - Shouldn't happen with this validation

## Testing Requirements

17. **Test Coverage**: What should we test?
    - Create duplicate (same first/last/phone) - blocked
    - Create similar but different (different phone) - allowed
    - Case variations (e.g., "John" vs "john") - blocked
    - Update existing owner - allowed (no self-match)
    - All of the above

18. **Test Types**: Which test levels?
    - Unit tests for duplicate detection logic
    - Repository tests for query method
    - Controller integration tests
    - Playwright E2E test
    - All of the above

---

**Please answer these questions to proceed with spec generation. You can provide short answers like "1A, 2A, 3A" for efficiency.**
