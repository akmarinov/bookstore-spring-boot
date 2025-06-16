import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '../../test/utils';
import userEvent from '@testing-library/user-event';
import BookList from '../../components/BookList';
import { mockBooks, mockPaginatedResponse } from '../../test/mocks/mockData';

// Mock the hooks
const mockUseBooks = vi.fn();
const mockUseBook = vi.fn();
const mockNavigate = vi.fn();

vi.mock('../../hooks/useBooks', () => ({
  useBooks: () => mockUseBooks(),
}));

vi.mock('../../hooks/useBook', () => ({
  useBook: () => mockUseBook(),
}));

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

describe('BookList', () => {
  const mockHooksReturnValue = {
    books: mockBooks,
    loading: false,
    error: null,
    totalPages: 1,
    fetchBooks: vi.fn(),
    refetch: vi.fn(),
  };

  const mockUseBookReturnValue = {
    deleteBook: vi.fn(),
  };

  beforeEach(() => {
    vi.clearAllMocks();
    mockUseBooks.mockReturnValue(mockHooksReturnValue);
    mockUseBook.mockReturnValue(mockUseBookReturnValue);
  });

  it('renders book collection title', () => {
    render(<BookList />);
    expect(screen.getByText('Book Collection')).toBeInTheDocument();
  });

  it('renders search and filter controls', () => {
    render(<BookList />);
    
    expect(screen.getByPlaceholderText('Search books by title or author...')).toBeInTheDocument();
    expect(screen.getByLabelText('Category')).toBeInTheDocument();
  });

  it('renders books when loaded successfully', () => {
    render(<BookList />);

    mockBooks.forEach(book => {
      expect(screen.getByText(book.title)).toBeInTheDocument();
    });
  });

  it('shows loading spinner when loading', () => {
    mockUseBooks.mockReturnValue({
      ...mockHooksReturnValue,
      loading: true,
      books: [],
    });

    render(<BookList />);
    expect(screen.getByRole('progressbar')).toBeInTheDocument();
  });

  it('shows error message when there is an error', () => {
    const errorMessage = 'Failed to load books';
    mockUseBooks.mockReturnValue({
      ...mockHooksReturnValue,
      error: errorMessage,
    });

    render(<BookList />);
    expect(screen.getByText(errorMessage)).toBeInTheDocument();
  });

  it('shows no books message when books array is empty', () => {
    mockUseBooks.mockReturnValue({
      ...mockHooksReturnValue,
      books: [],
    });

    render(<BookList />);
    expect(screen.getByText(/no books found/i)).toBeInTheDocument();
  });

  it('calls fetchBooks when search term changes', async () => {
    const user = userEvent.setup();
    const mockFetchBooks = vi.fn();
    
    mockUseBooks.mockReturnValue({
      ...mockHooksReturnValue,
      fetchBooks: mockFetchBooks,
    });

    render(<BookList />);

    const searchInput = screen.getByPlaceholderText('Search books by title or author...');
    await user.type(searchInput, 'test search');

    await waitFor(() => {
      expect(mockFetchBooks).toHaveBeenCalledWith({
        page: 0,
        size: 12,
        category: undefined,
        search: 'test search',
      });
    });
  });

  it('calls fetchBooks when category changes', async () => {
    const user = userEvent.setup();
    const mockFetchBooks = vi.fn();
    
    mockUseBooks.mockReturnValue({
      ...mockHooksReturnValue,
      fetchBooks: mockFetchBooks,
    });

    render(<BookList />);

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

  it('navigates to edit page when edit is clicked', async () => {
    const user = userEvent.setup();
    render(<BookList />);

    const editButtons = screen.getAllByRole('button', { name: /edit/i });
    await user.click(editButtons[0]);

    expect(mockNavigate).toHaveBeenCalledWith(`/edit-book/${mockBooks[0].id}`);
  });

  it('navigates to view page when view is clicked', async () => {
    const user = userEvent.setup();
    render(<BookList />);

    const viewButtons = screen.getAllByRole('button', { name: /view/i });
    await user.click(viewButtons[0]);

    expect(mockNavigate).toHaveBeenCalledWith(`/edit-book/${mockBooks[0].id}?view=true`);
  });

  it('opens delete confirmation dialog when delete is clicked', async () => {
    const user = userEvent.setup();
    render(<BookList />);

    const deleteButtons = screen.getAllByRole('button', { name: /delete/i });
    await user.click(deleteButtons[0]);

    expect(screen.getByText('Confirm Delete')).toBeInTheDocument();
    expect(screen.getByText(`Are you sure you want to delete "${mockBooks[0].title}"? This action cannot be undone.`)).toBeInTheDocument();
  });

  it('calls deleteBook and refetch when delete is confirmed', async () => {
    const user = userEvent.setup();
    const mockDeleteBook = vi.fn().mockResolvedValue(undefined);
    const mockRefetch = vi.fn();
    
    mockUseBook.mockReturnValue({
      deleteBook: mockDeleteBook,
    });
    
    mockUseBooks.mockReturnValue({
      ...mockHooksReturnValue,
      refetch: mockRefetch,
    });

    render(<BookList />);

    // Open delete dialog
    const deleteButtons = screen.getAllByRole('button', { name: /delete/i });
    await user.click(deleteButtons[0]);

    // Confirm delete
    const confirmButton = screen.getByRole('button', { name: /delete$/i });
    await user.click(confirmButton);

    await waitFor(() => {
      expect(mockDeleteBook).toHaveBeenCalledWith(mockBooks[0].id);
      expect(mockRefetch).toHaveBeenCalled();
    });
  });

  it('closes delete dialog when cancel is clicked', async () => {
    const user = userEvent.setup();
    render(<BookList />);

    // Open delete dialog
    const deleteButtons = screen.getAllByRole('button', { name: /delete/i });
    await user.click(deleteButtons[0]);

    expect(screen.getByText('Confirm Delete')).toBeInTheDocument();

    // Cancel delete
    const cancelButton = screen.getByRole('button', { name: /cancel/i });
    await user.click(cancelButton);

    await waitFor(() => {
      expect(screen.queryByText('Confirm Delete')).not.toBeInTheDocument();
    });
  });

  it('renders pagination when total pages > 1', () => {
    mockUseBooks.mockReturnValue({
      ...mockHooksReturnValue,
      totalPages: 3,
    });

    render(<BookList />);
    expect(screen.getByRole('navigation')).toBeInTheDocument();
  });

  it('does not render pagination when total pages <= 1', () => {
    mockUseBooks.mockReturnValue({
      ...mockHooksReturnValue,
      totalPages: 1,
    });

    render(<BookList />);
    expect(screen.queryByRole('navigation')).not.toBeInTheDocument();
  });

  it('handles pagination change', async () => {
    const user = userEvent.setup();
    const mockFetchBooks = vi.fn();
    
    mockUseBooks.mockReturnValue({
      ...mockHooksReturnValue,
      totalPages: 3,
      fetchBooks: mockFetchBooks,
    });

    render(<BookList />);

    const page2Button = screen.getByRole('button', { name: 'Go to page 2' });
    await user.click(page2Button);

    expect(mockFetchBooks).toHaveBeenCalledWith({
      page: 1,
      size: 12,
      category: undefined,
      search: undefined,
    });
  });

  it('initializes with provided initial search and category', () => {
    const mockFetchBooks = vi.fn();
    
    mockUseBooks.mockReturnValue({
      ...mockHooksReturnValue,
      fetchBooks: mockFetchBooks,
    });

    render(<BookList initialCategory="Fiction" initialSearch="test" />);

    expect(screen.getByDisplayValue('test')).toBeInTheDocument();
    expect(screen.getByDisplayValue('Fiction')).toBeInTheDocument();
  });
});