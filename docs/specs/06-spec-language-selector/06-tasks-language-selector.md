# Task Breakdown: Language Selector Feature (Issue #3)

**Feature ID**: Issue #3
**Spec Document**: `docs/specs/06-spec-language-selector/SPEC.md`
**Status**: Ready for Implementation
**Created**: 2026-02-12
**TDD Methodology**: Strict RED-GREEN-REFACTOR

---

## Overview

This document provides a detailed task breakdown for implementing the Language Selector feature. The feature adds a dropdown menu to the navbar allowing users to switch between 9 supported languages with cookie-based persistence.

**Key Components**:
- Spring LocaleConfiguration with CookieLocaleResolver
- LocaleChangeInterceptor for `?lang=xx` parameter handling
- Thymeleaf UI dropdown component with 9 languages
- Accessibility features (WCAG 2.1 AA compliance)
- End-to-end Playwright tests

---

## Relevant Files

### Files to Create

| File Path | Purpose |
|-----------|---------|
| `src/main/java/org/springframework/samples/petclinic/system/LocaleConfiguration.java` | Spring configuration for locale resolution and interceptor |
| `src/test/java/org/springframework/samples/petclinic/system/LocaleConfigurationTests.java` | Unit tests for LocaleConfiguration |
| `src/test/java/org/springframework/samples/petclinic/system/LocaleSwitchingTests.java` | Integration tests for locale switching behavior |
| `e2e-tests/tests/language-selector.spec.ts` | End-to-end Playwright tests for language selector |

### Files to Modify

| File Path | Changes Required |
|-----------|------------------|
| `src/main/resources/templates/fragments/layout.html` | Add language selector dropdown to navbar, update navigation links with lang parameter |
| `src/main/resources/static/resources/css/petclinic.scss` | Add custom styles for language selector dropdown |
| `src/test/java/org/springframework/samples/petclinic/system/LayoutTests.java` | Add tests for language selector UI presence and behavior |

---

## Parent Tasks (Demoable Units)

### Task 1: Spring Locale Configuration
**Epic**: Backend Infrastructure
**Estimated Effort**: 2 hours
**Dependencies**: None

**Description**: Implement Spring LocaleConfiguration with CookieLocaleResolver and LocaleChangeInterceptor to enable language switching via `?lang=xx` query parameter with cookie persistence.

**Proof of Completion**:
- [ ] LocaleConfiguration.java created with @Configuration annotation
- [ ] LocaleResolver bean configured using CookieLocaleResolver
- [ ] LocaleChangeInterceptor bean registered with "lang" parameter
- [ ] Cookie name set to "petclinic-locale" with 30-day expiration
- [ ] All unit tests passing (100% coverage of LocaleConfiguration)
- [ ] Integration tests confirm locale switching via query parameter
- [ ] Cookie persistence verified across requests

**Acceptance Criteria**:
- Accessing `/?lang=de` switches locale to German
- Cookie `petclinic-locale=de` is set in response
- Subsequent requests without `?lang=xx` use cookie value
- Default locale falls back to Accept-Language header then English

---

### Task 2: Language Selector UI Component
**Epic**: Frontend UI
**Estimated Effort**: 3 hours
**Dependencies**: Task 1 (Spring Configuration must be working)

**Description**: Add Bootstrap dropdown component to navbar with all 9 supported languages, flag emojis, and active language highlighting.

**Proof of Completion**:
- [ ] Dropdown component added to layout.html between logo and nav items
- [ ] All 9 languages listed with native names (English, Deutsch, Español, etc.)
- [ ] Globe icon (Font Awesome `fa-globe`) used for dropdown button
- [ ] Flag emojis added as decorative elements (aria-hidden)
- [ ] Active language highlighted with `fw-bold` class
- [ ] ARIA labels present on dropdown button
- [ ] Unit tests verify dropdown HTML structure
- [ ] Screenshot captured showing dropdown in open state

**Acceptance Criteria**:
- Language selector visible on all pages
- Dropdown displays all 9 languages with correct native names
- Clicking a language navigates to current URL + `?lang=xx`
- Active language is bold/highlighted in dropdown menu
- Globe icon is visible and accessible

---

### Task 3: CSS Styling and Accessibility
**Epic**: Frontend UI
**Estimated Effort**: 2 hours
**Dependencies**: Task 2 (UI component must exist)

**Description**: Apply custom CSS styling to match Liatrio branding, ensure WCAG 2.1 AA compliance with proper color contrast, focus indicators, and keyboard navigation.

**Proof of Completion**:
- [ ] Custom CSS classes added to petclinic.scss
- [ ] Dropdown button styled to match navbar theme
- [ ] Hover and focus states meet 4.5:1 contrast ratio
- [ ] Visible focus outline on all interactive elements
- [ ] Keyboard navigation tested (Tab, Arrow keys)
- [ ] Screen reader testing completed (announces "Language selector")
- [ ] Manual accessibility audit passed
- [ ] Screenshot showing focus indicators

**Acceptance Criteria**:
- Language selector matches existing navbar dark theme
- Hover states provide visual feedback
- Focus indicators are clearly visible
- Keyboard-only navigation works without mouse
- Screen readers announce dropdown purpose and current selection
- Color contrast meets WCAG 2.1 AA standards

---

### Task 4: URL Parameter Propagation
**Epic**: Backend Integration
**Estimated Effort**: 2 hours
**Dependencies**: Task 1 (Locale configuration working)

**Description**: Update all navigation links and forms to include `lang` parameter, ensuring language persists across page navigation.

**Proof of Completion**:
- [ ] All `th:href` links in layout.html updated with `(lang=${#locale.language})`
- [ ] Navigation menu links preserve language parameter
- [ ] Form cancel/back buttons preserve language parameter
- [ ] Owner, Pet, Vet detail pages updated
- [ ] Integration tests verify parameter persistence
- [ ] Manual testing confirms navigation preserves language

**Acceptance Criteria**:
- Navigating from home to owners page preserves `?lang=xx`
- Clicking "Find Owners" maintains selected language
- Form submission doesn't lose language parameter
- Back/cancel buttons maintain language context
- URL bar consistently shows `?lang=xx` after navigation

---

### Task 5: LocalStorage JavaScript Enhancement
**Epic**: Frontend Enhancement
**Estimated Effort**: 1.5 hours
**Dependencies**: Task 2 (UI component exists)

**Description**: Add optional JavaScript to sync locale to LocalStorage for client-side persistence and improved user experience.

**Proof of Completion**:
- [ ] Inline JavaScript added to layout.html
- [ ] LocalStorage key `petclinic-locale` set on page load
- [ ] Script reads URL parameter and updates LocalStorage
- [ ] Fallback logic redirects if LocalStorage differs from cookie
- [ ] Manual testing confirms LocalStorage sync
- [ ] Script works without breaking non-JS users

**Acceptance Criteria**:
- Locale stored in LocalStorage after selection
- LocalStorage persists after browser restart
- Feature degrades gracefully without JavaScript
- No console errors in browser devtools
- Works across all supported browsers

---

### Task 6: End-to-End Tests and Documentation
**Epic**: Quality Assurance
**Estimated Effort**: 3 hours
**Dependencies**: Tasks 1-5 (All features implemented)

**Description**: Create comprehensive Playwright E2E tests validating language switching, persistence, and accessibility. Generate proof screenshots for PR.

**Proof of Completion**:
- [ ] Playwright test file created: `language-selector.spec.ts`
- [ ] Test: Switch language to German (verify translated text)
- [ ] Test: Switch language to Spanish (verify persistence)
- [ ] Test: Navigate across pages with language preserved
- [ ] Test: Keyboard navigation (Tab, Arrow keys)
- [ ] Test: Screenshot comparison (English vs Spanish)
- [ ] All E2E tests passing in CI/CD pipeline
- [ ] Screenshots committed to test-results folder
- [ ] README updated with language selector usage

**Acceptance Criteria**:
- E2E tests cover all critical user journeys
- Tests run successfully in headless mode
- Screenshots demonstrate feature working
- Tests fail appropriately if feature breaks
- Test output includes HTML report with traces
- CI/CD pipeline includes E2E test execution

---

## Detailed Sub-Tasks

### Task 1: Spring Locale Configuration

#### 1.1: TDD - Create Failing Test for LocaleResolver Bean
**Phase**: RED
**Estimated Effort**: 15 minutes

**Steps**:
1. Create test class `LocaleConfigurationTests.java` in `src/test/java/.../system/`
2. Add `@SpringBootTest` annotation
3. Autowire `ApplicationContext`
4. Write test `shouldHaveLocaleResolverBean()`:
   - Get bean of type `LocaleResolver.class`
   - Assert bean is not null
   - Assert bean is instance of `CookieLocaleResolver`
5. Run test - **EXPECTED: FAIL** (bean doesn't exist yet)

**Acceptance Criteria**:
- [ ] Test file created with proper package structure
- [ ] Test fails with message about missing bean
- [ ] Test follows Arrange-Act-Assert pattern

---

#### 1.2: TDD - Implement LocaleConfiguration Class
**Phase**: GREEN
**Estimated Effort**: 30 minutes

**Steps**:
1. Create `LocaleConfiguration.java` in `src/main/java/.../system/`
2. Add `@Configuration` annotation
3. Implement `LocaleResolver` bean method:
   - Return `CookieLocaleResolver` instance
   - Set default locale to `Locale.ENGLISH`
   - Set cookie name to "petclinic-locale"
   - Set cookie max age to 2592000 seconds (30 days)
4. Run test - **EXPECTED: PASS**

**Acceptance Criteria**:
- [ ] LocaleConfiguration class created
- [ ] Bean method properly annotated with `@Bean`
- [ ] CookieLocaleResolver configured correctly
- [ ] Test `shouldHaveLocaleResolverBean()` passes

---

#### 1.3: TDD - Add Test for LocaleChangeInterceptor
**Phase**: RED
**Estimated Effort**: 15 minutes

**Steps**:
1. Add test `shouldHaveLocaleChangeInterceptor()` to `LocaleConfigurationTests`
2. Autowire `LocaleChangeInterceptor` bean
3. Assert bean is not null
4. Assert parameter name is "lang"
5. Run test - **EXPECTED: FAIL** (bean doesn't exist yet)

**Acceptance Criteria**:
- [ ] Test added to existing test class
- [ ] Test checks interceptor configuration
- [ ] Test fails appropriately

---

#### 1.4: TDD - Implement LocaleChangeInterceptor Bean
**Phase**: GREEN
**Estimated Effort**: 20 minutes

**Steps**:
1. Add `LocaleChangeInterceptor` bean method to `LocaleConfiguration`
2. Create interceptor instance
3. Set parameter name to "lang"
4. Return interceptor
5. Implement `WebMvcConfigurer` interface
6. Override `addInterceptors(InterceptorRegistry)` method
7. Register `localeChangeInterceptor()` bean
8. Run test - **EXPECTED: PASS**

**Acceptance Criteria**:
- [ ] Interceptor bean created and configured
- [ ] Interceptor registered in Spring MVC
- [ ] Test `shouldHaveLocaleChangeInterceptor()` passes
- [ ] No compilation errors

---

#### 1.5: TDD - Integration Test for Locale Switching via Query Param
**Phase**: RED
**Estimated Effort**: 30 minutes

**Steps**:
1. Create `LocaleSwitchingTests.java` in `src/test/java/.../system/`
2. Add `@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)`
3. Autowire `TestRestTemplate`
4. Write test `shouldSwitchLocaleViaQueryParameter()`:
   - Perform GET request to `/?lang=de`
   - Assert response status is OK
   - Assert response body contains German text (e.g., "willkommen")
5. Run test - **EXPECTED: FAIL** (locale not switching or German translations not visible)

**Acceptance Criteria**:
- [ ] Integration test class created
- [ ] Test uses TestRestTemplate for HTTP requests
- [ ] Test verifies German translation appears
- [ ] Test fails with appropriate error message

---

#### 1.6: TDD - Verify Configuration Enables Locale Switching
**Phase**: GREEN
**Estimated Effort**: 15 minutes

**Steps**:
1. Run test `shouldSwitchLocaleViaQueryParameter()`
2. If test fails, verify:
   - LocaleConfiguration beans are loaded
   - Interceptor is registered
   - Thymeleaf templates use `#{...}` message keys
3. Check `messages_de.properties` has "welcome" key
4. Run test - **EXPECTED: PASS**

**Acceptance Criteria**:
- [ ] Test passes without code changes (configuration sufficient)
- [ ] German translations appear in response
- [ ] Locale switching mechanism working

---

#### 1.7: TDD - Test Cookie Persistence After Locale Switch
**Phase**: RED
**Estimated Effort**: 20 minutes

**Steps**:
1. Add test `shouldSetCookieAfterLocaleSwitch()` to `LocaleSwitchingTests`
2. Perform GET request to `/?lang=es`
3. Extract `Set-Cookie` header from response
4. Assert header contains `petclinic-locale=es`
5. Run test - **EXPECTED: PASS** (CookieLocaleResolver automatically sets cookie)

**Acceptance Criteria**:
- [ ] Test verifies cookie is set in response
- [ ] Cookie name matches configuration
- [ ] Cookie value matches selected locale

---

#### 1.8: TDD - Test Locale Persists Across Requests
**Phase**: RED
**Estimated Effort**: 30 minutes

**Steps**:
1. Add test `shouldPersistLocaleAcrossRequests()` to `LocaleSwitchingTests`
2. First request: GET `/?lang=de`, extract cookie
3. Second request: GET `/` with cookie header (no `lang` param)
4. Assert response contains German text
5. Run test - **EXPECTED: PASS**

**Acceptance Criteria**:
- [ ] Test verifies cookie persistence
- [ ] Second request honors cookie value
- [ ] German text appears without query parameter

---

#### 1.9: TDD - Refactor and Add Edge Case Tests
**Phase**: REFACTOR
**Estimated Effort**: 30 minutes

**Steps**:
1. Add test for unsupported language code: `shouldFallbackToEnglishForUnsupportedLanguage()`
   - Request `/?lang=xyz`
   - Assert English text appears
2. Add test for default Accept-Language header behavior
3. Refactor common test setup into `@BeforeEach` method
4. Extract test data factories for reusable fixtures
5. Run all tests - **EXPECTED: ALL PASS**

**Acceptance Criteria**:
- [ ] Edge cases covered (invalid locale, missing translations)
- [ ] Tests are DRY (no duplication)
- [ ] Test coverage for LocaleConfiguration is 100%
- [ ] All integration tests pass

---

### Task 2: Language Selector UI Component

#### 2.1: TDD - Test Language Selector Present in HTML
**Phase**: RED
**Estimated Effort**: 20 minutes

**Steps**:
1. Locate or create `LayoutTests.java` in `src/test/java/.../system/`
2. Add `@WebMvcTest(WelcomeController.class)` if creating new file
3. Autowire `MockMvc`
4. Write test `shouldContainLanguageSelectorInNavbar()`:
   - Perform GET request to `/`
   - Assert status is OK
   - Assert HTML contains "languageDropdown" (dropdown ID)
   - Assert HTML contains "fa-globe" (icon class)
5. Run test - **EXPECTED: FAIL** (dropdown not in template)

**Acceptance Criteria**:
- [ ] Test verifies presence of language selector
- [ ] Test checks for specific HTML element IDs
- [ ] Test fails with clear error message

---

#### 2.2: TDD - Add Language Selector Dropdown to Template
**Phase**: GREEN
**Estimated Effort**: 45 minutes

**Steps**:
1. Open `src/main/resources/templates/fragments/layout.html`
2. Locate navbar section between logo and nav items
3. Add language selector dropdown component:
   ```html
   <div class="language-selector">
       <div class="dropdown">
           <button class="btn btn-sm btn-outline-light dropdown-toggle" type="button"
                   id="languageDropdown" data-bs-toggle="dropdown" aria-expanded="false"
                   aria-label="Language selector">
               <i class="fa fa-globe" aria-hidden="true"></i>
           </button>
           <ul class="dropdown-menu" aria-labelledby="languageDropdown">
               <!-- Languages will be added in next step -->
           </ul>
       </div>
   </div>
   ```
4. Run test - **EXPECTED: PASS**

**Acceptance Criteria**:
- [ ] Dropdown button added with correct ID
- [ ] Globe icon present
- [ ] ARIA label added for accessibility
- [ ] Test `shouldContainLanguageSelectorInNavbar()` passes

---

#### 2.3: TDD - Test All 9 Languages in Dropdown
**Phase**: RED
**Estimated Effort**: 30 minutes

**Steps**:
1. Add test `shouldIncludeAllNineLanguagesInDropdown()` to `LayoutTests`
2. Perform GET request to `/`
3. Assert HTML contains all 9 language codes and native names:
   - `lang='en'` and "English"
   - `lang='de'` and "Deutsch"
   - `lang='es'` and "Español"
   - `lang='ko'` and "한국어"
   - `lang='fa'` and "فارسی"
   - `lang='pt'` and "Português"
   - `lang='ru'` and "Русский"
   - `lang='tr'` and "Türkçe"
   - `lang='zh'` and "中文"
4. Run test - **EXPECTED: FAIL** (languages not added yet)

**Acceptance Criteria**:
- [ ] Test verifies all 9 languages present
- [ ] Test checks both language codes and native names
- [ ] Test fails appropriately

---

#### 2.4: TDD - Add All 9 Language Options to Dropdown
**Phase**: GREEN
**Estimated Effort**: 30 minutes

**Steps**:
1. Open `layout.html` and locate dropdown menu `<ul>` element
2. Add all 9 language items:
   ```html
   <li><a class="dropdown-item" th:href="@{''(lang='en')}">
       <span aria-hidden="true">🇺🇸</span> English
   </a></li>
   <li><a class="dropdown-item" th:href="@{''(lang='de')}">
       <span aria-hidden="true">🇩🇪</span> Deutsch
   </a></li>
   <!-- ... repeat for all 9 languages ... -->
   ```
3. Ensure `th:href="@{''(lang='xx')}"` preserves current URL with lang parameter
4. Add flag emojis with `aria-hidden="true"`
5. Run test - **EXPECTED: PASS**

**Acceptance Criteria**:
- [ ] All 9 languages added to dropdown menu
- [ ] Each language links to current URL + `?lang=xx`
- [ ] Flag emojis added as decorative elements
- [ ] Test `shouldIncludeAllNineLanguagesInDropdown()` passes

---

#### 2.5: TDD - Test Active Language Highlighting
**Phase**: RED
**Estimated Effort**: 20 minutes

**Steps**:
1. Add test `shouldHighlightCurrentLanguageInDropdown()` to `LayoutTests`
2. Perform GET request to `/?lang=de`
3. Assert response contains `lang='de'`
4. Assert HTML matches regex pattern showing active class on German option
5. Run test - **EXPECTED: FAIL** (active class not applied)

**Acceptance Criteria**:
- [ ] Test verifies active language has visual indicator
- [ ] Test uses regex or CSS selector matching
- [ ] Test fails with clear error

---

#### 2.6: TDD - Add Active Class to Current Language
**Phase**: GREEN
**Estimated Effort**: 20 minutes

**Steps**:
1. Update each language option in dropdown with conditional class:
   ```html
   <a class="dropdown-item" th:href="@{''(lang='de')}"
      th:classappend="${#locale.language == 'de'} ? 'active fw-bold' : ''">
       <span aria-hidden="true">🇩🇪</span> Deutsch
   </a>
   ```
2. Repeat for all 9 languages
3. Run test - **EXPECTED: PASS**

**Acceptance Criteria**:
- [ ] Active class applied based on current locale
- [ ] Bold styling applied to current language
- [ ] Test `shouldHighlightCurrentLanguageInDropdown()` passes

---

#### 2.7: TDD - Refactor Template for Maintainability
**Phase**: REFACTOR
**Estimated Effort**: 30 minutes

**Steps**:
1. Review dropdown code for duplication
2. Consider extracting language list to model attribute (optional)
3. Ensure consistent formatting and indentation
4. Add HTML comments for clarity
5. Run all tests - **EXPECTED: ALL PASS**

**Acceptance Criteria**:
- [ ] Template code is clean and readable
- [ ] No unnecessary duplication
- [ ] All existing tests still pass
- [ ] Visual verification: dropdown renders correctly

---

### Task 3: CSS Styling and Accessibility

#### 3.1: TDD - Create CSS Classes for Language Selector
**Phase**: GREEN (Styling doesn't require failing test first)
**Estimated Effort**: 30 minutes

**Steps**:
1. Open `src/main/resources/static/resources/css/petclinic.scss`
2. Add custom CSS classes:
   ```scss
   .language-selector {
       margin-right: 1rem;
   }

   .language-selector .dropdown-toggle {
       border-color: rgba(255, 255, 255, 0.5);
   }

   .language-selector .dropdown-toggle:hover {
       border-color: rgba(255, 255, 255, 0.8);
       background-color: rgba(255, 255, 255, 0.1);
   }

   .language-selector .dropdown-item.active {
       background-color: var(--bs-primary);
       color: white;
   }

   .language-selector .dropdown-item:focus {
       outline: 2px solid var(--bs-primary);
       outline-offset: -2px;
   }
   ```
3. Rebuild CSS: `./mvnw package -P css`
4. Verify styles in browser

**Acceptance Criteria**:
- [ ] CSS classes added to petclinic.scss
- [ ] Styles compiled successfully
- [ ] Dropdown matches navbar theme
- [ ] Hover states provide visual feedback

---

#### 3.2: Manual Test - Verify Color Contrast
**Phase**: Manual Verification
**Estimated Effort**: 20 minutes

**Steps**:
1. Start application: `./mvnw spring-boot:run`
2. Open browser to `http://localhost:8080`
3. Use browser devtools to inspect language selector
4. Use contrast checker tool (e.g., WebAIM Contrast Checker)
5. Verify text-to-background contrast is at least 4.5:1
6. Check focus indicators are visible
7. Document results

**Acceptance Criteria**:
- [ ] Dropdown text meets WCAG 2.1 AA contrast ratio (4.5:1)
- [ ] Hover state text meets contrast requirements
- [ ] Active item text meets contrast requirements
- [ ] Focus outline is clearly visible

---

#### 3.3: Manual Test - Keyboard Navigation
**Phase**: Manual Verification
**Estimated Effort**: 20 minutes

**Steps**:
1. Start application and open in browser
2. Use Tab key to navigate to language selector
3. Press Enter or Space to open dropdown
4. Use Arrow keys to navigate through languages
5. Press Enter to select language
6. Verify page reloads with selected language
7. Test Escape key to close dropdown
8. Document behavior

**Acceptance Criteria**:
- [ ] Tab key reaches language selector button
- [ ] Enter/Space opens dropdown menu
- [ ] Arrow keys navigate dropdown items
- [ ] Enter selects language and reloads page
- [ ] Escape closes dropdown
- [ ] Focus indicators visible throughout

---

#### 3.4: Manual Test - Screen Reader Accessibility
**Phase**: Manual Verification
**Estimated Effort**: 30 minutes

**Steps**:
1. Enable screen reader (NVDA on Windows, VoiceOver on Mac)
2. Navigate to application homepage
3. Tab to language selector
4. Verify screen reader announces: "Language selector, button"
5. Open dropdown and verify language options are announced
6. Verify current language is indicated
7. Document screen reader output
8. Take notes for any improvements

**Acceptance Criteria**:
- [ ] Button role and label announced correctly
- [ ] Dropdown items announced with language names
- [ ] Current selection indicated to screen reader
- [ ] ARIA labels working as expected
- [ ] No confusing or redundant announcements

---

#### 3.5: Add ARIA Labels and Semantic HTML
**Phase**: REFACTOR
**Estimated Effort**: 20 minutes

**Steps**:
1. Review dropdown HTML in layout.html
2. Ensure `aria-label="Language selector"` on button
3. Ensure `aria-labelledby="languageDropdown"` on dropdown menu
4. Add `aria-hidden="true"` to flag emojis
5. Consider adding `role="navigation"` to wrapper (optional)
6. Re-test with screen reader
7. Run existing tests - **EXPECTED: ALL PASS**

**Acceptance Criteria**:
- [ ] All interactive elements have ARIA labels
- [ ] Decorative elements marked with aria-hidden
- [ ] Semantic HTML structure maintained
- [ ] Screen reader announces elements correctly

---

#### 3.6: Take Screenshots for Proof
**Phase**: Documentation
**Estimated Effort**: 15 minutes

**Steps**:
1. Start application
2. Open browser to homepage
3. Take screenshot of navbar with language selector closed
4. Click language selector to open dropdown
5. Take screenshot of open dropdown menu
6. Hover over a language option
7. Take screenshot showing focus indicator
8. Save screenshots to `docs/specs/06-spec-language-selector/screenshots/`

**Acceptance Criteria**:
- [ ] Screenshots show language selector in closed state
- [ ] Screenshots show dropdown menu open with all languages
- [ ] Screenshots demonstrate focus indicators
- [ ] Images saved in appropriate directory

---

### Task 4: URL Parameter Propagation

#### 4.1: Identify All Navigation Links Requiring Updates
**Phase**: Analysis
**Estimated Effort**: 30 minutes

**Steps**:
1. Search codebase for all `th:href` attributes
2. Create checklist of files with navigation links:
   - `layout.html` (navbar menu)
   - Owner detail pages
   - Pet detail pages
   - Vet directory pages
   - Form cancel/back buttons
3. Document current link structure
4. Plan update strategy

**Acceptance Criteria**:
- [ ] Complete list of files requiring updates
- [ ] Current link patterns documented
- [ ] Update strategy defined

---

#### 4.2: TDD - Test Navigation Links Include Lang Parameter
**Phase**: RED
**Estimated Effort**: 30 minutes

**Steps**:
1. Create test `shouldIncludeLangParameterInNavigationLinks()` in `LayoutTests`
2. Perform GET request to `/?lang=de`
3. Assert navigation links contain `lang=de`:
   - Home link
   - Find Owners link
   - Veterinarians link
   - Error link
4. Run test - **EXPECTED: FAIL** (links don't include parameter yet)

**Acceptance Criteria**:
- [ ] Test verifies all nav links have lang parameter
- [ ] Test checks multiple navigation items
- [ ] Test fails with appropriate message

---

#### 4.3: TDD - Update Navigation Menu Links in Layout
**Phase**: GREEN
**Estimated Effort**: 30 minutes

**Steps**:
1. Open `layout.html` and locate navbar navigation section
2. Update all navigation links to include lang parameter:
   ```html
   <!-- Before -->
   <a th:href="@{/}">Home</a>

   <!-- After -->
   <a th:href="@{/(lang=${#locale.language})}">Home</a>
   ```
3. Update all navbar links:
   - Home: `@{/(lang=${#locale.language})}`
   - Find Owners: `@{/owners/find(lang=${#locale.language})}`
   - Veterinarians: `@{/vets.html(lang=${#locale.language})}`
   - Error: `@{/oups(lang=${#locale.language})}`
4. Run test - **EXPECTED: PASS**

**Acceptance Criteria**:
- [ ] All navbar links updated
- [ ] Links preserve current locale on navigation
- [ ] Test `shouldIncludeLangParameterInNavigationLinks()` passes

---

#### 4.4: Update Owner Detail Page Links
**Phase**: GREEN
**Estimated Effort**: 30 minutes

**Steps**:
1. Open owner-related templates (e.g., `ownerDetails.html`)
2. Update all `th:href` links to include lang parameter:
   - Edit owner link
   - Add pet link
   - Back to owners list link
3. Test manually by navigating through owner flows
4. Verify lang parameter persists

**Acceptance Criteria**:
- [ ] All owner page links updated
- [ ] Manual test confirms persistence
- [ ] No broken links

---

#### 4.5: Update Pet and Visit Page Links
**Phase**: GREEN
**Estimated Effort**: 30 minutes

**Steps**:
1. Open pet-related templates (e.g., `createOrUpdatePetForm.html`)
2. Update form action URLs and cancel buttons
3. Update visit form links
4. Test form submission with lang parameter
5. Verify cancel buttons preserve language

**Acceptance Criteria**:
- [ ] Pet form links updated
- [ ] Visit form links updated
- [ ] Form submission preserves language
- [ ] Cancel buttons work correctly

---

#### 4.6: Update Vet Directory Page Links
**Phase**: GREEN
**Estimated Effort**: 20 minutes

**Steps**:
1. Open vet-related templates (e.g., `vetList.html`)
2. Update any navigation links on vet pages
3. Test vet directory navigation with different languages
4. Verify lang parameter persists

**Acceptance Criteria**:
- [ ] Vet page links updated
- [ ] Manual test confirms persistence
- [ ] No regressions in vet functionality

---

#### 4.7: TDD - Integration Test for Parameter Persistence
**Phase**: RED
**Estimated Effort**: 30 minutes

**Steps**:
1. Add test `shouldPersistLangParameterAcrossNavigation()` to `LocaleSwitchingTests`
2. Simulate navigation flow:
   - Start at `/?lang=es`
   - Click "Find Owners" link
   - Verify URL contains `lang=es`
   - Verify Spanish text appears
3. Run test - **EXPECTED: PASS** (if all links updated correctly)

**Acceptance Criteria**:
- [ ] Integration test verifies end-to-end persistence
- [ ] Test simulates realistic user navigation
- [ ] Test passes confirming parameter propagation

---

#### 4.8: Refactor and Document Link Update Pattern
**Phase**: REFACTOR
**Estimated Effort**: 20 minutes

**Steps**:
1. Review all updated templates for consistency
2. Document link update pattern in code comments
3. Add developer note about maintaining lang parameter
4. Run all tests - **EXPECTED: ALL PASS**

**Acceptance Criteria**:
- [ ] Consistent pattern across all templates
- [ ] Documentation added for future maintainers
- [ ] All tests passing
- [ ] No regressions

---

### Task 5: LocalStorage JavaScript Enhancement

#### 5.1: Add Inline JavaScript for LocalStorage Sync
**Phase**: GREEN (Optional feature)
**Estimated Effort**: 30 minutes

**Steps**:
1. Open `layout.html`
2. Add inline script at end of `<body>` or in `<head>`:
   ```html
   <script>
       document.addEventListener('DOMContentLoaded', function() {
           const urlParams = new URLSearchParams(window.location.search);
           const lang = urlParams.get('lang');

           if (lang) {
               localStorage.setItem('petclinic-locale', lang);
           } else {
               const storedLang = localStorage.getItem('petclinic-locale');
               if (storedLang && !document.cookie.includes('petclinic-locale')) {
                   window.location.search = '?lang=' + storedLang;
               }
           }
       });
   </script>
   ```
3. Test in browser with devtools open
4. Verify LocalStorage is updated

**Acceptance Criteria**:
- [ ] Script added to layout.html
- [ ] LocalStorage syncs with URL parameter
- [ ] Script doesn't break without JavaScript
- [ ] No console errors

---

#### 5.2: Manual Test - LocalStorage Persistence
**Phase**: Manual Verification
**Estimated Effort**: 20 minutes

**Steps**:
1. Start application and open browser
2. Open browser devtools → Application tab → LocalStorage
3. Navigate to `/?lang=de`
4. Verify `petclinic-locale` key shows "de" in LocalStorage
5. Close browser and reopen
6. Navigate to homepage without lang parameter
7. Verify browser redirects to `/?lang=de`
8. Document behavior

**Acceptance Criteria**:
- [ ] LocalStorage updated on language selection
- [ ] LocalStorage persists after browser close
- [ ] Fallback logic redirects correctly
- [ ] Feature works across browsers

---

#### 5.3: Test Graceful Degradation Without JavaScript
**Phase**: Manual Verification
**Estimated Effort**: 15 minutes

**Steps**:
1. Disable JavaScript in browser settings
2. Navigate to homepage
3. Select language from dropdown
4. Verify language still switches (via cookie)
5. Verify no errors or broken functionality
6. Re-enable JavaScript

**Acceptance Criteria**:
- [ ] Language switching works without JavaScript
- [ ] Cookie-based persistence still functional
- [ ] No visual errors or broken UI
- [ ] Feature degrades gracefully

---

#### 5.4: Document LocalStorage Feature
**Phase**: Documentation
**Estimated Effort**: 15 minutes

**Steps**:
1. Add inline code comment explaining LocalStorage sync
2. Document fallback behavior
3. Note that feature is optional enhancement
4. Add comment about browser compatibility

**Acceptance Criteria**:
- [ ] Code comments explain feature purpose
- [ ] Fallback behavior documented
- [ ] Browser compatibility notes added

---

### Task 6: End-to-End Tests and Documentation

#### 6.1: TDD - Create Playwright Test File
**Phase**: RED
**Estimated Effort**: 30 minutes

**Steps**:
1. Create `e2e-tests/tests/language-selector.spec.ts`
2. Import Playwright test utilities
3. Write test `should switch language to German and display translated text`:
   ```typescript
   test('should switch language to German', async ({ page }) => {
       await page.goto('/');
       await expect(page.locator('h2')).toContainText('Welcome');

       await page.click('#languageDropdown');
       await page.click('text=Deutsch');

       await page.waitForURL(/\?lang=de/);
       await expect(page.locator('h2')).toContainText('willkommen');
   });
   ```
4. Run test: `npm test -- language-selector.spec.ts`
5. **EXPECTED: FAIL** (if UI not fully integrated yet)

**Acceptance Criteria**:
- [ ] Test file created in correct location
- [ ] Test structure follows Playwright best practices
- [ ] Test fails with clear error if feature incomplete

---

#### 6.2: TDD - Verify E2E Test Passes with Implementation
**Phase**: GREEN
**Estimated Effort**: 20 minutes

**Steps**:
1. Ensure all previous tasks (1-5) are complete
2. Run Playwright test: `npm test -- language-selector.spec.ts`
3. If test fails, debug:
   - Check dropdown selector `#languageDropdown`
   - Verify language option text matches (e.g., "Deutsch")
   - Check URL includes `?lang=de`
4. Fix any issues and re-run
5. **EXPECTED: PASS**

**Acceptance Criteria**:
- [ ] E2E test passes successfully
- [ ] German text appears after language switch
- [ ] URL contains correct parameter

---

#### 6.3: TDD - Add Test for Language Persistence Across Navigation
**Phase**: RED
**Estimated Effort**: 30 minutes

**Steps**:
1. Add new test `should persist language across navigation` to same file:
   ```typescript
   test('should persist language across navigation', async ({ page }) => {
       await page.goto('/?lang=es');
       await expect(page.locator('.navbar')).toContainText('Inicio');

       await page.click('text=Buscar Propietarios');

       await expect(page).toHaveURL(/\?lang=es/);
       await expect(page.locator('h2')).toContainText('Propietarios');
   });
   ```
2. Run test - **EXPECTED: PASS** (if Task 4 completed)

**Acceptance Criteria**:
- [ ] Test verifies language persists on navigation
- [ ] Test checks URL parameter presence
- [ ] Test confirms translated text on new page

---

#### 6.4: TDD - Add Test for Keyboard Navigation
**Phase**: RED
**Estimated Effort**: 30 minutes

**Steps**:
1. Add test `should navigate language selector via keyboard`:
   ```typescript
   test('should navigate language selector via keyboard', async ({ page }) => {
       await page.goto('/');

       await page.keyboard.press('Tab'); // Tab to language selector
       await page.keyboard.press('Enter'); // Open dropdown

       await page.keyboard.press('ArrowDown'); // Navigate to first option
       await page.keyboard.press('ArrowDown'); // Navigate to second option
       await page.keyboard.press('Enter'); // Select option

       await expect(page).toHaveURL(/\?lang=/);
   });
   ```
2. Run test - **EXPECTED: PASS** (Bootstrap handles keyboard nav)

**Acceptance Criteria**:
- [ ] Test verifies keyboard navigation works
- [ ] Test uses keyboard events only (no mouse)
- [ ] Test passes confirming accessibility

---

#### 6.5: Add Test for Screenshot Comparison
**Phase**: RED
**Estimated Effort**: 30 minutes

**Steps**:
1. Add test `should capture screenshots in multiple languages`:
   ```typescript
   test('should capture screenshots in English and Spanish', async ({ page }) => {
       await page.goto('/?lang=en');
       await page.screenshot({ path: 'test-results/artifacts/home-english.png', fullPage: true });

       await page.goto('/?lang=es');
       await page.screenshot({ path: 'test-results/artifacts/home-spanish.png', fullPage: true });
   });
   ```
2. Run test - **EXPECTED: PASS** (screenshots generated)
3. Manually review screenshots

**Acceptance Criteria**:
- [ ] Screenshots generated in test-results folder
- [ ] English screenshot shows English text
- [ ] Spanish screenshot shows Spanish text
- [ ] Screenshots demonstrate feature working

---

#### 6.6: Run All E2E Tests in CI/CD
**Phase**: Integration
**Estimated Effort**: 20 minutes

**Steps**:
1. Ensure GitHub Actions workflow includes E2E tests
2. Push changes to trigger CI pipeline
3. Monitor test execution in GitHub Actions
4. Download artifacts (screenshots, traces) from workflow
5. Verify all tests pass in CI environment

**Acceptance Criteria**:
- [ ] E2E tests run successfully in CI/CD
- [ ] Test results visible in GitHub Actions logs
- [ ] Screenshots uploaded as artifacts
- [ ] No flaky tests or failures

---

#### 6.7: Generate Proof Documentation
**Phase**: Documentation
**Estimated Effort**: 30 minutes

**Steps**:
1. Collect all screenshots from E2E tests
2. Create proof document: `docs/specs/06-spec-language-selector/PROOF.md`
3. Include:
   - Screenshots of language selector (closed and open)
   - Screenshots of different languages (English, German, Spanish)
   - Test results summary
   - Code coverage metrics
4. Add section to main README about language selector

**Acceptance Criteria**:
- [ ] PROOF.md document created
- [ ] All screenshots included with captions
- [ ] Test results documented
- [ ] README updated with feature usage

---

#### 6.8: Final Refactor and Code Review
**Phase**: REFACTOR
**Estimated Effort**: 30 minutes

**Steps**:
1. Review all code changes for consistency
2. Ensure all tests follow naming conventions
3. Add missing JavaDoc/comments
4. Run full test suite: `./mvnw test`
5. Run E2E tests: `cd e2e-tests && npm test`
6. Generate coverage report: `./mvnw jacoco:report`
7. Verify coverage meets >90% threshold
8. Create checklist for code review

**Acceptance Criteria**:
- [ ] All tests passing (unit, integration, E2E)
- [ ] Code coverage >90% for new code
- [ ] No Checkstyle or SpotBugs violations
- [ ] Code follows project conventions
- [ ] Ready for pull request

---

## Dependencies and Blockers

### Task Dependencies
- Task 2 depends on Task 1 (UI requires working backend)
- Task 3 depends on Task 2 (Styling requires UI elements)
- Task 4 depends on Task 1 (Link updates require locale configuration)
- Task 5 depends on Task 2 (JavaScript requires UI component)
- Task 6 depends on Tasks 1-5 (E2E tests require complete feature)

### External Dependencies
- Bootstrap 5 (already in project)
- Font Awesome (already in project)
- Thymeleaf (already in project)
- Spring MVC (already in project)
- Playwright (already in project)

### Potential Blockers
- Missing translation keys in message files (mitigation: verify all 9 language files)
- Browser compatibility issues with flag emojis (mitigation: use Unicode or Font Awesome)
- Cookie security settings in test environment (mitigation: configure test profile)
- Playwright browser installation on CI (mitigation: ensure `npx playwright install` in workflow)

---

## Testing Strategy Summary

### Unit Tests
- **LocaleConfigurationTests**: Verify Spring beans and configuration
- **LayoutTests**: Verify UI component structure in HTML

### Integration Tests
- **LocaleSwitchingTests**: Verify locale switching via query parameter and cookie persistence

### End-to-End Tests
- **language-selector.spec.ts**: Verify complete user journey across multiple languages

### Manual Tests
- Accessibility verification (WCAG 2.1 AA)
- Keyboard navigation
- Screen reader compatibility
- Color contrast
- Cross-browser testing

### Coverage Goals
- LocaleConfiguration: 100% line coverage
- UI template changes: Verified via integration tests
- E2E critical paths: All covered

---

## Definition of Done Checklist

- [ ] All parent tasks (1-6) completed
- [ ] All sub-tasks completed
- [ ] Unit tests written and passing (>90% coverage)
- [ ] Integration tests written and passing
- [ ] E2E tests written and passing in CI/CD
- [ ] Manual accessibility testing completed
- [ ] Screenshots generated for proof
- [ ] Code follows TDD RED-GREEN-REFACTOR cycle
- [ ] No Checkstyle or SpotBugs violations
- [ ] All navigation links preserve lang parameter
- [ ] Language selector visible on all pages
- [ ] All 9 languages functional
- [ ] Cookie persistence verified
- [ ] Keyboard navigation working
- [ ] Screen reader announces correctly
- [ ] WCAG 2.1 AA compliance verified
- [ ] Documentation updated (README, PROOF.md)
- [ ] Code review completed
- [ ] PR merged to main branch

---

## Estimated Total Effort

| Task | Estimated Hours |
|------|----------------|
| Task 1: Spring Configuration | 2.0 |
| Task 2: UI Component | 3.0 |
| Task 3: Styling & Accessibility | 2.0 |
| Task 4: URL Propagation | 2.0 |
| Task 5: LocalStorage | 1.5 |
| Task 6: E2E Tests & Docs | 3.0 |
| **Total** | **13.5 hours** |

---

## Notes for Implementation

1. **Follow TDD Strictly**: Every code change must be driven by a failing test
2. **Commit Frequently**: Commit after each GREEN phase
3. **Run Tests Often**: Run full test suite after each REFACTOR phase
4. **Manual Verification**: Perform manual accessibility testing even with passing automated tests
5. **Screenshot Everything**: Visual proof is critical for UI changes
6. **Document Decisions**: Add comments explaining non-obvious implementation choices
7. **Review Coverage**: Use JaCoCo reports to identify untested code paths

---

**End of Task Breakdown**
