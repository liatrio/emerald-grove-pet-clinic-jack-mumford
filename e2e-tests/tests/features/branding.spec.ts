import { test, expect } from '@fixtures/base-test';

import { HomePage } from '@pages/home-page';
import { OwnerPage } from '@pages/owner-page';

test('Branding uses Emerald Grove logo, colors, and typography', async ({ page }) => {
  const homePage = new HomePage(page);
  const ownerPage = new OwnerPage(page);

  await homePage.open();

  const brandLogo = page.locator('.navbar-brand-logo');
  await expect(brandLogo).toHaveCSS('background-image', /emerald-grove/);

  const body = page.locator('body');
  await expect(body).toHaveCSS('font-family', /DM Sans/);
  await expect(body).toHaveCSS('color', 'rgb(248, 249, 250)');

  await ownerPage.openFindOwners();

  await expect(page.locator('h2').first()).toHaveCSS('font-family', /DM Sans/);

  const primaryButton = page.getByRole('button', { name: /Find Owner/i });
  await expect(primaryButton).toHaveCSS('background-color', 'rgb(36, 174, 29)');
  await expect(primaryButton).toHaveCSS('color', 'rgb(17, 17, 17)');
});
