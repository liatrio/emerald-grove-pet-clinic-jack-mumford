import { test, expect } from '@fixtures/base-test';

import { HomePage } from '@pages/home-page';
import { OwnerPage } from '@pages/owner-page';

test('Branding uses Liatrio logo, colors, and typography', async ({ page }) => {
  const homePage = new HomePage(page);
  const ownerPage = new OwnerPage(page);

  await homePage.open();

  const brandLogo = page.locator('.navbar-brand-logo');
  const brandBackgroundImage = await brandLogo.evaluate((element) => {
    return window.getComputedStyle(element).backgroundImage;
  });
  expect(brandBackgroundImage.toLowerCase()).toContain('emerald-grove');

  const bodyFontFamily = await page.locator('body').evaluate((element) => {
    return window.getComputedStyle(element).fontFamily;
  });
  expect(bodyFontFamily).toContain('DM Sans');

  const bodyTextColor = await page.locator('body').evaluate((element) => {
    return window.getComputedStyle(element).color;
  });
  expect(bodyTextColor).toBe('rgb(248, 249, 250)');

  await ownerPage.openFindOwners();

  const headingFontFamily = await page.locator('h2').first().evaluate((element) => {
    return window.getComputedStyle(element).fontFamily;
  });
  expect(headingFontFamily).toContain('DM Sans');

  const primaryButton = page.getByRole('button', { name: /Find Owner/i });
  const buttonBackgroundColor = await primaryButton.evaluate((element) => {
    return window.getComputedStyle(element).backgroundColor;
  });
  expect(buttonBackgroundColor).toBe('rgb(36, 174, 29)');

  const buttonTextColor = await primaryButton.evaluate((element) => {
    return window.getComputedStyle(element).color;
  });
  expect(buttonTextColor).toBe('rgb(17, 17, 17)');
});
