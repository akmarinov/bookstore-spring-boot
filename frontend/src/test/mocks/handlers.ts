import { http, HttpResponse } from 'msw';
import { mockBooks, mockPaginatedResponse, mockBook, createMockPaginatedResponse } from './mockData';
import type { Book, BookFormData } from '../../types/Book';

const BASE_URL = 'http://localhost:8080/api/v1';

export const handlers = [
  // Get all books with pagination and filtering
  http.get(`${BASE_URL}/books`, ({ request }) => {
    const url = new URL(request.url);
    const page = parseInt(url.searchParams.get('page') || '0');
    const size = parseInt(url.searchParams.get('size') || '10');
    const category = url.searchParams.get('category');
    const search = url.searchParams.get('search');

    let filteredBooks = [...mockBooks];

    // Apply category filter
    if (category) {
      filteredBooks = filteredBooks.filter(book => book.category === category);
    }

    // Apply search filter
    if (search) {
      const searchLower = search.toLowerCase();
      filteredBooks = filteredBooks.filter(book =>
        book.title.toLowerCase().includes(searchLower) ||
        book.author.toLowerCase().includes(searchLower)
      );
    }

    // Apply pagination
    const startIndex = page * size;
    const endIndex = startIndex + size;
    const paginatedBooks = filteredBooks.slice(startIndex, endIndex);

    const response = createMockPaginatedResponse(paginatedBooks, filteredBooks.length);
    response.pageNumber = page;
    response.pageSize = size;
    response.first = page === 0;
    response.last = endIndex >= filteredBooks.length;

    return HttpResponse.json(response);
  }),

  // Get book by ID
  http.get(`${BASE_URL}/books/:id`, ({ params }) => {
    const id = parseInt(params.id as string);
    const book = mockBooks.find(b => b.id === id);
    
    if (!book) {
      return HttpResponse.json(
        { message: 'Book not found', status: 404 },
        { status: 404 }
      );
    }
    
    return HttpResponse.json(book);
  }),

  // Create new book
  http.post(`${BASE_URL}/books`, async ({ request }) => {
    const bookData = await request.json() as BookFormData;
    const newBook: Book = {
      ...bookData,
      id: Math.max(...mockBooks.map(b => b.id || 0)) + 1,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    };
    
    return HttpResponse.json(newBook, { status: 201 });
  }),

  // Update book
  http.put(`${BASE_URL}/books/:id`, async ({ params, request }) => {
    const id = parseInt(params.id as string);
    const bookData = await request.json() as BookFormData;
    const existingBook = mockBooks.find(b => b.id === id);
    
    if (!existingBook) {
      return HttpResponse.json(
        { message: 'Book not found', status: 404 },
        { status: 404 }
      );
    }
    
    const updatedBook: Book = {
      ...existingBook,
      ...bookData,
      updatedAt: new Date().toISOString(),
    };
    
    return HttpResponse.json(updatedBook);
  }),

  // Delete book
  http.delete(`${BASE_URL}/books/:id`, ({ params }) => {
    const id = parseInt(params.id as string);
    const bookExists = mockBooks.some(b => b.id === id);
    
    if (!bookExists) {
      return HttpResponse.json(
        { message: 'Book not found', status: 404 },
        { status: 404 }
      );
    }
    
    return HttpResponse.json({}, { status: 204 });
  }),


  // Get books by category
  http.get(`${BASE_URL}/books/category/:category`, ({ params, request }) => {
    const category = params.category as string;
    const url = new URL(request.url);
    const page = parseInt(url.searchParams.get('page') || '0');
    const size = parseInt(url.searchParams.get('size') || '10');

    const categoryBooks = mockBooks.filter(book => book.category === category);
    
    const startIndex = page * size;
    const endIndex = startIndex + size;
    const paginatedBooks = categoryBooks.slice(startIndex, endIndex);

    const response = createMockPaginatedResponse(paginatedBooks, categoryBooks.length);
    response.pageNumber = page;
    response.pageSize = size;
    response.first = page === 0;
    response.last = endIndex >= categoryBooks.length;

    return HttpResponse.json(response);
  }),


  // Error handlers for testing error scenarios
  http.get(`${BASE_URL}/books/error`, () => {
    return HttpResponse.json(
      { message: 'Internal server error', status: 500 },
      { status: 500 }
    );
  }),
];