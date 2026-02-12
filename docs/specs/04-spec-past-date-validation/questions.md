# Clarifying Questions - Issue #8: Disallow scheduling visits in the past

## Validation Rule

1. **Date Boundary**: What is the exact rule for "past" dates?
   - Option A: Date must be >= today (visits can be scheduled for today)
   - Option B: Date must be > today (visits must be in the future, not today)
   - Option C: Date must be >= today in the clinic's timezone (if timezone matters)

2. **Time Consideration**: Since we use `LocalDate` (no time), should we:
   - Use current system date (simple)
   - Use clinic's timezone-specific date (if multi-timezone)
   - Document that "today" means server's current date

## Validation Implementation

3. **Validation Approach**: Where should we implement the validation?
   - Option A: Custom `VisitValidator` (like existing `PetValidator`)
   - Option B: Bean Validation annotation (e.g., custom `@NotPastDate`)
   - Option C: Controller-level validation in `VisitController`

4. **Existing Visits**: What should happen to visits already scheduled in the past?
   - Leave them untouched (validation applies to new visits only)
   - Add migration to update old data
   - No concern - historical data is fine

## Error Messages

5. **Validation Message Key**: What i18n message key should we use?
   - `visit.date.notPast` (new key)
   - `visit.date.invalid` (generic)
   - `typeMismatch.visitDate` (follow existing pattern)

6. **Error Message Content**: What should the error message say?
   - "Visit date cannot be in the past"
   - "Visit date must be today or later"
   - "Please select a current or future date"
   - Other?

7. **Translation Requirements**: Should we translate the error message to all 9 languages?
   - Yes, translate to all supported languages (en, de, es, ko, fa, pt, ru, tr, + 1 more)
   - Start with English only, defer translations
   - Provide only for most common languages (en, de, es)

## Form Behavior

8. **Form Field Default**: Currently the `Visit()` constructor sets `date = LocalDate.now()`. Should we:
   - Keep current behavior (default to today)
   - Change default to tomorrow (ensures future date by default)
   - Leave field empty (force user to select)

9. **UI Feedback**: Where should the error appear?
   - Next to the date field (inline validation)
   - Top of form (global error message)
   - Both locations

10. **Date Picker Constraints**: Should we also restrict the HTML date input?
    - Add `min="today"` attribute to prevent past selection in UI
    - Keep validation server-side only
    - Both (defense in depth)

## Edge Cases

11. **Timezone Edge Cases**: If a user submits at 11:59 PM and server processes at 12:01 AM:
    - Accept as valid if date matches submission time
    - Reject if date is now in the past
    - Not a concern for initial scope

12. **Backdated Visits**: Should there be any override capability?
    - No override - rule is absolute
    - Admin override option (future enhancement)
    - Not applicable initially

## Testing Requirements

13. **Test Scenarios**: Which test cases should we cover?
    - Past date (yesterday)
    - Today's date
    - Future date
    - Edge: date at boundary (today at midnight)
    - All of the above

14. **Test Levels**: What test coverage is needed?
    - Unit tests for validator/annotation
    - Controller integration tests
    - Playwright E2E test
    - All of the above

---

**Please answer these questions to proceed with spec generation. You can provide short answers like "1A, 2A, 3A" for efficiency.**
