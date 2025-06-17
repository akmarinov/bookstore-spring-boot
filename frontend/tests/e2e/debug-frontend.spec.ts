import { test, expect } from '@playwright/test';

test.describe('Debug Frontend Issues', () => {
  test('should capture detailed frontend loading issues', async ({ page }) => {
    const consoleMessages: string[] = [];
    const networkErrors: string[] = [];
    const networkRequests: string[] = [];

    // Capture console messages
    page.on('console', msg => {
      const message = `[${msg.type().toUpperCase()}] ${msg.text()}`;
      console.log('Console:', message);
      consoleMessages.push(message);
    });

    // Capture network requests and errors
    page.on('request', request => {
      networkRequests.push(`REQUEST: ${request.method()} ${request.url()}`);
    });

    page.on('response', response => {
      const message = `RESPONSE: ${response.status()} ${response.url()}`;
      console.log('Network:', message);
      if (!response.ok()) {
        networkErrors.push(message);
      }
    });

    // Capture JavaScript errors
    page.on('pageerror', error => {
      const message = `PAGE ERROR: ${error.message}`;
      console.log('Page Error:', message);
      consoleMessages.push(message);
    });

    console.log('Navigating to frontend...');
    
    try {
      // Navigate to the application
      await page.goto('/', { waitUntil: 'networkidle' });

      // Wait for any potential async operations
      await page.waitForTimeout(5000);

      // Check if React has rendered anything
      const rootContent = await page.locator('#root').innerHTML();
      console.log('Root innerHTML length:', rootContent.length);
      
      if (rootContent.length > 0) {
        console.log('Root content preview:', rootContent.substring(0, 500));
      } else {
        console.log('Root div is empty - React likely failed to mount');
      }

      // Try to detect if React is loaded at all
      const reactExists = await page.evaluate(() => {
        return typeof window !== 'undefined' && 
               (window as any).React !== undefined ||
               document.querySelector('[data-reactroot]') !== null ||
               document.querySelector('#root').hasChildNodes();
      });

      console.log('React appears to be loaded:', reactExists);

      // Check for specific error patterns
      const hasReactErrors = consoleMessages.some(msg => 
        msg.includes('React') || 
        msg.includes('TypeError') || 
        msg.includes('SyntaxError') ||
        msg.includes('Failed to fetch')
      );

      console.log('Has React-related errors:', hasReactErrors);

      // Take screenshot for visual debugging
      await page.screenshot({ path: 'debug-frontend.png', fullPage: true });

      // Log all captured information
      console.log('\n=== CONSOLE MESSAGES ===');
      consoleMessages.forEach(msg => console.log(msg));
      
      console.log('\n=== NETWORK ERRORS ===');
      networkErrors.forEach(err => console.log(err));

      console.log('\n=== NETWORK REQUESTS ===');
      networkRequests.forEach(req => console.log(req));

      // Check if backend is accessible from frontend perspective
      try {
        const backendResponse = await page.request.get('http://localhost:8080/api/books');
        console.log('Backend API accessible:', backendResponse.ok(), backendResponse.status());
      } catch (error) {
        console.log('Backend API error:', error);
      }

    } catch (error) {
      console.log('Navigation error:', error);
    }

    // The test passes regardless - we're just debugging
    expect(true).toBe(true);
  });
});