import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '../../test/utils';
import userEvent from '@testing-library/user-event';
import Layout from '../../components/Layout';

// Mock useNavigate and useLocation
const mockNavigate = vi.fn();
const mockLocation = { pathname: '/' };

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
    useLocation: () => mockLocation,
  };
});

describe('Layout', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders the app bar with title', () => {
    render(
      <Layout>
        <div>Test Content</div>
      </Layout>
    );

    expect(screen.getByText('Bookstore')).toBeInTheDocument();
  });

  it('renders navigation buttons', () => {
    render(
      <Layout>
        <div>Test Content</div>
      </Layout>
    );

    expect(screen.getByRole('button', { name: 'Home' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /add book/i })).toBeInTheDocument();
  });

  it('renders children content', () => {
    const testContent = 'Test Content';
    render(
      <Layout>
        <div>{testContent}</div>
      </Layout>
    );

    expect(screen.getByText(testContent)).toBeInTheDocument();
  });

  it('navigates to home when home button is clicked', async () => {
    const user = userEvent.setup();
    render(
      <Layout>
        <div>Test Content</div>
      </Layout>
    );

    const homeButton = screen.getByRole('button', { name: 'Home' });
    await user.click(homeButton);

    expect(mockNavigate).toHaveBeenCalledWith('/');
  });

  it('navigates to add book when add book button is clicked', async () => {
    const user = userEvent.setup();
    render(
      <Layout>
        <div>Test Content</div>
      </Layout>
    );

    const addBookButton = screen.getByRole('button', { name: /add book/i });
    await user.click(addBookButton);

    expect(mockNavigate).toHaveBeenCalledWith('/add-book');
  });

  it('navigates to home when logo is clicked', async () => {
    const user = userEvent.setup();
    render(
      <Layout>
        <div>Test Content</div>
      </Layout>
    );

    const logoButton = screen.getByRole('button', { name: 'home' });
    // Find the book icon button (logo)
    const bookIconButton = screen.getByLabelText('home');
    await user.click(bookIconButton);

    expect(mockNavigate).toHaveBeenCalledWith('/');
  });

  it('highlights active navigation button', () => {
    // Mock location for home page
    mockLocation.pathname = '/';
    
    render(
      <Layout>
        <div>Test Content</div>
      </Layout>
    );

    const homeButton = screen.getByRole('button', { name: 'Home' });
    expect(homeButton).toHaveClass('MuiButton-outlined');
  });

  it('highlights add book button when on add book page', () => {
    // Mock location for add book page
    mockLocation.pathname = '/add-book';
    
    render(
      <Layout>
        <div>Test Content</div>
      </Layout>
    );

    const addBookButton = screen.getByRole('button', { name: /add book/i });
    expect(addBookButton).toHaveClass('MuiButton-outlined');
  });

  it('contains proper semantic structure', () => {
    render(
      <Layout>
        <div>Test Content</div>
      </Layout>
    );

    // Check for AppBar
    expect(screen.getByRole('banner')).toBeInTheDocument();
    
    // Check for main content area
    expect(screen.getByText('Test Content')).toBeInTheDocument();
  });
});