import { describe, it, expect, beforeEach } from 'vitest';
import { bookApi } from '../../api/bookApi';
import { server } from '../../test/mocks/server';
import { http, HttpResponse } from 'msw';
import { mockBooks, mockBook, mockPaginatedResponse } from '../../test/mocks/mockData';
import type { BookFormData } from '../../types/Book';

const BASE_URL = 'http://localhost:8080/api/v1';

describe('bookApi', () => {
  beforeEach(() => {
    server.resetHandlers();
  });

  describe('getBooks', () => {
    it('fetches books successfully', async () => {
      const result = await bookApi.getBooks();
      
      expect(result).toEqual(mockPaginatedResponse);
      expect(result.content).toHaveLength(3);
      expect(result.totalElements).toBe(3);
    });

    it('fetches books with pagination parameters', async () => {
      const params = { page: 1, size: 5 };
      const result = await bookApi.getBooks(params);
      
      expect(result.pageNumber).toBe(1);
      expect(result.pageSize).toBe(5);
    });

    it('fetches books with category filter', async () => {
      const params = { category: 'Fiction' };
      const result = await bookApi.getBooks(params);
      
      // All mock books are fiction, so we should get all of them
      expect(result.content).toHaveLength(3);
      expect(result.content.every(book => book.category === 'Fiction')).toBe(true);
    });

    it('fetches books with search parameter', async () => {
      const params = { search: 'Gatsby' };
      const result = await bookApi.getBooks(params);
      
      expect(result.content).toHaveLength(1);
      expect(result.content[0].title).toBe('The Great Gatsby');
    });

    it('handles API error gracefully', async () => {
      server.use(
        http.get(`${BASE_URL}/books`, () => {
          return HttpResponse.json(
            { message: 'Server error', status: 500 },
            { status: 500 }
          );
        })
      );

      await expect(bookApi.getBooks()).rejects.toThrow();
    });
  });

  describe('getBookById', () => {
    it('fetches a single book successfully', async () => {
      const result = await bookApi.getBookById(1);
      
      expect(result).toEqual(mockBook);
      expect(result.id).toBe(1);
    });

    it('handles book not found', async () => {
      server.use(
        http.get(`${BASE_URL}/books/999`, () => {
          return HttpResponse.json(
            { message: 'Book not found', status: 404 },
            { status: 404 }
          );
        })
      );

      await expect(bookApi.getBookById(999)).rejects.toThrow();
    });
  });

  describe('createBook', () => {
    it('creates a new book successfully', async () => {
      const newBookData: BookFormData = {
        title: 'New Test Book',
        author: 'Test Author',
        isbn: '978-0-123456-78-9',
        price: 19.99,
        category: 'Fiction',
        description: 'Test description',
        imageUrl: 'https://example.com/image.jpg',
        inStock: true,
      };

      const result = await bookApi.createBook(newBookData);
      
      expect(result.title).toBe(newBookData.title);
      expect(result.author).toBe(newBookData.author);
      expect(result.isbn).toBe(newBookData.isbn);
      expect(result.price).toBe(newBookData.price);
      expect(result.id).toBeDefined();
      expect(result.createdAt).toBeDefined();
      expect(result.updatedAt).toBeDefined();
    });

    it('handles validation errors', async () => {
      server.use(
        http.post(`${BASE_URL}/books`, () => {
          return HttpResponse.json(
            { message: 'Validation failed', status: 400 },
            { status: 400 }
          );
        })
      );

      const invalidBookData: BookFormData = {
        title: '',
        author: '',
        isbn: '',
        price: -1,
        category: '',
        inStock: true,
      };

      await expect(bookApi.createBook(invalidBookData)).rejects.toThrow();
    });
  });

  describe('updateBook', () => {
    it('updates an existing book successfully', async () => {
      const updatedBookData: BookFormData = {
        ...mockBook,
        title: 'Updated Title',
        price: 25.99,
      };

      const result = await bookApi.updateBook(1, updatedBookData);
      
      expect(result.title).toBe('Updated Title');
      expect(result.price).toBe(25.99);
      expect(result.id).toBe(1);
      expect(result.updatedAt).toBeDefined();
    });

    it('handles book not found for update', async () => {
      server.use(
        http.put(`${BASE_URL}/books/999`, () => {
          return HttpResponse.json(
            { message: 'Book not found', status: 404 },
            { status: 404 }
          );
        })
      );

      const updatedBookData: BookFormData = {
        ...mockBook,
        title: 'Updated Title',
      };

      await expect(bookApi.updateBook(999, updatedBookData)).rejects.toThrow();
    });
  });

  describe('deleteBook', () => {
    it('deletes a book successfully', async () => {
      await expect(bookApi.deleteBook(1)).resolves.toBeUndefined();
    });

    it('handles book not found for deletion', async () => {
      server.use(
        http.delete(`${BASE_URL}/books/999`, () => {
          return HttpResponse.json(
            { message: 'Book not found', status: 404 },
            { status: 404 }
          );
        })
      );

      await expect(bookApi.deleteBook(999)).rejects.toThrow();
    });
  });


  describe('getBooksByCategory', () => {
    it('fetches books by category successfully', async () => {
      const result = await bookApi.getBooksByCategory('Fiction');
      
      expect(result.content.every(book => book.category === 'Fiction')).toBe(true);
      expect(result.content).toHaveLength(3);
    });

    it('fetches books by category with pagination', async () => {
      const result = await bookApi.getBooksByCategory('Fiction', { page: 0, size: 2 });
      
      expect(result.pageNumber).toBe(0);
      expect(result.pageSize).toBe(2);
    });

    it('returns empty results for unknown category', async () => {
      const result = await bookApi.getBooksByCategory('UnknownCategory');
      
      expect(result.content).toHaveLength(0);
      expect(result.totalElements).toBe(0);
    });
  });


  describe('Error handling', () => {
    it('handles network errors', async () => {
      server.use(
        http.get(`${BASE_URL}/books`, () => {
          return HttpResponse.error();
        })
      );

      await expect(bookApi.getBooks()).rejects.toThrow();
    });

    it('handles timeout errors', async () => {
      server.use(
        http.get(`${BASE_URL}/books`, async () => {
          // Simulate a delayed response
          await new Promise(resolve => setTimeout(resolve, 100));
          return HttpResponse.json(mockPaginatedResponse);
        })
      );

      const result = await bookApi.getBooks();
      expect(result).toEqual(mockPaginatedResponse);
    });

    it('handles malformed response data', async () => {
      server.use(
        http.get(`${BASE_URL}/books`, () => {
          return HttpResponse.json(null);
        })
      );

      const result = await bookApi.getBooks();
      expect(result).toBeNull();
    });
  });
});