# Bookstore Spring Boot Application

A complete full-stack bookstore management system built with **Spring Boot 3** backend and **React 19 TypeScript** frontend. This application demonstrates modern web development practices with comprehensive testing, security, monitoring, and containerization.

## 📋 Project Overview

This project represents a complete migration from JSP-based architecture to a modern, microservices-ready application stack. The application provides full CRUD operations for book management with advanced features like search, filtering, pagination, and real-time monitoring.

### Migration Summary
- **From**: JSP-based monolithic application
- **To**: Spring Boot REST API + React SPA
- **Architecture**: Clean, layered architecture with separation of concerns
- **Status**: ✅ **Migration Complete** - Production ready

## 🏗️ Technology Stack

### Backend
- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Database**: MySQL 8.0 (Production), H2 (Testing)
- **Security**: Spring Security with configurable authentication
- **Documentation**: OpenAPI 3 (Swagger)
- **Testing**: JUnit 5, TestContainers, Mockito
- **Monitoring**: Spring Boot Actuator, Micrometer, Prometheus
- **Build Tool**: Maven 3.9+

### Frontend
- **Framework**: React 19.1.0
- **Language**: TypeScript 5.8+
- **UI Library**: Material-UI v5
- **Routing**: React Router DOM v7
- **State Management**: React Hooks + Context
- **Forms**: React Hook Form + Yup validation
- **HTTP Client**: Axios
- **Testing**: Vitest, Testing Library, MSW
- **Bundle Tool**: Vite 6

### Infrastructure
- **Containerization**: Docker & Docker Compose
- **Database Migration**: Flyway
- **Proxy**: nginx (Production)
- **Monitoring**: Prometheus metrics, Health checks
- **Development**: Hot reloading, DevTools

## 🚀 Quick Start

### Prerequisites
- **Java 17** or higher
- **Node.js 18** or higher
- **Docker & Docker Compose** (optional)
- **Maven 3.9+** (optional if using Docker)

### Option 1: Docker Development Environment (Recommended)

```bash
# Clone the repository
git clone [repository-url]
cd bookstore-spring-boot

# Start all services with Docker
./scripts/docker/deploy-dev.sh start

# Access the application
# Frontend: http://localhost:3000
# Backend API: http://localhost:8080/api
# Swagger UI: http://localhost:8080/swagger-ui.html
# Database Admin: http://localhost:8082 (PhpMyAdmin)
```

### Option 2: Manual Development Setup

1. **Start Backend**:
   ```bash
   cd backend
   ./mvnw spring-boot:run
   # Backend runs on http://localhost:8080
   ```

2. **Start Frontend**:
   ```bash
   cd frontend
   npm install
   npm run dev
   # Frontend runs on http://localhost:5173
   ```

3. **Database Setup**:
   ```bash
   # Using Docker for MySQL
   docker-compose up mysql -d
   ```

## 📊 Application Features

### Core Functionality
- ✅ **Book Management**: Create, read, update, delete books
- ✅ **Advanced Search**: Search by title, author, category, ISBN
- ✅ **Filtering & Sorting**: Multi-criteria filtering with sorting
- ✅ **Pagination**: Efficient handling of large datasets
- ✅ **Inventory Tracking**: Stock quantity management
- ✅ **Data Validation**: Comprehensive client and server-side validation

### Technical Features
- ✅ **Responsive Design**: Mobile-first responsive UI
- ✅ **Error Handling**: Comprehensive error handling and user feedback
- ✅ **Security Headers**: CORS, CSP, XSS protection
- ✅ **API Documentation**: Auto-generated OpenAPI documentation
- ✅ **Health Monitoring**: Application health checks and metrics
- ✅ **Performance Optimization**: Connection pooling, caching, lazy loading

## 🔌 API Endpoints

### Books API (`/api/v1/books`)

| Method | Endpoint | Description | Parameters |
|--------|----------|-------------|------------|
| `GET` | `/api/v1/books` | Get all books (paginated) | `page`, `size`, `sort` |
| `GET` | `/api/v1/books/{id}` | Get book by ID | `id` (path) |
| `POST` | `/api/v1/books` | Create new book | Book data (body) |
| `PUT` | `/api/v1/books/{id}` | Update existing book | `id` (path), Book data (body) |
| `DELETE` | `/api/v1/books/{id}` | Delete book | `id` (path) |
| `GET` | `/api/v1/books/search` | Search books | `query`, `page`, `size` |

### Monitoring Endpoints (`/actuator`)

| Endpoint | Description | Access |
|----------|-------------|---------|
| `/actuator/health` | Application health status | Public |
| `/actuator/info` | Application information | Public |
| `/actuator/metrics` | Application metrics | Admin |
| `/actuator/prometheus` | Prometheus metrics | Admin |
| `/actuator/bookstats` | Custom book statistics | Public |

**Authentication**: Admin endpoints use HTTP Basic Auth
- Admin: `admin/admin123`
- Monitor: `monitor/monitor123`

## 📁 Project Structure

```
bookstore-spring-boot/
├── backend/                    # Spring Boot REST API
│   ├── src/main/java/com/example/bookstore/
│   │   ├── controller/         # REST controllers
│   │   ├── service/           # Business logic layer
│   │   ├── repository/        # Data access layer
│   │   ├── model/            # JPA entities
│   │   ├── dto/              # Data transfer objects
│   │   ├── config/           # Configuration classes
│   │   ├── exception/        # Custom exceptions
│   │   ├── metrics/          # Custom metrics
│   │   └── filter/           # Security filters
│   ├── src/main/resources/
│   │   ├── db/migration/     # Flyway database migrations
│   │   └── application.properties
│   └── src/test/             # Comprehensive test suite
├── frontend/                   # React TypeScript SPA
│   ├── src/
│   │   ├── components/       # Reusable UI components
│   │   ├── pages/           # Page components
│   │   ├── hooks/           # Custom React hooks
│   │   ├── api/             # API client functions
│   │   ├── types/           # TypeScript type definitions
│   │   └── tests/           # Frontend tests
│   ├── public/              # Static assets
│   └── package.json
├── docker/                     # Docker configuration
├── scripts/                    # Build and deployment scripts
├── docs/                      # Documentation (this README)
├── docker-compose.yml         # Development environment
├── docker-compose.prod.yml    # Production environment
└── .env.example              # Environment template
```

## 🔧 Development

### Backend Development

```bash
cd backend

# Run with development profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Run tests
./mvnw test

# Run with coverage
./mvnw test jacoco:report

# Package for production
./mvnw clean package -Pprod
```

### Frontend Development

```bash
cd frontend

# Install dependencies
npm install

# Start development server
npm run dev

# Run tests
npm run test

# Run tests with coverage
npm run test:coverage

# Build for production
npm run build:prod

# Type checking
npm run type-check
```

### Database Management

```bash
# Start MySQL with Docker
docker-compose up mysql -d

# Run Flyway migrations
./mvnw flyway:migrate

# Access database admin
# PhpMyAdmin: http://localhost:8082
```

## 🚀 Production Deployment

### Docker Production Deployment

```bash
# Build production images
./scripts/docker/build-prod.sh --security-scan

# Deploy to production
./scripts/docker/deploy-prod.sh deploy

# Access production application
# Frontend: http://your-domain.com
# Backend: http://your-domain.com/api
```

### Manual Production Deployment

```bash
# Build backend
cd backend
./mvnw clean package -Pprod

# Build frontend
cd frontend
npm run build:prod

# Configure nginx to serve frontend and proxy API
# Deploy JAR file to application server
# Configure MySQL database
# Set environment variables
```

### Environment Configuration

Create `.env.prod` for production:
```bash
# Database
MYSQL_ROOT_PASSWORD=secure_root_password
MYSQL_DATABASE=booksdb
MYSQL_USER=bookstore
MYSQL_PASSWORD=secure_password

# Application
BACKEND_PORT=8080
FRONTEND_PORT=80

# Security
JWT_SECRET=your_32_character_jwt_secret_key
CORS_ALLOWED_ORIGINS=https://yourdomain.com
```

## 🧪 Testing

The application includes comprehensive testing at all levels:

### Backend Testing
- **Unit Tests**: Service and repository layer testing
- **Integration Tests**: Full application context testing
- **Controller Tests**: REST API endpoint testing
- **Performance Tests**: Load and performance validation
- **TestContainers**: Real database integration testing

```bash
# Run all tests
./mvnw test

# Run specific test categories
./mvnw test -Dtest=BookServiceTest
./mvnw test -Dtest=BookControllerTest
./mvnw test -Dtest=BookRepositoryTest
```

### Frontend Testing
- **Unit Tests**: Component and utility testing
- **Integration Tests**: User interaction testing
- **API Tests**: Mock service worker testing
- **E2E Tests**: End-to-end workflow testing (planned)

```bash
# Run all tests
npm run test

# Run with coverage
npm run test:coverage

# Run tests in UI mode
npm run test:ui
```

### Test Coverage Goals
- **Backend**: >80% line coverage (enforced by Jacoco)
- **Frontend**: >80% line coverage
- **Critical Paths**: 100% coverage

## 📊 Monitoring and Observability

### Application Metrics
- **Book Operations**: Create, update, delete, view, search counters
- **Performance Metrics**: Operation duration timers
- **System Metrics**: Memory, CPU, database connections
- **HTTP Metrics**: Request counts, response times, error rates

### Health Checks
- **Application Health**: `/actuator/health`
- **Database Connectivity**: Automatic database health checks
- **Custom Health Indicators**: Book service availability

### Logging
- **Structured Logging**: JSON format for production
- **Log Levels**: Configurable per environment
- **Log Rotation**: Automatic log file management
- **Error Tracking**: Comprehensive error logging

## 🔒 Security

### Implemented Security Features
1. **Input Validation**: Server-side validation with Bean Validation
2. **XSS Protection**: Input sanitization and security headers
3. **CORS Configuration**: Restrictive cross-origin policy
4. **SQL Injection Prevention**: Parameterized queries with JPA
5. **Security Headers**: CSP, X-Frame-Options, HSTS
6. **Authentication Ready**: Configurable Spring Security setup

### Security Configuration
- **Development**: Permissive CORS for local development
- **Production**: Restrictive security policies
- **Headers**: Comprehensive security headers
- **Encryption**: Ready for HTTPS deployment

## 🚨 Troubleshooting

### Common Issues

**Port Conflicts**:
```bash
# Check port usage
netstat -tuln | grep :8080

# Change ports in docker-compose.yml or .env
```

**Database Connection Issues**:
```bash
# Check MySQL status
docker-compose logs mysql

# Verify database connectivity
docker exec -it bookstore-mysql-dev mysql -u bookstore -p
```

**Build Failures**:
```bash
# Clean Maven cache
./mvnw clean

# Clean Docker cache
docker system prune -a

# Rebuild without cache
docker-compose build --no-cache
```

**Frontend Issues**:
```bash
# Clear npm cache
npm run clean

# Reinstall dependencies
rm -rf node_modules package-lock.json
npm install
```

### Performance Issues
- Check application metrics: `/actuator/metrics`
- Monitor resource usage: `docker stats`
- Review logs: `docker-compose logs -f`
- Database performance: Check slow query logs

### Debug Mode
```bash
# Backend debug mode
./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"

# Frontend debug mode
npm run dev

# Docker debug logs
docker-compose logs -f [service-name]
```

## 📚 Documentation

- **[Architecture Documentation](ARCHITECTURE.md)** - System design and component overview
- **[API Documentation](API.md)** - Complete REST API reference
- **[Development Guide](DEVELOPMENT.md)** - Detailed development setup and guidelines
- **[Deployment Guide](DEPLOYMENT.md)** - Production deployment instructions
- **[Security Documentation](backend/SECURITY.md)** - Security implementation details
- **[Testing Guide](backend/TESTING.md)** - Testing strategy and guidelines
- **[Docker Guide](DOCKER.md)** - Containerization and Docker usage

## 🤝 Contributing

1. Fork the project
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Standards
- Follow existing code style and conventions
- Write comprehensive tests for new features
- Update documentation for API changes
- Ensure security best practices
- Maintain >80% test coverage

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🙏 Acknowledgments

- **Spring Boot** team for the excellent framework
- **React** team for the powerful frontend library
- **Material-UI** for the beautiful components
- **TestContainers** for integration testing capabilities
- **Docker** for containerization platform

## 📞 Support

For questions, issues, or contributions:
- Create an issue in the project repository
- Review existing documentation
- Check troubleshooting section
- Contact project maintainers

---

**Project Status**: ✅ Production Ready  
**Last Updated**: January 2025  
**Version**: 1.0.0