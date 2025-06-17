import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '../test/utils';
import userEvent from '@testing-library/user-event';
import { Routes, Route } from 'react-router-dom';
import Layout from '../components/Layout';
import HomePage from '../pages/HomePage';
import AddBookPage from '../pages/AddBookPage';
import EditBookPage from '../pages/EditBookPage';
import { mockBooks } from '../test/mocks/mockData';

// Mock the hooks used by components
const mockUseBooks = vi.fn();
const mockUseBook = vi.fn();

vi.mock('../hooks/useBooks', () => ({
  useBooks: () => mockUseBooks(),
}));

vi.mock('../hooks/useBook', () => ({
  useBook: () => mockUseBook(),
}));

// Create testable App content (without Router since test utils provide it)
const AppContent = () => (
  <Layout>
    <Routes>
      <Route path="/" element={<HomePage />} />
      <Route path="/add-book" element={<AddBookPage />} />
      <Route path="/edit-book/:id" element={<EditBookPage />} />
    </Routes>
  </Layout>
);

// Custom render for App content with specific routes
const renderApp = (initialEntries = ['/']) => {
  return render(<AppContent />, { 
    routerOptions: { initialEntries } 
  });
};

describe('App Integration Tests', () => {
  const mockUseBooksReturnValue = {
    books: mockBooks,
    loading: false,
    error: null,
    totalPages: 1,
    fetchBooks: vi.fn(),
    refetch: vi.fn(),
  };

  const mockUseBookReturnValue = {
    book: null,
    loading: false,
    error: null,
    fetchBook: vi.fn(),
    createBook: vi.fn(),
    updateBook: vi.fn(),
    deleteBook: vi.fn(),
  };

  beforeEach(() => {
    vi.clearAllMocks();
    mockUseBooks.mockReturnValue(mockUseBooksReturnValue);
    mockUseBook.mockReturnValue(mockUseBookReturnValue);
  });

  describe('Routing', () => {
    it('renders home page at root path', async () => {
      renderApp(['/']);

      expect(screen.getByText('Bookstore')).toBeInTheDocument();
      expect(screen.getByText('Book Collection')).toBeInTheDocument();
      
      await waitFor(() => {
        expect(screen.getByText('The Great Gatsby')).toBeInTheDocument();
      });
    });

    it('renders add book page at /add-book', () => {
      renderApp(['/add-book']);

      expect(screen.getByText('Bookstore')).toBeInTheDocument();
      expect(screen.getByText('Add New Book')).toBeInTheDocument();
      expect(screen.getByLabelText(/title/i)).toBeInTheDocument();
    });

    it('renders edit book page at /edit-book/:id', () => {
      mockUseBook.mockReturnValue({
        ...mockUseBookReturnValue,
        book: mockBooks[0],
      });

      renderApp(['/edit-book/1']);

      expect(screen.getByText('Bookstore')).toBeInTheDocument();
      // The edit page should display the book form
      expect(screen.getByLabelText(/title/i)).toBeInTheDocument();
    });

    it('renders 404 for unknown routes', () => {
      renderApp(['/unknown-route']);

      // Should still render the layout but with no matching route content
      expect(screen.getByText('Bookstore')).toBeInTheDocument();
    });
  });

  describe('Navigation', () => {
    it('navigates to home when Home button is clicked', async () => {
      const user = userEvent.setup();
      renderApp(['/add-book']);

      // Should be on add book page
      expect(screen.getByText('Add New Book')).toBeInTheDocument();

      // Click home button (the text button, not the icon)
      const homeButton = screen.getByRole('button', { name: 'Home' });
      await user.click(homeButton);

      // Should navigate to home page
      expect(screen.getByText('Book Collection')).toBeInTheDocument();
    });

    it('navigates to add book when Add Book button is clicked', async () => {
      const user = userEvent.setup();
      renderApp(['/']);

      // Should be on home page
      expect(screen.getByText('Book Collection')).toBeInTheDocument();

      // Click add book button
      const addBookButton = screen.getByRole('button', { name: /add book/i });
      await user.click(addBookButton);

      // Should navigate to add book page
      expect(screen.getByText('Add New Book')).toBeInTheDocument();
    });

    it('navigates to edit book when edit button is clicked', async () => {
      const user = userEvent.setup();
      renderApp(['/']);

      // Wait for books to load and find edit button
      await waitFor(() => {
        expect(screen.getByText('The Great Gatsby')).toBeInTheDocument();
      });

      const editButtons = screen.getAllByRole('button', { name: /edit/i });
      await user.click(editButtons[0]);

      // Should navigate to edit page (mocked navigation in BookList component)
    });
  });

  describe('User Flows', () => {
    it('completes add book flow', async () => {
      const user = userEvent.setup();
      const mockCreateBook = vi.fn().mockResolvedValue(mockBooks[0]);
      
      mockUseBook.mockReturnValue({
        ...mockUseBookReturnValue,
        createBook: mockCreateBook,
      });

      renderApp(['/add-book']);

      // Fill out the form
      await user.type(screen.getByLabelText(/title/i), 'New Test Book');
      await user.type(screen.getByLabelText(/author/i), 'Test Author');
      await user.type(screen.getByLabelText(/isbn/i), '978-0-123456-78-9');
      await user.clear(screen.getByLabelText(/price/i));
      await user.type(screen.getByLabelText(/price/i), '19.99');
      
      // Select category
      await user.click(screen.getByLabelText(/category/i));
      await user.click(screen.getByRole('option', { name: 'Fiction' }));

      // Submit the form
      const submitButton = screen.getByRole('button', { name: /create book/i });
      await user.click(submitButton);

      await waitFor(() => {
        expect(mockCreateBook).toHaveBeenCalledWith({
          title: 'New Test Book',
          author: 'Test Author',
          isbn: '978-0-123456-78-9',
          price: 19.99,
          category: 'Fiction',
          description: '',
          imageUrl: '',
          inStock: true,
        });
      });
    });

    it('cancels add book flow', async () => {
      const user = userEvent.setup();
      renderApp(['/add-book']);

      // Click cancel button
      const cancelButton = screen.getByRole('button', { name: /cancel/i });
      await user.click(cancelButton);

      // Should navigate back to home page
      expect(screen.getByText('Book Collection')).toBeInTheDocument();
    });

    it('handles search functionality', async () => {
      const user = userEvent.setup();
      const mockFetchBooks = vi.fn();
      
      mockUseBooks.mockReturnValue({
        ...mockUseBooksReturnValue,
        fetchBooks: mockFetchBooks,
      });

      renderApp(['/']);

      // Perform search
      const searchInput = screen.getByPlaceholderText('Search books by title or author...');
      await user.type(searchInput, 'gatsby');

      await waitFor(() => {
        expect(mockFetchBooks).toHaveBeenCalledWith({
          page: 0,
          size: 12,
          category: undefined,
          search: 'gatsby',
        });
      });
    });

    it('handles category filtering', async () => {
      const user = userEvent.setup();
      const mockFetchBooks = vi.fn();
      
      mockUseBooks.mockReturnValue({
        ...mockUseBooksReturnValue,
        fetchBooks: mockFetchBooks,
      });

      renderApp(['/']);

      // Select category filter
      const categorySelect = screen.getByLabelText('Category');
      await user.click(categorySelect);
      await user.click(screen.getByRole('option', { name: 'Fiction' }));

      await waitFor(() => {
        expect(mockFetchBooks).toHaveBeenCalledWith({
          page: 0,
          size: 12,
          category: 'Fiction',
          search: undefined,
        });
      });
    });

    it('handles delete book flow', async () => {
      const user = userEvent.setup();
      const mockDeleteBook = vi.fn().mockResolvedValue(undefined);
      const mockRefetch = vi.fn();
      
      mockUseBook.mockReturnValue({
        ...mockUseBookReturnValue,
        deleteBook: mockDeleteBook,
      });
      
      mockUseBooks.mockReturnValue({
        ...mockUseBooksReturnValue,
        refetch: mockRefetch,
      });

      renderApp(['/']);

      // Wait for books to load
      await waitFor(() => {
        expect(screen.getByText('The Great Gatsby')).toBeInTheDocument();
      });

      // Click delete button
      const deleteButtons = screen.getAllByRole('button', { name: /delete/i });
      await user.click(deleteButtons[0]);

      // Confirm delete in dialog
      expect(screen.getByText('Confirm Delete')).toBeInTheDocument();
      const confirmButton = screen.getByRole('button', { name: /delete$/i });
      await user.click(confirmButton);

      await waitFor(() => {
        expect(mockDeleteBook).toHaveBeenCalledWith(1);
        expect(mockRefetch).toHaveBeenCalled();
      });
    });
  });

  describe('Error Handling', () => {
    it('displays error when books fail to load', () => {
      mockUseBooks.mockReturnValue({
        ...mockUseBooksReturnValue,
        error: 'Failed to load books',
        books: [],
      });

      renderApp(['/']);

      expect(screen.getByText('Failed to load books')).toBeInTheDocument();
    });

    it('displays error when book creation fails', () => {
      mockUseBook.mockReturnValue({
        ...mockUseBookReturnValue,
        error: 'Failed to create book',
      });

      renderApp(['/add-book']);

      expect(screen.getByText('Failed to create book')).toBeInTheDocument();
    });

    it('shows loading state during book creation', () => {
      mockUseBook.mockReturnValue({
        ...mockUseBookReturnValue,
        loading: true,
      });

      renderApp(['/add-book']);

      expect(screen.getByText('Saving...')).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /saving/i })).toBeDisabled();
    });

    it('shows loading state during books fetch', () => {
      mockUseBooks.mockReturnValue({
        ...mockUseBooksReturnValue,
        loading: true,
        books: [],
      });

      renderApp(['/']);

      expect(screen.getByRole('progressbar')).toBeInTheDocument();
    });
  });

  describe('Responsive Design', () => {
    it('renders correctly on different screen sizes', () => {
      renderApp(['/']);

      // Check that Material-UI responsive components are rendered
      expect(screen.getByText('Bookstore')).toBeInTheDocument();
      expect(screen.getByText('Book Collection')).toBeInTheDocument();
      
      // Material-UI Grid and Container components should handle responsive layout
      const container = screen.getByText('Book Collection').closest('[class*="MuiContainer"]');
      expect(container).toBeInTheDocument();
    });
  });

  describe('Theme Integration', () => {
    it('applies Material-UI theme correctly', () => {
      renderApp(['/']);

      // Check that theme is applied by verifying Material-UI components render
      const appBar = screen.getByRole('banner');
      expect(appBar).toBeInTheDocument();
      expect(appBar).toHaveClass('MuiAppBar-root');
    });
  });
});