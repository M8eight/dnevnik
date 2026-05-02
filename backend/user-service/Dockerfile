FROM gradle:8-jdk17-focal AS build
WORKDIR /app

# Copy root Gradle configs
COPY build.gradle .
COPY gradle.properties .
COPY gradlew .
COPY settings.gradle .
COPY gradle gradle

COPY user-service user-service

# Build jar
RUN ./gradlew :user-service:bootJar --no-daemon

# Jre image
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Create safety user
RUN useradd -ms /bin/bash springuser

# Create logs directory
RUN mkdir -p /app/logs

COPY --from=build /app/user-service/build/libs/*.jar app.jar

# Set permissions springuser for app directory
RUN chown -R springuser:springuser /app

USER springuser

# Run with container support and limit max RAM usage to 75%
ENTRYPOINT [ "java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar" ]