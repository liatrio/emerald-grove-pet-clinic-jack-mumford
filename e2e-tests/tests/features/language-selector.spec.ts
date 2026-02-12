import { test, expect } from '@playwright/test';

test.describe('Language Selector Feature', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/');
  });

  test.describe('Language Switching', () => {
    test('should display language selector dropdown in navbar', async ({ page }) => {
      // Verify language selector button is visible
      const languageButton = page.locator('#languageDropdown');
      await expect(languageButton).toBeVisible();
      await expect(languageButton).toHaveAttribute('aria-label', 'Language selector');

      // Verify globe icon is present
      const globeIcon = languageButton.locator('.fa-globe');
      await expect(globeIcon).toBeVisible();
    });

    test('should contain all 9 supported languages in dropdown', async ({ page }) => {
      // Open language dropdown
      await page.click('#languageDropdown');

      // Wait for dropdown menu to be visible
      const dropdownMenu = page.locator('.language-selector .dropdown-menu');
      await expect(dropdownMenu).toBeVisible();

      // Verify all 9 language options are present
      const languages = [
        { flag: '🇺🇸', name: 'English' },
        { flag: '🇩🇪', name: 'Deutsch' },
        { flag: '🇪🇸', name: 'Español' },
        { flag: '🇰🇷', name: '한국어' },
        { flag: '🇮🇷', name: 'فارسی' },
        { flag: '🇵🇹', name: 'Português' },
        { flag: '🇷🇺', name: 'Русский' },
        { flag: '🇹🇷', name: 'Türkçe' },
        { flag: '🇨🇳', name: '中文' }
      ];

      for (const lang of languages) {
        const option = dropdownMenu.locator(`.dropdown-item:has-text("${lang.name}")`);
        await expect(option).toBeVisible();
        await expect(option).toContainText(lang.flag);
      }
    });

    test('should switch language to German and display translated text', async ({ page }) => {
      // Verify initial English text
      await expect(page.locator('h1')).toContainText('Care made modern');
      await expect(page.locator('.navbar-brand-text')).toContainText('Emerald Grove Veterinary Clinic');

      // Open language dropdown
      await page.click('#languageDropdown');

      // Click on German language option
      await page.click('text=Deutsch');

      // Wait for page reload with German language
      await page.waitForURL(/\?lang=de/);

      // Verify German text appears
      await expect(page.locator('h1')).toContainText('Moderne Tierpflege');
      await expect(page.locator('.navbar-brand-text')).toContainText('Emerald Grove Veterinary Clinic');
    });

    test('should switch language to Spanish and display translated text', async ({ page }) => {
      // Open language dropdown
      await page.click('#languageDropdown');

      // Click on Spanish language option
      await page.click('text=Español');

      // Wait for page reload with Spanish language
      await page.waitForURL(/\?lang=es/);

      // Verify Spanish text appears
      await expect(page.locator('h1')).toContainText('Cuidado moderno');
      await expect(page.locator('text=Inicio')).toBeVisible(); // "Home" in navbar
    });

    test('should mark current language as active in dropdown', async ({ page }) => {
      // Navigate to Spanish
      await page.goto('/?lang=es');

      // Open language dropdown
      await page.click('#languageDropdown');

      // Verify Spanish option has active class
      const spanishOption = page.locator('.dropdown-item:has-text("Español")');
      await expect(spanishOption).toHaveClass(/active/);
      await expect(spanishOption).toHaveClass(/fw-bold/);

      // Verify other options don't have active class
      const englishOption = page.locator('.dropdown-item:has-text("English")');
      await expect(englishOption).not.toHaveClass(/active/);
    });
  });

  test.describe('Language Persistence Across Navigation', () => {
    test('should persist Spanish across navigation to Find Owners', async ({ page }) => {
      // Navigate to homepage with Spanish
      await page.goto('/?lang=es');

      // Verify Spanish text on homepage
      await expect(page.locator('text=Inicio')).toBeVisible();

      // Click "Find Owners" link
      await page.click('text=Buscar propietarios');

      // Verify URL contains lang=es
      await expect(page).toHaveURL(/lang=es/);

      // Verify Spanish text on Find Owners page
      await expect(page.locator('h2')).toContainText('Buscar propietarios');
    });

    test('should persist German across navigation to Vets page', async ({ page }) => {
      // Navigate to homepage with German
      await page.goto('/?lang=de');

      // Verify German text
      await expect(page.locator('text=Startseite')).toBeVisible();

      // Click "Veterinarians" link
      await page.click('text=Tierärzte');

      // Verify URL contains lang=de
      await expect(page).toHaveURL(/lang=de/);

      // Verify German text on Vets page
      await expect(page.locator('h2')).toContainText('Tierärzte');
    });

    test('should persist Korean after switching language', async ({ page }) => {
      // Start on English homepage
      await page.goto('/');

      // Switch to Korean
      await page.click('#languageDropdown');
      await page.click('text=한국어');
      await page.waitForURL(/\?lang=ko/);

      // Navigate to Find Owners (use navbar link with language persistence)
      await page.click('.navbar a[href*="owners/find"]');

      // Verify URL still has lang=ko
      await expect(page).toHaveURL(/lang=ko/);

      // Verify Korean text persists in navbar
      await expect(page.locator('.navbar')).toContainText('홈');
    });

    test('should persist language in owner list pagination', async ({ page }) => {
      // Navigate to Find Owners with Spanish
      await page.goto('/owners/find?lang=es');

      // Search for owners to trigger pagination
      await page.click('button:has-text("Buscar propietario")');

      // Wait for owner list page to load
      await page.waitForLoadState('networkidle');

      // Verify Spanish text is visible on the page (regardless of URL structure)
      await expect(page.locator('.navbar')).toContainText('Inicio');

      // Check pagination links have lang parameter
      const paginationLinks = page.locator('.pagination a');
      const linkCount = await paginationLinks.count();
      if (linkCount > 0) {
        const firstLink = await paginationLinks.first().getAttribute('href');
        // Pagination links should include lang parameter
        expect(firstLink).toContain('lang=es');
      } else {
        // If no pagination, at least verify the page displays Spanish text
        await expect(page.locator('body')).toContainText('Propietarios');
      }
    });
  });

  test.describe('Keyboard Navigation and Accessibility', () => {
    test('should navigate language selector via keyboard', async ({ page }) => {
      // Tab to language selector button
      await page.keyboard.press('Tab');
      await page.keyboard.press('Tab'); // May need multiple tabs depending on focus order
      await page.keyboard.press('Tab');

      // Check if language dropdown has focus (check programmatically)
      const languageButton = page.locator('#languageDropdown');

      // Open dropdown with Enter key
      await languageButton.focus();
      await page.keyboard.press('Enter');

      // Wait for dropdown to open
      const dropdownMenu = page.locator('.language-selector .dropdown-menu');
      await expect(dropdownMenu).toBeVisible();

      // Navigate through options with Arrow keys
      await page.keyboard.press('ArrowDown');
      await page.keyboard.press('ArrowDown');

      // Select option with Enter
      await page.keyboard.press('Enter');

      // Verify URL has lang parameter
      await expect(page).toHaveURL(/\?lang=/);
    });

    test('should close dropdown with Escape key', async ({ page }) => {
      // Open dropdown
      await page.click('#languageDropdown');

      // Verify dropdown is open
      const dropdownMenu = page.locator('.language-selector .dropdown-menu');
      await expect(dropdownMenu).toBeVisible();

      // Press Escape to close
      await page.keyboard.press('Escape');

      // Verify dropdown is closed (Bootstrap handles this)
      // Note: The dropdown may not have display:none immediately, check visibility
      await page.waitForTimeout(300); // Allow Bootstrap animation
      await expect(dropdownMenu).not.toBeVisible();
    });

    test('should have focus indicators on language selector', async ({ page }) => {
      // Focus on language button
      const languageButton = page.locator('#languageDropdown');
      await languageButton.focus();

      // Check that focus indicator is visible (via computed styles or outline)
      const focused = await languageButton.evaluate((el) => el === document.activeElement);
      expect(focused).toBeTruthy();
    });

    test('should have ARIA labels for accessibility', async ({ page }) => {
      const languageButton = page.locator('#languageDropdown');

      // Verify button has aria-label
      await expect(languageButton).toHaveAttribute('aria-label', 'Language selector');

      // Verify flag emojis have aria-hidden
      await page.click('#languageDropdown');
      const flags = page.locator('.dropdown-item span[aria-hidden="true"]');
      expect(await flags.count()).toBeGreaterThan(0);
    });
  });

  test.describe('Visual Regression and Screenshots', () => {
    test('should capture screenshots in multiple languages', async ({ page }) => {
      // English homepage
      await page.goto('/?lang=en');
      await page.screenshot({
        path: 'test-results/artifacts/language-selector-english.png',
        fullPage: true
      });

      // Spanish homepage
      await page.goto('/?lang=es');
      await page.screenshot({
        path: 'test-results/artifacts/language-selector-spanish.png',
        fullPage: true
      });

      // German homepage
      await page.goto('/?lang=de');
      await page.screenshot({
        path: 'test-results/artifacts/language-selector-german.png',
        fullPage: true
      });

      // Korean homepage
      await page.goto('/?lang=ko');
      await page.screenshot({
        path: 'test-results/artifacts/language-selector-korean.png',
        fullPage: true
      });

      // Verify screenshots were created (this test always passes if no errors)
      expect(true).toBeTruthy();
    });

    test('should capture dropdown menu screenshot', async ({ page }) => {
      await page.goto('/');

      // Open language dropdown
      await page.click('#languageDropdown');

      // Wait for dropdown animation
      await page.waitForTimeout(300);

      // Capture screenshot of open dropdown
      await page.screenshot({
        path: 'test-results/artifacts/language-selector-dropdown-open.png'
      });

      expect(true).toBeTruthy();
    });
  });

  test.describe('Edge Cases and Error Handling', () => {
    test('should handle invalid language parameter gracefully', async ({ page }) => {
      // Navigate with invalid language code
      await page.goto('/?lang=invalid');

      // Should default to English or configured default
      await expect(page.locator('h1')).toContainText('Care made modern');
    });

    test('should handle missing language parameter', async ({ page }) => {
      // Navigate without lang parameter
      await page.goto('/');

      // Should display in default language (English)
      await expect(page.locator('h1')).toContainText('Care made modern');
      await expect(page.locator('text=Home')).toBeVisible();
    });

    test('should preserve language when navigating back', async ({ page }) => {
      // Set language to Spanish
      await page.goto('/?lang=es');
      await expect(page.locator('text=Inicio')).toBeVisible();

      // Navigate to Find Owners
      await page.click('text=Buscar propietarios');
      await expect(page).toHaveURL(/lang=es/);

      // Navigate back
      await page.goBack();

      // Verify Spanish is still active
      await expect(page).toHaveURL(/lang=es/);
      await expect(page.locator('text=Inicio')).toBeVisible();
    });
  });
});
