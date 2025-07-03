# infrastructure/discovery-server/Dockerfile
FROM gradle:8.5-jdk21 AS build

WORKDIR /app

# Copy gradle files first for dependency caching
COPY build.gradle.kts .
COPY settings.gradle.kts .
#COPY gradle.properties .

# Copy common module (shared library)
COPY common/ ./common/

# Copy the discovery-server module
COPY infrastructure/discovery-server/ ./infrastructure/discovery-server/

# Build the common module first, then the discovery-server
RUN gradle :common:publishToMavenLocal --no-daemon && \
    gradle :infrastructure:discovery-server:bootJar --no-daemon

FROM openjdk:21-jdk-slim

WORKDIR /app

# Copy the built JAR file from build stage
COPY --from=build /app/infrastructure/discovery-server/build/libs/*.jar discovery-server.jar

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Expose the port (typically 8761 for Eureka)
EXPOSE 8761

# Add health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8761/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "discovery-server.jar"]