# syntax=docker/dockerfile:1

# --- Builder stage --------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build

COPY pom.xml ./
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

# --- Runtime stage --------------------------------------------------------
FROM eclipse-temurin:21-jre

LABEL maintainer="NII Platform Team" \
      description="NII Alert Simulator - Generates synthetic network alerts" \
      version="1.0.0"

RUN groupadd --gid 1001 appuser && \
    useradd --uid 1001 --gid 1001 --create-home --shell /bin/bash appuser

WORKDIR /app

COPY --from=builder /build/target/alert-simulator-*.jar app.jar

RUN chown -R appuser:appuser /app

USER appuser

EXPOSE 8081 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider \
      http://localhost:8081/health/simulator || exit 1

ENV JAVA_OPTS="-XX:MaxGCPauseMillis=200 \
-XX:InitiatingHeapOccupancyPercent=35 \
-XX:+UseStringDeduplication \
-XX:+ExitOnOutOfMemoryError \
-Dspring.profiles.active=production"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
