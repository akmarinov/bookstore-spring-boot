import { describe, it, expect, beforeEach, vi } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { useBook } from '../../hooks/useBook';
import { bookApi } from '../../api/bookApi';
import { mockBook } from '../../test/mocks/mockData';
import type { BookFormData } from '../../types/Book';

// Mock the bookApi
vi.mock('../../api/bookApi', () => ({
  bookApi: {
    getBookById: vi.fn(),
    createBook: vi.fn(),
    updateBook: vi.fn(),
    deleteBook: vi.fn(),
  },
}));

describe('useBook', () => {
  const mockGetBookById = vi.mocked(bookApi.getBookById);
  const mockCreateBook = vi.mocked(bookApi.createBook);
  const mockUpdateBook = vi.mocked(bookApi.updateBook);
  const mockDeleteBook = vi.mocked(bookApi.deleteBook);

  beforeEach(() => {
    vi.clearAllMocks();
    mockGetBookById.mockResolvedValue(mockBook);
    mockCreateBook.mockResolvedValue(mockBook);
    mockUpdateBook.mockResolvedValue(mockBook);
    mockDeleteBook.mockResolvedValue(undefined);
  });

  it('initializes with default state', () => {
    const { result } = renderHook(() => useBook());

    expect(result.current.book).toBe(null);
    expect(result.current.loading).toBe(false);
    expect(result.current.error).toBe(null);
  });

  it('fetches book automatically when bookId is provided', async () => {
    const { result } = renderHook(() => useBook(1));

    expect(result.current.loading).toBe(true);

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    expect(mockGetBookById).toHaveBeenCalledWith(1);
    expect(result.current.book).toEqual(mockBook);
    expect(result.current.error).toBe(null);
  });

  it('does not fetch book when no bookId is provided', () => {
    renderHook(() => useBook());
    expect(mockGetBookById).not.toHaveBeenCalled();
  });

  describe('fetchBook', () => {
    it('fetches book successfully', async () => {
      const { result } = renderHook(() => useBook());

      await result.current.fetchBook(1);

      expect(mockGetBookById).toHaveBeenCalledWith(1);
      expect(result.current.book).toEqual(mockBook);
      expect(result.current.error).toBe(null);
      expect(result.current.loading).toBe(false);
    });

    it('handles fetch book error', async () => {
      const errorMessage = 'Book not found';
      mockGetBookById.mockRejectedValue(new Error(errorMessage));

      const { result } = renderHook(() => useBook());

      await result.current.fetchBook(1);

      await waitFor(() => {
        expect(result.current.error).toBe(errorMessage);
      });

      expect(result.current.book).toBe(null);
      expect(result.current.loading).toBe(false);
    });

    it('sets loading state correctly during fetch', async () => {
      let resolvePromise: (value: any) => void;
      const pendingPromise = new Promise((resolve) => {
        resolvePromise = resolve;
      });
      mockGetBookById.mockReturnValue(pendingPromise);

      const { result } = renderHook(() => useBook());

      const fetchPromise = result.current.fetchBook(1);
      
      expect(result.current.loading).toBe(true);

      resolvePromise!(mockBook);
      await fetchPromise;

      expect(result.current.loading).toBe(false);
    });
  });

  describe('createBook', () => {
    const mockBookFormData: BookFormData = {
      title: 'New Book',
      author: 'New Author',
      isbn: '978-0-123456-78-9',
      price: 19.99,
      category: 'Fiction',
      description: 'A new book',
      imageUrl: 'https://example.com/image.jpg',
      inStock: true,
    };

    it('creates book successfully', async () => {
      const { result } = renderHook(() => useBook());

      const createdBook = await result.current.createBook(mockBookFormData);

      expect(mockCreateBook).toHaveBeenCalledWith(mockBookFormData);
      expect(createdBook).toEqual(mockBook);
      expect(result.current.book).toEqual(mockBook);
      expect(result.current.error).toBe(null);
      expect(result.current.loading).toBe(false);
    });

    it('handles create book error', async () => {
      const errorMessage = 'Failed to create book';
      mockCreateBook.mockRejectedValue(new Error(errorMessage));

      const { result } = renderHook(() => useBook());

      await expect(result.current.createBook(mockBookFormData)).rejects.toThrow(errorMessage);

      await waitFor(() => {
        expect(result.current.error).toBe(errorMessage);
      });

      expect(result.current.loading).toBe(false);
    });

    it('sets loading state correctly during create', async () => {
      let resolvePromise: (value: any) => void;
      const pendingPromise = new Promise((resolve) => {
        resolvePromise = resolve;
      });
      mockCreateBook.mockReturnValue(pendingPromise);

      const { result } = renderHook(() => useBook());

      const createPromise = result.current.createBook(mockBookFormData);
      
      expect(result.current.loading).toBe(true);

      resolvePromise!(mockBook);
      await createPromise;

      expect(result.current.loading).toBe(false);
    });
  });

  describe('updateBook', () => {
    const mockBookFormData: BookFormData = {
      title: 'Updated Book',
      author: 'Updated Author',
      isbn: '978-0-123456-78-9',
      price: 29.99,
      category: 'Fiction',
      description: 'An updated book',
      imageUrl: 'https://example.com/updated-image.jpg',
      inStock: false,
    };

    it('updates book successfully', async () => {
      const { result } = renderHook(() => useBook());

      const updatedBook = await result.current.updateBook(1, mockBookFormData);

      expect(mockUpdateBook).toHaveBeenCalledWith(1, mockBookFormData);
      expect(updatedBook).toEqual(mockBook);
      expect(result.current.book).toEqual(mockBook);
      expect(result.current.error).toBe(null);
      expect(result.current.loading).toBe(false);
    });

    it('handles update book error', async () => {
      const errorMessage = 'Failed to update book';
      mockUpdateBook.mockRejectedValue(new Error(errorMessage));

      const { result } = renderHook(() => useBook());

      await expect(result.current.updateBook(1, mockBookFormData)).rejects.toThrow(errorMessage);

      await waitFor(() => {
        expect(result.current.error).toBe(errorMessage);
      });

      expect(result.current.loading).toBe(false);
    });

    it('sets loading state correctly during update', async () => {
      let resolvePromise: (value: any) => void;
      const pendingPromise = new Promise((resolve) => {
        resolvePromise = resolve;
      });
      mockUpdateBook.mockReturnValue(pendingPromise);

      const { result } = renderHook(() => useBook());

      const updatePromise = result.current.updateBook(1, mockBookFormData);
      
      expect(result.current.loading).toBe(true);

      resolvePromise!(mockBook);
      await updatePromise;

      expect(result.current.loading).toBe(false);
    });
  });

  describe('deleteBook', () => {
    it('deletes book successfully', async () => {
      const { result } = renderHook(() => useBook());

      // Set a book first
      await result.current.fetchBook(1);
      expect(result.current.book).toEqual(mockBook);

      // Delete the book
      await result.current.deleteBook(1);

      expect(mockDeleteBook).toHaveBeenCalledWith(1);
      expect(result.current.book).toBe(null);
      expect(result.current.error).toBe(null);
      expect(result.current.loading).toBe(false);
    });

    it('handles delete book error', async () => {
      const errorMessage = 'Failed to delete book';
      mockDeleteBook.mockRejectedValue(new Error(errorMessage));

      const { result } = renderHook(() => useBook());

      await expect(result.current.deleteBook(1)).rejects.toThrow(errorMessage);

      await waitFor(() => {
        expect(result.current.error).toBe(errorMessage);
      });

      expect(result.current.loading).toBe(false);
    });

    it('sets loading state correctly during delete', async () => {
      let resolvePromise: (value: any) => void;
      const pendingPromise = new Promise((resolve) => {
        resolvePromise = resolve;
      });
      mockDeleteBook.mockReturnValue(pendingPromise);

      const { result } = renderHook(() => useBook());

      const deletePromise = result.current.deleteBook(1);
      
      expect(result.current.loading).toBe(true);

      resolvePromise!(undefined);
      await deletePromise;

      expect(result.current.loading).toBe(false);
    });
  });

  describe('error handling', () => {
    it('clears error on successful operation after error', async () => {
      const { result } = renderHook(() => useBook());

      // First operation with error
      mockGetBookById.mockRejectedValueOnce(new Error('Network error'));
      await result.current.fetchBook(1);

      expect(result.current.error).toBe('Network error');

      // Second operation successful
      mockGetBookById.mockResolvedValue(mockBook);
      await result.current.fetchBook(1);

      expect(result.current.error).toBe(null);
      expect(result.current.book).toEqual(mockBook);
    });

    it('handles error without message', async () => {
      mockGetBookById.mockRejectedValue({});

      const { result } = renderHook(() => useBook());

      await result.current.fetchBook(1);

      await waitFor(() => {
        expect(result.current.error).toBe('Failed to fetch book');
      });
    });
  });

  describe('bookId changes', () => {
    it('refetches when bookId changes', async () => {
      const { result, rerender } = renderHook(
        ({ bookId }) => useBook(bookId),
        { initialProps: { bookId: 1 } }
      );

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      expect(mockGetBookById).toHaveBeenCalledWith(1);

      // Change bookId
      rerender({ bookId: 2 });

      await waitFor(() => {
        expect(mockGetBookById).toHaveBeenCalledWith(2);
      });
    });

    it('does not fetch when bookId becomes undefined', async () => {
      const { rerender } = renderHook(
        ({ bookId }) => useBook(bookId),
        { initialProps: { bookId: 1 } }
      );

      await waitFor(() => {
        expect(mockGetBookById).toHaveBeenCalledWith(1);
      });

      vi.clearAllMocks();

      // Change bookId to undefined
      rerender({ bookId: undefined });

      expect(mockGetBookById).not.toHaveBeenCalled();
    });
  });
});