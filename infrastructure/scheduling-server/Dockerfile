# infrastructure/scheduling-server/Dockerfile
FROM gradle:8.5-jdk21 AS build

WORKDIR /app

# Copy gradle files first for dependency caching
COPY build.gradle.kts .
COPY settings.gradle.kts .
#COPY gradle.properties .

# Copy common module (shared library)
COPY common/ ./common/

# Copy the scheduling-server module
COPY infrastructure/scheduling-server/ ./infrastructure/scheduling-server/

# Build the common module first, then the scheduling-server
RUN gradle :common:publishToMavenLocal --no-daemon && \
    gradle :infrastructure:scheduling-server:bootJar --no-daemon

FROM openjdk:21-jdk-slim

WORKDIR /app

# Copy the built JAR file from build stage
COPY --from=build /app/infrastructure/scheduling-server/build/libs/*.jar scheduling-server.jar

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Expose the port (typically 3333 for gateway)
EXPOSE 3333

# Add health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:3333/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "scheduling-server.jar"]