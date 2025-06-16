# Development Guide

Comprehensive development guide for the Bookstore Spring Boot application. This guide covers development environment setup, coding standards, testing practices, contribution guidelines, and best practices for both frontend and backend development.

## 📋 Development Overview

This project follows modern software development practices with a focus on:

- **Clean Architecture**: Separation of concerns across layers
- **Test-Driven Development**: Comprehensive testing at all levels
- **Code Quality**: Automated code analysis and formatting
- **Security First**: Security considerations in all development phases
- **DevOps Integration**: Continuous integration and deployment practices

## 🛠️ Development Environment Setup

### Prerequisites

Ensure you have the following tools installed:

| Tool | Version | Purpose | Installation |
|------|---------|---------|--------------|
| **Java JDK** | 17+ | Backend development | [OpenJDK](https://openjdk.org/) or [Oracle JDK](https://www.oracle.com/java/) |
| **Node.js** | 18+ | Frontend development | [Node.js Official](https://nodejs.org/) |
| **Maven** | 3.9+ | Backend build tool | [Maven Installation](https://maven.apache.org/install.html) |
| **Docker** | 20.10+ | Containerization | [Docker Desktop](https://www.docker.com/products/docker-desktop) |
| **Git** | 2.30+ | Version control | [Git Official](https://git-scm.com/) |

### IDE Setup

#### Recommended IDEs

**Backend (Java):**
- **IntelliJ IDEA** (recommended)
- **Eclipse IDE**
- **Visual Studio Code** with Java extensions

**Frontend (React/TypeScript):**
- **Visual Studio Code** (recommended)
- **WebStorm**
- **Atom** with TypeScript support

#### IntelliJ IDEA Configuration

1. **Install required plugins**:
   ```
   - Lombok Plugin
   - Spring Boot Plugin
   - Docker Plugin
   - SonarLint
   ```

2. **Import project**:
   ```
   File → Open → Select project root directory
   Import as Maven project
   ```

3. **Configure code style**:
   ```
   File → Settings → Editor → Code Style
   Import scheme from: .idea/codeStyles/Project.xml
   ```

4. **Enable annotation processing**:
   ```
   File → Settings → Build → Compiler → Annotation Processors
   ✓ Enable annotation processing
   ```

#### Visual Studio Code Configuration

1. **Install extensions**:
   ```json
   {
     "recommendations": [
       "vscjava.vscode-java-pack",
       "vmware.vscode-spring-boot",
       "bradlc.vscode-tailwindcss",
       "esbenp.prettier-vscode",
       "ms-vscode.vscode-typescript-next",
       "ms-vscode-remote.remote-containers"
     ]
   }
   ```

2. **Workspace settings** (`.vscode/settings.json`):
   ```json
   {
     "java.home": "/usr/lib/jvm/java-17-openjdk",
     "java.configuration.updateBuildConfiguration": "automatic",
     "spring-boot.ls.problem.application-properties.unknown-property": "ignore",
     "editor.formatOnSave": true,
     "editor.defaultFormatter": "esbenp.prettier-vscode",
     "typescript.preferences.quoteStyle": "single",
     "javascript.preferences.quoteStyle": "single"
   }
   ```

### Local Development Setup

#### Option 1: Docker Development Environment (Recommended)

```bash
# Clone the repository
git clone [repository-url]
cd bookstore-spring-boot

# Copy development environment configuration
cp .env.example .env.dev

# Start development environment
./scripts/docker/deploy-dev.sh start

# Access services:
# Frontend: http://localhost:3000
# Backend: http://localhost:8080
# Database: localhost:3306
# PhpMyAdmin: http://localhost:8082
```

#### Option 2: Manual Development Setup

1. **Setup MySQL database**:
   ```bash
   # Using Docker (recommended for development)
   docker run --name bookstore-mysql \
     -e MYSQL_ROOT_PASSWORD=password \
     -e MYSQL_DATABASE=bookstore_db \
     -e MYSQL_USER=bookstore \
     -e MYSQL_PASSWORD=bookstore123 \
     -p 3306:3306 \
     -d mysql:8.0
   
   # Or install MySQL locally
   sudo apt install mysql-server
   sudo mysql_secure_installation
   ```

2. **Configure database**:
   ```sql
   CREATE DATABASE bookstore_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   CREATE USER 'bookstore'@'localhost' IDENTIFIED BY 'bookstore123';
   GRANT ALL PRIVILEGES ON bookstore_db.* TO 'bookstore'@'localhost';
   FLUSH PRIVILEGES;
   ```

3. **Start backend development server**:
   ```bash
   cd backend
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   
   # Backend will be available at http://localhost:8080
   # Swagger UI at http://localhost:8080/swagger-ui.html
   ```

4. **Start frontend development server**:
   ```bash
   cd frontend
   npm install
   npm run dev
   
   # Frontend will be available at http://localhost:5173
   # With hot reloading enabled
   ```

### Development Configuration

#### Backend Configuration (application-dev.properties)

```properties
# Development database configuration
spring.datasource.url=jdbc:mysql://localhost:3306/bookstore_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=bookstore
spring.datasource.password=bookstore123

# JPA/Hibernate configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Logging configuration
logging.level.com.example.bookstore=DEBUG
logging.level.org.springframework.web=DEBUG
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE

# DevTools configuration
spring.devtools.restart.enabled=true
spring.devtools.livereload.enabled=true

# Actuator configuration
management.endpoints.web.exposure.include=*
management.endpoint.health.show-details=always

# CORS configuration for development
app.cors.allowed-origins=http://localhost:3000,http://localhost:5173,http://localhost:3001
app.cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
app.cors.allowed-headers=*
app.cors.allow-credentials=true

# Security configuration for development
app.security.enable-csrf=false
app.security.public-endpoints=/**
```

#### Frontend Configuration (vite.config.ts)

```typescript
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    host: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      },
    },
  },
  build: {
    outDir: 'dist',
    sourcemap: true,
    rollupOptions: {
      output: {
        manualChunks: {
          vendor: ['react', 'react-dom'],
          mui: ['@mui/material', '@mui/icons-material'],
          router: ['react-router-dom'],
        },
      },
    },
  },
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: ['./src/test/setup.ts'],
  },
});
```

## 🏗️ Project Structure and Architecture

### Backend Architecture

```
backend/src/main/java/com/example/bookstore/
├── config/                     # Configuration classes
│   ├── CorsConfig.java         # CORS configuration
│   ├── SecurityConfig.java     # Security configuration
│   ├── SwaggerConfig.java      # API documentation config
│   └── MonitoringConfig.java   # Monitoring configuration
├── controller/                 # REST controllers
│   ├── BookController.java     # Book CRUD operations
│   └── HealthController.java   # Health check endpoints
├── dto/                        # Data Transfer Objects
│   ├── BookCreateRequest.java  # Book creation request
│   ├── BookUpdateRequest.java  # Book update request
│   ├── BookDTO.java            # Book response DTO
│   └── ErrorResponse.java      # Error response DTO
├── exception/                  # Custom exceptions
│   ├── BookNotFoundException.java
│   └── GlobalExceptionHandler.java
├── model/                      # JPA entities
│   └── Book.java              # Book entity
├── repository/                 # Data access layer
│   └── BookRepository.java    # Book repository interface
├── service/                    # Business logic layer
│   └── BookService.java       # Book service implementation
├── metrics/                    # Custom metrics
│   └── BookMetricsCollector.java
└── filter/                     # Security filters
    └── SecurityHeadersFilter.java
```

### Frontend Architecture

```
frontend/src/
├── components/                 # Reusable UI components
│   ├── BookForm.tsx           # Book creation/edit form
│   ├── BookItem.tsx           # Individual book display
│   ├── BookList.tsx           # Book list with pagination
│   └── Layout.tsx             # Application layout
├── pages/                      # Page components
│   ├── HomePage.tsx           # Home page with book list
│   ├── AddBookPage.tsx        # Add new book page
│   └── EditBookPage.tsx       # Edit book page
├── hooks/                      # Custom React hooks
│   ├── useBooks.ts            # Books data management
│   └── useBook.ts             # Individual book management
├── api/                        # API client functions
│   ├── bookApi.ts             # Book API operations
│   └── client.ts              # Axios configuration
├── types/                      # TypeScript type definitions
│   ├── Book.ts                # Book type definition
│   └── vitest.d.ts            # Test type definitions
├── tests/                      # Test files
│   ├── components/            # Component tests
│   ├── hooks/                 # Hook tests
│   ├── api/                   # API tests
│   └── App.test.tsx           # Main app tests
└── test/                       # Test utilities
    ├── setup.ts               # Test setup configuration
    ├── utils.tsx              # Test utilities
    └── mocks/                 # Mock data and handlers
        ├── handlers.ts        # MSW request handlers
        ├── mockData.ts        # Mock test data
        └── server.ts          # MSW server setup
```

## 📝 Coding Standards and Guidelines

### Backend Coding Standards (Java)

#### Code Style Guidelines

1. **Naming Conventions**:
   ```java
   // Classes: PascalCase
   public class BookService { }
   
   // Methods and variables: camelCase
   public List<Book> getAllBooks() { }
   private String bookTitle;
   
   // Constants: UPPER_SNAKE_CASE
   private static final String DEFAULT_CATEGORY = "UNCATEGORIZED";
   
   // Packages: lowercase with dots
   package com.example.bookstore.service;
   ```

2. **Method Structure**:
   ```java
   /**
    * Retrieves all books with pagination support.
    *
    * @param pageable pagination information
    * @return paginated list of books
    * @throws ServiceException if retrieval fails
    */
   @Transactional(readOnly = true)
   public Page<Book> getAllBooks(Pageable pageable) {
       // Validate input parameters
       if (pageable == null) {
           throw new IllegalArgumentException("Pageable cannot be null");
       }
       
       try {
           // Business logic implementation
           return bookRepository.findAll(pageable);
       } catch (Exception e) {
           // Error handling
           throw new ServiceException("Failed to retrieve books", e);
       }
   }
   ```

3. **Class Structure**:
   ```java
   @Service
   @Transactional
   @Slf4j
   public class BookService {
       
       // Static constants
       private static final int MAX_TITLE_LENGTH = 255;
       
       // Dependencies (final fields)
       private final BookRepository bookRepository;
       private final BookMetricsCollector metricsCollector;
       
       // Constructor
       public BookService(BookRepository bookRepository, 
                         BookMetricsCollector metricsCollector) {
           this.bookRepository = bookRepository;
           this.metricsCollector = metricsCollector;
       }
       
       // Public methods
       public Book createBook(BookCreateRequest request) { }
       
       // Private helper methods
       private void validateBookRequest(BookCreateRequest request) { }
   }
   ```

#### Spring Boot Best Practices

1. **Controller Layer**:
   ```java
   @RestController
   @RequestMapping("/api/v1/books")
   @Tag(name = "Books", description = "Book management APIs")
   @Validated
   public class BookController {
       
       @PostMapping
       @Operation(summary = "Create new book")
       public ResponseEntity<Book> createBook(@Valid @RequestBody BookCreateRequest request) {
           Book book = bookService.createBook(request);
           return ResponseEntity.status(HttpStatus.CREATED).body(book);
       }
   }
   ```

2. **Service Layer**:
   ```java
   @Service
   @Transactional
   public class BookService {
       
       @Transactional(readOnly = true)
       public Page<Book> getAllBooks(Pageable pageable) {
           return bookRepository.findAll(pageable);
       }
       
       @Transactional
       public Book createBook(BookCreateRequest request) {
           validateBookRequest(request);
           Book book = mapToEntity(request);
           return bookRepository.save(book);
       }
   }
   ```

3. **Repository Layer**:
   ```java
   @Repository
   public interface BookRepository extends JpaRepository<Book, Long> {
       
       @Query("SELECT b FROM Book b WHERE " +
              "LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
              "LOWER(b.author) LIKE LOWER(CONCAT('%', :query, '%'))")
       Page<Book> findByTitleOrAuthorContainingIgnoreCase(
           @Param("query") String query, 
           Pageable pageable);
       
       @Query("SELECT COUNT(b) FROM Book b")
       long countTotalBooks();
   }
   ```

### Frontend Coding Standards (TypeScript/React)

#### Code Style Guidelines

1. **Naming Conventions**:
   ```typescript
   // Components: PascalCase
   export const BookForm = () => { };
   
   // Variables and functions: camelCase
   const bookTitle = 'Example';
   const handleSubmit = () => { };
   
   // Constants: UPPER_SNAKE_CASE
   const MAX_BOOKS_PER_PAGE = 10;
   
   // Types and interfaces: PascalCase
   interface Book {
     id: number;
     title: string;
   }
   
   // Files: kebab-case or PascalCase for components
   book-form.tsx or BookForm.tsx
   ```

2. **Component Structure**:
   ```typescript
   import React from 'react';
   import { Button, TextField } from '@mui/material';
   import { useForm } from 'react-hook-form';
   import { Book } from '../types/Book';
   
   interface BookFormProps {
     book?: Book;
     onSubmit: (book: Book) => void;
     onCancel: () => void;
   }
   
   export const BookForm: React.FC<BookFormProps> = ({ 
     book, 
     onSubmit, 
     onCancel 
   }) => {
     // Hooks at the top
     const { register, handleSubmit, formState: { errors } } = useForm<Book>();
     
     // Event handlers
     const handleFormSubmit = (data: Book) => {
       onSubmit(data);
     };
     
     // Render
     return (
       <form onSubmit={handleSubmit(handleFormSubmit)}>
         {/* Form content */}
       </form>
     );
   };
   ```

3. **Custom Hooks**:
   ```typescript
   import { useState, useEffect } from 'react';
   import { bookApi } from '../api/bookApi';
   import { Book } from '../types/Book';
   
   export const useBooks = () => {
     const [books, setBooks] = useState<Book[]>([]);
     const [loading, setLoading] = useState(false);
     const [error, setError] = useState<string | null>(null);
     
     const fetchBooks = async () => {
       setLoading(true);
       try {
         const response = await bookApi.getAllBooks();
         setBooks(response.content);
       } catch (err) {
         setError(err instanceof Error ? err.message : 'Unknown error');
       } finally {
         setLoading(false);
       }
     };
     
     useEffect(() => {
       fetchBooks();
     }, []);
     
     return { books, loading, error, refetch: fetchBooks };
   };
   ```

#### React Best Practices

1. **Component Organization**:
   ```typescript
   // Prefer functional components with hooks
   const BookList: React.FC<BookListProps> = ({ books, onBookSelect }) => {
     // Group related state
     const [selectedBook, setSelectedBook] = useState<Book | null>(null);
     const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('asc');
     
     // Memoize expensive calculations
     const sortedBooks = useMemo(() => {
       return books.sort((a, b) => 
         sortOrder === 'asc' 
           ? a.title.localeCompare(b.title)
           : b.title.localeCompare(a.title)
       );
     }, [books, sortOrder]);
     
     // Extract complex logic to custom hooks
     const { pagination, handlePageChange } = usePagination(sortedBooks);
     
     return (
       <div>
         {/* Component JSX */}
       </div>
     );
   };
   ```

2. **Error Boundaries**:
   ```typescript
   class ErrorBoundary extends React.Component<
     { children: React.ReactNode },
     { hasError: boolean }
   > {
     constructor(props: { children: React.ReactNode }) {
       super(props);
       this.state = { hasError: false };
     }
     
     static getDerivedStateFromError(): { hasError: boolean } {
       return { hasError: true };
     }
     
     componentDidCatch(error: Error, errorInfo: React.ErrorInfo) {
       console.error('Error caught by boundary:', error, errorInfo);
     }
     
     render() {
       if (this.state.hasError) {
         return <h1>Something went wrong.</h1>;
       }
       return this.props.children;
     }
   }
   ```

## 🧪 Testing Guidelines

### Backend Testing

#### Unit Testing Best Practices

1. **Service Layer Tests**:
   ```java
   @ExtendWith(MockitoExtension.class)
   class BookServiceTest {
       
       @Mock
       private BookRepository bookRepository;
       
       @Mock
       private BookMetricsCollector metricsCollector;
       
       @InjectMocks
       private BookService bookService;
       
       @Test
       @DisplayName("Should create book successfully when valid request provided")
       void shouldCreateBookSuccessfully() {
           // Given
           BookCreateRequest request = TestDataBuilder.aBookCreateRequest()
               .withTitle("Test Book")
               .withAuthor("Test Author")
               .build();
           
           Book savedBook = TestDataBuilder.aBook()
               .withId(1L)
               .withTitle("Test Book")
               .build();
           
           when(bookRepository.save(any(Book.class))).thenReturn(savedBook);
           
           // When
           Book result = bookService.createBook(request);
           
           // Then
           assertThat(result)
               .isNotNull()
               .extracting(Book::getTitle, Book::getAuthor)
               .containsExactly("Test Book", "Test Author");
           
           verify(bookRepository).save(any(Book.class));
           verify(metricsCollector).incrementBookCreated();
       }
   }
   ```

2. **Repository Tests**:
   ```java
   @DataJpaTest
   @AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
   @Testcontainers
   class BookRepositoryTest {
       
       @Container
       static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
           .withDatabaseName("testdb")
           .withUsername("test")
           .withPassword("test");
       
       @Autowired
       private TestEntityManager entityManager;
       
       @Autowired
       private BookRepository bookRepository;
       
       @Test
       void shouldFindBooksByTitleContaining() {
           // Given
           Book book1 = TestDataBuilder.aBook().withTitle("Java Programming").build();
           Book book2 = TestDataBuilder.aBook().withTitle("Python Guide").build();
           entityManager.persistAndFlush(book1);
           entityManager.persistAndFlush(book2);
           
           // When
           Page<Book> result = bookRepository.findByTitleOrAuthorContainingIgnoreCase(
               "java", PageRequest.of(0, 10));
           
           // Then
           assertThat(result.getContent())
               .hasSize(1)
               .extracting(Book::getTitle)
               .containsExactly("Java Programming");
       }
   }
   ```

3. **Controller Tests**:
   ```java
   @WebMvcTest(BookController.class)
   class BookControllerTest {
       
       @Autowired
       private MockMvc mockMvc;
       
       @MockBean
       private BookService bookService;
       
       @Test
       void shouldReturnCreatedBookWhenValidRequest() throws Exception {
           // Given
           BookCreateRequest request = TestDataBuilder.aBookCreateRequest().build();
           Book createdBook = TestDataBuilder.aBook().withId(1L).build();
           
           when(bookService.createBook(any(BookCreateRequest.class)))
               .thenReturn(createdBook);
           
           // When & Then
           mockMvc.perform(post("/api/v1/books")
                   .contentType(MediaType.APPLICATION_JSON)
                   .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.id").value(1))
               .andExpect(jsonPath("$.title").value("Test Book"));
           
           verify(bookService).createBook(any(BookCreateRequest.class));
       }
   }
   ```

### Frontend Testing

#### Component Testing

1. **Component Tests with Testing Library**:
   ```typescript
   import { render, screen, fireEvent, waitFor } from '@testing-library/react';
   import userEvent from '@testing-library/user-event';
   import { BookForm } from '../BookForm';
   import { Book } from '../../types/Book';
   
   describe('BookForm', () => {
     const mockOnSubmit = jest.fn();
     const mockOnCancel = jest.fn();
     
     beforeEach(() => {
       jest.clearAllMocks();
     });
     
     it('should render form fields', () => {
       render(
         <BookForm onSubmit={mockOnSubmit} onCancel={mockOnCancel} />
       );
       
       expect(screen.getByLabelText(/title/i)).toBeInTheDocument();
       expect(screen.getByLabelText(/author/i)).toBeInTheDocument();
       expect(screen.getByLabelText(/price/i)).toBeInTheDocument();
     });
     
     it('should submit form with valid data', async () => {
       const user = userEvent.setup();
       
       render(
         <BookForm onSubmit={mockOnSubmit} onCancel={mockOnCancel} />
       );
       
       await user.type(screen.getByLabelText(/title/i), 'Test Book');
       await user.type(screen.getByLabelText(/author/i), 'Test Author');
       await user.type(screen.getByLabelText(/price/i), '19.99');
       
       await user.click(screen.getByRole('button', { name: /submit/i }));
       
       await waitFor(() => {
         expect(mockOnSubmit).toHaveBeenCalledWith({
           title: 'Test Book',
           author: 'Test Author',
           price: 19.99
         });
       });
     });
   });
   ```

2. **Hook Testing**:
   ```typescript
   import { renderHook, waitFor } from '@testing-library/react';
   import { useBooks } from '../useBooks';
   import { bookApi } from '../../api/bookApi';
   
   jest.mock('../../api/bookApi');
   const mockBookApi = bookApi as jest.Mocked<typeof bookApi>;
   
   describe('useBooks', () => {
     it('should fetch books on mount', async () => {
       const mockBooks = [
         { id: 1, title: 'Book 1', author: 'Author 1' },
         { id: 2, title: 'Book 2', author: 'Author 2' },
       ];
       
       mockBookApi.getAllBooks.mockResolvedValue({
         content: mockBooks,
         totalElements: 2,
       });
       
       const { result } = renderHook(() => useBooks());
       
       expect(result.current.loading).toBe(true);
       
       await waitFor(() => {
         expect(result.current.loading).toBe(false);
         expect(result.current.books).toEqual(mockBooks);
       });
     });
   });
   ```

### Testing Data Builders

```java
// Backend test data builder
public class TestDataBuilder {
    public static BookBuilder aBook() {
        return new BookBuilder();
    }
    
    public static class BookBuilder {
        private Long id;
        private String title = "Default Title";
        private String author = "Default Author";
        private BigDecimal price = new BigDecimal("19.99");
        
        public BookBuilder withId(Long id) {
            this.id = id;
            return this;
        }
        
        public BookBuilder withTitle(String title) {
            this.title = title;
            return this;
        }
        
        public Book build() {
            Book book = new Book();
            book.setId(id);
            book.setTitle(title);
            book.setAuthor(author);
            book.setPrice(price);
            return book;
        }
    }
}
```

```typescript
// Frontend test data builder
export const createMockBook = (overrides: Partial<Book> = {}): Book => ({
  id: 1,
  title: 'Test Book',
  author: 'Test Author',
  price: 19.99,
  isbn: '978-1234567890',
  description: 'Test description',
  category: 'Fiction',
  stockQuantity: 10,
  createdAt: '2025-01-06T10:00:00Z',
  updatedAt: '2025-01-06T10:00:00Z',
  ...overrides,
});
```

## 🔧 Build and Development Scripts

### Backend Scripts

#### Maven Commands

```bash
# Development
./mvnw spring-boot:run                    # Start development server
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev  # With dev profile

# Testing
./mvnw test                               # Run all tests
./mvnw test -Dtest=BookServiceTest        # Run specific test
./mvnw test jacoco:report                 # Run tests with coverage
./mvnw verify                             # Run tests and integration tests

# Build
./mvnw clean compile                      # Compile only
./mvnw clean package                      # Build JAR
./mvnw clean package -Pprod               # Build for production
./mvnw clean package -DskipTests          # Build without tests

# Database
./mvnw flyway:migrate                     # Run database migrations
./mvnw flyway:info                        # Check migration status
./mvnw flyway:clean                       # Clean database (dev only)

# Code Quality
./mvnw spotless:check                     # Check code formatting
./mvnw spotless:apply                     # Apply code formatting
./mvnw sonar:sonar                        # Run SonarQube analysis
```

#### Custom Scripts

```bash
# scripts/backend/dev-setup.sh
#!/bin/bash
echo "Setting up backend development environment..."

# Install dependencies
./mvnw dependency:resolve

# Run database migrations
./mvnw flyway:migrate

# Start development server
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Frontend Scripts

#### npm/yarn Commands

```bash
# Development
npm run dev                               # Start development server
npm run dev:debug                         # Start with debug mode
npm run type-check                        # TypeScript type checking
npm run type-check:watch                  # Watch mode type checking

# Testing
npm run test                              # Run tests in watch mode
npm run test:run                          # Run tests once
npm run test:coverage                     # Run tests with coverage
npm run test:ui                           # Run tests with UI
npm run test:ci                           # Run tests for CI

# Build
npm run build                             # Build for production
npm run build:dev                         # Build for development
npm run build:analyze                     # Build with bundle analysis
npm run preview                           # Preview production build

# Code Quality
npm run lint                              # Run ESLint
npm run lint:fix                          # Fix ESLint issues
npm run format                            # Format code with Prettier
npm run format:check                      # Check code formatting

# Maintenance
npm run clean                             # Clean build artifacts
npm run clean:deps                        # Clean and reinstall dependencies
npm run validate                          # Run all quality checks
```

#### Custom Scripts

```bash
# scripts/frontend/dev-setup.sh
#!/bin/bash
echo "Setting up frontend development environment..."

# Install dependencies
npm install

# Run type checking
npm run type-check

# Start development server
npm run dev
```

## 🔍 Code Quality and Tools

### Static Code Analysis

#### Backend (Java)

1. **SpotBugs Configuration** (spotbugs-maven-plugin):
   ```xml
   <plugin>
       <groupId>com.github.spotbugs</groupId>
       <artifactId>spotbugs-maven-plugin</artifactId>
       <configuration>
           <effort>Max</effort>
           <threshold>Low</threshold>
           <excludeFilterFile>spotbugs-exclude.xml</excludeFilterFile>
       </configuration>
   </plugin>
   ```

2. **Checkstyle Configuration**:
   ```xml
   <plugin>
       <groupId>org.apache.maven.plugins</groupId>
       <artifactId>maven-checkstyle-plugin</artifactId>
       <configuration>
           <configLocation>checkstyle.xml</configLocation>
           <encoding>UTF-8</encoding>
           <consoleOutput>true</consoleOutput>
           <failsOnError>true</failsOnError>
       </configuration>
   </plugin>
   ```

#### Frontend (TypeScript/React)

1. **ESLint Configuration** (.eslintrc.js):
   ```javascript
   module.exports = {
     root: true,
     env: { browser: true, es2020: true },
     extends: [
       'eslint:recommended',
       '@typescript-eslint/recommended',
       'plugin:react-hooks/recommended',
       'plugin:react/recommended',
     ],
     ignorePatterns: ['dist', '.eslintrc.cjs'],
     parser: '@typescript-eslint/parser',
     plugins: ['react-refresh'],
     rules: {
       'react-refresh/only-export-components': [
         'warn',
         { allowConstantExport: true },
       ],
       '@typescript-eslint/no-unused-vars': ['error'],
       'react/react-in-jsx-scope': 'off',
     },
   };
   ```

2. **Prettier Configuration** (.prettierrc):
   ```json
   {
     "semi": true,
     "trailingComma": "es5",
     "singleQuote": true,
     "printWidth": 80,
     "tabWidth": 2,
     "useTabs": false
   }
   ```

### Pre-commit Hooks

```bash
# .husky/pre-commit
#!/usr/bin/env sh
. "$(dirname -- "$0")/_/husky.sh"

# Backend checks
cd backend
./mvnw spotless:check
./mvnw test

# Frontend checks
cd ../frontend
npm run lint
npm run type-check
npm run test:run
```

## 🐛 Debugging

### Backend Debugging

#### IntelliJ IDEA Debug Configuration

1. **Remote Debug Configuration**:
   ```
   Name: Bookstore Backend Debug
   Host: localhost
   Port: 5005
   Use module classpath: bookstore-backend
   ```

2. **Start application in debug mode**:
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
   ```

#### Logging Configuration

```yaml
# logback-spring.xml (development)
<configuration>
    <springProfile name="dev">
        <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
            <encoder>
                <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
            </encoder>
        </appender>
        
        <logger name="com.example.bookstore" level="DEBUG"/>
        <logger name="org.springframework.web" level="DEBUG"/>
        <logger name="org.hibernate.SQL" level="DEBUG"/>
        
        <root level="INFO">
            <appender-ref ref="STDOUT"/>
        </root>
    </springProfile>
</configuration>
```

### Frontend Debugging

#### Browser DevTools

1. **React DevTools** - Install browser extension
2. **Redux DevTools** - For state management debugging (if using Redux)

#### VS Code Debug Configuration

```json
// .vscode/launch.json
{
  "version": "0.2.0",
  "configurations": [
    {
      "name": "Debug React App",
      "type": "node",
      "request": "launch",
      "program": "${workspaceFolder}/frontend/node_modules/.bin/vite",
      "args": ["--mode", "development"],
      "console": "integratedTerminal",
      "envFile": "${workspaceFolder}/.env.dev"
    }
  ]
}
```

## 📋 Git Workflow and Branching Strategy

### Branching Strategy

We follow **Git Flow** with the following branches:

- **main** - Production-ready code
- **develop** - Integration branch for features
- **feature/*** - Feature development branches
- **release/*** - Release preparation branches
- **hotfix/*** - Critical bug fixes

### Branch Naming Conventions

```
feature/add-book-search-functionality
feature/implement-user-authentication
bugfix/fix-pagination-issue
hotfix/critical-security-patch
release/1.2.0
```

### Commit Message Format

Follow **Conventional Commits** specification:

```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

Examples:
```
feat(api): add book search functionality

- Implement search by title and author
- Add pagination support for search results
- Include search query validation

Closes #123

fix(frontend): resolve pagination reset issue

The pagination was not resetting when searching,
causing confusion for users.

Fixes #456

docs: update API documentation for search endpoints

test(backend): add integration tests for book search

perf(database): optimize book search query performance

refactor(frontend): extract search logic to custom hook
```

### Pull Request Guidelines

1. **PR Title**: Use conventional commit format
2. **Description Template**:
   ```markdown
   ## Summary
   Brief description of changes
   
   ## Changes Made
   - List of specific changes
   - Another change
   
   ## Testing
   - [ ] Unit tests added/updated
   - [ ] Integration tests pass
   - [ ] Manual testing completed
   
   ## Screenshots (if applicable)
   
   ## Checklist
   - [ ] Code follows project conventions
   - [ ] Tests added for new functionality
   - [ ] Documentation updated
   - [ ] No breaking changes
   ```

3. **Review Requirements**:
   - At least one reviewer approval
   - All CI checks pass
   - No merge conflicts
   - Up-to-date with target branch

## 🚀 Continuous Integration

### GitHub Actions Workflow

```yaml
# .github/workflows/ci.yml
name: CI Pipeline

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main, develop]

jobs:
  backend-tests:
    runs-on: ubuntu-latest
    
    services:
      mysql:
        image: mysql:8.0
        env:
          MYSQL_ROOT_PASSWORD: password
          MYSQL_DATABASE: testdb
        options: >-
          --health-cmd="mysqladmin ping"
          --health-interval=10s
          --health-timeout=5s
          --health-retries=3
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Cache Maven dependencies
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
    
    - name: Run backend tests
      run: |
        cd backend
        ./mvnw clean test jacoco:report
    
    - name: Upload coverage reports
      uses: codecov/codecov-action@v3
      with:
        file: backend/target/site/jacoco/jacoco.xml

  frontend-tests:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up Node.js
      uses: actions/setup-node@v3
      with:
        node-version: '18'
        cache: 'npm'
        cache-dependency-path: frontend/package-lock.json
    
    - name: Install dependencies
      run: |
        cd frontend
        npm ci
    
    - name: Run frontend tests
      run: |
        cd frontend
        npm run test:coverage
    
    - name: Upload coverage reports
      uses: codecov/codecov-action@v3
      with:
        file: frontend/coverage/lcov.info

  build-and-test:
    needs: [backend-tests, frontend-tests]
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Build Docker images
      run: |
        docker-compose -f docker-compose.yml build
    
    - name: Run integration tests
      run: |
        docker-compose up -d
        ./scripts/wait-for-services.sh
        ./scripts/run-integration-tests.sh
        docker-compose down
```

### Local CI Scripts

```bash
# scripts/ci/run-local-ci.sh
#!/bin/bash
set -e

echo "Running local CI pipeline..."

# Backend tests
echo "Running backend tests..."
cd backend
./mvnw clean test jacoco:report
cd ..

# Frontend tests
echo "Running frontend tests..."
cd frontend
npm run test:coverage
npm run lint
npm run type-check
cd ..

# Build and integration tests
echo "Building Docker images..."
docker-compose build

echo "Running integration tests..."
docker-compose up -d
./scripts/wait-for-services.sh
./scripts/run-integration-tests.sh
docker-compose down

echo "Local CI pipeline completed successfully!"
```

## 📚 Resources and Documentation

### Learning Resources

#### Backend (Spring Boot)
- [Spring Boot Official Documentation](https://spring.io/projects/spring-boot)
- [Spring Data JPA Reference](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [Spring Security Reference](https://docs.spring.io/spring-security/site/docs/current/reference/html5/)
- [Testing Spring Boot Applications](https://spring.io/guides/gs/testing-web/)

#### Frontend (React/TypeScript)
- [React Official Documentation](https://react.dev/)
- [TypeScript Handbook](https://www.typescriptlang.org/docs/)
- [Material-UI Documentation](https://mui.com/getting-started/installation/)
- [Testing Library Documentation](https://testing-library.com/docs/)

#### Tools and Frameworks
- [Docker Documentation](https://docs.docker.com/)
- [Maven Documentation](https://maven.apache.org/guides/)
- [Vite Documentation](https://vitejs.dev/guide/)
- [Vitest Documentation](https://vitest.dev/guide/)

### Internal Documentation

- **[Architecture Documentation](ARCHITECTURE.md)** - System design and architecture
- **[API Documentation](API.md)** - REST API reference
- **[Deployment Guide](DEPLOYMENT.md)** - Production deployment instructions
- **[Security Documentation](backend/SECURITY.md)** - Security implementation details
- **[Testing Guide](backend/TESTING.md)** - Testing strategy and guidelines

## 🤝 Contributing

### Getting Started

1. **Fork the repository**
2. **Clone your fork**:
   ```bash
   git clone https://github.com/yourusername/bookstore-spring-boot.git
   cd bookstore-spring-boot
   ```

3. **Set up development environment**:
   ```bash
   ./scripts/setup-dev-environment.sh
   ```

4. **Create a feature branch**:
   ```bash
   git checkout -b feature/your-feature-name
   ```

5. **Make your changes and commit**:
   ```bash
   git add .
   git commit -m "feat: add your feature description"
   ```

6. **Push to your fork and create a PR**:
   ```bash
   git push origin feature/your-feature-name
   ```

### Development Checklist

Before submitting a PR, ensure:

- [ ] Code follows project conventions and style guides
- [ ] All tests pass (unit, integration, and e2e)
- [ ] Code coverage meets minimum requirements (80%)
- [ ] Documentation is updated (API docs, README, etc.)
- [ ] No security vulnerabilities introduced
- [ ] Performance impact is acceptable
- [ ] Accessibility guidelines followed (frontend)
- [ ] Cross-browser compatibility tested (frontend)
- [ ] Database migrations are backward compatible
- [ ] Environment variables documented
- [ ] Logging is appropriate and helpful

### Code Review Process

1. **Self-review** your changes before submitting
2. **Request review** from appropriate team members
3. **Address feedback** promptly and professionally
4. **Update documentation** if changes affect public APIs
5. **Squash commits** if requested before merging

## 📞 Support and Help

### Getting Help

1. **Check existing documentation** in this repository
2. **Search for existing issues** in the project repository
3. **Create a new issue** with detailed information
4. **Join team discussions** in project communication channels

### Reporting Issues

When reporting issues, include:

- **Environment details** (OS, Java version, Node.js version)
- **Steps to reproduce** the issue
- **Expected behavior** vs actual behavior
- **Error messages** and stack traces
- **Relevant logs** and configuration

### Development Environment Issues

Common issues and solutions:

1. **Port conflicts**: Change ports in configuration files
2. **Database connection**: Verify MySQL is running and credentials are correct
3. **Build failures**: Clear Maven/npm cache and retry
4. **Test failures**: Check if services are running and databases are accessible

---

**Development Guide Version**: 1.0.0  
**Last Updated**: January 2025  
**Next Review**: March 2025