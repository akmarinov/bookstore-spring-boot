import { useState, useEffect, useCallback } from 'react';
import type { Book, BookFormData } from '../types/Book';
import { bookApi } from '../api/bookApi';

export interface UseBookReturn {
  book: Book | null;
  loading: boolean;
  error: string | null;
  fetchBook: (id: number) => Promise<void>;
  createBook: (bookData: BookFormData) => Promise<Book>;
  updateBook: (id: number, bookData: BookFormData) => Promise<Book>;
  deleteBook: (id: number) => Promise<void>;
}

export const useBook = (bookId?: number): UseBookReturn => {
  const [book, setBook] = useState<Book | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchBook = useCallback(async (id: number) => {
    setLoading(true);
    setError(null);

    try {
      const bookData = await bookApi.getBookById(id);
      setBook(bookData);
    } catch (err: any) {
      setError(err.message || 'Failed to fetch book');
      setBook(null);
    } finally {
      setLoading(false);
    }
  }, []);

  const createBook = useCallback(async (bookData: BookFormData): Promise<Book> => {
    setLoading(true);
    setError(null);

    try {
      const newBook = await bookApi.createBook(bookData);
      setBook(newBook);
      return newBook;
    } catch (err: any) {
      setError(err.message || 'Failed to create book');
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  const updateBook = useCallback(async (id: number, bookData: BookFormData): Promise<Book> => {
    setLoading(true);
    setError(null);

    try {
      const updatedBook = await bookApi.updateBook(id, bookData);
      setBook(updatedBook);
      return updatedBook;
    } catch (err: any) {
      setError(err.message || 'Failed to update book');
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  const deleteBook = useCallback(async (id: number): Promise<void> => {
    setLoading(true);
    setError(null);

    try {
      await bookApi.deleteBook(id);
      setBook(null);
    } catch (err: any) {
      setError(err.message || 'Failed to delete book');
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (bookId) {
      fetchBook(bookId);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [bookId]);

  return {
    book,
    loading,
    error,
    fetchBook,
    createBook,
    updateBook,
    deleteBook,
  };
};