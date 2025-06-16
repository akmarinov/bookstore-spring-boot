import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '../../test/utils';
import userEvent from '@testing-library/user-event';
import BookItem from '../../components/BookItem';
import { mockBook } from '../../test/mocks/mockData';

describe('BookItem', () => {
  const mockHandlers = {
    onEdit: vi.fn(),
    onDelete: vi.fn(),
    onView: vi.fn(),
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders book information correctly', () => {
    render(<BookItem book={mockBook} {...mockHandlers} />);

    expect(screen.getByText(mockBook.title)).toBeInTheDocument();
    expect(screen.getByText(`by ${mockBook.author}`)).toBeInTheDocument();
    expect(screen.getByText(`ISBN: ${mockBook.isbn}`)).toBeInTheDocument();
    expect(screen.getByText(`$${mockBook.price.toFixed(2)}`)).toBeInTheDocument();
    expect(screen.getByText(mockBook.category)).toBeInTheDocument();
    expect(screen.getByText('In Stock')).toBeInTheDocument();
    expect(screen.getByText(mockBook.description!)).toBeInTheDocument();
  });

  it('renders out of stock status when book is not in stock', () => {
    const outOfStockBook = { ...mockBook, inStock: false };
    render(<BookItem book={outOfStockBook} {...mockHandlers} />);

    expect(screen.getByText('Out of Stock')).toBeInTheDocument();
  });

  it('renders image when imageUrl is provided', () => {
    render(<BookItem book={mockBook} {...mockHandlers} />);

    const image = screen.getByRole('img', { name: mockBook.title });
    expect(image).toBeInTheDocument();
    expect(image).toHaveAttribute('src', mockBook.imageUrl);
  });

  it('does not render image when imageUrl is not provided', () => {
    const bookWithoutImage = { ...mockBook, imageUrl: undefined };
    render(<BookItem book={bookWithoutImage} {...mockHandlers} />);

    expect(screen.queryByRole('img')).not.toBeInTheDocument();
  });

  it('does not render description when not provided', () => {
    const bookWithoutDescription = { ...mockBook, description: undefined };
    render(<BookItem book={bookWithoutDescription} {...mockHandlers} />);

    expect(screen.queryByText(mockBook.description!)).not.toBeInTheDocument();
  });

  it('calls onView when View button is clicked', async () => {
    const user = userEvent.setup();
    render(<BookItem book={mockBook} {...mockHandlers} />);

    const viewButton = screen.getByRole('button', { name: /view/i });
    await user.click(viewButton);

    expect(mockHandlers.onView).toHaveBeenCalledWith(mockBook);
    expect(mockHandlers.onView).toHaveBeenCalledTimes(1);
  });

  it('calls onEdit when Edit button is clicked', async () => {
    const user = userEvent.setup();
    render(<BookItem book={mockBook} {...mockHandlers} />);

    const editButton = screen.getByRole('button', { name: /edit/i });
    await user.click(editButton);

    expect(mockHandlers.onEdit).toHaveBeenCalledWith(mockBook);
    expect(mockHandlers.onEdit).toHaveBeenCalledTimes(1);
  });

  it('calls onDelete when Delete button is clicked', async () => {
    const user = userEvent.setup();
    render(<BookItem book={mockBook} {...mockHandlers} />);

    const deleteButton = screen.getByRole('button', { name: /delete/i });
    await user.click(deleteButton);

    expect(mockHandlers.onDelete).toHaveBeenCalledWith(mockBook);
    expect(mockHandlers.onDelete).toHaveBeenCalledTimes(1);
  });

  it('renders without handlers when not provided', () => {
    render(<BookItem book={mockBook} />);

    expect(screen.getByRole('button', { name: /view/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /edit/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /delete/i })).toBeInTheDocument();
  });

  it('handles buttons clicks gracefully when handlers are not provided', async () => {
    const user = userEvent.setup();
    render(<BookItem book={mockBook} />);

    const viewButton = screen.getByRole('button', { name: /view/i });
    const editButton = screen.getByRole('button', { name: /edit/i });
    const deleteButton = screen.getByRole('button', { name: /delete/i });

    // These should not throw errors
    await user.click(viewButton);
    await user.click(editButton);
    await user.click(deleteButton);
  });
});