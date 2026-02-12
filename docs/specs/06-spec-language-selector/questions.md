# Clarifying Questions - Issue #3: Add language selector to header

## Language Selection Mechanism

1. **UI Component Type**: What kind of language selector should we use?
   - Option A: Dropdown/Select menu with language names
   - Option B: Dropdown with flag icons + language names
   - Option C: Simple text links (EN | ES | DE)
   - Option D: Button with dropdown (Bootstrap dropdown component)

2. **Language Display Format**: How should languages be shown?
   - Option A: Native names (e.g., "English", "Deutsch", "Español")
   - Option B: ISO codes (e.g., "EN", "DE", "ES")
   - Option C: Both (e.g., "EN - English")
   - Option D: Native names with flags

3. **Initial Language Set**: How many languages should we support initially?
   - Option A: All 9 existing languages (en, de, es, ko, fa, pt, ru, tr, + 1 more)
   - Option B: Small set (EN, DE, ES) as suggested in issue
   - Option C: English + 2 others (which ones?)

## Placement & Design

4. **Selector Placement**: Where in the header should the language selector appear?
   - Option A: Far right of navbar (after menu items)
   - Option B: Next to the logo (left side)
   - Option C: In the navbar collapse menu (with other nav items)
   - Option D: Above/below the navbar

5. **Mobile Behavior**: How should it work on mobile devices?
   - Option A: Collapse into hamburger menu with other nav items
   - Option B: Stay visible outside hamburger menu
   - Option C: Different UI pattern for mobile (smaller dropdown)

6. **Visual Design**: Should we match existing Liatrio branding?
   - Yes, follow existing navbar styling in `layout.html`
   - Add custom styling consistent with branding
   - Minimal styling (default Bootstrap)

## Locale Handling

7. **Locale Switching Mechanism**: How should we handle locale changes?
   - Option A: Query parameter `?lang=xx` (as mentioned in issue)
   - Option B: Cookie-based persistence (`LocaleChangeInterceptor`)
   - Option C: Session-based storage
   - Option D: Combination (query param + cookie for persistence)

8. **Persistence Scope**: How long should language selection persist?
   - Session only (until browser close)
   - Cookie with expiration (e.g., 30 days)
   - LocalStorage (client-side persistence)
   - No persistence (always default to browser/system language)

9. **Default Language**: What should the default language be?
   - Browser's Accept-Language header
   - English (en) always
   - System property configured default
   - User preference (if login system exists)

## URL Behavior

10. **Current Page Reload**: When changing language, should we:
    - Reload current page with `?lang=xx` parameter
    - Redirect to home page in new language
    - Use AJAX to update without reload (complex)

11. **URL Parameter Propagation**: Should language parameter persist across navigation?
    - Yes, append `?lang=xx` to all links automatically
    - Use session/cookie instead (cleaner URLs)
    - Hybrid: use param initially, then cookie

## Spring Configuration

12. **LocaleResolver Configuration**: Which Spring locale resolver should we use?
    - `SessionLocaleResolver` (session-based)
    - `CookieLocaleResolver` (cookie-based)
    - `AcceptHeaderLocaleResolver` (browser default)
    - Combination with fallback chain

13. **LocaleChangeInterceptor**: Should we add it?
    - Yes, to support `?lang=xx` parameter switching
    - No, custom implementation
    - Already exists? (need to check)

## Visual Feedback

14. **Active Language Indicator**: How should we show the current language?
    - Highlight/bold current language in dropdown
    - Show current language code in selector button
    - Both
    - Not needed (user can see content is translated)

15. **Icon/Flag Usage**: Should we use flag icons?
    - Yes, use flags with Font Awesome or emoji
    - No, text only (more accessible)
    - Optional flag icons with accessible text

## Internationalization

16. **Selector Label**: Should the language selector itself have a label?
    - No label, just the dropdown
    - Internationalized label (e.g., "Language" / "Idioma" / "Sprache")
    - Icon label (globe icon)
    - Aria-label for accessibility only

17. **Missing Translations**: What happens if a translation is missing?
    - Fall back to English
    - Show key (current Spring behavior)
    - Show error message

## Testing Requirements

18. **Test Coverage**: What should we test?
    - Switching between languages updates UI text
    - Language persists across navigation
    - All supported languages render correctly
    - Default language behavior
    - All of the above

19. **Playwright E2E Test**: What should the E2E test verify?
    - Switch language, assert translated text appears
    - Test multiple languages (at least 2)
    - Verify persistence after navigation
    - All of the above

20. **Screenshot Requirements**: What screenshots should we provide?
    - Same page in 2 different languages (as mentioned in issue)
    - Language selector in expanded state
    - Mobile view of selector
    - All of the above

## Accessibility

21. **Accessibility Requirements**: Should we ensure:
    - ARIA labels for screen readers
    - Keyboard navigation support
    - Sufficient color contrast
    - All of the above (required for production)

---

**Please answer these questions to proceed with spec generation. You can provide short answers like "1A, 2A, 3A" for efficiency.**
