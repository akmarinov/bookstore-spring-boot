import axios from 'axios';
import type { ErrorResponse } from '../types/Book';

const API_BASE_URL = `${import.meta.env.VITE_API_URL || 'http://localhost:8080'}${import.meta.env.VITE_API_BASE_PATH || '/api/v1'}`;

console.log('API_BASE_URL:', API_BASE_URL);
console.log('VITE_API_URL:', import.meta.env.VITE_API_URL);
console.log('VITE_API_BASE_PATH:', import.meta.env.VITE_API_BASE_PATH);

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor
apiClient.interceptors.request.use(
  (config) => {
    // Add auth token if available
    const token = localStorage.getItem('authToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor
apiClient.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    const errorResponse: ErrorResponse = {
      message: error.response?.data?.message || error.message || 'An error occurred',
      status: error.response?.status || 500,
      timestamp: new Date().toISOString(),
      path: error.config?.url || '',
    };

    // Handle specific error status codes
    if (error.response?.status === 401) {
      // Handle unauthorized access
      localStorage.removeItem('authToken');
      window.location.href = '/login';
    }

    return Promise.reject(errorResponse);
  }
);

export default apiClient;