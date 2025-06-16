import type { Book, PaginatedResponse } from '../../types/Book';

export const mockBook: Book = {
  id: 1,
  title: 'The Great Gatsby',
  author: 'F. Scott Fitzgerald',
  isbn: '978-0-7432-7356-5',
  price: 12.99,
  category: 'Fiction',
  description: 'A classic American novel set in the Jazz Age.',
  imageUrl: 'https://example.com/gatsby.jpg',
  inStock: true,
  createdAt: '2023-01-01T00:00:00Z',
  updatedAt: '2023-01-01T00:00:00Z',
};

export const mockBooks: Book[] = [
  mockBook,
  {
    id: 2,
    title: '1984',
    author: 'George Orwell',
    isbn: '978-0-452-28423-4',
    price: 13.99,
    category: 'Fiction',
    description: 'A dystopian social science fiction novel.',
    imageUrl: 'https://example.com/1984.jpg',
    inStock: true,
    createdAt: '2023-01-02T00:00:00Z',
    updatedAt: '2023-01-02T00:00:00Z',
  },
  {
    id: 3,
    title: 'To Kill a Mockingbird',
    author: 'Harper Lee',
    isbn: '978-0-06-112008-4',
    price: 14.99,
    category: 'Fiction',
    description: 'A novel about racial injustice and childhood innocence.',
    imageUrl: 'https://example.com/mockingbird.jpg',
    inStock: false,
    createdAt: '2023-01-03T00:00:00Z',
    updatedAt: '2023-01-03T00:00:00Z',
  },
];

export const mockPaginatedResponse: PaginatedResponse<Book> = {
  content: mockBooks,
  totalElements: 3,
  totalPages: 1,
  pageNumber: 0,
  pageSize: 10,
  first: true,
  last: true,
};

export const mockEmptyPaginatedResponse: PaginatedResponse<Book> = {
  content: [],
  totalElements: 0,
  totalPages: 0,
  pageNumber: 0,
  pageSize: 10,
  first: true,
  last: true,
};

export const createMockBook = (overrides: Partial<Book> = {}): Book => ({
  ...mockBook,
  ...overrides,
});

export const createMockPaginatedResponse = (
  books: Book[] = mockBooks,
  totalElements: number = books.length
): PaginatedResponse<Book> => ({
  content: books,
  totalElements,
  totalPages: Math.ceil(totalElements / 10),
  pageNumber: 0,
  pageSize: 10,
  first: true,
  last: totalElements <= 10,
});