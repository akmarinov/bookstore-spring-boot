# Project Structure Documentation

This document provides a detailed overview of the Bookstore Spring Boot application structure and organization.

## Root Directory Structure

```
bookstore-spring-boot/
├── backend/                    # Spring Boot backend application
├── docker/                     # Docker configuration files
├── docker-compose.yml          # Docker services configuration
├── .env.example               # Environment variables template
├── .gitignore                 # Git ignore rules
├── README.md                  # Project documentation
└── PROJECT-STRUCTURE.md       # This file
```

## Backend Application Structure

```
backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── example/
│   │   │           └── bookstore/
│   │   │               ├── BookstoreApplication.java    # Main application class
│   │   │               ├── config/                      # Configuration classes
│   │   │               │   ├── DatabaseConfig.java
│   │   │               │   ├── WebConfig.java
│   │   │               │   └── SecurityConfig.java
│   │   │               ├── controller/                  # REST controllers
│   │   │               │   ├── BookController.java
│   │   │               │   ├── AuthorController.java
│   │   │               │   └── CategoryController.java
│   │   │               ├── dto/                         # Data Transfer Objects
│   │   │               │   ├── request/
│   │   │               │   │   ├── BookRequestDto.java
│   │   │               │   │   └── AuthorRequestDto.java
│   │   │               │   └── response/
│   │   │               │       ├── BookResponseDto.java
│   │   │               │       └── AuthorResponseDto.java
│   │   │               ├── entity/                      # JPA entities
│   │   │               │   ├── Book.java
│   │   │               │   ├── Author.java
│   │   │               │   ├── Category.java
│   │   │               │   └── BaseEntity.java
│   │   │               ├── exception/                   # Custom exceptions
│   │   │               │   ├── BookNotFoundException.java
│   │   │               │   ├── DuplicateBookException.java
│   │   │               │   └── GlobalExceptionHandler.java
│   │   │               ├── mapper/                      # Entity-DTO mappers
│   │   │               │   ├── BookMapper.java
│   │   │               │   └── AuthorMapper.java
│   │   │               ├── repository/                  # Data access layer
│   │   │               │   ├── BookRepository.java
│   │   │               │   ├── AuthorRepository.java
│   │   │               │   └── CategoryRepository.java
│   │   │               └── service/                     # Business logic layer
│   │   │                   ├── BookService.java
│   │   │                   ├── AuthorService.java
│   │   │                   ├── CategoryService.java
│   │   │                   └── impl/
│   │   │                       ├── BookServiceImpl.java
│   │   │                       ├── AuthorServiceImpl.java
│   │   │                       └── CategoryServiceImpl.java
│   │   └── resources/
│   │       ├── application.properties          # Main configuration
│   │       ├── application-development.properties  # Dev configuration
│   │       ├── application-production.properties   # Prod configuration
│   │       ├── logback-spring.xml             # Logging configuration
│   │       ├── db/
│   │       │   └── migration/                 # Flyway database migrations
│   │       │       ├── V1__Create_initial_schema.sql
│   │       │       ├── V2__Insert_sample_data.sql
│   │       │       └── V3__Add_indexes.sql
│   │       └── static/                        # Static resources
│   │           └── api-docs/                  # API documentation
│   └── test/
│       ├── java/
│       │   └── com/
│       │       └── example/
│       │           └── bookstore/
│       │               ├── BookstoreApplicationTests.java
│       │               ├── controller/        # Controller tests
│       │               │   ├── BookControllerTest.java
│       │               │   └── AuthorControllerTest.java
│       │               ├── repository/        # Repository tests
│       │               │   ├── BookRepositoryTest.java
│       │               │   └── AuthorRepositoryTest.java
│       │               ├── service/           # Service tests
│       │               │   ├── BookServiceTest.java
│       │               │   └── AuthorServiceTest.java
│       │               └── integration/       # Integration tests
│       │                   ├── BookIntegrationTest.java
│       │                   └── DatabaseIntegrationTest.java
│       └── resources/
│           ├── application-test.properties    # Test configuration
│           └── test-data/                     # Test data files
│               ├── books.json
│               └── authors.json
├── logs/                                      # Application logs
│   ├── archived/                             # Archived log files
│   └── .gitkeep                              # Keep directory in git
└── pom.xml                                   # Maven configuration
```

## Layer Architecture

### 1. Controller Layer (`controller/`)
- **Purpose**: Handles HTTP requests and responses
- **Responsibilities**:
  - Request validation
  - Response formatting
  - HTTP status code management
  - API documentation annotations
- **Naming Convention**: `*Controller.java`
- **Dependencies**: Services, DTOs

### 2. Service Layer (`service/`)
- **Purpose**: Contains business logic
- **Responsibilities**:
  - Business rule implementation
  - Transaction management
  - Orchestration between repositories
  - Data validation
- **Naming Convention**: `*Service.java` (interface), `*ServiceImpl.java` (implementation)
- **Dependencies**: Repositories, Mappers

### 3. Repository Layer (`repository/`)
- **Purpose**: Data access abstraction
- **Responsibilities**:
  - Database operations
  - Query methods
  - Custom query implementations
- **Naming Convention**: `*Repository.java`
- **Dependencies**: Entities

### 4. Entity Layer (`entity/`)
- **Purpose**: Database table representations
- **Responsibilities**:
  - JPA annotations
  - Relationships definition
  - Data model structure
- **Naming Convention**: `*.java` (no suffix)
- **Dependencies**: None (should be independent)

### 5. DTO Layer (`dto/`)
- **Purpose**: Data transfer between layers
- **Responsibilities**:
  - Request/Response data structure
  - Validation annotations
  - API contract definition
- **Structure**:
  - `request/`: Input DTOs for API endpoints
  - `response/`: Output DTOs for API responses
- **Naming Convention**: `*RequestDto.java`, `*ResponseDto.java`

## Configuration Structure

### 1. Application Properties
- `application.properties`: Base configuration
- `application-{profile}.properties`: Profile-specific configurations
- `.env`: Environment variables (not committed to git)
- `.env.example`: Environment variables template

### 2. Java Configuration Classes (`config/`)
- `DatabaseConfig.java`: Database and JPA configuration
- `WebConfig.java`: Web MVC configuration
- `SecurityConfig.java`: Security configuration (if applicable)

### 3. Logging Configuration
- `logback-spring.xml`: Comprehensive logging setup
- Profile-specific logging levels
- File rotation and archival
- Separate log files for different concerns

## Database Structure

### Migration Files (`db/migration/`)
- `V1__Create_initial_schema.sql`: Initial database schema
- `V2__Insert_sample_data.sql`: Sample data for development
- `V3__Add_indexes.sql`: Performance indexes
- **Naming Convention**: `V{version}__{description}.sql`

### Entity Relationships
```
Author (1) -----> (N) Book
Category (1) ---> (N) Book
Book (N) -------> (N) Category (through join table)
```

## Testing Structure

### Test Categories
1. **Unit Tests**: Individual component testing
2. **Integration Tests**: Component interaction testing
3. **Controller Tests**: API endpoint testing
4. **Repository Tests**: Database operation testing

### Test Configuration
- `application-test.properties`: Test-specific configuration
- H2 in-memory database for testing
- Test data management with JSON files

## Docker Structure

### Docker Compose Services
```
docker/
├── mysql/
│   └── init/                  # Database initialization scripts
│       ├── 01-create-user.sql
│       └── 02-grant-permissions.sql
└── docker-compose.yml         # Service definitions
```

### Available Services
- **mysql**: MySQL 8.0 database
- **phpmyadmin**: Database administration interface

## Build and Deployment

### Maven Structure
- `pom.xml`: Project dependencies and build configuration
- `target/`: Build output directory (ignored in git)
- Maven profiles for different environments

### Build Artifacts
- JAR file for deployment
- Docker images (if containerized)
- Test reports and coverage

## Development Guidelines

### Package Naming
- Base package: `com.example.bookstore`
- Feature packages: `com.example.bookstore.{feature}`
- Cross-cutting concerns: `com.example.bookstore.{concern}`

### Class Naming Conventions
- Entities: Simple nouns (e.g., `Book`, `Author`)
- Services: Noun + Service (e.g., `BookService`)
- Controllers: Noun + Controller (e.g., `BookController`)
- Repositories: Noun + Repository (e.g., `BookRepository`)
- DTOs: Noun + RequestDto/ResponseDto

### Method Naming Conventions
- Controllers: HTTP verb style (`getBooks`, `createBook`)
- Services: Business operation style (`findBookById`, `saveBook`)
- Repositories: Data operation style (`findById`, `save`)

## Security Considerations

### Configuration Security
- Sensitive data in environment variables
- Production profiles with restricted permissions
- Secure database connections

### Code Security
- Input validation at controller level
- SQL injection prevention through JPA
- Error handling without information leakage

## Performance Considerations

### Database Optimization
- Proper indexing strategy
- Connection pooling configuration
- Query optimization

### Application Optimization
- Lazy loading for JPA entities
- Caching strategies
- Async processing where appropriate

## Monitoring and Observability

### Application Monitoring
- Spring Boot Actuator endpoints
- Health checks
- Metrics collection

### Logging Strategy
- Structured logging for production
- Different log levels for different environments
- Log rotation and retention policies

## Extension Points

### Adding New Features
1. Create entity in `entity/` package
2. Create repository in `repository/` package
3. Create service interface and implementation
4. Create DTOs for request/response
5. Create controller with endpoints
6. Add database migration
7. Add comprehensive tests

### Configuration Extensions
- Additional profiles for different environments
- Feature flags through configuration
- External configuration sources

This structure promotes:
- **Separation of Concerns**: Each layer has distinct responsibilities
- **Testability**: Easy to unit test individual components
- **Maintainability**: Clear organization and naming conventions
- **Scalability**: Easy to add new features and components
- **Industry Standards**: Follows Spring Boot and Java best practices