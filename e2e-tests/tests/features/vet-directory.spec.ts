import { test, expect } from '@playwright/test';

import { VetPage } from '@pages/vet-page';

test.describe('Vet Directory', () => {
  test('can browse veterinarian list and view specialties', async ({ page }) => {
    const vetPage = new VetPage(page);

    await vetPage.open();

    await expect(vetPage.vetsTable()).toBeVisible();
    const rowCount = await vetPage.vetsTable().locator('tbody tr').count();
    expect(rowCount).toBeGreaterThan(0);

    await page.screenshot({ path: 'test-results/vet-directory.png', fullPage: true });

    // Basic specialty presence assertion: either a specialty name or the word 'none'
    await expect(vetPage.vetsTable()).toContainText(/none|surgery|dentistry|radiology|medicine/i);
  });
});
