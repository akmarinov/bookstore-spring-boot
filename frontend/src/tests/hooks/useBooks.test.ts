import { describe, it, expect, beforeEach, vi } from 'vitest';
import { renderHook, waitFor, act } from '@testing-library/react';
import { useBooks } from '../../hooks/useBooks';
import { bookApi } from '../../api/bookApi';
import { mockPaginatedResponse, mockBooks } from '../../test/mocks/mockData';

// Mock the bookApi
vi.mock('../../api/bookApi', () => ({
  bookApi: {
    getBooks: vi.fn(),
  },
}));

describe('useBooks', () => {
  const mockGetBooks = vi.mocked(bookApi.getBooks);

  beforeEach(() => {
    vi.clearAllMocks();
    mockGetBooks.mockResolvedValue(mockPaginatedResponse);
  });

  it('initializes with default state', () => {
    const { result } = renderHook(() => useBooks());

    expect(result.current.books).toEqual([]);
    expect(result.current.loading).toBe(false);
    expect(result.current.error).toBe(null);
    expect(result.current.totalPages).toBe(0);
    expect(result.current.currentPage).toBe(0);
    expect(result.current.totalElements).toBe(0);
  });

  it('fetches books automatically when initialized with parameters', async () => {
    const initialParams = { page: 0, size: 10 };
    const { result } = renderHook(() => useBooks(initialParams));

    expect(result.current.loading).toBe(true);

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    expect(mockGetBooks).toHaveBeenCalledWith(initialParams);
    expect(result.current.books).toEqual(mockBooks);
    expect(result.current.totalPages).toBe(mockPaginatedResponse.totalPages);
    expect(result.current.currentPage).toBe(mockPaginatedResponse.pageNumber);
    expect(result.current.totalElements).toBe(mockPaginatedResponse.totalElements);
  });

  it('does not fetch books automatically when no parameters provided', () => {
    renderHook(() => useBooks());
    expect(mockGetBooks).not.toHaveBeenCalled();
  });

  it('handles successful fetch books', async () => {
    const { result } = renderHook(() => useBooks());

    expect(result.current.loading).toBe(false);

    const params = { page: 0, size: 10 };
    await act(async () => {
      await result.current.fetchBooks(params);
    });

    expect(mockGetBooks).toHaveBeenCalledWith(params);
    expect(result.current.books).toEqual(mockBooks);
    expect(result.current.error).toBe(null);
  });

  it('handles API error during fetch', async () => {
    const errorMessage = 'Failed to fetch books';
    mockGetBooks.mockRejectedValue(new Error(errorMessage));

    const { result } = renderHook(() => useBooks());

    await act(async () => {
      await result.current.fetchBooks({ page: 0, size: 10 });
    });

    await waitFor(() => {
      expect(result.current.error).toBe(errorMessage);
    });

    expect(result.current.books).toEqual([]);
    expect(result.current.loading).toBe(false);
  });

  it('sets loading state correctly during fetch', async () => {
    let resolvePromise: (value: any) => void;
    const pendingPromise = new Promise((resolve) => {
      resolvePromise = resolve;
    });
    mockGetBooks.mockReturnValue(pendingPromise);

    const { result } = renderHook(() => useBooks());

    let fetchPromise: Promise<void>;
    act(() => {
      fetchPromise = result.current.fetchBooks({ page: 0, size: 10 });
    });
    
    expect(result.current.loading).toBe(true);

    await act(async () => {
      resolvePromise!(mockPaginatedResponse);
      await fetchPromise;
    });

    expect(result.current.loading).toBe(false);
  });

  it('refetch uses the last parameters', async () => {
    const { result } = renderHook(() => useBooks());

    const params1 = { page: 0, size: 10, category: 'Fiction' };
    await act(async () => {
      await result.current.fetchBooks(params1);
    });

    const params2 = { page: 1, size: 5, search: 'test' };
    await act(async () => {
      await result.current.fetchBooks(params2);
    });

    vi.clearAllMocks();

    await act(async () => {
      await result.current.refetch();
    });

    expect(mockGetBooks).toHaveBeenCalledWith(params2);
  });

  it('refetch works with initial parameters', async () => {
    const initialParams = { page: 0, size: 10, category: 'Fiction' };
    const { result } = renderHook(() => useBooks(initialParams));

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    vi.clearAllMocks();

    await act(async () => {
      await result.current.refetch();
    });

    expect(mockGetBooks).toHaveBeenCalledWith(initialParams);
  });

  it('handles fetch with search parameter', async () => {
    const { result } = renderHook(() => useBooks());

    const params = { page: 0, size: 10, search: 'gatsby' };
    await act(async () => {
      await result.current.fetchBooks(params);
    });

    expect(mockGetBooks).toHaveBeenCalledWith(params);
  });

  it('handles fetch with category parameter', async () => {
    const { result } = renderHook(() => useBooks());

    const params = { page: 0, size: 10, category: 'Fiction' };
    await act(async () => {
      await result.current.fetchBooks(params);
    });

    expect(mockGetBooks).toHaveBeenCalledWith(params);
  });

  it('handles fetch with all parameters', async () => {
    const { result } = renderHook(() => useBooks());

    const params = { 
      page: 1, 
      size: 20, 
      category: 'Fiction', 
      search: 'test',
      sort: 'title'
    };
    await act(async () => {
      await result.current.fetchBooks(params);
    });

    expect(mockGetBooks).toHaveBeenCalledWith(params);
  });

  it('clears error on successful fetch after error', async () => {
    const { result } = renderHook(() => useBooks());

    // First fetch with error
    mockGetBooks.mockRejectedValueOnce(new Error('Network error'));
    await act(async () => {
      await result.current.fetchBooks({ page: 0, size: 10 });
    });

    expect(result.current.error).toBe('Network error');

    // Second fetch successful
    mockGetBooks.mockResolvedValue(mockPaginatedResponse);
    await act(async () => {
      await result.current.fetchBooks({ page: 0, size: 10 });
    });

    expect(result.current.error).toBe(null);
    expect(result.current.books).toEqual(mockBooks);
  });

  it('resets books when fetch fails', async () => {
    const { result } = renderHook(() => useBooks());

    // First successful fetch
    await act(async () => {
      await result.current.fetchBooks({ page: 0, size: 10 });
    });
    expect(result.current.books).toEqual(mockBooks);

    // Then failed fetch
    mockGetBooks.mockRejectedValue(new Error('Network error'));
    await act(async () => {
      await result.current.fetchBooks({ page: 0, size: 10 });
    });

    expect(result.current.books).toEqual([]);
  });
});