# ==============================================
# Multi-Stage Dockerfile for AI Integration Server
# ==============================================

# Stage 1: Build Stage
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime Stage
FROM eclipse-temurin:17-jre

# Add metadata
LABEL maintainer="Campus Marketplace Team"
LABEL description="Campus Marketplace AI Integration Service"

# Install wget for health checks
RUN apt-get update && apt-get install -y --no-install-recommends wget && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Create non-root user for security
RUN groupadd -r appgroup && useradd -r -g appgroup appuser

# Copy the built JAR from build stage
COPY --from=build /app/target/ai-integration-server-*.jar app.jar

# Change ownership to non-root user
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose port 3001
EXPOSE 3001

# Health check
HEALTHCHECK --interval=30s --timeout=10s --retries=3 --start-period=40s \
  CMD wget --no-verbose --tries=1 --spider http://localhost:3001/api/health || exit 1

# Default JVM options (can be overridden via JAVA_OPTS environment variable)
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

# Run the application with JAVA_OPTS support
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
