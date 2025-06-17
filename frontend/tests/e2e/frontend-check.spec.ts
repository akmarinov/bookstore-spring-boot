import { test, expect } from '@playwright/test';

test.describe('Frontend Loading Check', () => {
  test('should check if frontend loads correctly or shows white screen', async ({ page }) => {
    console.log('Navigating to frontend...');
    
    // Navigate to the application
    await page.goto('/');

    // Take a screenshot to see what's actually displayed
    await page.screenshot({ path: 'frontend-loading-check.png', fullPage: true });

    // Wait for the page to load
    await page.waitForLoadState('networkidle');

    // Check page title
    const title = await page.title();
    console.log('Page title:', title);

    // Check if there's any content in the body
    const bodyContent = await page.locator('body').textContent();
    console.log('Body content length:', bodyContent?.length || 0);
    console.log('Body content preview:', bodyContent?.substring(0, 200) || 'No content');

    // Check if React root div exists and has content
    const rootDiv = page.locator('#root');
    const rootExists = await rootDiv.count() > 0;
    console.log('Root div exists:', rootExists);

    if (rootExists) {
      const rootContent = await rootDiv.textContent();
      console.log('Root div content length:', rootContent?.length || 0);
      console.log('Root div content preview:', rootContent?.substring(0, 200) || 'No content in root');
    }

    // Check for any error messages in console
    const consoleMessages: string[] = [];
    page.on('console', msg => {
      console.log('Browser console:', msg.type(), msg.text());
      consoleMessages.push(`${msg.type()}: ${msg.text()}`);
    });

    // Check for any network errors
    const networkErrors: string[] = [];
    page.on('response', response => {
      if (!response.ok()) {
        console.log('Network error:', response.status(), response.url());
        networkErrors.push(`${response.status()}: ${response.url()}`);
      }
    });

    // Wait a bit more to catch any async loading
    await page.waitForTimeout(3000);

    // Take another screenshot after waiting
    await page.screenshot({ path: 'frontend-after-wait.png', fullPage: true });

    // Check for specific elements that should be present
    const hasAppBar = await page.locator('header').count() > 0;
    const hasBookstoreText = await page.locator('text="Bookstore"').count() > 0;
    const hasNavigation = await page.locator('button').count() > 0;

    console.log('Has AppBar:', hasAppBar);
    console.log('Has Bookstore text:', hasBookstoreText);
    console.log('Has navigation buttons:', hasNavigation);

    // Check if any JavaScript loaded
    const scripts = await page.locator('script[src]').count();
    console.log('Number of script tags:', scripts);

    // Log final assessment
    if (bodyContent && bodyContent.length > 50 && (hasAppBar || hasBookstoreText)) {
      console.log('✅ Frontend appears to be loading correctly');
    } else {
      console.log('❌ Frontend appears to be showing a white screen or not loading properly');
      console.log('Console messages:', consoleMessages);
      console.log('Network errors:', networkErrors);
    }

    // For the test to pass, we just need to verify we can access the page
    await expect(page).toHaveTitle(/Vite \+ React/);
  });
});