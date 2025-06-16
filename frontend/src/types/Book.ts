export interface Book {
  id?: number;
  title: string;
  author: string;
  isbn: string;
  price: number;
  category: string;
  description?: string;
  imageUrl?: string;
  inStock: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface BookFormData {
  title: string;
  author: string;
  isbn: string;
  price: number;
  category: string;
  description?: string;
  imageUrl?: string;
  inStock: boolean;
}

export interface ApiResponse<T> {
  data: T;
  message?: string;
  status: number;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  pageNumber: number;
  pageSize: number;
  first: boolean;
  last: boolean;
}

export interface ErrorResponse {
  message: string;
  status: number;
  timestamp: string;
  path: string;
}