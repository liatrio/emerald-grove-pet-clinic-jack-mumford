import { test, expect } from '@fixtures/base-test';

import { VetPage } from '@pages/vet-page';

test.describe('Vet Directory', () => {
  test('can browse veterinarian list and view specialties', async ({ page }, testInfo) => {
    const vetPage = new VetPage(page);

    await vetPage.open();

    await expect(vetPage.vetsTable()).toBeVisible();

    // This test relies on Petclinic's startup seed data providing vets.
    const rows = vetPage.vetsTable().locator('tbody tr');
    const rowCount = await rows.count();
    expect(rowCount, 'Expected seeded veterinarians to be present').toBeGreaterThan(0);

    await page.screenshot({ path: testInfo.outputPath('vet-directory.png'), fullPage: true });

    // Validate each row's specialty cell contains a known specialty or "none".
    for (let i = 0; i < rowCount; i++) {
      const specialtyCell = rows.nth(i).locator('td').nth(1);
      await expect(specialtyCell).toContainText(/none|surgery|dentistry|radiology|medicine/i);
    }
  });
});
