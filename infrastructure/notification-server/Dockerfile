# infrastructure/notification-server/Dockerfile
FROM gradle:8.5-jdk21 AS build

WORKDIR /app

# Copy gradle files first for dependency caching
COPY build.gradle.kts .
COPY settings.gradle.kts .
#COPY gradle.properties .

# Copy common module (shared library)
COPY common/ ./common/

# Copy the notification-server module
COPY infrastructure/notification-server/ ./infrastructure/notification-server/

# Build the common module first, then the notification-server
RUN gradle :common:publishToMavenLocal --no-daemon && \
    gradle :infrastructure:notification-server:bootJar --no-daemon

FROM openjdk:21-jdk-slim

WORKDIR /app

# Copy the built JAR file from build stage
COPY --from=build /app/infrastructure/notification-server/build/libs/*.jar notification-server.jar

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Expose the port (adjust based on your configuration)
EXPOSE 2222

# Add health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:2222/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "notification-server.jar"]