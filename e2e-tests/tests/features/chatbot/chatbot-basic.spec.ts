import { test, expect } from '@playwright/test';

test.describe('Chatbot Basic Functionality', () => {
  test('should show chatbot toggle button on homepage', async ({ page }) => {
    await page.goto('/');
    const toggleBtn = page.locator('#chatbot-toggle');
    await expect(toggleBtn).toBeVisible();
  });

  test('should expand chat widget when toggle clicked', async ({ page }) => {
    await page.goto('/');
    await page.click('#chatbot-toggle');
    const widget = page.locator('#chatbot-widget');
    await expect(widget).toBeVisible();
  });

  test('should send message and receive response', async ({ page }) => {
    await page.goto('/');
    await page.click('#chatbot-toggle');

    // Wait for input to be visible and enabled
    await expect(page.locator('#chatbot-input')).toBeVisible();
    await expect(page.locator('#chatbot-input')).toBeEnabled();

    await page.fill('#chatbot-input', 'Hello');
    await page.click('#chatbot-send');

    // Wait for typing indicator
    await expect(page.locator('#chatbot-typing')).toBeVisible({ timeout: 5000 });

    // Wait for response (AI message)
    await expect(page.locator('.chatbot-message-ai')).toBeVisible({ timeout: 10000 });
  });

  test('should show rate limit error after 11 messages', async ({ page }) => {
    await page.goto('/');
    await page.click('#chatbot-toggle');

    // Send 11 messages rapidly
    for (let i = 0; i < 11; i++) {
      await page.fill('#chatbot-input', `Message ${i}`);
      await page.click('#chatbot-send');

      // Wait for the message to be sent before sending the next one
      // The input gets disabled during sending, so wait for it to be enabled again
      await page.waitForTimeout(200);
    }

    // Verify rate limit error appears - look for any error message in AI responses
    const lastMessage = page.locator('.chatbot-message-ai').last();
    await expect(lastMessage).toBeVisible({ timeout: 10000 });

    // The error message should contain something about rate limits
    const messageText = await lastMessage.textContent();
    expect(messageText?.toLowerCase()).toContain('rate');
  });

  test('should persist conversation in session storage', async ({ page }) => {
    await page.goto('/');
    await page.click('#chatbot-toggle');

    // Wait for input to be ready
    await expect(page.locator('#chatbot-input')).toBeEnabled();

    await page.fill('#chatbot-input', 'Test message');
    await page.click('#chatbot-send');

    // Wait for the message to be sent and response received
    await expect(page.locator('.chatbot-message-user')).toBeVisible({ timeout: 5000 });

    // Wait a bit for session storage to be updated
    await page.waitForTimeout(500);

    // Check session storage for conversation history
    const history = await page.evaluate(() => sessionStorage.getItem('chatbot_conversation_history'));
    expect(history).toBeTruthy();

    // The history should contain at least the user message
    const historyArray = JSON.parse(history!);
    expect(historyArray.length).toBeGreaterThan(0);
  });

  test('should display chat in German language', async ({ page }) => {
    await page.goto('/?lang=de');
    await page.click('#chatbot-toggle');

    // Verify German text - the actual translation is "Tierklinik-Assistent"
    await expect(page.locator('.chatbot-header h5')).toContainText('Tierklinik-Assistent');
  });
});
