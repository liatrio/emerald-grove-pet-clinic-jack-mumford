# 07 Questions Round 1 - Upcoming Visits Page

Please answer each question below (select one or more options, or add your own notes). Feel free to add additional context under any question.

## 1. Page Purpose and Scope

What should the "Upcoming Visits" page display?

- [x] (A) All future visits (visits with date >= today) across all pets in the clinic
- [ ] (B) Only visits within a specific time range (e.g., next 7 days, next 30 days)
- [ ] (C) Both upcoming AND past visits (a complete visit history for the clinic)
- [ ] (D) Only today's visits (daily schedule view)
- [ ] (E) Other (describe)

## 2. Visit Information Display

What information should be shown for each visit in the list?

- [ ] (A) Minimal: Visit date, pet name, owner name
- [x] (B) Standard: Visit date, pet name, owner name, visit description
- [ ] (C) Detailed: Visit date, pet name, owner name, visit description, pet type, owner contact info
- [ ] (D) Comprehensive: All available visit, pet, and owner information
- [ ] (E) Other (describe)

## 3. Sorting and Ordering

How should visits be sorted on the page?

- [x] (A) By date (oldest/nearest first) - chronological order
- [ ] (B) By date (newest/farthest first) - reverse chronological
- [ ] (C) By owner name alphabetically
- [ ] (D) User-selectable sorting (allow users to choose sort order)
- [ ] (E) Other (describe)

## 4. Filtering Capabilities

Should users be able to filter the upcoming visits?

- [ ] (A) No filtering - show all upcoming visits
- [ ] (B) Filter by date range (e.g., "Show visits in next 7 days")
- [ ] (C) Filter by pet type (e.g., "Show only dog visits")
- [ ] (D) Filter by owner name (search/filter by owner)
- [x] (E) Multiple filters (date range + pet type + owner name)
- [ ] (F) Other (describe)

## 5. Pagination

How should large numbers of visits be handled?

- [x] (A) Show all visits on one page (no pagination)
- [ ] (B) Paginate with 10 visits per page
- [ ] (C) Paginate with 25 visits per page
- [ ] (D) Paginate with 50 visits per page
- [ ] (E) User-selectable page size
- [ ] (F) Other (describe)

## 6. Navigation Integration

Where should the link to this page appear?

- [x] (A) In the main navigation bar (top menu) alongside "Find Owners" and "Veterinarians"
- [ ] (B) On the homepage/welcome page only
- [ ] (C) On the owner details page
- [ ] (D) Multiple locations (A + B, or A + C, etc.)
- [ ] (E) Other (describe)

## 7. Visit Actions

What actions should be available from the upcoming visits page?

- [x] (A) View only - no actions, just display information
- [ ] (B) Click to view owner details page (navigate to owner/pet)
- [ ] (C) Edit visit details directly from the list
- [ ] (D) Cancel/delete visits from the list
- [ ] (E) Multiple actions (B + C, B + D, etc.)
- [ ] (F) Other (describe)

## 8. Empty State

What should be displayed when there are no upcoming visits?

- [x] (A) Simple message: "No upcoming visits scheduled"
- [ ] (B) Helpful message with call-to-action: "No upcoming visits. Schedule a visit for a pet."
- [ ] (C) Show instructions on how to schedule visits
- [ ] (D) Display recent past visits instead
- [ ] (E) Other (describe)

## 9. Internationalization

Should this page support multiple languages?

- [ ] (A) Yes - add translations for all 9 supported languages (EN, DE, ES, KO, FA, PT, RU, TR, ZH)
- [x] (B) Yes - but only for English initially, others can be added later
- [ ] (C) No - English only
- [ ] (D) Other (describe)

## 10. Mobile Responsiveness

How should this page behave on mobile devices?

- [x] (A) Fully responsive - adapt layout for mobile screens using Bootstrap responsive classes
- [ ] (B) Desktop-first - mobile support is nice-to-have but not required
- [ ] (C) Mobile-optimized - prioritize mobile experience over desktop
- [ ] (D) Other (describe)

## 11. Performance Considerations

Are there any specific performance requirements?

- [x] (A) No specific requirements - standard page load is fine
- [ ] (B) Must load quickly (<1 second) for large numbers of visits (1000+)
- [ ] (C) Should use caching to improve performance
- [ ] (D) Should use lazy loading or infinite scroll for better UX
- [ ] (E) Other (describe)

## 12. Success Criteria

How will we know this feature is successful?

- [x] (A) Page displays upcoming visits correctly with accurate information
- [ ] (B) Clinic staff can easily see their schedule for the day/week
- [ ] (C) Page loads quickly and is easy to navigate
- [ ] (D) Users can find specific visits quickly using filters/search
- [ ] (E) Other (describe)
