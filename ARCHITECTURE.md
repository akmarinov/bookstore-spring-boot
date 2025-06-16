# Architecture Documentation

This document provides a comprehensive overview of the Bookstore Spring Boot application architecture, including system design, component interactions, data flow, and technical decisions.

## 📐 System Overview

The Bookstore application follows a **modern 3-tier architecture** with clear separation of concerns, implementing **RESTful API design** and **reactive frontend patterns**. The system is designed for scalability, maintainability, and production deployment.

### High-Level Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │   Backend       │    │   Database      │
│   (React SPA)   │◄───│ (Spring Boot)   │◄───│   (MySQL)       │
│                 │    │   REST API      │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
    ┌────▼────┐             ┌────▼────┐             ┌────▼────┐
    │ nginx   │             │Actuator │             │Flyway   │
    │(Static) │             │(Monitor)│             │(Migration)│
    └─────────┘             └─────────┘             └─────────┘
```

## 🏗️ Component Architecture

### Frontend Architecture (React SPA)

```
┌─────────────────────────────────────────────────────────────┐
│                    React Application                         │
├─────────────────────────────────────────────────────────────┤
│  Pages Layer (Route Components)                             │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐           │
│  │ HomePage    │ │ AddBookPage │ │ EditBookPage│           │
│  └─────────────┘ └─────────────┘ └─────────────┘           │
├─────────────────────────────────────────────────────────────┤
│  Components Layer (Reusable UI)                            │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐           │
│  │ BookList    │ │ BookForm    │ │ BookItem    │           │
│  └─────────────┘ └─────────────┘ └─────────────┘           │
├─────────────────────────────────────────────────────────────┤
│  Hooks Layer (State Management)                            │
│  ┌─────────────┐ ┌─────────────┐                           │
│  │ useBooks    │ │ useBook     │                           │
│  └─────────────┘ └─────────────┘                           │
├─────────────────────────────────────────────────────────────┤
│  API Layer (HTTP Client)                                   │
│  ┌─────────────┐ ┌─────────────┐                           │
│  │ bookApi.ts  │ │ client.ts   │                           │
│  └─────────────┘ └─────────────┘                           │
├─────────────────────────────────────────────────────────────┤
│  Infrastructure Layer                                      │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐           │
│  │ React Router│ │ Material-UI │ │ Axios       │           │
│  └─────────────┘ └─────────────┘ └─────────────┘           │
└─────────────────────────────────────────────────────────────┘
```

**Key Design Patterns:**
- **Component Composition**: Reusable UI components with prop drilling
- **Custom Hooks**: Encapsulated state management and side effects
- **Separation of Concerns**: Clear separation between UI, state, and API layers
- **Unidirectional Data Flow**: Props down, events up pattern

### Backend Architecture (Spring Boot)

```
┌─────────────────────────────────────────────────────────────┐
│                    Spring Boot Application                   │
├─────────────────────────────────────────────────────────────┤
│  Presentation Layer (REST Controllers)                     │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐           │
│  │BookController│ │HealthCtrl   │ │ActuatorEndpts│          │
│  │@RestController│ │@RestController│ │@Endpoint    │          │
│  └─────────────┘ └─────────────┘ └─────────────┘           │
├─────────────────────────────────────────────────────────────┤
│  Application Layer (Business Logic)                        │
│  ┌─────────────┐                                           │
│  │ BookService │ ← Interface-based design                  │
│  │@Service     │                                           │
│  └─────────────┘                                           │
├─────────────────────────────────────────────────────────────┤
│  Data Layer (Repositories)                                 │
│  ┌─────────────┐                                           │
│  │BookRepository│ ← Spring Data JPA                        │
│  │@Repository  │                                           │
│  └─────────────┘                                           │
├─────────────────────────────────────────────────────────────┤
│  Domain Layer (Entities & DTOs)                           │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐           │
│  │ Book.java   │ │BookCreateReq│ │BookUpdateReq│           │
│  │@Entity      │ │@DTO         │ │@DTO         │           │
│  └─────────────┘ └─────────────┘ └─────────────┘           │
├─────────────────────────────────────────────────────────────┤
│  Infrastructure Layer                                      │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐           │
│  │ Security    │ │ Monitoring  │ │ Database    │           │
│  │ Config      │ │ Config      │ │ Config      │           │
│  └─────────────┘ └─────────────┘ └─────────────┘           │
└─────────────────────────────────────────────────────────────┘
```

**Key Design Patterns:**
- **Layered Architecture**: Clear separation of concerns across layers
- **Dependency Injection**: IoC container for dependency management
- **Repository Pattern**: Data access abstraction
- **DTO Pattern**: Separate request/response models from domain entities
- **Service Layer Pattern**: Business logic encapsulation

## 🔄 Data Flow Architecture

### Request Flow (Create Book Example)

```
Frontend Request Flow:
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   BookForm  │───►│  useBooks   │───►│  bookApi    │───►│ HTTP Client │
│  (User Input)│    │   (Hook)    │    │(API Layer) │    │   (Axios)   │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
                                                                  │
                                                                  ▼
Backend Request Flow:                                    ┌─────────────┐
┌─────────────┐    ┌─────────────┐    ┌─────────────┐   │ Spring Boot │
│BookController│───►│ BookService │───►│BookRepository│◄──│   Server    │
│(@PostMapping)│    │(@Transactional)  │(@Repository)│   └─────────────┘
└─────────────┘    └─────────────┘    └─────────────┘            │
       │                   │                   │                 ▼
       ▼                   ▼                   ▼        ┌─────────────┐
┌─────────────┐    ┌─────────────┐    ┌─────────────┐   │   MySQL     │
│ Validation  │    │ Business    │    │   Data      │   │  Database   │
│   Layer     │    │   Logic     │    │  Persistence│   └─────────────┘
└─────────────┘    └─────────────┘    └─────────────┘
```

### Data Flow Layers

1. **Presentation Layer**:
   - Handles HTTP requests/responses
   - Input validation and sanitization
   - Response formatting (JSON)
   - Error handling and status codes

2. **Application Layer**:
   - Business logic execution
   - Transaction management
   - Service orchestration
   - Metrics collection

3. **Data Layer**:
   - Entity persistence
   - Query execution
   - Database transaction management
   - Connection pooling

## 🗄️ Database Architecture

### Entity Relationship Diagram

```
┌─────────────────────────────────────────────────────────┐
│                      books                              │
├─────────────────────────────────────────────────────────┤
│ id               BIGINT AUTO_INCREMENT PRIMARY KEY     │
│ title            VARCHAR(255) NOT NULL                 │
│ author           VARCHAR(255) NOT NULL                 │
│ price            DECIMAL(10,2) NOT NULL                │
│ isbn             VARCHAR(20) UNIQUE                    │
│ description      TEXT                                  │
│ category         VARCHAR(100)                          │
│ publisher        VARCHAR(255)                          │
│ publication_date DATE                                  │
│ pages            INT                                   │
│ stock_quantity   INT DEFAULT 0                        │
│ image_url        VARCHAR(500)                          │
│ created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP  │
│ updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP  │
│                  ON UPDATE CURRENT_TIMESTAMP           │
└─────────────────────────────────────────────────────────┘

Indexes:
- PRIMARY KEY (id)
- UNIQUE KEY uk_books_isbn (isbn)
- KEY idx_books_title (title)
- KEY idx_books_author (author)
- KEY idx_books_category (category)
- KEY idx_books_created_at (created_at)
```

### Database Design Principles

1. **Normalization**: Single table design for book entity (KISS principle)
2. **Indexing Strategy**: Optimized for common query patterns
3. **Data Integrity**: Constraints and validations at database level
4. **Audit Trail**: Created/updated timestamps for all records
5. **Performance**: Proper indexing for search and pagination queries

### Migration Strategy (Flyway)

```
db/migration/
├── V1__Create_initial_schema.sql     # Initial table creation
├── V1__Create_books_table.sql        # Books table with constraints
└── [future migrations]               # Version-controlled schema changes
```

## 🔒 Security Architecture

### Security Layers

```
┌─────────────────────────────────────────────────────────────┐
│                     Security Stack                         │
├─────────────────────────────────────────────────────────────┤
│  Web Security (nginx/Spring Security)                      │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐           │
│  │    CORS     │ │    CSP      │ │   Headers   │           │
│  │  Protection │ │ Protection  │ │ Protection  │           │
│  └─────────────┘ └─────────────┘ └─────────────┘           │
├─────────────────────────────────────────────────────────────┤
│  Application Security (Spring Security)                    │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐           │
│  │ Input       │ │    XSS      │ │ SQL Injection│          │
│  │ Validation  │ │ Protection  │ │  Prevention │           │
│  └─────────────┘ └─────────────┘ └─────────────┘           │
├─────────────────────────────────────────────────────────────┤
│  Data Security (JPA/MySQL)                                 │
│  ┌─────────────┐ ┌─────────────┐                           │
│  │Parameterized│ │ Connection  │                           │
│  │  Queries    │ │ Encryption  │                           │
│  └─────────────┘ └─────────────┘                           │
└─────────────────────────────────────────────────────────────┘
```

### Security Implementation

1. **Frontend Security**:
   - Input validation with Yup schemas
   - XSS protection through React's built-in escaping
   - Content Security Policy headers
   - Secure HTTP-only cookies (when authentication is enabled)

2. **Backend Security**:
   - Spring Security configuration
   - Input sanitization with OWASP HTML Sanitizer
   - CORS configuration with specific origins
   - Security headers filter
   - SQL injection prevention through JPA parameterized queries

3. **Infrastructure Security**:
   - TLS/HTTPS ready configuration
   - Docker security best practices
   - Non-root user execution
   - Network isolation

## 📊 Monitoring Architecture

### Observability Stack

```
┌─────────────────────────────────────────────────────────────┐
│                   Monitoring Stack                          │
├─────────────────────────────────────────────────────────────┤
│  Metrics Collection (Micrometer)                           │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐           │
│  │ Application │ │    JVM      │ │   Custom    │           │
│  │   Metrics   │ │   Metrics   │ │   Metrics   │           │
│  └─────────────┘ └─────────────┘ └─────────────┘           │
├─────────────────────────────────────────────────────────────┤
│  Health Monitoring (Actuator)                              │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐           │
│  │   Health    │ │ Readiness   │ │  Liveness   │           │
│  │   Checks    │ │   Probes    │ │   Probes    │           │
│  └─────────────┘ └─────────────┘ └─────────────┘           │
├─────────────────────────────────────────────────────────────┤
│  Export Formats                                            │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐           │
│  │ Prometheus  │ │    JSON     │ │   Custom    │           │
│  │   Format    │ │   Format    │ │  Endpoints  │           │
│  └─────────────┘ └─────────────┘ └─────────────┘           │
└─────────────────────────────────────────────────────────────┘
```

### Custom Metrics

1. **Business Metrics**:
   - Book operation counters (create, update, delete, view, search)
   - Operation duration timers
   - Active operations gauge
   - Total books count gauge

2. **Technical Metrics**:
   - HTTP request metrics
   - Database connection pool metrics
   - JVM metrics (memory, GC, threads)
   - Custom application metrics

3. **Health Indicators**:
   - Database connectivity
   - Disk space availability
   - Custom book service health

## 🚀 Deployment Architecture

### Development Environment

```
┌─────────────────────────────────────────────────────────────┐
│                 Development Deployment                      │
├─────────────────────────────────────────────────────────────┤
│  Docker Compose Development Stack                          │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐           │
│  │   React     │ │ Spring Boot │ │    MySQL    │           │
│  │    :3000    │ │    :8080    │ │    :3306    │           │
│  └─────────────┘ └─────────────┘ └─────────────┘           │
│  ┌─────────────┐ ┌─────────────┐                           │
│  │ PhpMyAdmin  │ │   Volumes   │                           │
│  │    :8082    │ │   (Data)    │                           │
│  └─────────────┘ └─────────────┘                           │
└─────────────────────────────────────────────────────────────┘
```

### Production Environment

```
┌─────────────────────────────────────────────────────────────┐
│                 Production Deployment                       │
├─────────────────────────────────────────────────────────────┤
│  Load Balancer / Reverse Proxy                             │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │                   nginx                                 │ │
│  │  ┌─────────────┐         ┌─────────────┐               │ │
│  │  │   Static    │         │     API     │               │ │
│  │  │   Assets    │         │    Proxy    │               │ │
│  │  │    :80      │         │   /api/*    │               │ │
│  │  └─────────────┘         └─────────────┘               │ │
│  └─────────────────────────────────────────────────────────┘ │
├─────────────────────────────────────────────────────────────┤
│  Application Tier                                          │
│  ┌─────────────┐ ┌─────────────┐                           │
│  │ Spring Boot │ │ Spring Boot │ ← (Multiple instances)    │
│  │  Instance 1 │ │  Instance N │                           │
│  └─────────────┘ └─────────────┘                           │
├─────────────────────────────────────────────────────────────┤
│  Data Tier                                                 │
│  ┌─────────────┐ ┌─────────────┐                           │
│  │    MySQL    │ │   Backup    │                           │
│  │   Primary   │ │   Storage   │                           │
│  └─────────────┘ └─────────────┘                           │
└─────────────────────────────────────────────────────────────┘
```

## 🔧 Configuration Architecture

### Environment-based Configuration

```
Configuration Hierarchy:
application.properties          ← Base configuration
├── application-dev.properties  ← Development overrides
├── application-test.yml        ← Test environment
├── application-prod.yml        ← Production configuration
└── Environment Variables       ← Runtime configuration
```

### Configuration Areas

1. **Database Configuration**:
   - Connection pooling (HikariCP)
   - JPA/Hibernate settings
   - Flyway migration settings

2. **Security Configuration**:
   - CORS origins and headers
   - Security headers
   - Authentication settings

3. **Monitoring Configuration**:
   - Actuator endpoints
   - Metrics export settings
   - Health check settings

4. **Application Configuration**:
   - Server port and context
   - Logging levels
   - Feature flags

## 📈 Performance Architecture

### Performance Optimization Strategies

1. **Frontend Performance**:
   - Code splitting with React lazy loading
   - Bundle optimization with Vite
   - HTTP/2 support
   - Static asset caching

2. **Backend Performance**:
   - Connection pooling with HikariCP
   - JPA query optimization
   - Pagination for large datasets
   - Response compression

3. **Database Performance**:
   - Strategic indexing
   - Query optimization
   - Connection pooling
   - Read replica support (future)

4. **Caching Strategy**:
   - Browser caching for static assets
   - HTTP response caching
   - Application-level caching (future)

## 🔮 Scalability Considerations

### Horizontal Scaling

1. **Stateless Application Design**:
   - No server-side sessions
   - Externalized configuration
   - Database-backed state

2. **Load Balancing Ready**:
   - Health check endpoints
   - Graceful shutdown
   - Multiple instance support

3. **Database Scaling**:
   - Read replica support
   - Connection pooling
   - Query optimization

### Future Enhancements

1. **Microservices Architecture**:
   - Service decomposition
   - API Gateway
   - Service discovery

2. **Event-Driven Architecture**:
   - Message queues
   - Event sourcing
   - CQRS patterns

3. **Distributed Caching**:
   - Redis integration
   - Cache invalidation strategies
   - Distributed cache

## 🧪 Testing Architecture

### Testing Strategy Pyramid

```
┌─────────────────────────────────────────────────────────────┐
│                    Testing Pyramid                          │
├─────────────────────────────────────────────────────────────┤
│  End-to-End Tests (Few)                                    │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │            Browser-based E2E Tests                     │ │
│  └─────────────────────────────────────────────────────────┘ │
├─────────────────────────────────────────────────────────────┤
│  Integration Tests (More)                                  │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐           │
│  │ API Tests   │ │ Database    │ │ Container   │           │
│  │(@SpringBootTest)│ │ Tests      │ │ Tests      │           │
│  └─────────────┘ └─────────────┘ └─────────────┘           │
├─────────────────────────────────────────────────────────────┤
│  Unit Tests (Most)                                         │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐           │
│  │ Service     │ │ Component   │ │ Repository  │           │
│  │ Tests       │ │ Tests       │ │ Tests       │           │
│  └─────────────┘ └─────────────┘ └─────────────┘           │
└─────────────────────────────────────────────────────────────┘
```

### Test Architecture Principles

1. **Test Isolation**: Each test is independent and can run in any order
2. **Test Data Management**: Builder patterns and fixture factories
3. **Mock Strategies**: Appropriate mocking for external dependencies
4. **Test Coverage**: >80% line coverage requirement
5. **Performance Testing**: Load testing for critical paths

## 📋 Technology Decisions

### Backend Technology Choices

| Technology | Decision | Rationale |
|------------|----------|-----------|
| **Spring Boot 3** | ✅ Chosen | Modern framework, production-ready, extensive ecosystem |
| **Java 17** | ✅ Chosen | LTS version, performance improvements, modern language features |
| **MySQL 8** | ✅ Chosen | Proven reliability, excellent performance, wide support |
| **JPA/Hibernate** | ✅ Chosen | ORM abstraction, productivity, query optimization |
| **Maven** | ✅ Chosen | Industry standard, excellent dependency management |

### Frontend Technology Choices

| Technology | Decision | Rationale |
|------------|----------|-----------|
| **React 19** | ✅ Chosen | Modern features, excellent ecosystem, team expertise |
| **TypeScript** | ✅ Chosen | Type safety, better developer experience, maintainability |
| **Material-UI** | ✅ Chosen | Comprehensive component library, consistent design |
| **Vite** | ✅ Chosen | Fast build times, modern tooling, excellent DX |
| **Vitest** | ✅ Chosen | Fast testing, Vite integration, modern test runner |

### Infrastructure Technology Choices

| Technology | Decision | Rationale |
|------------|----------|-----------|
| **Docker** | ✅ Chosen | Containerization, environment consistency, deployment flexibility |
| **nginx** | ✅ Chosen | High performance, reverse proxy, static file serving |
| **Flyway** | ✅ Chosen | Database migration management, version control |
| **Actuator** | ✅ Chosen | Production monitoring, health checks, metrics |

## 🔄 Migration Strategy

### JSP to Spring Boot Migration

The migration from JSP-based architecture to modern Spring Boot + React stack involved:

1. **API-First Design**: REST API design before frontend development
2. **Incremental Migration**: Gradual replacement of JSP pages with React components
3. **Data Layer Modernization**: JPA entities replacing raw JDBC
4. **Security Enhancement**: Modern security practices implementation
5. **Testing Modernization**: Comprehensive test suite development
6. **DevOps Enhancement**: Containerization and CI/CD pipeline setup

### Migration Benefits Achieved

1. **Performance**: Significant improvement in page load times and API response times
2. **Maintainability**: Clear separation of concerns, modern code structure
3. **Scalability**: Horizontal scaling capabilities, stateless design
4. **Developer Experience**: Modern tooling, hot reloading, type safety
5. **Security**: Enhanced security posture with modern practices
6. **Monitoring**: Comprehensive observability and monitoring capabilities

This architecture provides a solid foundation for future enhancements and demonstrates modern software development best practices while maintaining production-ready quality and performance.