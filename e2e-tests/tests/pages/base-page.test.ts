import { test, expect } from '@playwright/test';

import { HomePage } from './home-page';
import { OwnerPage } from './owner-page';
import { VetPage } from './vet-page';

test('BasePage navigation links route to expected pages', async ({ page }) => {
  const homePage = new HomePage(page);
  const ownerPage = new OwnerPage(page);
  const vetPage = new VetPage(page);

  await homePage.open();

  await homePage.goFindOwners();
  await expect(ownerPage.heading()).toHaveText(/Find Owners/i);

  await ownerPage.goVeterinarians();
  await expect(vetPage.heading()).toBeVisible();

  await vetPage.goHome();
  await expect(page).toHaveURL(/\/$/);
});
