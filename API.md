# API Documentation

Complete REST API documentation for the Bookstore Spring Boot application. This API provides comprehensive book management functionality with pagination, search capabilities, and monitoring endpoints.

## 📖 API Overview

- **Base URL**: `http://localhost:8080/api`
- **API Version**: `v1`
- **Content Type**: `application/json`
- **Authentication**: HTTP Basic Auth (for admin endpoints)
- **Documentation**: OpenAPI 3.0 (Swagger UI available at `/swagger-ui.html`)

## 🔗 Base Endpoints

| Environment | Base URL | Swagger UI |
|-------------|----------|------------|
| **Development** | `http://localhost:8080/api` | `http://localhost:8080/swagger-ui.html` |
| **Production** | `https://yourdomain.com/api` | `https://yourdomain.com/swagger-ui.html` |

## 📚 Books API

### Data Model

#### Book Entity

```json
{
  "id": 1,
  "title": "The Great Gatsby",
  "author": "F. Scott Fitzgerald",
  "price": 12.99,
  "isbn": "978-0-7432-7356-5",
  "description": "A classic American novel set in the summer of 1922.",
  "category": "Fiction",
  "publisher": "Scribner",
  "publicationDate": "1925-04-10",
  "pages": 180,
  "stockQuantity": 25,
  "imageUrl": "https://example.com/images/great-gatsby.jpg",
  "createdAt": "2025-01-06T10:00:00Z",
  "updatedAt": "2025-01-06T10:00:00Z"
}
```

#### Book Create Request

```json
{
  "title": "The Great Gatsby",
  "author": "F. Scott Fitzgerald",
  "price": 12.99,
  "isbn": "978-0-7432-7356-5",
  "description": "A classic American novel set in the summer of 1922.",
  "category": "Fiction",
  "publisher": "Scribner",
  "publicationDate": "1925-04-10",
  "pages": 180,
  "stockQuantity": 25,
  "imageUrl": "https://example.com/images/great-gatsby.jpg"
}
```

#### Book Update Request

```json
{
  "title": "The Great Gatsby (Updated Edition)",
  "author": "F. Scott Fitzgerald",
  "price": 14.99,
  "isbn": "978-0-7432-7356-5",
  "description": "A classic American novel set in the summer of 1922. Updated edition with new introduction.",
  "category": "Fiction",
  "publisher": "Scribner",
  "publicationDate": "1925-04-10",
  "pages": 200,
  "stockQuantity": 30,
  "imageUrl": "https://example.com/images/great-gatsby-updated.jpg"
}
```

### Field Validation Rules

| Field | Type | Required | Validation Rules |
|-------|------|----------|------------------|
| `title` | String | ✅ | Max 255 characters, not blank |
| `author` | String | ✅ | Max 255 characters, not blank |
| `price` | Decimal | ✅ | Must be > 0, precision 10, scale 2 |
| `isbn` | String | ❌ | Max 20 characters, unique |
| `description` | String | ❌ | Text field, unlimited length |
| `category` | String | ❌ | Max 100 characters |
| `publisher` | String | ❌ | Max 255 characters |
| `publicationDate` | Date | ❌ | ISO 8601 format (YYYY-MM-DD) |
| `pages` | Integer | ❌ | Must be ≥ 1 |
| `stockQuantity` | Integer | ❌ | Must be ≥ 0, defaults to 0 |
| `imageUrl` | String | ❌ | Max 500 characters, valid URL format |

## 📋 API Endpoints

### 1. Get All Books

Retrieve a paginated list of all books with optional sorting.

```http
GET /api/v1/books
```

#### Query Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | Integer | `0` | Page number (0-based) |
| `size` | Integer | `10` | Number of items per page |
| `sort` | String | `title` | Sort field and direction (e.g., `title,asc` or `price,desc`) |

#### Example Request

```bash
curl -X GET "http://localhost:8080/api/v1/books?page=0&size=5&sort=title,asc" \
  -H "Accept: application/json"
```

#### Example Response

```json
{
  "content": [
    {
      "id": 1,
      "title": "The Great Gatsby",
      "author": "F. Scott Fitzgerald",
      "price": 12.99,
      "isbn": "978-0-7432-7356-5",
      "description": "A classic American novel.",
      "category": "Fiction",
      "publisher": "Scribner",
      "publicationDate": "1925-04-10",
      "pages": 180,
      "stockQuantity": 25,
      "imageUrl": "https://example.com/images/great-gatsby.jpg",
      "createdAt": "2025-01-06T10:00:00Z",
      "updatedAt": "2025-01-06T10:00:00Z"
    }
  ],
  "pageable": {
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    },
    "pageNumber": 0,
    "pageSize": 5,
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "totalElements": 50,
  "totalPages": 10,
  "last": false,
  "first": true,
  "number": 0,
  "size": 5,
  "numberOfElements": 5,
  "empty": false
}
```

#### Response Codes

- `200 OK` - Successfully retrieved books
- `400 Bad Request` - Invalid pagination parameters
- `500 Internal Server Error` - Server error

### 2. Get Book by ID

Retrieve a specific book by its unique identifier.

```http
GET /api/v1/books/{id}
```

#### Path Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | Long | Unique book identifier |

#### Example Request

```bash
curl -X GET "http://localhost:8080/api/v1/books/1" \
  -H "Accept: application/json"
```

#### Example Response

```json
{
  "id": 1,
  "title": "The Great Gatsby",
  "author": "F. Scott Fitzgerald",
  "price": 12.99,
  "isbn": "978-0-7432-7356-5",
  "description": "A classic American novel set in the summer of 1922.",
  "category": "Fiction",
  "publisher": "Scribner",
  "publicationDate": "1925-04-10",
  "pages": 180,
  "stockQuantity": 25,
  "imageUrl": "https://example.com/images/great-gatsby.jpg",
  "createdAt": "2025-01-06T10:00:00Z",
  "updatedAt": "2025-01-06T10:00:00Z"
}
```

#### Response Codes

- `200 OK` - Book found and returned
- `404 Not Found` - Book with specified ID not found
- `400 Bad Request` - Invalid ID format
- `500 Internal Server Error` - Server error

### 3. Create New Book

Create a new book with the provided information.

```http
POST /api/v1/books
```

#### Request Body

```json
{
  "title": "1984",
  "author": "George Orwell",
  "price": 13.99,
  "isbn": "978-0-452-28423-4",
  "description": "A dystopian social science fiction novel.",
  "category": "Science Fiction",
  "publisher": "Penguin Books",
  "publicationDate": "1949-06-08",
  "pages": 328,
  "stockQuantity": 15,
  "imageUrl": "https://example.com/images/1984.jpg"
}
```

#### Example Request

```bash
curl -X POST "http://localhost:8080/api/v1/books" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "title": "1984",
    "author": "George Orwell",
    "price": 13.99,
    "isbn": "978-0-452-28423-4",
    "description": "A dystopian social science fiction novel.",
    "category": "Science Fiction",
    "publisher": "Penguin Books",
    "publicationDate": "1949-06-08",
    "pages": 328,
    "stockQuantity": 15,
    "imageUrl": "https://example.com/images/1984.jpg"
  }'
```

#### Example Response

```json
{
  "id": 2,
  "title": "1984",
  "author": "George Orwell",
  "price": 13.99,
  "isbn": "978-0-452-28423-4",
  "description": "A dystopian social science fiction novel.",
  "category": "Science Fiction",
  "publisher": "Penguin Books",
  "publicationDate": "1949-06-08",
  "pages": 328,
  "stockQuantity": 15,
  "imageUrl": "https://example.com/images/1984.jpg",
  "createdAt": "2025-01-06T11:00:00Z",
  "updatedAt": "2025-01-06T11:00:00Z"
}
```

#### Response Codes

- `201 Created` - Book successfully created
- `400 Bad Request` - Invalid input data or validation errors
- `409 Conflict` - Book with same ISBN already exists
- `500 Internal Server Error` - Server error

### 4. Update Existing Book

Update an existing book with new information.

```http
PUT /api/v1/books/{id}
```

#### Path Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | Long | Unique book identifier |

#### Request Body

```json
{
  "title": "1984 (Updated Edition)",
  "author": "George Orwell",
  "price": 15.99,
  "isbn": "978-0-452-28423-4",
  "description": "A dystopian social science fiction novel. Updated with new foreword.",
  "category": "Science Fiction",
  "publisher": "Penguin Classics",
  "publicationDate": "1949-06-08",
  "pages": 350,
  "stockQuantity": 20,
  "imageUrl": "https://example.com/images/1984-updated.jpg"
}
```

#### Example Request

```bash
curl -X PUT "http://localhost:8080/api/v1/books/2" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "title": "1984 (Updated Edition)",
    "author": "George Orwell",
    "price": 15.99,
    "isbn": "978-0-452-28423-4",
    "description": "A dystopian social science fiction novel. Updated with new foreword.",
    "category": "Science Fiction",
    "publisher": "Penguin Classics",
    "publicationDate": "1949-06-08",
    "pages": 350,
    "stockQuantity": 20,
    "imageUrl": "https://example.com/images/1984-updated.jpg"
  }'
```

#### Example Response

```json
{
  "id": 2,
  "title": "1984 (Updated Edition)",
  "author": "George Orwell",
  "price": 15.99,
  "isbn": "978-0-452-28423-4",
  "description": "A dystopian social science fiction novel. Updated with new foreword.",
  "category": "Science Fiction",
  "publisher": "Penguin Classics",
  "publicationDate": "1949-06-08",
  "pages": 350,
  "stockQuantity": 20,
  "imageUrl": "https://example.com/images/1984-updated.jpg",
  "createdAt": "2025-01-06T11:00:00Z",
  "updatedAt": "2025-01-06T12:00:00Z"
}
```

#### Response Codes

- `200 OK` - Book successfully updated
- `404 Not Found` - Book with specified ID not found
- `400 Bad Request` - Invalid input data or validation errors
- `409 Conflict` - ISBN conflict with another book
- `500 Internal Server Error` - Server error

### 5. Delete Book

Delete a book by its unique identifier.

```http
DELETE /api/v1/books/{id}
```

#### Path Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | Long | Unique book identifier |

#### Example Request

```bash
curl -X DELETE "http://localhost:8080/api/v1/books/2" \
  -H "Accept: application/json"
```

#### Example Response

```
No content body (204 status code)
```

#### Response Codes

- `204 No Content` - Book successfully deleted
- `404 Not Found` - Book with specified ID not found
- `400 Bad Request` - Invalid ID format
- `500 Internal Server Error` - Server error

### 6. Search Books

Search books by title, author, or category with optional pagination.

```http
GET /api/v1/books/search
```

#### Query Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `query` | String | ✅ | Search term for title, author, or category |
| `page` | Integer | ❌ | Page number (0-based), default: 0 |
| `size` | Integer | ❌ | Number of items per page, default: 10 |
| `sort` | String | ❌ | Sort field and direction, default: title,asc |

#### Example Request

```bash
curl -X GET "http://localhost:8080/api/v1/books/search?query=gatsby&page=0&size=5" \
  -H "Accept: application/json"
```

#### Example Response

```json
{
  "content": [
    {
      "id": 1,
      "title": "The Great Gatsby",
      "author": "F. Scott Fitzgerald",
      "price": 12.99,
      "isbn": "978-0-7432-7356-5",
      "description": "A classic American novel.",
      "category": "Fiction",
      "publisher": "Scribner",
      "publicationDate": "1925-04-10",
      "pages": 180,
      "stockQuantity": 25,
      "imageUrl": "https://example.com/images/great-gatsby.jpg",
      "createdAt": "2025-01-06T10:00:00Z",
      "updatedAt": "2025-01-06T10:00:00Z"
    }
  ],
  "pageable": {
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    },
    "pageNumber": 0,
    "pageSize": 5,
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "totalElements": 1,
  "totalPages": 1,
  "last": true,
  "first": true,
  "number": 0,
  "size": 5,
  "numberOfElements": 1,
  "empty": false
}
```

#### Response Codes

- `200 OK` - Search completed successfully
- `400 Bad Request` - Missing or invalid query parameter
- `500 Internal Server Error` - Server error

## 🔧 Monitoring & Health Endpoints

### 1. Application Health

Check the overall health status of the application.

```http
GET /actuator/health
```

#### Example Request

```bash
curl -X GET "http://localhost:8080/actuator/health" \
  -H "Accept: application/json"
```

#### Example Response

```json
{
  "status": "UP",
  "components": {
    "bookService": {
      "status": "UP",
      "details": {
        "totalBooks": 50,
        "serviceStatus": "Available"
      }
    },
    "db": {
      "status": "UP",
      "details": {
        "database": "MySQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 499963174912,
        "free": 91943268352,
        "threshold": 104857600,
        "exists": true
      }
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

### 2. Application Information

Get application build and runtime information.

```http
GET /actuator/info
```

#### Example Request

```bash
curl -X GET "http://localhost:8080/actuator/info" \
  -H "Accept: application/json"
```

#### Example Response

```json
{
  "app": {
    "name": "Bookstore Backend",
    "description": "Spring Boot REST API for bookstore management",
    "version": "1.0.0",
    "features": [
      "Book CRUD operations",
      "Search and pagination",
      "Performance monitoring",
      "Health checks",
      "Security features"
    ]
  },
  "build": {
    "version": "1.0.0",
    "artifact": "bookstore-backend",
    "group": "com.example",
    "name": "bookstore-backend",
    "time": "2025-01-06T10:00:00.000Z"
  },
  "java": {
    "version": "17.0.2",
    "vendor": {
      "name": "Amazon.com Inc.",
      "version": "Corretto-17.0.2.8.1"
    }
  }
}
```

### 3. Application Metrics

Get application performance metrics (requires admin authentication).

```http
GET /actuator/metrics
```

#### Authentication

Use HTTP Basic Auth with admin credentials:
- Username: `admin`
- Password: `admin123`

#### Example Request

```bash
curl -X GET "http://localhost:8080/actuator/metrics" \
  -H "Accept: application/json" \
  -u admin:admin123
```

#### Example Response

```json
{
  "names": [
    "bookstore.books.created",
    "bookstore.books.updated",
    "bookstore.books.deleted",
    "bookstore.books.viewed",
    "bookstore.books.searched",
    "bookstore.books.create.duration",
    "bookstore.books.update.duration",
    "bookstore.books.delete.duration",
    "bookstore.books.search.duration",
    "bookstore.books.fetch.duration",
    "bookstore.books.active.operations",
    "bookstore.books.total.count",
    "http.server.requests",
    "jvm.memory.used",
    "jvm.memory.max",
    "jvm.gc.pause",
    "system.cpu.usage",
    "hikaricp.connections.active"
  ]
}
```

### 4. Specific Metric

Get details for a specific metric.

```http
GET /actuator/metrics/{metricName}
```

#### Example Request

```bash
curl -X GET "http://localhost:8080/actuator/metrics/bookstore.books.total.count" \
  -H "Accept: application/json" \
  -u admin:admin123
```

#### Example Response

```json
{
  "name": "bookstore.books.total.count",
  "description": "Total number of books in the system",
  "baseUnit": null,
  "measurements": [
    {
      "statistic": "VALUE",
      "value": 50.0
    }
  ],
  "availableTags": []
}
```

### 5. Prometheus Metrics

Get metrics in Prometheus format (requires admin authentication).

```http
GET /actuator/prometheus
```

#### Example Request

```bash
curl -X GET "http://localhost:8080/actuator/prometheus" \
  -H "Accept: text/plain" \
  -u admin:admin123
```

#### Example Response

```
# HELP bookstore_books_created_total Total number of books created
# TYPE bookstore_books_created_total counter
bookstore_books_created_total 10.0
# HELP bookstore_books_total_count Total number of books in the system
# TYPE bookstore_books_total_count gauge
bookstore_books_total_count 50.0
# HELP jvm_memory_used_bytes The amount of used memory
# TYPE jvm_memory_used_bytes gauge
jvm_memory_used_bytes{area="heap",id="PS Eden Space",} 1.73408256E8
```

### 6. Custom Book Statistics

Get custom book statistics and metrics.

```http
GET /actuator/bookstats
```

#### Example Request

```bash
curl -X GET "http://localhost:8080/actuator/bookstats" \
  -H "Accept: application/json"
```

#### Example Response

```json
{
  "totalBooks": 50,
  "booksInStock": 45,
  "booksOutOfStock": 5,
  "stockPercentage": 90.0,
  "outOfStockPercentage": 10.0,
  "status": "HEALTHY"
}
```

## 🔐 Authentication

### Admin Endpoints

The following endpoints require HTTP Basic Authentication:

- `/actuator/metrics`
- `/actuator/prometheus`
- `/actuator/env`
- `/actuator/loggers`
- `/actuator/configprops`
- `/actuator/beans`
- `/actuator/conditions`

#### Credentials

| Role | Username | Password |
|------|----------|----------|
| Admin | `admin` | `admin123` |
| Monitor | `monitor` | `monitor123` |

#### Example Authentication

```bash
curl -X GET "http://localhost:8080/actuator/metrics" \
  -H "Accept: application/json" \
  -u admin:admin123
```

### Public Endpoints

The following endpoints are publicly accessible:

- `/api/v1/books/**` (all book operations)
- `/actuator/health`
- `/actuator/info`
- `/actuator/bookstats`

## 🚨 Error Handling

### Error Response Format

All API errors return a consistent error response format:

```json
{
  "timestamp": "2025-01-06T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for object='bookCreateRequest'. Error count: 1",
  "details": [
    {
      "field": "title",
      "rejectedValue": "",
      "message": "Title is required"
    }
  ],
  "path": "/api/v1/books"
}
```

### Common Error Codes

| Status Code | Description | Common Causes |
|-------------|-------------|---------------|
| `400 Bad Request` | Invalid request data | Validation errors, missing required fields, invalid format |
| `401 Unauthorized` | Authentication required | Missing or invalid credentials |
| `403 Forbidden` | Access denied | Insufficient permissions |
| `404 Not Found` | Resource not found | Invalid book ID, endpoint not found |
| `409 Conflict` | Resource conflict | Duplicate ISBN, concurrent modification |
| `422 Unprocessable Entity` | Validation error | Business rule violations |
| `500 Internal Server Error` | Server error | Database errors, unexpected exceptions |

### Validation Error Examples

#### Missing Required Field

```json
{
  "timestamp": "2025-01-06T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "details": [
    {
      "field": "title",
      "rejectedValue": null,
      "message": "Title is required"
    }
  ],
  "path": "/api/v1/books"
}
```

#### Invalid Price

```json
{
  "timestamp": "2025-01-06T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "details": [
    {
      "field": "price",
      "rejectedValue": -5.99,
      "message": "Price must be greater than 0"
    }
  ],
  "path": "/api/v1/books"
}
```

#### Duplicate ISBN

```json
{
  "timestamp": "2025-01-06T12:00:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "Book with ISBN '978-0-7432-7356-5' already exists",
  "path": "/api/v1/books"
}
```

## 📋 Rate Limiting

Currently, no rate limiting is implemented, but the following headers are prepared for future implementation:

- `X-RateLimit-Limit`: Maximum number of requests per time window
- `X-RateLimit-Remaining`: Number of requests remaining
- `X-RateLimit-Reset`: Time when the rate limit resets

## 🔗 CORS Configuration

### Allowed Origins

- **Development**: `http://localhost:3000`, `http://localhost:3001`
- **Production**: Configured via `CORS_ALLOWED_ORIGINS` environment variable

### Allowed Methods

- `GET`, `POST`, `PUT`, `DELETE`, `OPTIONS`

### Allowed Headers

- `Origin`, `Content-Type`, `Accept`, `Authorization`, `X-Requested-With`, `Cache-Control`

### Exposed Headers

- `X-Total-Count`, `X-Page-Count`

## 📊 Pagination

### Pagination Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | Integer | `0` | Page number (0-based) |
| `size` | Integer | `10` | Items per page (1-100) |
| `sort` | String | `title,asc` | Sort field and direction |

### Pagination Response

```json
{
  "content": [...],
  "pageable": {
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    },
    "pageNumber": 0,
    "pageSize": 10,
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "totalElements": 50,
  "totalPages": 5,
  "last": false,
  "first": true,
  "number": 0,
  "size": 10,
  "numberOfElements": 10,
  "empty": false
}
```

### Sorting Options

- **Single field**: `?sort=title,asc`
- **Multiple fields**: `?sort=category,asc&sort=title,desc`
- **Available fields**: `title`, `author`, `price`, `category`, `createdAt`, `updatedAt`

## 🧪 Testing the API

### Using curl

```bash
# Get all books
curl -X GET "http://localhost:8080/api/v1/books"

# Get specific book
curl -X GET "http://localhost:8080/api/v1/books/1"

# Create new book
curl -X POST "http://localhost:8080/api/v1/books" \
  -H "Content-Type: application/json" \
  -d '{"title":"Test Book","author":"Test Author","price":9.99}'

# Update book
curl -X PUT "http://localhost:8080/api/v1/books/1" \
  -H "Content-Type: application/json" \
  -d '{"title":"Updated Book","author":"Test Author","price":12.99}'

# Delete book
curl -X DELETE "http://localhost:8080/api/v1/books/1"

# Search books
curl -X GET "http://localhost:8080/api/v1/books/search?query=test"
```

### Using Postman

1. Import the OpenAPI specification from `/swagger-ui.html`
2. Set up environment variables for base URL
3. Configure authentication for admin endpoints
4. Test all endpoints with various scenarios

### Swagger UI

Access the interactive API documentation at:
- **Development**: `http://localhost:8080/swagger-ui.html`
- **Production**: `https://yourdomain.com/swagger-ui.html`

## 📈 Performance Considerations

1. **Pagination**: Always use pagination for large datasets
2. **Filtering**: Use search endpoints for large datasets
3. **Caching**: Response caching headers are included
4. **Connection Pooling**: Database connections are pooled
5. **Indexing**: Database indexes optimize query performance

## 🔒 Security Best Practices

1. **Input Validation**: All inputs are validated server-side
2. **SQL Injection Prevention**: Parameterized queries used
3. **XSS Protection**: Input sanitization implemented
4. **CORS**: Restrictive CORS policy configured
5. **Security Headers**: Comprehensive security headers included

## 📞 Support

For API questions or issues:
- Review the Swagger UI documentation
- Check the troubleshooting section in the main README
- Create an issue in the project repository
- Contact the development team

---

**API Version**: 1.0.0  
**Last Updated**: January 2025  
**OpenAPI Specification**: Available at `/v3/api-docs`