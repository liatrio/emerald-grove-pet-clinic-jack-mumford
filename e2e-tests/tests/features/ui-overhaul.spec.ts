import { test, expect } from '@fixtures/base-test';

import { HomePage } from '@pages/home-page';
import { OwnerPage } from '@pages/owner-page';
import { VetPage } from '@pages/vet-page';

test('Liatrio-inspired layout and components are present', async ({ page }) => {
  const homePage = new HomePage(page);
  const ownerPage = new OwnerPage(page);
  const vetPage = new VetPage(page);

  await homePage.open();

  const hero = page.getByTestId('liatrio-hero');
  await expect(hero).toBeVisible();

  const featureCards = page.locator('.liatrio-feature-card');
  await expect(featureCards).toHaveCount(3);

  const tokens = await page.evaluate(() => {
    const styles = getComputedStyle(document.documentElement);
    return {
      space4: styles.getPropertyValue('--liatrio-space-4'),
      radiusLg: styles.getPropertyValue('--liatrio-radius-lg')
    };
  });
  expect(tokens.space4.trim()).not.toBe('');
  expect(tokens.radiusLg.trim()).not.toBe('');

  await ownerPage.openFindOwners();

  const formCard = page.locator('.liatrio-form-card');
  await expect(formCard).toBeVisible();

  await vetPage.goto('/vets.html');
  const vetTable = page.locator('.liatrio-table');
  await expect(vetTable).toBeVisible();
});

test('Dark theme and new clinic name are applied', async ({ page }) => {
  const homePage = new HomePage(page);

  await homePage.open();

  const hero = page.getByTestId('liatrio-hero');
  await expect(hero).toContainText('Emerald Grove Veterinary Clinic');

  const bodyBackground = await page.evaluate(() => {
    return getComputedStyle(document.body).backgroundColor;
  });
  expect(bodyBackground).toBe('rgb(26, 31, 35)');

  const cardBackground = await page.locator('.liatrio-feature-card').first().evaluate((element) => {
    return window.getComputedStyle(element).backgroundColor;
  });
  expect(cardBackground).toBe('rgb(30, 35, 39)');
});

test('Owners table text and rows are readable on dark theme', async ({ page }) => {
  const ownerPage = new OwnerPage(page);

  await ownerPage.openFindOwners();
  await page.getByRole('button', { name: /Find Owner/i }).click();

  const ownerTable = page.locator('table#owners');
  await expect(ownerTable).toBeVisible();

  const firstRowCell = ownerTable.locator('tbody tr').first().locator('td').nth(1);
  const cellColor = await firstRowCell.evaluate((element) => {
    return window.getComputedStyle(element).color;
  });
  expect(cellColor).toBe('rgb(248, 249, 250)');

  const rowBackground = await ownerTable.locator('tbody tr').first().evaluate((row) => {
    return window.getComputedStyle(row).backgroundColor;
  });
  expect(['rgb(26, 31, 35)', 'rgb(30, 35, 39)']).toContain(rowBackground);
});
