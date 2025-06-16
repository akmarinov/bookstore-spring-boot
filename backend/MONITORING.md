# Bookstore Backend - Monitoring and Actuator Configuration

This document describes the comprehensive monitoring and metrics collection capabilities implemented in the Bookstore Backend application.

## Overview

The application includes:
- Spring Boot Actuator for application monitoring
- Built-in health indicators
- Comprehensive metrics collection with Micrometer
- Prometheus integration
- Security for actuator endpoints
- Custom metrics for book operations
- HTTP request monitoring

## Actuator Endpoints

### Available Endpoints

| Endpoint | Description | Access Level |
|----------|-------------|--------------|
| `/actuator/health` | Application health status | Public |
| `/actuator/info` | Application information | Public |
| `/actuator/metrics` | Application metrics | Admin |
| `/actuator/prometheus` | Prometheus metrics | Admin |
| `/actuator/bookstats` | Custom book statistics | Public |
| `/actuator/env` | Environment properties | Admin |
| `/actuator/loggers` | Logger configuration | Admin |
| `/actuator/httptrace` | HTTP request traces | Admin |
| `/actuator/mappings` | Request mappings | Admin |
| `/actuator/configprops` | Configuration properties | Admin |
| `/actuator/beans` | Spring beans | Admin |
| `/actuator/conditions` | Auto-configuration conditions | Admin |

### Security

Actuator endpoints are secured with HTTP Basic authentication:
- **Admin User**: `admin` / `admin123` (full access to all endpoints)
- **Monitor User**: `monitor` / `monitor123` (limited access)

Public endpoints (no authentication required):
- `/actuator/health`
- `/actuator/info`

## Health Indicators

### Built-in Health Indicators
- **Database**: Checks database connectivity and health
- **Disk Space**: Monitors available disk space
- **Application**: General application health status

The health endpoint provides comprehensive status information about:
- Database connectivity
- Available disk space
- Overall application status

## Metrics Collection

### BookMetricsCollector
Located: `com.example.bookstore.metrics.BookMetricsCollector`

#### Counters
- `bookstore.books.created` - Number of books created
- `bookstore.books.updated` - Number of books updated
- `bookstore.books.deleted` - Number of books deleted
- `bookstore.books.viewed` - Number of book views
- `bookstore.books.searched` - Number of search operations

#### Timers
- `bookstore.books.create.duration` - Book creation time
- `bookstore.books.update.duration` - Book update time
- `bookstore.books.delete.duration` - Book deletion time
- `bookstore.books.search.duration` - Search operation time
- `bookstore.books.fetch.duration` - Book fetch time

#### Gauges
- `bookstore.books.active.operations` - Active book operations
- `bookstore.books.total.count` - Total books in system

#### HTTP Request Metrics
- `http.server.requests.custom` - Custom HTTP request duration
- `bookstore.http.requests.total` - Request count by endpoint

### Prometheus Integration

The application exposes metrics in Prometheus format at `/actuator/prometheus`.

Key features:
- Percentile histograms for request duration
- SLO (Service Level Objective) buckets
- Custom tags for better metric organization

## Custom Actuator Endpoints

### Book Statistics Endpoint
**URL**: `/actuator/bookstats`

Provides comprehensive book inventory statistics via a REST controller:
```json
{
  "totalBooks": 150,
  "booksInStock": 120,
  "booksOutOfStock": 30,
  "inStockPercentage": "80.00%",
  "outOfStockPercentage": "20.00%",
  "status": "healthy"
}
```

This endpoint is publicly accessible and provides real-time statistics about the book inventory.

## Configuration

### Development Configuration
File: `application.properties`

Key settings:
- All endpoints exposed for development
- Detailed health information shown
- Debug logging enabled
- Extended metrics collection

### Production Configuration
File: `application-prod.yml`

Security considerations:
- Limited endpoint exposure
- Health details hidden from public
- Optimized cache settings
- Production logging levels

## Info Endpoint

The `/actuator/info` endpoint provides detailed application information:

```json
{
  "app": {
    "name": "Bookstore Backend API",
    "description": "A comprehensive bookstore management system with Spring Boot",
    "version": "0.0.1-SNAPSHOT",
    "features": {
      "books": "CRUD operations for book management",
      "search": "Advanced search capabilities",
      "monitoring": "Comprehensive monitoring and metrics",
      "health": "Custom health indicators"
    }
  },
  "build": {
    "version": "0.0.1-SNAPSHOT",
    "time": "2023-12-06T10:30:00.000Z"
  },
  "git": {
    "branch": "main",
    "commit": {
      "id": "abcd1234",
      "time": "2023-12-06T10:25:00.000Z"
    }
  }
}
```

## Monitoring Best Practices

### Health Checks
1. Monitor `/actuator/health` for application status
2. Set up alerts for database connectivity issues
3. Watch memory usage patterns
4. Monitor disk space availability

### Metrics Monitoring
1. Track book operation rates and response times
2. Monitor HTTP request patterns
3. Set up alerts for high error rates
4. Watch for performance degradation

### Prometheus Queries Examples

```promql
# Average book creation time
rate(bookstore_books_create_duration_seconds_sum[5m]) / rate(bookstore_books_create_duration_seconds_count[5m])

# Books created per minute
rate(bookstore_books_created_total[1m]) * 60

# 95th percentile response time
histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m]))

# Error rate
rate(bookstore_http_requests_total{status=~"5.."}[5m]) / rate(bookstore_http_requests_total[5m])
```

## Troubleshooting

### Common Issues

1. **Health check failures**
   - Check database connectivity
   - Verify application configuration
   - Monitor memory usage

2. **Metrics not appearing**
   - Ensure Micrometer dependencies are included
   - Check actuator endpoint exposure
   - Verify security configuration

3. **Access denied to actuator endpoints**
   - Check authentication credentials
   - Verify security configuration
   - Ensure proper roles are assigned

### Logs

Monitor application logs for:
- Health indicator failures
- Metrics collection errors
- Security authentication issues
- Database connectivity problems

## Integration with External Monitoring

### Prometheus + Grafana
1. Configure Prometheus to scrape `/actuator/prometheus`
2. Import Grafana dashboards for Spring Boot metrics
3. Set up alerts based on custom metrics

### APM Tools
The application is compatible with:
- New Relic
- AppDynamics
- Datadog
- Elastic APM

Simply add the respective agent to your deployment.