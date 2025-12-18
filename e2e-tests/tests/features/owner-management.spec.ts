import { test, expect } from '@fixtures/base-test';

import { OwnerPage } from '@pages/owner-page';
import { createOwner } from '@utils/data-factory';
import { measureMs } from '@utils/test-helpers';

test.describe('Owner Management', () => {
  test('can search for an existing owner and view pets/visits', async ({ page }, testInfo) => {
    const ownerPage = new OwnerPage(page);

    await ownerPage.openFindOwners();

    const { durationMs } = await measureMs(async () => {
      await ownerPage.searchByLastName('Davis');
      await expect(ownerPage.ownersTable()).toBeVisible();
    });

    await page.screenshot({ path: testInfo.outputPath('owner-search-results.png'), fullPage: true });

    expect(durationMs).toBeLessThan(3_000);

    await ownerPage.openOwnerDetailsByName('Betty Davis');
    await expect(page.getByRole('heading', { name: /Owner Information/i })).toBeVisible();
    await expect(page.getByRole('heading', { name: /Pets and Visits/i })).toBeVisible();
  });

  test('can add a new owner and then edit owner info', async ({ page }, testInfo) => {
    const ownerPage = new OwnerPage(page);
    const owner = createOwner();

    await ownerPage.openFindOwners();
    await ownerPage.clickAddOwner();

    await ownerPage.fillOwnerForm(owner);
    await page.screenshot({ path: testInfo.outputPath('new-owner-form-filled.png'), fullPage: true });

    await ownerPage.submitOwnerForm();

    await expect(page.getByRole('heading', { name: /Owner Information/i })).toBeVisible();
    await expect(page.getByRole('cell', { name: `${owner.firstName} ${owner.lastName}` })).toBeVisible();

    await ownerPage.clickEditOwner();

    const updatedCity = 'Updated City';
    await ownerPage.fillCity(updatedCity);
    await ownerPage.submitOwnerForm();

    await expect(page.getByRole('heading', { name: /Owner Information/i })).toBeVisible();
    await expect(page.getByRole('cell', { name: /Updated City/i })).toBeVisible();

    await page.screenshot({ path: testInfo.outputPath('owner-details-after-edit.png'), fullPage: true });
  });

  test('shows validation error for invalid telephone', async ({ page }) => {
    const ownerPage = new OwnerPage(page);
    const owner = createOwner({ telephone: '123' });

    await ownerPage.openFindOwners();
    await ownerPage.clickAddOwner();

    await ownerPage.fillOwnerForm(owner);
    await ownerPage.submitOwnerForm();

    await expect(page.getByText(/Telephone must be a 10-digit number/i)).toBeVisible();
  });

  test('owner form is usable in a mobile viewport', async ({ page }) => {
    const ownerPage = new OwnerPage(page);

    await page.setViewportSize({ width: 375, height: 812 });

    await ownerPage.openFindOwners();
    await ownerPage.clickAddOwner();

    await expect(page.getByRole('button', { name: /Add Owner/i })).toBeVisible();
  });
});
