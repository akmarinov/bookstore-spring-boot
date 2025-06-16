import type { Book, BookFormData, PaginatedResponse } from '../types/Book';
import { apiClient } from './client';

export interface GetBooksParams {
  page?: number;
  size?: number;
  sort?: string;
  category?: string;
  search?: string;
}

export const bookApi = {
  // Get all books with pagination and filtering
  getBooks: async (params?: GetBooksParams): Promise<PaginatedResponse<Book>> => {
    const response = await apiClient.get('/books', { params });
    return response.data;
  },

  // Get a single book by ID
  getBookById: async (id: number): Promise<Book> => {
    const response = await apiClient.get(`/books/${id}`);
    return response.data;
  },

  // Create a new book
  createBook: async (bookData: BookFormData): Promise<Book> => {
    const response = await apiClient.post('/books', bookData);
    return response.data;
  },

  // Update an existing book
  updateBook: async (id: number, bookData: BookFormData): Promise<Book> => {
    const response = await apiClient.put(`/books/${id}`, bookData);
    return response.data;
  },

  // Delete a book
  deleteBook: async (id: number): Promise<void> => {
    await apiClient.delete(`/books/${id}`);
  },

  // Search books by title or author
  searchBooks: async (query: string, params?: GetBooksParams): Promise<PaginatedResponse<Book>> => {
    const response = await apiClient.get('/books/search', {
      params: { ...params, q: query }
    });
    return response.data;
  },

  // Get books by category
  getBooksByCategory: async (category: string, params?: GetBooksParams): Promise<PaginatedResponse<Book>> => {
    const response = await apiClient.get(`/books/category/${category}`, { params });
    return response.data;
  },

  // Get all categories
  getCategories: async (): Promise<string[]> => {
    const response = await apiClient.get('/books/categories');
    return response.data;
  },
};

export default bookApi;