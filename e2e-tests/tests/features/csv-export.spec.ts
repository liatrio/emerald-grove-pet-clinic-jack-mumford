import { test, expect } from '@fixtures/base-test';

import { OwnerPage } from '@pages/owner-page';
import * as fs from 'fs';

test.describe('CSV Export', () => {
  test('should download CSV file with correct filename format', async ({ page }, testInfo) => {
    const ownerPage = new OwnerPage(page);

    // Navigate to owners page
    await ownerPage.openFindOwners();
    await ownerPage.searchByLastName('');
    await expect(ownerPage.ownersTable()).toBeVisible();

    // Take screenshot showing the Export button
    await page.screenshot({ path: testInfo.outputPath('owners-list-with-export-button.png'), fullPage: true });

    // Wait for download event and trigger CSV export
    const downloadPromise = page.waitForEvent('download');
    await page.click('a:has-text("Export to CSV")');
    const download = await downloadPromise;

    // Verify filename matches pattern: owners-export-YYYY-MM-DD.csv
    const filename = download.suggestedFilename();
    expect(filename).toMatch(/owners-export-\d{4}-\d{2}-\d{2}\.csv/);

    // Save the downloaded file for inspection
    const downloadPath = testInfo.outputPath(filename);
    await download.saveAs(downloadPath);

    testInfo.attach('csv-download', {
      path: downloadPath,
      contentType: 'text/csv',
    });
  });

  test('should export only filtered results when searching by lastName', async ({ page }, testInfo) => {
    const ownerPage = new OwnerPage(page);

    // Navigate to owners page and search for "Franklin"
    await ownerPage.openFindOwners();
    await ownerPage.searchByLastName('Franklin');

    // Wait for search results
    await expect(page.getByRole('heading', { name: /Owner Information/i })).toBeVisible();

    // Go back to find owners to see the list
    await ownerPage.openFindOwners();
    await ownerPage.searchByLastName('Franklin');

    // For single result, we're redirected to owner details, so let's search for Davis instead
    // which has multiple results
    await ownerPage.openFindOwners();
    await ownerPage.searchByLastName('Davis');
    await expect(ownerPage.ownersTable()).toBeVisible();

    // Verify Export button includes lastName parameter
    const exportLink = page.locator('a:has-text("Export to CSV")');
    await expect(exportLink).toBeVisible();
    const href = await exportLink.getAttribute('href');
    expect(href).toContain('lastName=Davis');

    // Download and verify CSV contains only Davis owners
    const downloadPromise = page.waitForEvent('download');
    await exportLink.click();
    const download = await downloadPromise;

    const filename = download.suggestedFilename();
    const downloadPath = testInfo.outputPath(filename);
    await download.saveAs(downloadPath);

    // Read and verify CSV content
    const csvContent = fs.readFileSync(downloadPath, 'utf-8');

    // Verify header row exists
    expect(csvContent).toContain('First Name,Last Name,Address,City,Telephone');

    // Verify Davis owners are in the CSV
    expect(csvContent).toContain('Davis');

    // Verify no Franklin owners (since we filtered by Davis)
    expect(csvContent).not.toContain('Franklin');

    testInfo.attach('csv-filtered-download', {
      path: downloadPath,
      contentType: 'text/csv',
    });
  });

  test('should export all owners when no filter is applied', async ({ page }, testInfo) => {
    const ownerPage = new OwnerPage(page);

    // Navigate to owners page without filter
    await ownerPage.openFindOwners();
    await ownerPage.searchByLastName('');
    await expect(ownerPage.ownersTable()).toBeVisible();

    // Download CSV
    const downloadPromise = page.waitForEvent('download');
    await page.click('a:has-text("Export to CSV")');
    const download = await downloadPromise;

    const filename = download.suggestedFilename();
    const downloadPath = testInfo.outputPath(filename);
    await download.saveAs(downloadPath);

    // Read and verify CSV content
    const csvContent = fs.readFileSync(downloadPath, 'utf-8');

    // Verify header row
    expect(csvContent).toContain('First Name,Last Name,Address,City,Telephone');

    // Verify multiple owners are present (at least 5 owners in test data)
    const lines = csvContent.split('\n').filter((line) => line.trim() !== '');
    expect(lines.length).toBeGreaterThan(5); // Header + at least 5 owners

    // Verify some known owners
    expect(csvContent).toContain('Davis');
    expect(csvContent).toContain('Franklin');

    testInfo.attach('csv-all-owners', {
      path: downloadPath,
      contentType: 'text/csv',
    });
  });

  test('CSV export button has correct styling and icon', async ({ page }) => {
    const ownerPage = new OwnerPage(page);

    // Navigate to owners page
    await ownerPage.openFindOwners();
    await ownerPage.searchByLastName('');
    await expect(ownerPage.ownersTable()).toBeVisible();

    // Verify button styling
    const exportButton = page.locator('a:has-text("Export to CSV")');
    await expect(exportButton).toBeVisible();

    // Verify button has correct classes
    const classes = await exportButton.getAttribute('class');
    expect(classes).toContain('btn');
    expect(classes).toContain('btn-secondary');

    // Verify Font Awesome icon is present
    const icon = exportButton.locator('i.fa-download');
    await expect(icon).toBeVisible();
  });

  test('CSV export respects special characters in addresses', async ({ page }, testInfo) => {
    const ownerPage = new OwnerPage(page);

    // Navigate to owners page
    await ownerPage.openFindOwners();
    await ownerPage.searchByLastName('');
    await expect(ownerPage.ownersTable()).toBeVisible();

    // Download CSV
    const downloadPromise = page.waitForEvent('download');
    await page.click('a:has-text("Export to CSV")');
    const download = await downloadPromise;

    const filename = download.suggestedFilename();
    const downloadPath = testInfo.outputPath(filename);
    await download.saveAs(downloadPath);

    // Read and verify CSV content handles commas properly
    const csvContent = fs.readFileSync(downloadPath, 'utf-8');

    // Verify CSV is properly formatted (no broken lines due to unescaped commas)
    const lines = csvContent.split('\n').filter((line) => line.trim() !== '');

    // Each line should have 5 fields (comma-separated, but quoted if field contains commas)
    for (const line of lines) {
      if (line.startsWith('First Name')) continue; // Skip header

      // Count fields by parsing CSV (respecting quotes)
      const fields = line.match(/(".*?"|[^,]+)(?=\s*,|\s*$)/g) || [];
      expect(fields.length).toBe(5); // First Name, Last Name, Address, City, Telephone
    }
  });
});
