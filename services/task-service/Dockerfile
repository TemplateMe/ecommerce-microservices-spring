# services/task-service/Dockerfile
FROM gradle:8.5-jdk21 AS build

WORKDIR /app

# Copy gradle files first for dependency caching
COPY build.gradle.kts .
COPY settings.gradle.kts .
#COPY gradle.properties .

# Copy common module (shared library)
COPY common/ ./common/

# Copy the task-service module
COPY services/task-service/ ./services/task-service/

# Build the common module first, then the task-service
RUN gradle :common:publishToMavenLocal --no-daemon && \
    gradle :services:task-service:bootJar --no-daemon

FROM openjdk:21-jdk-slim

WORKDIR /app

# Copy the built JAR file from build stage
COPY --from=build /app/services/task-service/build/libs/*.jar task-service.jar

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Expose the port (typically 3434 for gateway)
EXPOSE 3434

# Add health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:3434/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "task-service.jar"]