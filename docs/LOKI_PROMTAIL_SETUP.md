# Loki and Promtail Setup Guide

This guide explains the centralized logging setup using Loki and Promtail in your microservices template.

## Overview

**Loki** - Log aggregation system that stores and indexes logs efficiently
**Promtail** - Log shipping agent that sends logs from your services to Loki
**Grafana** - Visualization tool for viewing and querying logs from Loki

## Architecture

```
[Spring Boot Services] → [Log Files] → [Promtail] → [Loki] → [Grafana]
```

## Configuration Files

### 1. Loki Configuration
- **File**: `docker/monitoring/loki-config.yaml`
- **Purpose**: Configures Loki server settings, retention policies, and storage

### 2. Promtail Configuration
- **File**: `docker/monitoring/promtail-config.yaml`
- **Purpose**: Defines log sources and parsing rules for different services

### 3. Grafana Datasources
- **File**: `docker/monitoring/grafana-datasources.yaml`
- **Purpose**: Automatically configures Loki as a datasource in Grafana

## Log Structure

### Directory Layout
```
logs/
├── infrastructure/
│   ├── config-server/
│   ├── discovery-server/
│   ├── api-gateway/
│   ├── notification-server/
│   ├── file-server/
│   └── scheduling-server/
└── services/
    └── task-service/
```

### Log Format
All services use a standardized log format:
```
2024-01-15 10:30:45 [http-nio-8080-exec-1] INFO  c.a.service.EmailService - Sending email to: user@example.com
```

## Services Configuration

### Docker Compose Services

#### Loki
- **Port**: 3100
- **Config**: `/docker/monitoring/loki-config.yaml`
- **Storage**: Persistent volume `loki-data`

#### Promtail
- **Port**: 9080 (internal)
- **Config**: `/docker/monitoring/promtail-config.yaml`
- **Mounts**: 
  - Application log directories
  - Docker container logs (read-only)
  - Docker socket (read-only)

#### Grafana
- **Port**: 3000
- **Login**: admin/admin
- **Datasources**: Automatically configured with Loki and Prometheus

## Starting the Stack

### 1. Start Infrastructure
```bash
docker-compose up -d loki promtail grafana
```

### 2. Start All Services
```bash
docker-compose up -d
```

### 3. Verify Services
```bash
# Check Loki is running
curl http://localhost:3100/ready

# Check Promtail is running
curl http://localhost:9080/targets

# Access Grafana
open http://localhost:3000
```

## Using Loki in Grafana

### 1. Access Grafana
- URL: http://localhost:3000
- Username: admin
- Password: admin

### 2. Basic Log Queries

#### View All Logs
```logql
{job="spring-boot"}
```

#### Filter by Service
```logql
{service="notification-server"}
```

#### Filter by Log Level
```logql
{service="notification-server"} |= "ERROR"
```

#### Filter by Time and Service
```logql
{service="api-gateway"} |= "HTTP" |= "500"
```

#### Find Specific User Activity
```logql
{job="spring-boot"} |= "user@example.com"
```

### 3. Advanced Queries

#### Rate of Error Logs
```logql
rate({service="notification-server"} |= "ERROR" [5m])
```

#### Count Logs by Service
```logql
count by (service) ({job="infrastructure"})
```

#### Extract JSON from Logs
```logql
{service="api-gateway"} | json | status_code="500"
```

## Log Levels and Services

### Infrastructure Services
- **config-server**: Configuration management logs
- **discovery-server**: Service registration/discovery logs
- **api-gateway**: HTTP request/response logs, routing logs
- **notification-server**: Email sending, Kafka message logs
- **file-server**: MinIO operations, file upload/download logs
- **scheduling-server**: Job execution, Quartz scheduler logs

### Business Services
- **task-service**: Business logic, API operations

### Log Levels
- **ERROR**: Service failures, exceptions
- **WARN**: Potential issues, deprecated usage
- **INFO**: Normal operations, service starts/stops
- **DEBUG**: Detailed operational information (only in DEBUG mode)

## Monitoring and Alerting

### 1. Create Grafana Dashboards

#### Service Health Dashboard
- Log volume by service
- Error rate trends
- Service startup/shutdown events

#### Error Monitoring Dashboard
- Error logs by service
- Exception stack traces
- Failed operation patterns

### 2. Log-Based Alerts

#### High Error Rate Alert
```logql
rate({job="spring-boot"} |= "ERROR" [5m]) > 0.1
```

#### Service Down Alert
```logql
count by (service) ({job="infrastructure"} |= "Started") - count by (service) ({job="infrastructure"} |= "Stopped") < 1
```

## Best Practices

### 1. Log Format Consistency
- Use structured logging when possible
- Include correlation IDs for tracing
- Add context information (user ID, session ID)

### 2. Log Level Usage
```kotlin
// Good examples
logger.info("User {} logged in successfully", userId)
logger.error("Failed to process payment for order {}", orderId, exception)
logger.debug("Database query executed in {}ms", duration)

// Avoid
logger.info("Debug info: {}", debugData) // Use DEBUG level
logger.error("User clicked button") // Not an error
```

### 3. Performance Considerations
- Use async logging for high-throughput services
- Rotate log files to prevent disk space issues
- Set appropriate retention policies in Loki

### 4. Security
- Avoid logging sensitive data (passwords, tokens, PII)
- Use structured logging to facilitate safe redaction
- Consider log encryption for compliance

## Troubleshooting

### 1. Promtail Not Shipping Logs
```bash
# Check Promtail targets
curl http://localhost:9080/targets

# Check Promtail logs
docker logs promtail

# Verify log file permissions
ls -la logs/
```

### 2. Loki Not Receiving Logs
```bash
# Check Loki health
curl http://localhost:3100/ready

# Check Loki metrics
curl http://localhost:3100/metrics

# Verify Loki logs
docker logs loki
```

### 3. Grafana Can't Query Loki
- Check datasource configuration in Grafana
- Verify Loki URL: http://loki:3100
- Test connection in datasource settings

### 4. No Logs Appearing
1. Check if services are writing to log files
2. Verify Promtail can read the log directories
3. Check Promtail configuration file paths
4. Ensure log format matches Promtail regex

## Configuration Examples

### Service-Specific Log Configuration

#### High-Volume Service (API Gateway)
```yaml
logging:
  level:
    com.azsumtoshko.api_gateway: INFO
    org.springframework.web: WARN
  file:
    name: logs/infrastructure/api-gateway/application.log
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

#### Debug Service (Notification Server)
```yaml
logging:
  level:
    com.azsumtoshko.notification_server: DEBUG
    org.springframework.mail: DEBUG
  file:
    name: logs/infrastructure/notification-server/application.log
```

### Custom Log Parsing

#### JSON Log Format
```yaml
# promtail-config.yaml
- job_name: json-logs
  static_configs:
    - targets:
        - localhost
      labels:
        job: json-spring-boot
        __path__: /var/log/json/*.log
  pipeline_stages:
    - json:
        expressions:
          timestamp: timestamp
          level: level
          logger: logger
          message: message
          service: service
    - labels:
        level:
        service:
        logger:
    - timestamp:
        source: timestamp
        format: RFC3339
```

## Integration with Existing Code

Your existing logging code works without changes:

```kotlin
@Service
class EmailService {
    private val logger = LoggerFactory.getLogger(EmailService::class.java)
    
    fun sendEmail(to: String, subject: String) {
        logger.info("Sending email to: {}", to)
        try {
            // Send email logic
            logger.info("Email sent successfully to: {}", to)
        } catch (exception: Exception) {
            logger.error("Failed to send email to: {}", to, exception)
            throw exception
        }
    }
}
```

This will automatically appear in Loki and be queryable in Grafana!

## Performance Tuning

### Loki Retention
```yaml
# loki-config.yaml
limits_config:
  retention_period: 336h  # 14 days

table_manager:
  retention_deletes_enabled: true
  retention_period: 336h
```

### Promtail Batching
```yaml
# promtail-config.yaml
clients:
  - url: http://loki:3100/loki/api/v1/push
    batchwait: 1s
    batchsize: 1048576
```

Your centralized logging is now fully configured and ready to use! 