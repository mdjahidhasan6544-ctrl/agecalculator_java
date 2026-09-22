# ==============================================================================
# STAGE 1: Build & Dependency Caching (Maven Builder)
# ==============================================================================
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# 1. Cache Maven dependencies first (Layer caching optimization)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 2. Copy source code and package the Spring Boot artifact
COPY src ./src
RUN mvn clean package -DskipTests -B

# ==============================================================================
# STAGE 2: Secure Production Runtime (Lightweight JRE)
# ==============================================================================
FROM eclipse-temurin:21-jre-alpine AS runner

# Metadata labels
LABEL maintainer="DevSecOps Team" \
      application="age-calculator" \
      environment="production" \
      version="1.0.0"

# Install curl for container health check
RUN apk --no-cache add curl

# Create dedicated non-root system group and user
RUN addgroup -g 10001 -S appgroup && \
    adduser -u 10001 -S appuser -G appgroup

WORKDIR /app

# Copy the built jar from Stage 1 and set non-root ownership
COPY --from=builder --chown=appuser:appgroup /build/target/age-calculator.jar /app/age-calculator.jar

# Enforce non-root execution
USER 10001:10001

# Container networking and runtime configuration
EXPOSE 8080

# Environment variables for container-optimized JVM
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# Healthcheck probe against the Spring Boot health endpoint
HEALTHCHECK --interval=30s --timeout=3s --start-period=25s --retries=3 \
  CMD curl -f http://localhost:8080/api/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/age-calculator.jar"]
