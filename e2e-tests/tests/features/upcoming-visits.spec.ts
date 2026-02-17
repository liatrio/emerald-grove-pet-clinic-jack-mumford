import { test, expect } from '@playwright/test';

test.describe('Upcoming Visits Page', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/');
  });

  test('can navigate to Upcoming Visits page from main navigation', async ({ page }) => {
    // Click the Upcoming Visits link in navigation
    await page.getByRole('link', { name: /Upcoming Visits/i }).click();

    // Verify we're on the correct page
    await expect(page).toHaveURL(/\/visits\/upcoming/);
    await expect(page.getByRole('heading', { name: /Upcoming Visits/i })).toBeVisible();
  });

  test('displays page title and subtitle', async ({ page }) => {
    await page.goto('/visits/upcoming');

    // Check for title
    await expect(page.getByRole('heading', { name: /Upcoming Visits/i })).toBeVisible();

    // Check for subtitle
    await expect(page.getByText(/All scheduled future visits sorted chronologically/i)).toBeVisible();
  });

  test('displays visits table with correct columns when visits exist', async ({ page }) => {
    // Use historical date to show visits from test data
    await page.goto('/visits/upcoming?fromDate=2013-01-01');

    // Check for table headers (only visible when visits exist)
    await expect(page.getByRole('columnheader', { name: /Visit Date/i })).toBeVisible();
    await expect(page.getByRole('columnheader', { name: /Pet Name/i })).toBeVisible();
    await expect(page.getByRole('columnheader', { name: /Owner Name/i })).toBeVisible();
    await expect(page.getByRole('columnheader', { name: /Description/i })).toBeVisible();
  });

  test('displays filter form with all filter inputs', async ({ page }) => {
    await page.goto('/visits/upcoming');

    // Check for filter form elements
    await expect(page.getByLabel(/From Date/i)).toBeVisible();
    await expect(page.getByLabel(/To Date/i)).toBeVisible();
    await expect(page.getByLabel(/Pet Type/i)).toBeVisible();
    await expect(page.getByLabel(/Owner Last Name/i)).toBeVisible();

    // Check for action buttons
    await expect(page.getByRole('button', { name: /Apply Filters/i })).toBeVisible();
    await expect(page.getByRole('link', { name: /Clear Filters/i })).toBeVisible();
  });

  test('can filter visits by date range', async ({ page }) => {
    await page.goto('/visits/upcoming');

    // Set date range filter
    const fromDate = '2013-01-02';
    const toDate = '2013-01-03';

    await page.getByLabel(/From Date/i).fill(fromDate);
    await page.getByLabel(/To Date/i).fill(toDate);
    await page.getByRole('button', { name: /Apply Filters/i }).click();

    // Verify URL contains query parameters
    await expect(page).toHaveURL(/fromDate=2013-01-02/);
    await expect(page).toHaveURL(/toDate=2013-01-03/);

    // Verify filter values are preserved in form
    await expect(page.getByLabel(/From Date/i)).toHaveValue(fromDate);
    await expect(page.getByLabel(/To Date/i)).toHaveValue(toDate);
  });

  test('can filter visits by pet type', async ({ page }) => {
    await page.goto('/visits/upcoming');

    // Select a pet type from dropdown
    await page.getByLabel(/Pet Type/i).selectOption('cat');
    await page.getByRole('button', { name: /Apply Filters/i }).click();

    // Verify URL contains query parameter
    await expect(page).toHaveURL(/petType=cat/);

    // Verify filter value is preserved in dropdown
    await expect(page.getByLabel(/Pet Type/i)).toHaveValue('cat');
  });

  test('can filter visits by owner last name', async ({ page }) => {
    await page.goto('/visits/upcoming');

    // Enter owner name search
    const ownerName = 'coleman';
    await page.getByLabel(/Owner Last Name/i).fill(ownerName);
    await page.getByRole('button', { name: /Apply Filters/i }).click();

    // Verify URL contains query parameter
    await expect(page).toHaveURL(/ownerLastName=coleman/);

    // Verify filter value is preserved in input
    await expect(page.getByLabel(/Owner Last Name/i)).toHaveValue(ownerName);
  });

  test('can apply multiple filters together', async ({ page }) => {
    await page.goto('/visits/upcoming');

    // Apply multiple filters
    await page.getByLabel(/From Date/i).fill('2013-01-01');
    await page.getByLabel(/To Date/i).fill('2013-01-04');
    await page.getByLabel(/Pet Type/i).selectOption('cat');
    await page.getByLabel(/Owner Last Name/i).fill('coleman');
    await page.getByRole('button', { name: /Apply Filters/i }).click();

    // Verify all filters are in URL
    await expect(page).toHaveURL(/fromDate=2013-01-01/);
    await expect(page).toHaveURL(/toDate=2013-01-04/);
    await expect(page).toHaveURL(/petType=cat/);
    await expect(page).toHaveURL(/ownerLastName=coleman/);
  });

  test('can clear all filters', async ({ page }) => {
    await page.goto('/visits/upcoming?fromDate=2013-01-01&petType=cat&ownerLastName=coleman');

    // Click Clear Filters button
    await page.getByRole('link', { name: /Clear Filters/i }).click();

    // Verify we're back at base URL without query parameters
    await expect(page).toHaveURL(/^[^?]*$/); // No query string

    // Verify all filter inputs are cleared/reset
    await expect(page.getByLabel(/Pet Type/i)).toHaveValue('');
    await expect(page.getByLabel(/Owner Last Name/i)).toHaveValue('');
  });

  test('displays empty state when no visits match filters', async ({ page }) => {
    await page.goto('/visits/upcoming');

    // Apply filters that should return no results (future date)
    await page.getByLabel(/From Date/i).fill('2030-01-01');
    await page.getByRole('button', { name: /Apply Filters/i }).click();

    // Check for empty state message
    await expect(page.getByText(/No upcoming visits scheduled/i)).toBeVisible();
  });

  test('is responsive on mobile viewport', async ({ page }) => {
    // Set mobile viewport
    await page.setViewportSize({ width: 375, height: 667 });
    await page.goto('/visits/upcoming');

    // Verify page is visible and interactive
    await expect(page.getByRole('heading', { name: /Upcoming Visits/i })).toBeVisible();
    await expect(page.getByLabel(/From Date/i)).toBeVisible();
    await expect(page.getByRole('button', { name: /Apply Filters/i })).toBeVisible();
  });

  test('supports keyboard navigation through filter form', async ({ page }) => {
    await page.goto('/visits/upcoming');

    // Tab through form elements
    await page.keyboard.press('Tab'); // Should focus on first interactive element
    await page.keyboard.press('Tab');
    await page.keyboard.press('Tab');
    await page.keyboard.press('Tab');

    // Verify focus is visible (check that an input or button is focused)
    const focusedElement = await page.locator(':focus');
    await expect(focusedElement).toBeVisible();
  });

  test('filter values persist after page reload', async ({ page }) => {
    await page.goto('/visits/upcoming');

    // Apply filters
    await page.getByLabel(/From Date/i).fill('2013-01-02');
    await page.getByLabel(/Pet Type/i).selectOption('cat');
    await page.getByRole('button', { name: /Apply Filters/i }).click();

    // Reload the page
    await page.reload();

    // Verify filters are still applied
    await expect(page.getByLabel(/From Date/i)).toHaveValue('2013-01-02');
    await expect(page.getByLabel(/Pet Type/i)).toHaveValue('cat');
  });
});
