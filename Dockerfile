# Multi-stage Dockerfile for Spring Boot Pet Clinic Application
# Stage 1: Build application using Maven
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build application
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime image with JRE only (multi-platform compatible)
FROM eclipse-temurin:17-jre

WORKDIR /app

# Install curl for health checks (since we're not using Alpine)
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create non-root user for security
RUN groupadd -r appgroup && useradd -r -g appgroup appuser

# Copy JAR from build stage
COPY --from=build /app/target/*.jar /app/petclinic.jar

# Set ownership to non-root user
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose application port
EXPOSE 8080

# Set JVM options for container environment
ENV JAVA_OPTS="-Xmx768m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Health check using Spring Boot Actuator
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/petclinic.jar"]
