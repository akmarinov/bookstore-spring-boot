import { describe, it, expect, vi } from 'vitest';
import { render, screen, waitFor } from '../../test/utils';
import userEvent from '@testing-library/user-event';
import BookForm from '../../components/BookForm';
import { mockBook } from '../../test/mocks/mockData';
import type { BookFormData } from '../../types/Book';

describe('BookForm', () => {
  const mockProps = {
    onSubmit: vi.fn(),
    onCancel: vi.fn(),
    title: 'Test Form',
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders form title correctly', () => {
    render(<BookForm {...mockProps} />);
    expect(screen.getByText('Test Form')).toBeInTheDocument();
  });

  it('renders all form fields', () => {
    render(<BookForm {...mockProps} />);

    expect(screen.getByLabelText(/title/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/author/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/isbn/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/price/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/category/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/description/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/image url/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/in stock/i)).toBeInTheDocument();
  });

  it('renders Cancel and Create Book buttons by default', () => {
    render(<BookForm {...mockProps} />);

    expect(screen.getByRole('button', { name: /cancel/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /create book/i })).toBeInTheDocument();
  });

  it('pre-fills form when editing existing book', () => {
    render(<BookForm {...mockProps} book={mockBook} />);

    expect(screen.getByDisplayValue(mockBook.title)).toBeInTheDocument();
    expect(screen.getByDisplayValue(mockBook.author)).toBeInTheDocument();
    expect(screen.getByDisplayValue(mockBook.isbn)).toBeInTheDocument();
    expect(screen.getByDisplayValue(mockBook.price.toString())).toBeInTheDocument();
    expect(screen.getByDisplayValue(mockBook.category)).toBeInTheDocument();
    expect(screen.getByDisplayValue(mockBook.description!)).toBeInTheDocument();
    expect(screen.getByDisplayValue(mockBook.imageUrl!)).toBeInTheDocument();
  });

  it('shows Update Book button when editing', () => {
    render(<BookForm {...mockProps} book={mockBook} />);
    expect(screen.getByRole('button', { name: /update book/i })).toBeInTheDocument();
  });

  it('shows error message when error prop is provided', () => {
    const errorMessage = 'Something went wrong';
    render(<BookForm {...mockProps} error={errorMessage} />);
    expect(screen.getByText(errorMessage)).toBeInTheDocument();
  });

  it('shows Back button in view mode', () => {
    render(<BookForm {...mockProps} book={mockBook} isViewMode={true} />);
    
    expect(screen.getByRole('button', { name: /back/i })).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /update book/i })).not.toBeInTheDocument();
  });

  it('disables form fields in view mode', () => {
    render(<BookForm {...mockProps} book={mockBook} isViewMode={true} />);

    expect(screen.getByLabelText(/title/i)).toBeDisabled();
    expect(screen.getByLabelText(/author/i)).toBeDisabled();
    expect(screen.getByLabelText(/isbn/i)).toBeDisabled();
    expect(screen.getByLabelText(/price/i)).toBeDisabled();
  });

  it('calls onCancel when Cancel button is clicked', async () => {
    const user = userEvent.setup();
    render(<BookForm {...mockProps} />);

    const cancelButton = screen.getByRole('button', { name: /cancel/i });
    await user.click(cancelButton);

    expect(mockProps.onCancel).toHaveBeenCalledTimes(1);
  });

  it('validates required fields', async () => {
    const user = userEvent.setup();
    render(<BookForm {...mockProps} />);

    const submitButton = screen.getByRole('button', { name: /create book/i });
    await user.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText('Title is required')).toBeInTheDocument();
      expect(screen.getByText('Author is required')).toBeInTheDocument();
      expect(screen.getByText('ISBN is required')).toBeInTheDocument();
      expect(screen.getByText('Category is required')).toBeInTheDocument();
    });

    expect(mockProps.onSubmit).not.toHaveBeenCalled();
  });

  it('validates ISBN format', async () => {
    const user = userEvent.setup();
    render(<BookForm {...mockProps} />);

    const isbnField = screen.getByLabelText(/isbn/i);
    await user.type(isbnField, 'invalid-isbn');

    const submitButton = screen.getByRole('button', { name: /create book/i });
    await user.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText('ISBN should contain only digits and hyphens')).toBeInTheDocument();
    });
  });

  it('validates price is positive', async () => {
    const user = userEvent.setup();
    render(<BookForm {...mockProps} />);

    const priceField = screen.getByLabelText(/price/i);
    await user.clear(priceField);
    await user.type(priceField, '-10');

    const submitButton = screen.getByRole('button', { name: /create book/i });
    await user.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText('Price must be positive')).toBeInTheDocument();
    });
  });

  it('validates image URL format', async () => {
    const user = userEvent.setup();
    render(<BookForm {...mockProps} />);

    const imageUrlField = screen.getByLabelText(/image url/i);
    await user.type(imageUrlField, 'not-a-valid-url');

    const submitButton = screen.getByRole('button', { name: /create book/i });
    await user.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText('Must be a valid URL')).toBeInTheDocument();
    });
  });

  it('submits valid form data', async () => {
    const user = userEvent.setup();
    render(<BookForm {...mockProps} />);

    const formData: BookFormData = {
      title: 'Test Book',
      author: 'Test Author',
      isbn: '978-0-123456-78-9',
      price: 19.99,
      category: 'Fiction',
      description: 'Test description',
      imageUrl: 'https://example.com/image.jpg',
      inStock: true,
    };

    // Fill in the form
    await user.type(screen.getByLabelText(/title/i), formData.title);
    await user.type(screen.getByLabelText(/author/i), formData.author);
    await user.type(screen.getByLabelText(/isbn/i), formData.isbn);
    await user.clear(screen.getByLabelText(/price/i));
    await user.type(screen.getByLabelText(/price/i), formData.price.toString());
    await user.click(screen.getByLabelText(/category/i));
    await user.click(screen.getByRole('option', { name: formData.category }));
    await user.type(screen.getByLabelText(/description/i), formData.description!);
    await user.type(screen.getByLabelText(/image url/i), formData.imageUrl!);

    const submitButton = screen.getByRole('button', { name: /create book/i });
    await user.click(submitButton);

    await waitFor(() => {
      expect(mockProps.onSubmit).toHaveBeenCalledWith(formData);
    });
  });

  it('shows loading state when loading prop is true', () => {
    render(<BookForm {...mockProps} loading={true} />);

    expect(screen.getByText('Saving...')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /saving/i })).toBeDisabled();
  });

  it('disables form fields when loading', () => {
    render(<BookForm {...mockProps} loading={true} />);

    expect(screen.getByLabelText(/title/i)).toBeDisabled();
    expect(screen.getByLabelText(/author/i)).toBeDisabled();
    expect(screen.getByRole('button', { name: /cancel/i })).toBeDisabled();
  });
});