import { test, expect } from '@playwright/test';

test.describe('Visit Scheduling', () => {
  test('can schedule a visit for an existing pet', async ({ page }) => {
    // Note: searching by last name may redirect directly to owner details when there is a single match.
    // Use a stable direct URL to avoid depending on the owners list table.
    await page.goto('/owners/1');
    await expect(page.getByRole('heading', { name: /Owner Information/i })).toBeVisible();

    // There is at least one pet; click first Add Visit link
    await page.getByRole('link', { name: /Add Visit/i }).first().click();

    await expect(page.getByRole('heading', { name: /Visit/i })).toBeVisible();

    const description = `E2E visit ${Date.now()}`;
    await page.locator('input#date').fill('2024-02-02');
    await page.locator('input#description').fill(description);

    await page.screenshot({ path: 'test-results/visit-scheduling-form.png', fullPage: true });

    await page.getByRole('button', { name: /Add Visit/i }).click();

    await expect(page.getByRole('heading', { name: /Pets and Visits/i })).toBeVisible();
    const matchingDates = await page.getByText('2024-02-02').count();
    expect(matchingDates).toBeGreaterThan(0);
    const matchingDescriptions = await page.getByText(description).count();
    expect(matchingDescriptions).toBeGreaterThan(0);
  });

  test('validates visit description is required', async ({ page }) => {
    await page.goto('/owners/1');
    await expect(page.getByRole('heading', { name: /Owner Information/i })).toBeVisible();

    await page.getByRole('link', { name: /Add Visit/i }).first().click();

    await page.locator('input#date').fill('2024-03-03');
    await page.getByRole('button', { name: /Add Visit/i }).click();

    await expect(page.locator('.help-inline').filter({ hasText: /must not be blank/i }).first()).toBeVisible();
  });
});
