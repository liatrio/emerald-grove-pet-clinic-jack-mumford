import { test, expect } from '@fixtures/base-test';

test.describe('404 Error Handling', () => {
  test('should show friendly 404 page for non-existent owner', async ({ page }, testInfo) => {
    // Navigate to a non-existent owner ID
    await page.goto('/owners/99999');
    await page.waitForLoadState('networkidle');

    // Take screenshot for proof artifact
    await page.screenshot({ path: testInfo.outputPath('404-owner-not-found.png'), fullPage: true });

    // Assert friendly error message is visible
    await expect(page.getByRole('heading', { name: /couldn't find/i })).toBeVisible();
    await expect(page.getByText(/couldn't find that owner/i)).toBeVisible();

    // Assert no stack trace is visible
    await expect(page.getByText(/exception/i)).not.toBeVisible();
    await expect(page.getByText(/stack trace/i)).not.toBeVisible();
    await expect(page.getByText(/java\./i)).not.toBeVisible();

    // Assert "Find Owners" button exists and is visible (in error page, not nav)
    const findOwnersButton = page.locator('a.btn.btn-primary', { hasText: 'Find Owners' });
    await expect(findOwnersButton).toBeVisible();

    // Verify the link has the correct href
    await expect(findOwnersButton).toHaveAttribute('href', /\/owners\/find/);
  });

  test('should navigate to owner search from 404 page', async ({ page }, testInfo) => {
    // Navigate to a non-existent owner ID
    await page.goto('/owners/99999');
    await page.waitForLoadState('networkidle');

    // Click "Find Owners" button on error page
    await page.locator('a.btn.btn-primary', { hasText: 'Find Owners' }).click();

    // Assert navigation to search page
    await expect(page).toHaveURL(/\/owners\/find/);
    await expect(page.getByRole('heading', { name: /Find Owners/i })).toBeVisible();

    await page.screenshot({ path: testInfo.outputPath('404-navigation-to-search.png'), fullPage: true });
  });

  test('should show friendly 404 page for non-existent pet', async ({ page }, testInfo) => {
    // Navigate to a non-existent pet ID (using valid owner ID 1)
    await page.goto('/owners/1/pets/99999/edit');
    await page.waitForLoadState('networkidle');

    // Take screenshot for proof artifact
    await page.screenshot({ path: testInfo.outputPath('404-pet-not-found.png'), fullPage: true });

    // Assert friendly error message is visible
    await expect(page.getByRole('heading', { name: /couldn't find/i })).toBeVisible();
    await expect(page.getByText(/couldn't find that pet/i)).toBeVisible();

    // Assert no stack trace is visible
    await expect(page.getByText(/exception/i)).not.toBeVisible();
    await expect(page.getByText(/stack trace/i)).not.toBeVisible();
    await expect(page.getByText(/java\./i)).not.toBeVisible();

    // Assert "Find Owners" button exists and is visible (in error page, not nav)
    const findOwnersButton = page.locator('a.btn.btn-primary', { hasText: 'Find Owners' });
    await expect(findOwnersButton).toBeVisible();
  });

  test('should display error page with proper layout and branding', async ({ page }) => {
    // Navigate to a non-existent owner ID
    await page.goto('/owners/99999');
    await page.waitForLoadState('networkidle');

    // Verify Liatrio branding elements are present
    await expect(page.locator('.liatrio-section')).toBeVisible();
    await expect(page.locator('.liatrio-error-card')).toBeVisible();

    // Verify pets image is displayed
    const petsImage = page.locator('img[alt*="Pets"]');
    await expect(petsImage).toBeVisible();

    // Verify heading is present
    await expect(page.getByRole('heading', { name: /couldn't find/i })).toBeVisible();
  });
});
