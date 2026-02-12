# Specification: Language Selector in Header

**Feature ID**: Issue #3
**Status**: Draft
**Created**: 2026-02-12
**TDD Required**: Yes (Strict RED-GREEN-REFACTOR)

---

## 1. Feature Overview

### Summary
Add a language selector dropdown in the application header, allowing users to switch the UI language dynamically. The selector will support all 9 existing languages and use Spring's locale resolution mechanisms for persistence.

### Business Value
- Improves accessibility for international users
- Leverages existing i18n infrastructure (9 languages already supported)
- Enhances user experience by allowing runtime language switching
- Demonstrates modern i18n best practices

### Scope
- **In Scope**: Dropdown UI component, all 9 languages, query parameter + cookie persistence, Spring configuration, accessibility features
- **Out of Scope**: Adding new languages, auto-detection refinement, user preference storage in database

---

## 2. Acceptance Criteria

From GitHub Issue #3:
- [x] A language selector is visible in the global header on all pages
- [x] Selecting a language updates visible UI text (e.g., page headings/nav labels)
- [x] The selected language persists across navigation in the same session
- [x] Dropdown menu displays native language names (e.g., "English", "Deutsch", "Español")
- [x] All 9 existing languages supported
- [x] Selector placed next to logo (left side of navbar)
- [x] Stays visible outside hamburger menu on mobile
- [x] Follows existing Liatrio navbar styling
- [x] Uses query parameter (`?lang=xx`) + cookie for persistence
- [x] LocalStorage used for client-side persistence
- [x] Default language based on browser's Accept-Language header
- [x] Current page reloads with `?lang=xx` parameter on selection
- [x] Language parameter appended to all navigation links automatically
- [x] Spring LocaleResolver configured with fallback chain
- [x] LocaleChangeInterceptor added/verified for `?lang=xx` support
- [x] Active language highlighted in dropdown
- [x] Optional flag icons with accessible text
- [x] Globe icon label for selector
- [x] Missing translations fall back to English
- [x] ARIA labels and keyboard navigation support

---

## 3. Functional Requirements

### FR-1: Language Selector UI Component
**Requirement**: Add dropdown menu to navbar with native language names.

**Component Type**: Bootstrap dropdown button

**Languages Supported** (9 total):
1. **English (en)**: "English"
2. **German (de)**: "Deutsch"
3. **Spanish (es)**: "Español"
4. **Korean (ko)**: "한국어"
5. **Persian (fa)**: "فارسی"
6. **Portuguese (pt)**: "Português"
7. **Russian (ru)**: "Русский"
8. **Turkish (tr)**: "Türkçe"
9. **Chinese (zh)**: "中文" *(assuming 9th language is Chinese)*

**UI Specification**:
- **Type**: Dropdown button with Bootstrap styling
- **Button Label**: Globe icon (🌐 or Font Awesome `fa-globe`)
- **Dropdown Items**: List of languages with native names
- **Active Indicator**: Current language shown in bold or with checkmark
- **Optional Icons**: Small flag emoji next to language name (e.g., 🇺🇸 English)

### FR-2: Placement and Layout
**Requirement**: Position selector in navbar, next to logo on left side.

**Layout**:
```
[Logo] [Language Selector] ... [Home] [Find Owners] [Vets] [Error]
```

**Responsive Behavior**:
- **Desktop**: Visible next to logo, left of nav items
- **Mobile**: Stay visible outside hamburger menu (always accessible)
- **Styling**: Match existing navbar dark theme and Liatrio branding

### FR-3: Locale Switching Mechanism
**Requirement**: Switch language via `?lang=xx` query parameter with cookie persistence.

**Flow**:
1. User selects language from dropdown
2. Browser navigates to current page with `?lang=xx` appended
3. Spring `LocaleChangeInterceptor` detects parameter
4. Locale stored in cookie via `CookieLocaleResolver`
5. Page renders with new locale
6. Cookie persists locale for subsequent requests

**Example URLs**:
- `/owners?lang=de` → German
- `/vets.html?lang=es` → Spanish
- `/?lang=ko` → Korean

### FR-4: Locale Persistence
**Requirement**: Persist language selection across navigation and sessions.

**Mechanism**: Multi-layer approach
1. **Query Parameter** (`?lang=xx`): Immediate switch
2. **Cookie**: Server-side persistence (30-day expiration)
3. **LocalStorage**: Client-side persistence (JavaScript fallback)

**Priority**: Query param > Cookie > LocalStorage > Accept-Language header > Default (English)

### FR-5: Default Language Detection
**Requirement**: Use browser's Accept-Language header as initial default.

**Behavior**:
- First visit: Check Accept-Language header
- If header specifies supported language (e.g., `es-MX`) → use Spanish
- If header specifies unsupported language → use English (fallback)
- Subsequent visits: Use cookie/LocalStorage preference

### FR-6: URL Parameter Propagation
**Requirement**: Append `?lang=xx` to all navigation links automatically.

**Implementation**:
- Use Thymeleaf URL builder: `@{/path(lang=${#locale})}`
- Update all `th:href` attributes in navigation to include locale
- Ensures language persists when navigating via links

**Example**:
```html
<!-- Before -->
<a th:href="@{/owners/find}">Find Owners</a>

<!-- After -->
<a th:href="@{/owners/find(lang=${#locale.language})}">Find Owners</a>
```

### FR-7: Active Language Indicator
**Requirement**: Highlight current language in dropdown.

**UI Behavior**:
- Current language shown in bold
- Optional: Checkmark icon next to current language
- Dropdown button label shows current language code or flag

### FR-8: Accessibility
**Requirement**: Ensure WCAG 2.1 AA compliance.

**Features**:
- **ARIA Labels**: `aria-label="Language selector"` on dropdown button
- **Keyboard Navigation**: Dropdown navigable via Tab and Arrow keys
- **Screen Reader**: Announces "Language selector, current: English"
- **Color Contrast**: Text meets 4.5:1 contrast ratio
- **Focus Indicators**: Visible focus outline on dropdown items

### FR-9: Fallback for Missing Translations
**Requirement**: Fall back to English if translation key not found.

**Behavior**:
- Spring's `MessageSource` configured with fallback locale (English)
- If `messages_de.properties` missing a key → use `messages.properties` (English)
- No error thrown, graceful degradation

---

## 4. Technical Design

### 4.1 Architecture

**Components**:
1. **UI Component**: Thymeleaf dropdown in `layout.html`
2. **Spring Configuration**: LocaleResolver + LocaleChangeInterceptor
3. **JavaScript** (optional): LocalStorage sync
4. **CSS**: Custom styling for dropdown

**Flow**:
```
User Selects Language → Navigate to current URL + ?lang=xx
                         ↓
                         LocaleChangeInterceptor intercepts request
                         ↓
                         CookieLocaleResolver sets locale in cookie
                         ↓
                         LocaleContextHolder updates current locale
                         ↓
                         Thymeleaf renders page with new locale
                         ↓
                         JavaScript syncs locale to LocalStorage
```

### 4.2 Spring Configuration

**Location**: `src/main/java/org/springframework/samples/petclinic/system/LocaleConfiguration.java` (new file)

**Implementation**:
```java
package org.springframework.samples.petclinic.system;

import java.util.Locale;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

/**
 * Configuration for internationalization (i18n) and locale resolution.
 * Supports language switching via query parameter (?lang=xx) with cookie persistence.
 */
@Configuration
public class LocaleConfiguration implements WebMvcConfigurer {

    /**
     * Configure locale resolver to use cookies with fallback to Accept-Language header.
     */
    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver resolver = new CookieLocaleResolver();
        resolver.setDefaultLocale(Locale.ENGLISH); // Fallback default
        resolver.setCookieName("petclinic-locale");
        resolver.setCookieMaxAge(60 * 60 * 24 * 30); // 30 days
        return resolver;
    }

    /**
     * Interceptor to detect ?lang=xx query parameter and update locale.
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }

    /**
     * Register locale change interceptor.
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}
```

**Key Points**:
- **CookieLocaleResolver**: Stores locale in cookie, reads from Accept-Language header if no cookie
- **LocaleChangeInterceptor**: Detects `?lang=xx` parameter and updates resolver
- **Cookie Name**: `petclinic-locale`
- **Cookie Expiration**: 30 days

### 4.3 UI Component (Thymeleaf)

**Location**: `src/main/resources/templates/fragments/layout.html`

**Insertion Point**: Between logo and nav items in navbar

**Template Code**:
```html
<!-- Language Selector Dropdown -->
<div class="language-selector">
    <div class="dropdown">
        <button class="btn btn-sm btn-outline-light dropdown-toggle" type="button"
                id="languageDropdown" data-bs-toggle="dropdown" aria-expanded="false"
                aria-label="Language selector">
            <i class="fa fa-globe" aria-hidden="true"></i>
            <span class="visually-hidden" th:text="#{home}"></span>
        </button>
        <ul class="dropdown-menu" aria-labelledby="languageDropdown">
            <li><a class="dropdown-item" th:href="@{''(lang='en')}"
                   th:classappend="${#locale.language == 'en'} ? 'active fw-bold' : ''">
                <span aria-hidden="true">🇺🇸</span> English
            </a></li>
            <li><a class="dropdown-item" th:href="@{''(lang='de')}"
                   th:classappend="${#locale.language == 'de'} ? 'active fw-bold' : ''">
                <span aria-hidden="true">🇩🇪</span> Deutsch
            </a></li>
            <li><a class="dropdown-item" th:href="@{''(lang='es')}"
                   th:classappend="${#locale.language == 'es'} ? 'active fw-bold' : ''">
                <span aria-hidden="true">🇪🇸</span> Español
            </a></li>
            <li><a class="dropdown-item" th:href="@{''(lang='ko')}"
                   th:classappend="${#locale.language == 'ko'} ? 'active fw-bold' : ''">
                <span aria-hidden="true">🇰🇷</span> 한국어
            </a></li>
            <li><a class="dropdown-item" th:href="@{''(lang='fa')}"
                   th:classappend="${#locale.language == 'fa'} ? 'active fw-bold' : ''">
                <span aria-hidden="true">🇮🇷</span> فارسی
            </a></li>
            <li><a class="dropdown-item" th:href="@{''(lang='pt')}"
                   th:classappend="${#locale.language == 'pt'} ? 'active fw-bold' : ''">
                <span aria-hidden="true">🇵🇹</span> Português
            </a></li>
            <li><a class="dropdown-item" th:href="@{''(lang='ru')}"
                   th:classappend="${#locale.language == 'ru'} ? 'active fw-bold' : ''">
                <span aria-hidden="true">🇷🇺</span> Русский
            </a></li>
            <li><a class="dropdown-item" th:href="@{''(lang='tr')}"
                   th:classappend="${#locale.language == 'tr'} ? 'active fw-bold' : ''">
                <span aria-hidden="true">🇹🇷</span> Türkçe
            </a></li>
            <li><a class="dropdown-item" th:href="@{''(lang='zh')}"
                   th:classappend="${#locale.language == 'zh'} ? 'active fw-bold' : ''">
                <span aria-hidden="true">🇨🇳</span> 中文
            </a></li>
        </ul>
    </div>
</div>
```

**Key Features**:
- **Current URL Preservation**: `@{''(lang='xx')}` generates current path + `?lang=xx`
- **Active Indicator**: `fw-bold` class applied to current language
- **Flag Emojis**: Optional visual indicator (aria-hidden for accessibility)
- **ARIA Labels**: `aria-label` on button, `aria-hidden` on decorative icons

### 4.4 CSS Styling

**Location**: `src/main/resources/static/resources/css/petclinic.scss` (or inline in layout)

**Custom Styles**:
```css
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

### 4.5 JavaScript (Optional Enhancement)

**Location**: `src/main/resources/templates/fragments/layout.html` (inline script)

**Purpose**: Sync locale to LocalStorage for client-side persistence

**Implementation**:
```html
<script>
    // Sync locale to LocalStorage on page load
    document.addEventListener('DOMContentLoaded', function() {
        const urlParams = new URLSearchParams(window.location.search);
        const lang = urlParams.get('lang');

        if (lang) {
            localStorage.setItem('petclinic-locale', lang);
        } else {
            const storedLang = localStorage.getItem('petclinic-locale');
            if (storedLang && !document.cookie.includes('petclinic-locale')) {
                // Redirect to current page with stored lang if cookie missing
                window.location.search = '?lang=' + storedLang;
            }
        }
    });
</script>
```

**Note**: This is optional and may be deferred to Phase 2 if not essential.

### 4.6 URL Link Updates

**Requirement**: Update all navigation links to include `lang` parameter

**Files to Update**:
- `layout.html` (nav menu items)
- All templates with `th:href` links

**Pattern**:
```html
<!-- Original -->
<a th:href="@{/owners/find}">Find Owners</a>

<!-- Updated -->
<a th:href="@{/owners/find(lang=${#locale.language})}">Find Owners</a>
```

**Scope**: Update all `th:href` in:
- Navigation menu (`layout.html`)
- Owner/Pet/Vet detail pages
- Form cancel/back links

**Alternative**: Use `#request.getParameter('lang')` to preserve existing param

---

## 5. TDD Approach (RED-GREEN-REFACTOR)

### Phase 1: Configuration Tests

#### RED 1: Test LocaleResolver Bean Exists
**Test**: `LocaleConfigurationTests.shouldHaveLocaleResolverBean()`
```java
@SpringBootTest
class LocaleConfigurationTests {

    @Autowired
    private ApplicationContext context;

    @Test
    void shouldHaveLocaleResolverBean() {
        LocaleResolver resolver = context.getBean(LocaleResolver.class);
        assertThat(resolver).isNotNull();
        assertThat(resolver).isInstanceOf(CookieLocaleResolver.class);
    }
}
```
**Expected**: FAIL (LocaleConfiguration doesn't exist)

#### GREEN 1: Create LocaleConfiguration
Implement `LocaleConfiguration.java` as shown in Section 4.2.
**Expected**: PASS

#### RED 2: Test LocaleChangeInterceptor Registered
**Test**: `LocaleConfigurationTests.shouldHaveLocaleChangeInterceptor()`
```java
@Test
void shouldHaveLocaleChangeInterceptor() {
    LocaleChangeInterceptor interceptor = context.getBean(LocaleChangeInterceptor.class);
    assertThat(interceptor).isNotNull();
    assertThat(interceptor.getParamName()).isEqualTo("lang");
}
```
**Expected**: PASS (already implemented in GREEN 1)

### Phase 2: Locale Switching Integration Tests

#### RED 3: Test Locale Switches via Query Parameter
**Test**: `LocaleSwitchingTests.shouldSwitchLocaleViaQueryParameter()`
```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class LocaleSwitchingTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldSwitchLocaleViaQueryParameter() {
        ResponseEntity<String> response = restTemplate.getForEntity("/?lang=de", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("willkommen"); // German welcome message
    }
}
```
**Expected**: FAIL (UI not updated, locale not switching)

#### GREEN 3: Verify Configuration Works
Configuration should work after GREEN 1. Verify Thymeleaf templates use i18n keys.
**Expected**: PASS

#### RED 4: Test Cookie Set After Locale Switch
**Test**: `LocaleSwitchingTests.shouldSetCookieAfterLocaleSwitchViaQueryParam()`
```java
@Test
void shouldSetCookieAfterLocaleSwitch() {
    ResponseEntity<String> response = restTemplate.getForEntity("/?lang=es", String.class);

    List<String> cookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
    assertThat(cookies).isNotNull();
    assertThat(cookies).anyMatch(cookie -> cookie.contains("petclinic-locale=es"));
}
```
**Expected**: PASS (CookieLocaleResolver sets cookie automatically)

#### RED 5: Test Locale Persists Across Requests
**Test**: `LocaleSwitchingTests.shouldPersistLocaleAcrossRequests()`
```java
@Test
void shouldPersistLocaleAcrossRequests() {
    // First request: set locale to German
    ResponseEntity<String> response1 = restTemplate.getForEntity("/?lang=de", String.class);
    List<String> cookies = response1.getHeaders().get(HttpHeaders.SET_COOKIE);
    String cookie = cookies.stream()
        .filter(c -> c.contains("petclinic-locale"))
        .findFirst()
        .orElseThrow();

    // Second request: no lang param, but send cookie
    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.COOKIE, cookie);
    HttpEntity<String> request = new HttpEntity<>(headers);

    ResponseEntity<String> response2 = restTemplate.exchange("/", HttpMethod.GET, request, String.class);

    assertThat(response2.getBody()).contains("willkommen"); // Still German
}
```
**Expected**: PASS

### Phase 3: UI Component Tests

#### RED 6: Test Language Selector Present in HTML
**Test**: `LayoutTests.shouldContainLanguageSelector()`
```java
@WebMvcTest(WelcomeController.class)
class LayoutTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldContainLanguageSelectorInNavbar() throws Exception {
        mockMvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("languageDropdown")))
            .andExpect(content().string(containsString("fa-globe")));
    }
}
```
**Expected**: FAIL (language selector not in template)

#### GREEN 6: Add Language Selector to Template
Add dropdown UI component to `layout.html` as shown in Section 4.3.
**Expected**: PASS

#### RED 7: Test All 9 Languages in Dropdown
**Test**: `LayoutTests.shouldIncludeAllNineLanguagesInDropdown()`
```java
@Test
void shouldIncludeAllNineLanguagesInDropdown() throws Exception {
    mockMvc.perform(get("/"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("lang='en'")))
        .andExpect(content().string(containsString("English")))
        .andExpect(content().string(containsString("lang='de'")))
        .andExpect(content().string(containsString("Deutsch")))
        .andExpect(content().string(containsString("lang='es'")))
        .andExpect(content().string(containsString("Español")))
        .andExpect(content().string(containsString("lang='ko'")))
        .andExpect(content().string(containsString("한국어")))
        // ... repeat for all 9 languages
        ;
}
```
**Expected**: PASS (after GREEN 6)

#### RED 8: Test Active Language Highlighted
**Test**: `LayoutTests.shouldHighlightCurrentLanguage()`
```java
@Test
void shouldHighlightCurrentLanguageInDropdown() throws Exception {
    mockMvc.perform(get("/?lang=de"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("lang='de'")))
        .andExpect(content().string(matchesRegex(".*lang='de'.*active.*")));
}
```
**Expected**: PASS (active class added based on `${#locale.language}`)

### Phase 4: End-to-End Tests (Playwright)

#### RED 9: Test Language Switching E2E
**Test**: `e2e-tests/tests/language-selector.spec.ts`
```typescript
test('should switch language to German and display translated text', async ({ page }) => {
    await page.goto('/');

    // Initial language should be English (or browser default)
    await expect(page.locator('h2')).toContainText('Welcome');

    // Click language dropdown
    await page.click('#languageDropdown');

    // Select German
    await page.click('text=Deutsch');

    // Wait for page reload
    await page.waitForURL(/\?lang=de/);

    // Verify German text appears
    await expect(page.locator('h2')).toContainText('willkommen');
    await expect(page.locator('.navbar')).toContainText('Veterinäre'); // "Vets" in German
});
```
**Expected**: FAIL (UI not integrated)

#### GREEN 9: Complete Integration
All previous steps complete the integration.
**Expected**: PASS

#### RED 10: Test Language Persists Across Navigation
**Test**: `e2e-tests/tests/language-selector.spec.ts`
```typescript
test('should persist language across navigation', async ({ page }) => {
    await page.goto('/?lang=es');

    // Verify Spanish
    await expect(page.locator('.navbar')).toContainText('Inicio'); // "Home" in Spanish

    // Navigate to Owners page
    await page.click('text=Buscar Propietarios'); // "Find Owners" in Spanish

    // Language should persist
    await expect(page).toHaveURL(/\?lang=es/);
    await expect(page.locator('h2')).toContainText('Propietarios'); // "Owners" in Spanish
});
```
**Expected**: PASS (if links updated with `lang` parameter)

#### RED 11: Test Screenshot in Two Languages
**Test**: `e2e-tests/tests/language-selector.spec.ts`
```typescript
test('should capture screenshots in English and Spanish', async ({ page }) => {
    // English screenshot
    await page.goto('/?lang=en');
    await page.screenshot({ path: 'test-results/home-english.png' });

    // Spanish screenshot
    await page.goto('/?lang=es');
    await page.screenshot({ path: 'test-results/home-spanish.png' });

    // Manual verification: compare screenshots
});
```
**Expected**: PASS (screenshots generated for proof)

---

## 6. Test Scenarios

### 6.1 Configuration Tests (LocaleConfigurationTests.java)

| Test Case | Description | Expected Result |
|-----------|-------------|-----------------|
| `shouldHaveLocaleResolverBean()` | LocaleResolver bean exists | CookieLocaleResolver instance |
| `shouldHaveLocaleChangeInterceptor()` | Interceptor bean exists | Param name = "lang" |
| `shouldSetDefaultLocaleToEnglish()` | Default locale configured | Locale.ENGLISH |

### 6.2 Locale Switching Integration Tests (LocaleSwitchingTests.java)

| Test Case | Description | Expected Result |
|-----------|-------------|-----------------|
| `shouldSwitchLocaleViaQueryParameter()` | `?lang=de` switches to German | German text rendered |
| `shouldSetCookieAfterLocaleSwitch()` | Cookie set after switch | `petclinic-locale=de` |
| `shouldPersistLocaleAcrossRequests()` | Cookie used on subsequent requests | Locale persists |
| `shouldFallbackToEnglishForUnsupportedLang()` | `?lang=xyz` uses English | English text rendered |

### 6.3 UI Component Tests (LayoutTests.java)

| Test Case | Description | Expected Result |
|-----------|-------------|-----------------|
| `shouldContainLanguageSelectorInNavbar()` | Selector present in HTML | `languageDropdown` exists |
| `shouldIncludeAllNineLanguagesInDropdown()` | All 9 languages listed | All lang codes present |
| `shouldHighlightCurrentLanguage()` | Active language has `active` class | Bold/highlighted |
| `shouldHaveAccessibleARIALabels()` | ARIA labels present | `aria-label` on button |

### 6.4 End-to-End Tests (Playwright)

| Test Case | Description | Expected Result |
|-----------|-------------|-----------------|
| `shouldSwitchLanguageToGerman()` | Select German from dropdown | German text appears |
| `shouldSwitchLanguageToSpanish()` | Select Spanish from dropdown | Spanish text appears |
| `shouldPersistLanguageAcrossNavigation()` | Navigate after switch | Language persists |
| `shouldCaptureScreenshotsInTwoLanguages()` | English + Spanish screenshots | Images saved |
| `shouldBeAccessibleViaKeyboard()` | Tab to dropdown, arrow navigate | Works without mouse |

---

## 7. Implementation Plan

### Step 1: Create Spring Configuration
**Files**: `LocaleConfiguration.java` (new)
- Create `@Configuration` class
- Define `LocaleResolver` bean (CookieLocaleResolver)
- Define `LocaleChangeInterceptor` bean
- Register interceptor

**Tests**: `LocaleConfigurationTests.java` (new)

### Step 2: Add Language Selector UI
**Files**: `layout.html`
- Add dropdown component between logo and nav items
- Include all 9 languages with native names
- Add flag emojis (optional)
- Add ARIA labels

**Tests**: `LayoutTests.java` (add tests)

### Step 3: Add CSS Styling
**Files**: `petclinic.scss` or inline in `layout.html`
- Style dropdown to match navbar theme
- Add hover/focus states
- Ensure accessibility (contrast, focus indicators)

**Tests**: Manual verification

### Step 4: Update Navigation Links
**Files**: All templates with `th:href`
- Update links to include `(lang=${#locale.language})`
- Ensure language persists across navigation

**Tests**: Manual verification or E2E tests

### Step 5: Add JavaScript LocalStorage Sync (Optional)
**Files**: `layout.html` (inline script)
- Sync locale to LocalStorage on page load
- Redirect if LocalStorage differs from URL

**Tests**: Manual verification

### Step 6: Add End-to-End Tests
**Files**: `e2e-tests/tests/language-selector.spec.ts` (new)
- Test language switching
- Test persistence
- Capture screenshots

**Tests**: Run via `npm test`

### Step 7: Generate Proof Screenshots
**Action**: Run Playwright tests and save screenshots
- English homepage
- Spanish homepage (or another language)
- Include in PR for proof

---

## 8. Dependencies

### Internal
- **Spring MVC**: LocaleResolver, LocaleChangeInterceptor
- **Thymeleaf**: `#locale` utility for current locale
- **Bootstrap 5**: Dropdown component
- **Font Awesome**: Globe icon

### External
- None

### Configuration
- **application.properties**: Already has `spring.messages.basename=messages/messages`
- **Message Files**: All 9 languages already exist

---

## 9. Non-Functional Requirements

### Performance
- **Impact**: Minimal (one cookie read/write per request)
- **Dropdown Render**: No performance impact (static HTML)

### Security
- **Query Parameter Injection**: Mitigated by Spring validating locale codes
- **Cookie Security**: Use `HttpOnly` flag (optional enhancement)

### Usability
- **Discoverability**: Globe icon universally recognized
- **Ease of Use**: One-click language switch
- **Persistence**: Language persists across session

### Accessibility (WCAG 2.1 AA)
- **Keyboard Navigation**: Tab to dropdown, arrow keys to select
- **Screen Reader**: Announces "Language selector, current: English"
- **Color Contrast**: Text meets 4.5:1 ratio
- **Focus Indicators**: Visible focus outline

### Internationalization
- **Coverage**: All 9 existing languages supported
- **Fallback**: Missing translations use English
- **Extensibility**: Easy to add new languages (add to dropdown + message file)

---

## 10. Risk Assessment

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| Locale parameter lost on form submission | Medium | Medium | Update all forms to include lang param |
| Translation key missing | Low | Low | Fallback to English configured |
| Cookie not persisting (browser settings) | Low | Low | LocalStorage as secondary persistence |
| Flag emojis not rendering | Low | Low | Use Unicode emojis or Font Awesome icons |
| Accessibility issues (screen reader) | Medium | Medium | Thorough ARIA label testing |

---

## 11. Future Enhancements (Out of Scope)

- **User Preference Storage**: Save language preference to database per user account
- **Auto-Detection Refinement**: Use IP geolocation for better default language
- **Right-to-Left (RTL) Support**: Full RTL layout for Persian (fa) language
- **Regional Variants**: Support en-US vs en-GB, es-ES vs es-MX
- **Language Negotiation API**: Use browser's Navigator.languages for better detection
- **Admin Panel**: Manage translations via UI instead of property files
- **Translation Coverage Report**: Dashboard showing missing translation keys per language

---

## 12. Acceptance Testing Checklist

- [ ] LocaleConfiguration class created with LocaleResolver and LocaleChangeInterceptor
- [ ] Language selector dropdown present in navbar on all pages
- [ ] All 9 languages listed in dropdown with native names
- [ ] Selecting language navigates to current page with `?lang=xx`
- [ ] Cookie `petclinic-locale` set after language switch
- [ ] Language persists across navigation (links include `lang` param)
- [ ] Active language highlighted in dropdown (bold or checkmark)
- [ ] Globe icon displayed on dropdown button
- [ ] ARIA labels present for accessibility
- [ ] Keyboard navigation works (Tab, Arrow keys)
- [ ] Screen reader announces language selector
- [ ] Default language based on Accept-Language header
- [ ] Missing translations fall back to English
- [ ] Screenshots captured in English and one other language
- [ ] Unit tests for configuration achieve 100% coverage
- [ ] Integration tests for locale switching pass
- [ ] Playwright E2E tests pass
- [ ] Manual testing confirms UX behavior

---

## 13. Definition of Done

- [ ] All TDD cycles completed (RED-GREEN-REFACTOR)
- [ ] Spring configuration implemented and tested
- [ ] UI component added to layout.html
- [ ] CSS styling applied and matches branding
- [ ] All navigation links updated with lang parameter
- [ ] Unit tests written and passing (>90% coverage)
- [ ] Integration tests written and passing
- [ ] Playwright E2E tests written and passing
- [ ] Screenshots generated for proof (2 languages)
- [ ] Accessibility verified (WCAG 2.1 AA)
- [ ] Code review completed
- [ ] No Checkstyle/SpotBugs violations
- [ ] Feature tested manually in dev environment
- [ ] Merged to main branch via PR

---

**End of Specification**
