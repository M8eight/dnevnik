FROM gradle:8-jdk17-focal AS build
WORKDIR /app

# Copy root Gradle configs
COPY build.gradle .
COPY gradle.properties .
COPY gradlew .
COPY settings.gradle .
COPY gradle gradle

COPY api-gateway api-gateway

# Build jar
RUN ./gradlew :api-gateway:bootJar --no-daemon

# Jre image
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Create safety user
RUN useradd -ms /bin/bash springuser
RUN mkdir -p /app/logs && chown -R springuser:springuser /app

COPY --from=build /app/api-gateway/build/libs/*.jar app.jar

USER springuser

# Run with container support and limit max RAM usage to 75%
ENTRYPOINT [ "java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar" ]