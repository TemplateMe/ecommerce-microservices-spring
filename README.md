# Spring Boot Microservices Template

A comprehensive microservices template built with Spring Boot, Kotlin, and modern cloud-native technologies.

## Architecture Overview

This template provides a complete microservices ecosystem with:

- **Infrastructure Services**: Config Server, Discovery Server, API Gateway
- **Business Services**: Task Service, File Server, Notification Server, Scheduling Server
- **Supporting Infrastructure**: PostgreSQL, MinIO, Kafka, Keycloak
- **Monitoring & Observability**: Prometheus, Grafana, Zipkin, Loki, Promtail

### Architecture Diagram

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   API Gateway   │    │  Discovery Server │    │  Config Server  │
│     :8080       │    │      :8761       │    │      :8888      │
└─────────────────┘    └──────────────────┘    └─────────────────┘
         │                        │                        │
         └────────────────────────┼────────────────────────┘
                                  │
    ┌─────────────────────────────┼─────────────────────────────┐
    │                             │                             │
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│  Task Service   │    │ Notification Srv │    │  File Server    │
│     :3434       │    │      :2222       │    │      :4444      │
└─────────────────┘    └──────────────────┘    └─────────────────┘
         │                        │                        │
         └────────────────────────┼────────────────────────┘
                                  │
                     ┌──────────────────┐
                     │ Scheduling Server│
                     │      :3333       │
                     └──────────────────┘
```

### Supporting Infrastructure

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   PostgreSQL    │    │     MinIO        │    │     Kafka       │
│     :5431       │    │   :9000/:9001    │    │     :9092       │
└─────────────────┘    └──────────────────┘    └─────────────────┘

┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│    Keycloak     │    │   Prometheus     │    │    Grafana      │
│     :8180       │    │      :9090       │    │     :3000       │
└─────────────────┘    └──────────────────┘    └─────────────────┘

┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│     Zipkin      │    │      Loki        │    │   Promtail      │
│     :9411       │    │      :3100       │    │   (internal)    │
└─────────────────┘    └──────────────────┘    └─────────────────┘
```

## Features

### 🏗️ Infrastructure Services
- **Config Server**: Centralized configuration management
- **Discovery Server**: Service registration and discovery (Eureka)
- **API Gateway**: Routing, load balancing, and security

### 📊 Observability & Monitoring
- **Metrics**: Prometheus integration with custom metrics
- **Tracing**: Distributed tracing with Zipkin
- **Logging**: Centralized logging with Loki and Promtail
- **Visualization**: Grafana dashboards for metrics and logs
- **Health Checks**: Actuator endpoints for all services

### 🔐 Security
- **OAuth2/OIDC**: Keycloak integration for authentication
- **JWT**: Token-based authentication
- **Resource Server**: Protected endpoints

### 📁 File Management
- **Object Storage**: MinIO integration
- **File Operations**: Upload, download, bucket management
- **Presigned URLs**: Secure file access

### 📧 Notifications
- **Email Service**: SMTP integration
- **Event-Driven**: Kafka-based messaging
- **Template Engine**: HTML email templates

### ⏰ Scheduling
- **Job Scheduling**: Quartz integration
- **HTTP Jobs**: REST API job execution
- **Persistence**: PostgreSQL job store

### 🔧 Development Features
- **Hot Reload**: Docker Compose development setup
- **Testing**: Comprehensive test scripts
- **Documentation**: Detailed setup guides

## Quick Start

### Prerequisites
- Docker and Docker Compose
- JDK 17+ (for local development)
- Gradle (for building)

### Environment Setup

1. **Clone the repository**:
```bash
git clone <repository-url>
cd microservices-spring
```

2. **Create environment file**:
```bash
cp .env.example .env
# Edit .env with your configuration
```

3. **Start the stack**:
```bash
# Start supporting infrastructure
docker-compose up -d postgres keycloak kafka zookeeper minio prometheus grafana loki promtail zipkin

# Start application services
docker-compose up -d
```

4. **Verify setup**:
```bash
# Run test script (Unix/Mac)
./test-loki-setup.sh

# Run test script (Windows)
./test-loki-setup.ps1
```

### Service Endpoints

| Service | URL | Description |
|---------|-----|-------------|
| API Gateway | http://localhost:8080 | Main entry point |
| Config Server | http://localhost:8888 | Configuration management |
| Discovery Server | http://localhost:8761 | Service registry |
| Task Service | http://localhost:3434 | Business logic service |
| File Server | http://localhost:4444 | File management |
| Notification Server | http://localhost:2222 | Email notifications |
| Scheduling Server | http://localhost:3333 | Job scheduling |
| Grafana | http://localhost:3000 | Monitoring dashboard |
| Prometheus | http://localhost:9090 | Metrics collection |
| Loki | http://localhost:3100 | Log aggregation |
| Zipkin | http://localhost:9411 | Distributed tracing |
| Keycloak | http://localhost:8180 | Authentication |
| MinIO Console | http://localhost:9001 | Object storage |

## Centralized Logging with Loki

### Overview
The template includes a complete centralized logging solution using:
- **Loki**: Log aggregation and storage
- **Promtail**: Log shipping agent
- **Grafana**: Log visualization and querying

### Log Structure
```
logs/
├── infrastructure/
│   ├── config-server/application.log
│   ├── discovery-server/application.log
│   ├── api-gateway/application.log
│   ├── notification-server/application.log
│   ├── file-server/application.log
│   └── scheduling-server/application.log
└── services/
    └── task-service/application.log
```

### Using Logs in Grafana

1. **Access Grafana**: http://localhost:3000 (admin/admin)
2. **Navigate to Explore** → Select "Loki" datasource
3. **Try sample queries**:

```logql
# All application logs
{job="spring-boot"}

# Specific service logs
{service="notification-server"}

# Error logs only
{service="api-gateway"} |= "ERROR"

# Rate of errors over time
rate({job="spring-boot"} |= "ERROR" [5m])
```

### Log Format
All services use a standardized format:
```
2024-01-15 10:30:45 [http-nio-8080-exec-1] INFO  c.a.service.EmailService - Sending email to: user@example.com
```

For detailed logging setup, see [LOKI_PROMTAIL_SETUP.md](docs/LOKI_PROMTAIL_SETUP.md)

## Monitoring & Observability

### Prometheus Metrics
All services expose metrics at `/actuator/prometheus`:
- JVM metrics (memory, GC, threads)
- HTTP request metrics
- Custom business metrics
- Database connection pool metrics

### Zipkin Tracing
Distributed tracing is enabled across all services:
- HTTP request tracing
- Service-to-service communication
- Database operations
- External API calls

### Grafana Dashboards
Pre-configured datasources for:
- Prometheus (metrics)
- Loki (logs)
- Zipkin (traces - optional)

## Development

### Building Services
```bash
# Build all services
./gradlew build

# Build specific service
./gradlew :infrastructure:api-gateway:build
```

### Running Locally
```bash
# Start infrastructure
docker-compose up -d postgres keycloak kafka zookeeper minio

# Run services locally with IDE or:
./gradlew :infrastructure:config-server:bootRun
```

### Testing
```bash
# Run all tests
./gradlew test

# Test specific service
./gradlew :services:task-service:test
```

## Configuration

### Centralized Configuration
All service configurations are managed by Config Server:
- Located in `infrastructure/config-server/src/main/resources/configurations/`
- Environment-specific overrides supported
- Hot reload capability

### Key Configuration Files
- `application.yaml` - Global settings
- `api-gateway.yaml` - Gateway routing rules
- `logging.yaml` - Centralized logging configuration
- `discovery-server.yaml` - Eureka settings
- Service-specific configurations

## Security

### Authentication Flow
1. **Client** requests access token from **Keycloak**
2. **Keycloak** validates credentials and returns JWT
3. **Client** includes JWT in requests to **API Gateway**
4. **API Gateway** validates JWT and routes to services
5. **Services** receive validated user context

### OAuth2 Configuration
```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8180/realms/microservices
```

## API Documentation

### Example API Calls

#### File Upload
```bash
curl -X POST \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@document.pdf" \
  http://localhost:8080/api/v1/bucket/upload
```

#### Send Notification
```bash
curl -X POST \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"to":"user@example.com","subject":"Test","templateName":"VERIFY_EMAIL"}' \
  http://localhost:8080/api/v1/notification/email
```

#### Schedule Job
```bash
curl -X POST \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"jobName":"test-job","url":"http://example.com/webhook","cronExpression":"0 */5 * * * ?"}' \
  http://localhost:8080/api/v1/scheduling/jobs
```

## Troubleshooting

### Common Issues

1. **Service Discovery Issues**
   - Verify Eureka server is running
   - Check service registration in Eureka dashboard

2. **Configuration Not Loading**
   - Ensure Config Server is running first
   - Verify `SPRING_CONFIG_IMPORT` environment variable

3. **Database Connection Issues**
   - Check PostgreSQL container status
   - Verify database credentials in `.env`

4. **Log Issues**
   - Run `./test-loki-setup.sh` to verify logging setup
   - Check Promtail targets: http://localhost:9080/targets
   - Verify log directories exist and are writable

### Health Checks
All services expose health endpoints:
```bash
curl http://localhost:8080/actuator/health  # API Gateway
curl http://localhost:8888/actuator/health  # Config Server
curl http://localhost:8761/actuator/health  # Discovery Server
```

## Contributing

1. **Fork the repository**
2. **Create a feature branch**: `git checkout -b feature/amazing-feature`
3. **Commit changes**: `git commit -m 'Add amazing feature'`
4. **Push to branch**: `git push origin feature/amazing-feature`
5. **Open a Pull Request**

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

For support and questions:
- 📖 Check the [documentation](docs/)
- 🐛 Open an [issue](../../issues)
- 💬 Start a [discussion](../../discussions)

---

**Happy Coding!** 🚀
