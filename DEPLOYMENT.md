# Deployment Guide

Complete production deployment guide for the Bookstore Spring Boot application. This guide covers Docker-based deployment, traditional server deployment, environment configuration, monitoring setup, and maintenance procedures.

## 📋 Deployment Overview

The Bookstore application supports multiple deployment strategies:

1. **Docker-based Deployment** (Recommended) - Containerized deployment with Docker Compose
2. **Traditional Server Deployment** - Direct deployment to application servers
3. **Cloud Platform Deployment** - AWS, Azure, GCP deployment options
4. **Kubernetes Deployment** - Orchestrated container deployment

## 🐳 Docker Production Deployment (Recommended)

### Prerequisites

- **Docker Engine** 20.10+ with Docker Compose
- **Minimum Resources**: 2 GB RAM, 2 CPU cores, 10 GB disk space
- **Operating System**: Linux (Ubuntu 20.04+, CentOS 8+, RHEL 8+)
- **Network**: Ports 80, 443, and 8080 available

### Quick Production Setup

1. **Clone and configure the repository**:
   ```bash
   git clone [repository-url]
   cd bookstore-spring-boot
   
   # Copy and configure production environment
   cp .env.example .env.prod
   
   # Edit .env.prod with your production values
   nano .env.prod
   ```

2. **Configure production environment**:
   ```bash
   # .env.prod - IMPORTANT: Change all passwords and secrets!
   
   # Database Configuration
   MYSQL_ROOT_PASSWORD=SECURE_ROOT_PASSWORD_HERE
   MYSQL_DATABASE=booksdb
   MYSQL_USER=bookstore
   MYSQL_PASSWORD=SECURE_DATABASE_PASSWORD_HERE
   
   # Application Configuration
   BACKEND_PORT=8080
   FRONTEND_PORT=80
   
   # Security Configuration
   JWT_SECRET=your_32_character_jwt_secret_key_here
   CORS_ALLOWED_ORIGINS=https://yourdomain.com,https://www.yourdomain.com
   
   # SSL Configuration (if using HTTPS)
   SSL_CERTIFICATE_PATH=/etc/ssl/certs/yourdomain.crt
   SSL_PRIVATE_KEY_PATH=/etc/ssl/private/yourdomain.key
   
   # Monitoring Configuration
   ENABLE_METRICS=true
   METRICS_RETENTION_DAYS=30
   
   # Backup Configuration
   BACKUP_RETENTION_DAYS=7
   BACKUP_SCHEDULE="0 2 * * *"  # Daily at 2 AM
   ```

3. **Build production images**:
   ```bash
   # Build with security scanning (recommended)
   ./scripts/docker/build-prod.sh --security-scan
   
   # Or build without security scanning (faster)
   ./scripts/docker/build-prod.sh
   ```

4. **Deploy to production**:
   ```bash
   # Deploy with all checks and backup
   ./scripts/docker/deploy-prod.sh deploy
   
   # Or deploy with skip checks (if needed)
   ./scripts/docker/deploy-prod.sh deploy --skip-checks
   ```

5. **Verify deployment**:
   ```bash
   # Check service status
   ./scripts/docker/deploy-prod.sh status
   
   # Run smoke tests
   ./scripts/docker/deploy-prod.sh smoke-test
   
   # View logs
   ./scripts/docker/deploy-prod.sh logs
   ```

### Production Environment Configuration

#### Core Environment Variables

```bash
# Database Configuration
MYSQL_ROOT_PASSWORD=              # Strong root password (required)
MYSQL_DATABASE=booksdb            # Database name
MYSQL_USER=bookstore              # Application database user
MYSQL_PASSWORD=                   # Strong database password (required)

# Application Configuration
BACKEND_PORT=8080                 # Backend service port
FRONTEND_PORT=80                  # Frontend service port (80 for HTTP, 443 for HTTPS)
SERVER_URL=https://yourdomain.com # External server URL

# Security Configuration
JWT_SECRET=                       # 32+ character JWT secret (required)
CORS_ALLOWED_ORIGINS=             # Comma-separated allowed origins (required)
ENABLE_CSRF=false                 # CSRF protection (typically false for REST APIs)

# SSL/TLS Configuration (for HTTPS)
SSL_ENABLED=true                  # Enable SSL
SSL_CERTIFICATE_PATH=             # Path to SSL certificate
SSL_PRIVATE_KEY_PATH=             # Path to SSL private key
SSL_CERTIFICATE_CHAIN_PATH=       # Path to certificate chain (optional)

# Performance Configuration
JVM_MEMORY_MAX=1024m              # Maximum JVM heap size
JVM_MEMORY_MIN=512m               # Minimum JVM heap size
DB_POOL_SIZE=20                   # Database connection pool size

# Monitoring Configuration
ENABLE_METRICS=true               # Enable metrics collection
METRICS_RETENTION_DAYS=30         # Metrics retention period
ENABLE_HEALTH_CHECKS=true         # Enable health checks
HEALTH_CHECK_INTERVAL=30s         # Health check interval

# Logging Configuration
LOG_LEVEL=INFO                    # Application log level
LOG_RETENTION_DAYS=30             # Log file retention
ENABLE_ACCESS_LOGS=true           # Enable access logging

# Backup Configuration
ENABLE_BACKUP=true                # Enable automatic backups
BACKUP_RETENTION_DAYS=7           # Backup retention period
BACKUP_SCHEDULE="0 2 * * *"       # Backup schedule (cron format)
BACKUP_PATH=/opt/bookstore/backups # Backup storage path
```

#### Advanced Configuration

```bash
# Resource Limits
BACKEND_MEMORY_LIMIT=1.5G         # Backend container memory limit
BACKEND_CPU_LIMIT=2.0             # Backend container CPU limit
FRONTEND_MEMORY_LIMIT=128M        # Frontend container memory limit
FRONTEND_CPU_LIMIT=0.5            # Frontend container CPU limit

# Database Optimization
DB_QUERY_CACHE_SIZE=64M           # MySQL query cache size
DB_INNODB_BUFFER_POOL_SIZE=512M   # InnoDB buffer pool size
DB_MAX_CONNECTIONS=200            # Maximum database connections

# Cache Configuration
ENABLE_REDIS=false                # Enable Redis caching (future feature)
REDIS_URL=redis://redis:6379      # Redis connection URL

# External Integrations
SMTP_HOST=                        # SMTP server for email notifications
SMTP_PORT=587                     # SMTP server port
SMTP_USERNAME=                    # SMTP username
SMTP_PASSWORD=                    # SMTP password
SMTP_TLS_ENABLED=true             # Enable SMTP TLS

# Monitoring Integrations
PROMETHEUS_ENABLED=true           # Enable Prometheus metrics
GRAFANA_ENABLED=false             # Enable Grafana dashboard
ALERTING_WEBHOOK_URL=             # Webhook URL for alerts
```

### Production Docker Compose Configuration

The production deployment uses `docker-compose.prod.yml` with the following services:

```yaml
# docker-compose.prod.yml (excerpt)
version: '3.8'

services:
  # nginx reverse proxy
  nginx:
    image: nginx:1.27-alpine
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./docker/nginx/nginx.conf:/etc/nginx/nginx.conf:ro
      - ./ssl:/etc/ssl:ro
      - logs_nginx:/var/log/nginx
    depends_on:
      - backend
      - frontend

  # Backend Spring Boot application
  backend:
    image: bookstore/backend:latest
    environment:
      SPRING_PROFILES_ACTIVE: prod
      DB_HOST: mysql
      DB_PORT: 3306
      JAVA_OPTS: >-
        -Xms${JVM_MEMORY_MIN:-512m}
        -Xmx${JVM_MEMORY_MAX:-1024m}
        -XX:+UseContainerSupport
        -XX:MaxRAMPercentage=75.0
    deploy:
      resources:
        limits:
          memory: ${BACKEND_MEMORY_LIMIT:-1.5G}
          cpus: '${BACKEND_CPU_LIMIT:-2.0}'
        reservations:
          memory: 512M
          cpus: '0.5'
    healthcheck:
      test: ["CMD", "/app/health-check.sh"]
      interval: 30s
      timeout: 10s
      retries: 5
      start_period: 60s

  # Frontend React application
  frontend:
    image: bookstore/frontend:latest
    deploy:
      resources:
        limits:
          memory: ${FRONTEND_MEMORY_LIMIT:-128M}
          cpus: '${FRONTEND_CPU_LIMIT:-0.5}'

  # MySQL database
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: ${MYSQL_DATABASE}
      MYSQL_USER: ${MYSQL_USER}
      MYSQL_PASSWORD: ${MYSQL_PASSWORD}
    volumes:
      - mysql_data:/var/lib/mysql
      - ./docker/mysql/conf.d:/etc/mysql/conf.d:ro
    deploy:
      resources:
        limits:
          memory: 1G
          cpus: '1.0'
```

## 🖥️ Traditional Server Deployment

### Prerequisites

- **Java 17+** (OpenJDK or Oracle JDK)
- **MySQL 8.0+** database server
- **nginx** web server (for frontend)
- **Minimum Hardware**: 4 GB RAM, 2 CPU cores, 20 GB disk space

### Backend Deployment

1. **Prepare the environment**:
   ```bash
   # Create application user
   sudo useradd -m -s /bin/bash bookstore
   sudo mkdir -p /opt/bookstore
   sudo chown bookstore:bookstore /opt/bookstore
   
   # Create directories
   sudo mkdir -p /opt/bookstore/{app,logs,config,backups}
   sudo chown -R bookstore:bookstore /opt/bookstore
   ```

2. **Build the application**:
   ```bash
   cd backend
   ./mvnw clean package -Pprod -DskipTests
   
   # Copy JAR file to server
   scp target/bookstore-backend-*.jar user@server:/opt/bookstore/app/
   ```

3. **Configure application properties**:
   ```bash
   # /opt/bookstore/config/application-prod.yml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/booksdb?useSSL=true&serverTimezone=UTC
       username: ${DB_USERNAME:bookstore}
       password: ${DB_PASSWORD}
       driver-class-name: com.mysql.cj.jdbc.Driver
   
   server:
     port: 8080
     servlet:
       context-path: /api
   
   logging:
     file:
       name: /opt/bookstore/logs/application.log
     level:
       root: INFO
       com.example.bookstore: INFO
   ```

4. **Create systemd service**:
   ```bash
   # /etc/systemd/system/bookstore.service
   [Unit]
   Description=Bookstore Spring Boot Application
   After=network.target mysql.service
   
   [Service]
   Type=simple
   User=bookstore
   Group=bookstore
   ExecStart=/usr/bin/java -jar \
     -Dspring.config.location=/opt/bookstore/config/ \
     -Dspring.profiles.active=prod \
     -Xms512m -Xmx1024m \
     /opt/bookstore/app/bookstore-backend-*.jar
   
   Restart=always
   RestartSec=10
   StandardOutput=journal
   StandardError=journal
   SyslogIdentifier=bookstore
   
   Environment=DB_USERNAME=bookstore
   Environment=DB_PASSWORD=your_database_password
   Environment=CORS_ALLOWED_ORIGINS=https://yourdomain.com
   
   [Install]
   WantedBy=multi-user.target
   ```

5. **Start the service**:
   ```bash
   sudo systemctl daemon-reload
   sudo systemctl enable bookstore
   sudo systemctl start bookstore
   sudo systemctl status bookstore
   ```

### Frontend Deployment

1. **Build the frontend**:
   ```bash
   cd frontend
   npm install
   npm run build:prod
   
   # Copy build files to server
   scp -r dist/* user@server:/var/www/bookstore/
   ```

2. **Configure nginx**:
   ```nginx
   # /etc/nginx/sites-available/bookstore
   server {
       listen 80;
       server_name yourdomain.com www.yourdomain.com;
       
       # Redirect HTTP to HTTPS
       return 301 https://$server_name$request_uri;
   }
   
   server {
       listen 443 ssl http2;
       server_name yourdomain.com www.yourdomain.com;
       
       # SSL Configuration
       ssl_certificate /etc/ssl/certs/yourdomain.crt;
       ssl_certificate_key /etc/ssl/private/yourdomain.key;
       ssl_protocols TLSv1.2 TLSv1.3;
       ssl_ciphers ECDHE-RSA-AES256-GCM-SHA512:DHE-RSA-AES256-GCM-SHA512;
       ssl_prefer_server_ciphers off;
       
       # Security Headers
       add_header X-Frame-Options DENY always;
       add_header X-Content-Type-Options nosniff always;
       add_header X-XSS-Protection "1; mode=block" always;
       add_header Strict-Transport-Security "max-age=31536000; includeSubDomains; preload" always;
       
       # Frontend Static Files
       location / {
           root /var/www/bookstore;
           index index.html index.htm;
           try_files $uri $uri/ /index.html;
           
           # Cache static assets
           location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
               expires 1y;
               add_header Cache-Control "public, immutable";
           }
       }
       
       # API Proxy
       location /api/ {
           proxy_pass http://localhost:8080/api/;
           proxy_set_header Host $host;
           proxy_set_header X-Real-IP $remote_addr;
           proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
           proxy_set_header X-Forwarded-Proto $scheme;
           
           # Timeouts
           proxy_connect_timeout 30s;
           proxy_send_timeout 30s;
           proxy_read_timeout 30s;
       }
   }
   ```

3. **Enable the site**:
   ```bash
   sudo ln -s /etc/nginx/sites-available/bookstore /etc/nginx/sites-enabled/
   sudo nginx -t
   sudo systemctl reload nginx
   ```

### Database Setup

1. **Install and configure MySQL**:
   ```bash
   sudo apt update
   sudo apt install mysql-server
   sudo mysql_secure_installation
   ```

2. **Create database and user**:
   ```sql
   CREATE DATABASE booksdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   CREATE USER 'bookstore'@'localhost' IDENTIFIED BY 'secure_password_here';
   GRANT ALL PRIVILEGES ON booksdb.* TO 'bookstore'@'localhost';
   FLUSH PRIVILEGES;
   ```

3. **Configure MySQL for production**:
   ```ini
   # /etc/mysql/mysql.conf.d/mysqld.cnf
   [mysqld]
   # Basic Settings
   bind-address = 127.0.0.1
   port = 3306
   
   # Performance Settings
   innodb_buffer_pool_size = 512M
   innodb_log_file_size = 64M
   max_connections = 200
   query_cache_size = 64M
   
   # Security Settings
   local_infile = 0
   skip_show_database
   ```

## ☁️ Cloud Platform Deployment

### AWS Deployment

#### Using AWS Elastic Beanstalk

1. **Prepare the application**:
   ```bash
   # Create deployment package
   cd backend
   ./mvnw clean package -Pprod
   zip -r bookstore-backend.zip target/bookstore-backend-*.jar .ebextensions/
   ```

2. **Create Elastic Beanstalk application**:
   ```bash
   # Using AWS CLI
   aws elasticbeanstalk create-application \
     --application-name bookstore-backend \
     --description "Bookstore Spring Boot Backend"
   
   aws elasticbeanstalk create-environment \
     --application-name bookstore-backend \
     --environment-name bookstore-prod \
     --solution-stack-name "64bit Amazon Linux 2 v3.4.0 running Corretto 17"
   ```

3. **Configure environment variables**:
   ```bash
   aws elasticbeanstalk update-environment \
     --environment-name bookstore-prod \
     --option-settings \
       Namespace=aws:elasticbeanstalk:application:environment,OptionName=DB_USERNAME,Value=bookstore \
       Namespace=aws:elasticbeanstalk:application:environment,OptionName=DB_PASSWORD,Value=secure_password \
       Namespace=aws:elasticbeanstalk:application:environment,OptionName=SPRING_PROFILES_ACTIVE,Value=prod
   ```

#### Using AWS ECS (Fargate)

1. **Create task definition**:
   ```json
   {
     "family": "bookstore-backend",
     "networkMode": "awsvpc",
     "requiresCompatibilities": ["FARGATE"],
     "cpu": "1024",
     "memory": "2048",
     "executionRoleArn": "arn:aws:iam::account:role/ecsTaskExecutionRole",
     "containerDefinitions": [
       {
         "name": "bookstore-backend",
         "image": "your-ecr-repo/bookstore-backend:latest",
         "portMappings": [
           {
             "containerPort": 8080,
             "protocol": "tcp"
           }
         ],
         "environment": [
           {
             "name": "SPRING_PROFILES_ACTIVE",
             "value": "prod"
           }
         ],
         "logConfiguration": {
           "logDriver": "awslogs",
           "options": {
             "awslogs-group": "/ecs/bookstore-backend",
             "awslogs-region": "us-east-1",
             "awslogs-stream-prefix": "ecs"
           }
         }
       }
     ]
   }
   ```

### Azure Deployment

#### Using Azure App Service

1. **Create App Service**:
   ```bash
   # Using Azure CLI
   az appservice plan create \
     --name bookstore-plan \
     --resource-group bookstore-rg \
     --sku P1V2 \
     --is-linux
   
   az webapp create \
     --name bookstore-backend \
     --resource-group bookstore-rg \
     --plan bookstore-plan \
     --runtime "JAVA|17-java17"
   ```

2. **Deploy application**:
   ```bash
   az webapp deploy \
     --name bookstore-backend \
     --resource-group bookstore-rg \
     --src-path target/bookstore-backend-*.jar \
     --type jar
   ```

### Google Cloud Platform Deployment

#### Using Google App Engine

1. **Create app.yaml**:
   ```yaml
   runtime: java17
   instance_class: F2
   
   env_variables:
     SPRING_PROFILES_ACTIVE: prod
     DB_USERNAME: bookstore
     DB_PASSWORD: secure_password
   
   automatic_scaling:
     min_instances: 1
     max_instances: 10
     target_cpu_utilization: 0.6
   ```

2. **Deploy application**:
   ```bash
   gcloud app deploy app.yaml
   ```

## 🎛️ Kubernetes Deployment

### Prerequisites

- **Kubernetes cluster** (1.20+)
- **kubectl** configured
- **Docker registry** access

### Kubernetes Manifests

#### Namespace

```yaml
# k8s/namespace.yaml
apiVersion: v1
kind: Namespace
metadata:
  name: bookstore
```

#### ConfigMap

```yaml
# k8s/configmap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: bookstore-config
  namespace: bookstore
data:
  application-prod.yml: |
    spring:
      profiles:
        active: prod
      datasource:
        url: jdbc:mysql://mysql-service:3306/booksdb
        username: bookstore
    server:
      port: 8080
```

#### Secrets

```yaml
# k8s/secrets.yaml
apiVersion: v1
kind: Secret
metadata:
  name: bookstore-secrets
  namespace: bookstore
type: Opaque
data:
  db-password: <base64-encoded-password>
  jwt-secret: <base64-encoded-jwt-secret>
```

#### Backend Deployment

```yaml
# k8s/backend-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: bookstore-backend
  namespace: bookstore
spec:
  replicas: 3
  selector:
    matchLabels:
      app: bookstore-backend
  template:
    metadata:
      labels:
        app: bookstore-backend
    spec:
      containers:
      - name: backend
        image: bookstore/backend:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: bookstore-secrets
              key: db-password
        volumeMounts:
        - name: config-volume
          mountPath: /app/config
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
      volumes:
      - name: config-volume
        configMap:
          name: bookstore-config
```

#### Service

```yaml
# k8s/backend-service.yaml
apiVersion: v1
kind: Service
metadata:
  name: bookstore-backend-service
  namespace: bookstore
spec:
  selector:
    app: bookstore-backend
  ports:
  - port: 80
    targetPort: 8080
  type: ClusterIP
```

#### Ingress

```yaml
# k8s/ingress.yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: bookstore-ingress
  namespace: bookstore
  annotations:
    kubernetes.io/ingress.class: nginx
    cert-manager.io/cluster-issuer: letsencrypt-prod
spec:
  tls:
  - hosts:
    - yourdomain.com
    secretName: bookstore-tls
  rules:
  - host: yourdomain.com
    http:
      paths:
      - path: /api
        pathType: Prefix
        backend:
          service:
            name: bookstore-backend-service
            port:
              number: 80
      - path: /
        pathType: Prefix
        backend:
          service:
            name: bookstore-frontend-service
            port:
              number: 80
```

### Deploy to Kubernetes

```bash
# Apply all manifests
kubectl apply -f k8s/

# Check deployment status
kubectl get pods -n bookstore
kubectl get services -n bookstore
kubectl get ingress -n bookstore

# Check logs
kubectl logs -f deployment/bookstore-backend -n bookstore
```

## 📊 Monitoring Setup

### Prometheus and Grafana

1. **Install Prometheus**:
   ```bash
   # Using Helm
   helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
   helm install prometheus prometheus-community/kube-prometheus-stack
   ```

2. **Configure Prometheus to scrape metrics**:
   ```yaml
   # prometheus-config.yaml
   scrape_configs:
   - job_name: 'bookstore-backend'
     static_configs:
     - targets: ['bookstore-backend-service:80']
     metrics_path: '/actuator/prometheus'
     scrape_interval: 30s
   ```

3. **Import Grafana dashboard**:
   - Use dashboard ID: `4701` (JVM Micrometer)
   - Create custom dashboard for book metrics

### Application Performance Monitoring

1. **Setup APM with Elastic APM**:
   ```bash
   # Add APM agent to application
   java -javaagent:/opt/elastic-apm-agent.jar \
     -Delastic.apm.service_name=bookstore-backend \
     -Delastic.apm.server_urls=http://apm-server:8200 \
     -jar bookstore-backend.jar
   ```

2. **Configure New Relic** (alternative):
   ```bash
   # Download New Relic agent
   curl -O https://download.newrelic.com/newrelic/java-agent/newrelic-agent/current/newrelic-java.zip
   unzip newrelic-java.zip
   
   # Configure application
   java -javaagent:/opt/newrelic/newrelic.jar \
     -Dnewrelic.config.license_key=your_license_key \
     -Dnewrelic.config.app_name=bookstore-backend \
     -jar bookstore-backend.jar
   ```

## 🔐 SSL/TLS Configuration

### Let's Encrypt with Certbot

1. **Install Certbot**:
   ```bash
   sudo apt install certbot python3-certbot-nginx
   ```

2. **Obtain SSL certificate**:
   ```bash
   sudo certbot --nginx -d yourdomain.com -d www.yourdomain.com
   ```

3. **Auto-renewal setup**:
   ```bash
   sudo systemctl enable certbot.timer
   sudo systemctl start certbot.timer
   ```

### Manual SSL Certificate

1. **Generate private key**:
   ```bash
   openssl genrsa -out yourdomain.key 2048
   ```

2. **Generate certificate request**:
   ```bash
   openssl req -new -key yourdomain.key -out yourdomain.csr
   ```

3. **Install certificate**:
   ```bash
   sudo cp yourdomain.crt /etc/ssl/certs/
   sudo cp yourdomain.key /etc/ssl/private/
   sudo chmod 600 /etc/ssl/private/yourdomain.key
   ```

## 🗄️ Database Management

### Backup and Restore

1. **Automated backup script**:
   ```bash
   #!/bin/bash
   # /opt/bookstore/scripts/backup.sh
   
   DATE=$(date +%Y%m%d_%H%M%S)
   BACKUP_DIR="/opt/bookstore/backups"
   DB_NAME="booksdb"
   DB_USER="bookstore"
   
   # Create backup
   mysqldump -u $DB_USER -p$DB_PASSWORD $DB_NAME > $BACKUP_DIR/backup_$DATE.sql
   
   # Compress backup
   gzip $BACKUP_DIR/backup_$DATE.sql
   
   # Remove old backups (keep 7 days)
   find $BACKUP_DIR -name "backup_*.sql.gz" -mtime +7 -delete
   ```

2. **Schedule backup with cron**:
   ```bash
   # Add to crontab
   0 2 * * * /opt/bookstore/scripts/backup.sh
   ```

3. **Restore from backup**:
   ```bash
   # Restore database
   gunzip -c backup_20250106_020000.sql.gz | mysql -u bookstore -p booksdb
   ```

### Database Migrations

1. **Flyway migrations** (automated):
   ```sql
   -- db/migration/V2__Add_indexes.sql
   CREATE INDEX idx_books_category ON books(category);
   CREATE INDEX idx_books_author ON books(author);
   CREATE INDEX idx_books_publication_date ON books(publication_date);
   ```

2. **Manual migration** (if needed):
   ```bash
   # Run specific migration
   ./mvnw flyway:migrate -Dflyway.target=2.1
   
   # Check migration status
   ./mvnw flyway:info
   ```

## 📈 Performance Optimization

### JVM Tuning

```bash
# Production JVM options
JAVA_OPTS="-Xms512m -Xmx1024m \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/opt/bookstore/logs/ \
  -Djava.security.egd=file:/dev/./urandom \
  -Dspring.profiles.active=prod"
```

### Database Optimization

```sql
-- MySQL optimization
SET GLOBAL innodb_buffer_pool_size = 512 * 1024 * 1024; -- 512MB
SET GLOBAL query_cache_size = 64 * 1024 * 1024; -- 64MB
SET GLOBAL max_connections = 200;
```

### nginx Optimization

```nginx
# nginx.conf optimizations
worker_processes auto;
worker_connections 1024;

http {
    # Gzip compression
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml application/xml+rss text/javascript;
    
    # Caching
    location ~* \.(jpg|jpeg|png|gif|ico|css|js)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
    
    # Connection keep-alive
    keepalive_timeout 65;
    keepalive_requests 100;
}
```

## 🚨 Troubleshooting

### Common Deployment Issues

1. **Out of Memory Error**:
   ```bash
   # Check JVM memory usage
   jstat -gc PID
   
   # Increase JVM heap size
   export JAVA_OPTS="-Xms1g -Xmx2g"
   ```

2. **Database Connection Issues**:
   ```bash
   # Check MySQL status
   sudo systemctl status mysql
   
   # Check connection
   mysql -u bookstore -p -h localhost booksdb
   
   # Check connection pool
   curl http://localhost:8080/actuator/metrics/hikaricp.connections.active
   ```

3. **SSL Certificate Issues**:
   ```bash
   # Check certificate validity
   openssl x509 -in /etc/ssl/certs/yourdomain.crt -text -noout
   
   # Test SSL connection
   openssl s_client -connect yourdomain.com:443
   ```

4. **Performance Issues**:
   ```bash
   # Check application metrics
   curl http://localhost:8080/actuator/metrics
   
   # Check system resources
   top
   iostat -x 1
   free -h
   df -h
   ```

### Log Analysis

```bash
# Application logs
tail -f /opt/bookstore/logs/application.log

# nginx access logs
tail -f /var/log/nginx/access.log

# MySQL slow query log
tail -f /var/log/mysql/slow.log

# System logs
journalctl -u bookstore -f
```

## 🔄 Rolling Updates and Blue-Green Deployment

### Rolling Update with Docker

```bash
# Build new version
./scripts/docker/build-prod.sh --version 1.1.0

# Update with zero downtime
docker service update --image bookstore/backend:1.1.0 bookstore_backend
```

### Blue-Green Deployment

```bash
# Start green environment
docker-compose -f docker-compose.green.yml up -d

# Test green environment
./scripts/test-environment.sh green

# Switch traffic (update nginx config)
# Stop blue environment
docker-compose -f docker-compose.blue.yml down
```

## 📋 Maintenance Tasks

### Regular Maintenance Checklist

**Daily:**
- [ ] Check application health status
- [ ] Review error logs
- [ ] Verify backup completion
- [ ] Monitor resource usage

**Weekly:**
- [ ] Update security patches
- [ ] Review performance metrics
- [ ] Clean up log files
- [ ] Test backup restoration

**Monthly:**
- [ ] Update dependencies
- [ ] Review security configurations
- [ ] Analyze performance trends
- [ ] Update documentation

### Maintenance Scripts

```bash
# /opt/bookstore/scripts/maintenance.sh
#!/bin/bash

# Clean old logs
find /opt/bookstore/logs -name "*.log.*" -mtime +30 -delete

# Clean old backups
find /opt/bookstore/backups -name "backup_*.sql.gz" -mtime +7 -delete

# Update system packages
sudo apt update && sudo apt upgrade -y

# Restart application if needed
sudo systemctl restart bookstore
```

## 📞 Support and Escalation

### Health Check Endpoints

- **Application Health**: `GET /actuator/health`
- **Detailed Health**: `GET /actuator/health?details=true`
- **Readiness Probe**: `GET /actuator/health/readiness`
- **Liveness Probe**: `GET /actuator/health/liveness`

### Emergency Contacts

1. **Application Issues**: Development team
2. **Infrastructure Issues**: DevOps team
3. **Database Issues**: DBA team
4. **Security Issues**: Security team

### Emergency Procedures

1. **Service Outage**:
   ```bash
   # Check service status
   systemctl status bookstore
   
   # Restart service
   systemctl restart bookstore
   
   # Rollback if needed
   ./scripts/rollback.sh
   ```

2. **Database Issues**:
   ```bash
   # Check MySQL status
   systemctl status mysql
   
   # Restore from backup
   ./scripts/restore-backup.sh latest
   ```

3. **Security Incident**:
   ```bash
   # Immediate isolation
   iptables -A INPUT -p tcp --dport 80,443 -j DROP
   
   # Check for unauthorized access
   grep "Invalid user" /var/log/auth.log
   ```

---

**Deployment Guide Version**: 1.0.0  
**Last Updated**: January 2025  
**Next Review**: March 2025