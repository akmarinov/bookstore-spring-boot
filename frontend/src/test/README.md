# Frontend Testing Suite

This directory contains the comprehensive testing setup for the React bookstore application.

## Structure

```
src/
├── test/
│   ├── setup.ts              # Test environment setup
│   ├── utils.tsx              # Custom render utilities
│   ├── mocks/
│   │   ├── mockData.ts        # Mock data for tests
│   │   ├── handlers.ts        # MSW request handlers
│   │   └── server.ts          # MSW server setup
│   └── README.md             # This file
├── tests/
│   ├── components/           # Component tests
│   │   ├── BookList.test.tsx
│   │   ├── BookForm.test.tsx
│   │   ├── BookItem.test.tsx
│   │   └── Layout.test.tsx
│   ├── api/                  # API tests
│   │   └── bookApi.test.ts
│   ├── hooks/                # Hook tests
│   │   ├── useBooks.test.ts
│   │   └── useBook.test.ts
│   ├── App.test.tsx          # Integration tests
│   └── basic.test.ts         # Basic environment test
```

## Testing Technologies

- **Vitest**: Fast unit testing framework
- **React Testing Library**: React component testing utilities
- **MSW (Mock Service Worker)**: API mocking for realistic testing
- **User Events**: Simulating user interactions
- **JSDOM**: Browser environment simulation

## Test Categories

### 1. Component Tests (`src/tests/components/`)

Test individual React components in isolation:

- **BookList.test.tsx**: Tests search, filtering, pagination, and book actions
- **BookForm.test.tsx**: Tests form validation, submission, and different modes
- **BookItem.test.tsx**: Tests book display and action buttons
- **Layout.test.tsx**: Tests navigation and layout functionality

### 2. API Tests (`src/tests/api/`)

Test API layer with mocked backend:

- **bookApi.test.ts**: Tests all CRUD operations, error handling, and edge cases

### 3. Hook Tests (`src/tests/hooks/`)

Test custom React hooks:

- **useBooks.test.ts**: Tests book listing, pagination, and search functionality
- **useBook.test.ts**: Tests individual book operations (CRUD)

### 4. Integration Tests

- **App.test.tsx**: Tests complete user flows and routing

## Running Tests

### Available Scripts

```bash
# Run tests in watch mode (development)
npm test

# Run tests once (CI/CD)
npm run test:run

# Run tests with coverage report
npm run test:coverage

# Run tests with UI
npm run test:ui

# Run tests for CI with detailed reporting
npm run test:ci

# Run tests in watch mode (explicit)
npm run test:watch
```

### Test Commands Examples

```bash
# Run all tests
npm test

# Run specific test file
npm test BookList.test.tsx

# Run tests matching pattern
npm test -- --grep "validation"

# Run tests with coverage
npm run test:coverage
```

## Coverage Requirements

The test suite is configured with coverage thresholds:

- **Branches**: 70% minimum
- **Functions**: 70% minimum
- **Lines**: 70% minimum
- **Statements**: 70% minimum

Coverage reports are generated in multiple formats:
- Terminal output (text)
- HTML report (`coverage/index.html`)
- JSON report (`coverage/coverage.json`)

## Mock Service Worker (MSW)

### API Mocking

MSW intercepts HTTP requests and provides realistic responses:

- **GET /api/books**: Returns paginated book list with filtering
- **GET /api/books/:id**: Returns single book by ID
- **POST /api/books**: Creates new book
- **PUT /api/books/:id**: Updates existing book
- **DELETE /api/books/:id**: Deletes book
- **GET /api/books/search**: Searches books
- **GET /api/books/category/:category**: Filters by category
- **GET /api/books/categories**: Returns available categories

### Mock Data

Predefined mock data includes:
- 3 sample books with realistic data
- Paginated responses
- Error scenarios
- Empty results

## Test Utilities

### Custom Render

`src/test/utils.tsx` provides a custom render function that wraps components with necessary providers:

- React Router (MemoryRouter for testing)
- Material-UI Theme Provider
- CSS Baseline

### Usage

```typescript
import { render, screen } from '../test/utils';
import MyComponent from './MyComponent';

test('renders component', () => {
  render(<MyComponent />);
  expect(screen.getByText('Hello')).toBeInTheDocument();
});
```

## Writing Tests

### Component Testing Example

```typescript
import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '../../test/utils';
import userEvent from '@testing-library/user-event';
import MyComponent from './MyComponent';

describe('MyComponent', () => {
  it('handles user interaction', async () => {
    const user = userEvent.setup();
    const mockCallback = vi.fn();
    
    render(<MyComponent onSubmit={mockCallback} />);
    
    const button = screen.getByRole('button', { name: /submit/i });
    await user.click(button);
    
    expect(mockCallback).toHaveBeenCalled();
  });
});
```

### Hook Testing Example

```typescript
import { describe, it, expect } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { useMyHook } from './useMyHook';

describe('useMyHook', () => {
  it('returns expected initial state', () => {
    const { result } = renderHook(() => useMyHook());
    
    expect(result.current.loading).toBe(false);
    expect(result.current.data).toBe(null);
  });
});
```

### API Testing Example

```typescript
import { describe, it, expect } from 'vitest';
import { myApi } from './myApi';

describe('myApi', () => {
  it('fetches data successfully', async () => {
    const result = await myApi.getData();
    
    expect(result).toBeDefined();
    expect(Array.isArray(result)).toBe(true);
  });
});
```

## Best Practices

### Test Organization

1. **Group related tests**: Use `describe` blocks to organize tests
2. **Clear test names**: Use descriptive test names that explain what's being tested
3. **One assertion per test**: Keep tests focused and atomic
4. **Setup and teardown**: Use `beforeEach` and `afterEach` for common setup

### Testing Guidelines

1. **Test user behavior**: Focus on what users can see and do
2. **Avoid implementation details**: Test interfaces, not internals
3. **Use realistic data**: Test with data similar to production
4. **Test error states**: Include tests for error scenarios
5. **Mock external dependencies**: Use MSW for API calls, mock complex dependencies

### Accessibility

Tests include accessibility considerations:
- Use semantic queries (`getByRole`, `getByLabelText`)
- Test keyboard navigation
- Verify ARIA attributes
- Check color contrast and focus management

## Configuration Files

### Vitest Configuration (`vite.config.ts`)

```typescript
test: {
  globals: true,
  environment: 'jsdom',
  setupFiles: ['./src/test/setup.ts'],
  css: true,
  coverage: {
    provider: 'v8',
    reporter: ['text', 'json', 'html'],
    thresholds: {
      global: {
        branches: 70,
        functions: 70,
        lines: 70,
        statements: 70,
      },
    },
  },
}
```

### Test Setup (`src/test/setup.ts`)

- Imports jest-dom matchers
- Configures MSW server
- Sets up global test environment
- Mocks browser APIs

## Continuous Integration

The test suite is designed for CI/CD environments:

- **Fast execution**: Optimized for quick feedback
- **Reliable**: Deterministic tests with proper mocking
- **Comprehensive**: High coverage with quality tests
- **Reporting**: JUnit XML output for CI integration

### CI Command

```bash
npm run test:ci
```

This command:
- Runs all tests once (no watch mode)
- Generates coverage report
- Outputs verbose results
- Creates JUnit XML for CI integration

## Debugging Tests

### Debug in VS Code

1. Set breakpoints in test files
2. Run "Debug Current Test File" command
3. Step through test execution

### Debug in Browser

```bash
# Run tests with UI for visual debugging
npm run test:ui
```

### Debug Failed Tests

```bash
# Run specific test file with more verbose output
npm test -- --reporter=verbose MyComponent.test.tsx
```

## Contributing

When adding new features:

1. **Write tests first** (TDD approach)
2. **Ensure coverage thresholds** are met
3. **Update mock data** if needed
4. **Test both happy path and edge cases**
5. **Follow existing patterns** in test organization

## Resources

- [Vitest Documentation](https://vitest.dev/)
- [React Testing Library](https://testing-library.com/docs/react-testing-library/intro/)
- [MSW Documentation](https://mswjs.io/)
- [Jest DOM Matchers](https://github.com/testing-library/jest-dom)