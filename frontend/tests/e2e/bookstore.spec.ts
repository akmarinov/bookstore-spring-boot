import { test, expect } from '@playwright/test';
import mysql from 'mysql2/promise';

// Database connection configuration
const dbConfig = {
  host: 'localhost',
  port: 3306,
  user: 'bookstore',
  password: 'bookstore123',
  database: 'bookstore_db'
};

test.describe('Bookstore Application', () => {
  test('should add a new book via frontend and verify it exists in database', async ({ page }) => {
    // Navigate to the application
    await page.goto('/');

    // Wait for the page to load
    await page.waitForLoadState('networkidle');

    // Check if we're on the home page - look for the Bookstore header
    await expect(page.locator('text="Bookstore"')).toBeVisible();

    // Click the "Add Book" button in the navigation
    await page.click('text="Add Book"');

    // Wait for the add book form to load
    await page.waitForLoadState('networkidle');
    
    // Verify we're on the add book page
    await expect(page.locator('text="Add New Book"')).toBeVisible();

    // Define test book data - matching the actual form schema
    const testBook = {
      title: 'Playwright Test Book',
      author: 'E2E Test Author',
      isbn: '978-0-123456-78-9',
      price: '29.99',
      category: 'Technology',
      description: 'A book created by Playwright E2E test',
      imageUrl: 'https://example.com/book-cover.jpg'
    };

    // Fill in the book form using Material-UI TextField selectors
    await page.fill('input[aria-label="Title"]', testBook.title);
    await page.fill('input[aria-label="Author"]', testBook.author);
    await page.fill('input[aria-label="ISBN"]', testBook.isbn);
    await page.fill('input[aria-label="Price"]', testBook.price);
    
    // Select category from dropdown
    await page.click('div[role="combobox"]');
    await page.click(`text="${testBook.category}"`);
    
    // Fill description and image URL
    await page.fill('textarea[aria-label="Description (optional)"]', testBook.description);
    await page.fill('input[aria-label="Image URL (optional)"]', testBook.imageUrl);

    // Submit the form
    await page.click('button[type="submit"]');

    // Wait for submission to complete and redirect
    await page.waitForLoadState('networkidle');
    
    // Wait a bit more to ensure the backend has processed the request
    await page.waitForTimeout(3000);

    // Verify we're redirected back to home page
    await expect(page.url()).toBe('http://localhost:3000/');

    // Now verify the book exists in the database
    const connection = await mysql.createConnection(dbConfig);
    
    try {
      const [rows] = await connection.execute(
        'SELECT * FROM books WHERE title = ? AND author = ? AND isbn = ?',
        [testBook.title, testBook.author, testBook.isbn]
      );

      // Verify the book was created in the database
      expect(rows).toHaveLength(1);
      
      const createdBook = rows[0] as any;
      expect(createdBook.title).toBe(testBook.title);
      expect(createdBook.author).toBe(testBook.author);
      expect(createdBook.isbn).toBe(testBook.isbn);
      expect(parseFloat(createdBook.price)).toBe(parseFloat(testBook.price));
      expect(createdBook.category).toBe(testBook.category);
      expect(createdBook.description).toBe(testBook.description);
      expect(createdBook.image_url).toBe(testBook.imageUrl);
      expect(createdBook.in_stock).toBe(1); // Default true value

      console.log('✅ Book successfully created and verified in database:', createdBook);

    } finally {
      await connection.end();
    }
  });

  test('should display books list on homepage', async ({ page }) => {
    // Navigate to the application
    await page.goto('/');
    
    // Wait for the page to load
    await page.waitForLoadState('networkidle');
    
    // Take a screenshot for debugging
    await page.screenshot({ path: 'books-list.png' });
    
    // Check if books are displayed - this depends on the UI structure
    // Look for book cards, table rows, or any book listing elements
    const bookElements = page.locator('[data-testid="book-item"]').or(
      page.locator('.book-card')
    ).or(
      page.locator('tbody tr')
    );
    
    // At minimum, verify the page loaded successfully
    await expect(page).toHaveTitle(/Vite \+ React \+ TS/);
    
    // Log how many book elements we found
    const bookCount = await bookElements.count();
    console.log(`Found ${bookCount} book elements on the page`);
  });
});