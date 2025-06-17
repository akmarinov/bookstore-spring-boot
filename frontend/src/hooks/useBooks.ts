import { useState, useEffect, useCallback, useRef } from 'react';
import type { Book, PaginatedResponse } from '../types/Book';
import { bookApi } from '../api/bookApi';
 import type { GetBooksParams } from '../api/bookApi';

export interface UseBooksReturn {
  books: Book[];
  loading: boolean;
  error: string | null;
  totalPages: number;
  currentPage: number;
  totalElements: number;
  fetchBooks: (params?: GetBooksParams) => Promise<void>;
  refetch: () => Promise<void>;
}

export const useBooks = (initialParams?: GetBooksParams): UseBooksReturn => {
  const [books, setBooks] = useState<Book[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [totalPages, setTotalPages] = useState(0);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [lastParams, setLastParams] = useState<GetBooksParams | undefined>();

  const fetchBooks = useCallback(async (params?: GetBooksParams) => {
    setLoading(true);
    setError(null);
    setLastParams(params);

    try {
      const response: PaginatedResponse<Book> = await bookApi.getBooks(params);
      setBooks(response.content);
      setTotalPages(response.totalPages);
      setCurrentPage(response.pageNumber);
      setTotalElements(response.totalElements);
    } catch (err: any) {
      setError(err.message || 'Failed to fetch books');
      setBooks([]);
    } finally {
      setLoading(false);
    }
  }, []);

  const refetch = useCallback(async () => {
    await fetchBooks(lastParams);
  }, [fetchBooks, lastParams]);

  // Initial fetch only if parameters are provided
  useEffect(() => {
    if (initialParams) {
      fetchBooks(initialParams);
    }
  }, [fetchBooks, initialParams]);

  return {
    books,
    loading,
    error,
    totalPages,
    currentPage,
    totalElements,
    fetchBooks,
    refetch,
  };
};