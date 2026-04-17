# ── Stage 1: Build ────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# Cache dependencies first (layer caching)
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Build the fat JAR
COPY src ./src
RUN mvn clean package -q

# ── Stage 2: Runtime ───────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

LABEL org.opencontainers.image.title="Poland Tax Calculator"
LABEL org.opencontainers.image.description="Terminal-based Polish income tax calculator"
LABEL org.opencontainers.image.version="1.0.0"

WORKDIR /app

# Copy fat JAR from builder
COPY --from=builder /build/target/poland-tax-calculator.jar app.jar

# Data directory for SQLite database (mount as volume)
RUN mkdir -p /app/data

# Run from /app/data so the DB file is created inside the volume
WORKDIR /app/data

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
