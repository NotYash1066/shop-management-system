# ============================================
# Shop Management System - Production Dockerfile
# Multi-stage build so it works from a fresh git clone on Railway / any Docker platform.
# ============================================

# ---- Build Stage ----
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper + pom first (better layer caching)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make mvnw executable
RUN chmod +x mvnw

# Pre-download dependencies (cache layer)
RUN ./mvnw dependency:go-offline -B

# Copy source
COPY src ./src

# Build (skip tests for speed in CI; run tests in GitHub Actions instead)
RUN ./mvnw clean package -DskipTests -B

# ---- Runtime Stage ----
FROM eclipse-temurin:17-jre-alpine AS runtime

WORKDIR /app

# Create non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy only the built JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Set ownership
RUN chown -R appuser:appgroup /app

USER appuser

# Railway / most platforms inject PORT. Spring Boot will use 8080 by default.
# If you need to respect $PORT, you can add: ENV SERVER_PORT=${PORT:-8080}
EXPOSE 8080

# Health check (matches railway.json and the /api/test endpoint in the app)
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/test || exit 1

# JVM container-aware flags
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
