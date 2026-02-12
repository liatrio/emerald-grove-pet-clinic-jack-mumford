import { test, expect } from '@fixtures/base-test';

import { OwnerPage } from '@pages/owner-page';
import { createOwner } from '@utils/data-factory';

/**
 * End-to-End Tests for Owner Duplicate Prevention (Issue #6)
 *
 * These tests verify that the application prevents duplicate owner records
 * based on matching first name, last name, and telephone number.
 *
 * Duplicate Rule: Exact match on all three fields (case-insensitive for names)
 */
test.describe('Owner Duplicate Prevention', () => {
  test('should prevent creating duplicate owner with exact match', async ({ page }, testInfo) => {
    const ownerPage = new OwnerPage(page);
    const owner = createOwner({
      firstName: 'John',
      lastName: 'Doe',
      address: '123 Main St',
      city: 'Springfield',
      telephone: '5551234567'
    });

    // Arrange: Create first owner
    await ownerPage.openFindOwners();
    await ownerPage.clickAddOwner();
    await ownerPage.fillOwnerForm(owner);
    await ownerPage.submitOwnerForm();

    // Verify first owner created successfully
    await expect(page.getByRole('heading', { name: /Owner Information/i })).toBeVisible();
    await expect(page.getByRole('cell', { name: `${owner.firstName} ${owner.lastName}` })).toBeVisible();

    await page.screenshot({ path: testInfo.outputPath('first-owner-created.png'), fullPage: true });

    // Act: Attempt to create duplicate with same first name, last name, and telephone
    await ownerPage.openFindOwners();
    await ownerPage.clickAddOwner();

    const duplicateOwner = {
      ...owner,
      address: '456 Elm St', // Different address (should not matter)
      city: 'Different City' // Different city (should not matter)
    };

    await ownerPage.fillOwnerForm(duplicateOwner);
    await ownerPage.submitOwnerForm();

    // Assert: Should stay on form with error message
    await expect(page).toHaveURL(/\/owners\/new/);
    await expect(page.getByText(/An owner with this information already exists/i)).toBeVisible();

    await page.screenshot({ path: testInfo.outputPath('duplicate-error-displayed.png'), fullPage: true });

    // Verify form retains user input
    await expect(page.getByLabel(/First Name/i)).toHaveValue(duplicateOwner.firstName);
    await expect(page.getByLabel(/Last Name/i)).toHaveValue(duplicateOwner.lastName);
    await expect(page.getByLabel(/Telephone/i)).toHaveValue(duplicateOwner.telephone);
  });

  test('should detect duplicate with different case (case-insensitive matching)', async ({ page }, testInfo) => {
    const ownerPage = new OwnerPage(page);
    const owner = createOwner({
      firstName: 'Alice',
      lastName: 'Smith',
      address: '789 Oak St',
      city: 'Madison',
      telephone: '5559876543'
    });

    // Arrange: Create first owner with capitalized names
    await ownerPage.openFindOwners();
    await ownerPage.clickAddOwner();
    await ownerPage.fillOwnerForm(owner);
    await ownerPage.submitOwnerForm();

    // Verify first owner created
    await expect(page.getByRole('heading', { name: /Owner Information/i })).toBeVisible();

    // Act: Attempt to create with different case
    await ownerPage.openFindOwners();
    await ownerPage.clickAddOwner();

    const caseVariantOwner = {
      firstName: 'alice', // lowercase
      lastName: 'SMITH', // uppercase
      address: '999 Different St',
      city: 'Different City',
      telephone: owner.telephone // Same phone
    };

    await ownerPage.fillOwnerForm(caseVariantOwner);
    await ownerPage.submitOwnerForm();

    // Assert: Duplicate detected despite case differences
    await expect(page).toHaveURL(/\/owners\/new/);
    await expect(page.getByText(/An owner with this information already exists/i)).toBeVisible();

    await page.screenshot({ path: testInfo.outputPath('case-insensitive-duplicate-error.png'), fullPage: true });
  });

  test('should allow creating owner with similar but different information', async ({ page }, testInfo) => {
    const ownerPage = new OwnerPage(page);
    const owner = createOwner({
      firstName: 'Bob',
      lastName: 'Jones',
      address: '111 Pine St',
      city: 'Chicago',
      telephone: '5551112222'
    });

    // Arrange: Create first owner
    await ownerPage.openFindOwners();
    await ownerPage.clickAddOwner();
    await ownerPage.fillOwnerForm(owner);
    await ownerPage.submitOwnerForm();

    // Verify first owner created
    await expect(page.getByRole('heading', { name: /Owner Information/i })).toBeVisible();
    await expect(page.getByRole('cell', { name: `${owner.firstName} ${owner.lastName}` })).toBeVisible();

    // Act: Create similar owner with different phone number
    await ownerPage.openFindOwners();
    await ownerPage.clickAddOwner();

    const similarOwner = {
      firstName: owner.firstName, // Same first name
      lastName: owner.lastName, // Same last name
      address: owner.address, // Same address
      city: owner.city, // Same city
      telephone: '9999999999' // Different phone number
    };

    await ownerPage.fillOwnerForm(similarOwner);
    await ownerPage.submitOwnerForm();

    // Assert: Should succeed (different phone = not duplicate)
    await expect(page).toHaveURL(/\/owners\/\d+/);
    await expect(page.getByRole('heading', { name: /Owner Information/i })).toBeVisible();
    await expect(page.getByRole('cell', { name: `${similarOwner.firstName} ${similarOwner.lastName}` })).toBeVisible();

    await page.screenshot({ path: testInfo.outputPath('non-duplicate-owner-created.png'), fullPage: true });
  });

  test('should allow creating owner with different name but same phone', async ({ page }) => {
    const ownerPage = new OwnerPage(page);
    const owner = createOwner({
      firstName: 'Charlie',
      lastName: 'Brown',
      telephone: '5553334444'
    });

    // Arrange: Create first owner
    await ownerPage.openFindOwners();
    await ownerPage.clickAddOwner();
    await ownerPage.fillOwnerForm(owner);
    await ownerPage.submitOwnerForm();

    // Verify first owner created
    await expect(page.getByRole('heading', { name: /Owner Information/i })).toBeVisible();

    // Act: Create owner with different name but same phone
    await ownerPage.openFindOwners();
    await ownerPage.clickAddOwner();

    const differentNameOwner = {
      firstName: 'David', // Different first name
      lastName: owner.lastName, // Same last name
      address: owner.address,
      city: owner.city,
      telephone: owner.telephone // Same phone
    };

    await ownerPage.fillOwnerForm(differentNameOwner);
    await ownerPage.submitOwnerForm();

    // Assert: Should succeed (different first name = not duplicate)
    await expect(page).toHaveURL(/\/owners\/\d+/);
    await expect(page.getByRole('heading', { name: /Owner Information/i })).toBeVisible();
  });

  test('should show error immediately without creating duplicate record', async ({ page }) => {
    const ownerPage = new OwnerPage(page);
    const owner = createOwner({
      firstName: 'Eve',
      lastName: 'Wilson',
      telephone: '5557778888'
    });

    // Arrange: Create first owner
    await ownerPage.openFindOwners();
    await ownerPage.clickAddOwner();
    await ownerPage.fillOwnerForm(owner);
    await ownerPage.submitOwnerForm();

    // Get owner ID from URL
    const ownerDetailsUrl = page.url();
    const ownerId = ownerDetailsUrl.match(/\/owners\/(\d+)/)?.[1];
    expect(ownerId).toBeDefined();

    // Act: Attempt to create duplicate
    await ownerPage.openFindOwners();
    await ownerPage.clickAddOwner();
    await ownerPage.fillOwnerForm(owner);
    await ownerPage.submitOwnerForm();

    // Assert: Error shown, no redirect (stays on form)
    await expect(page).toHaveURL(/\/owners\/new/);
    await expect(page.getByText(/An owner with this information already exists/i)).toBeVisible();

    // Verify no second owner was created by searching
    await ownerPage.openFindOwners();
    await ownerPage.searchByLastName(owner.lastName);

    // Should only find one owner
    await expect(ownerPage.ownersTable().getByRole('link', { name: new RegExp(owner.lastName) })).toHaveCount(1);
  });
});
