# --- Стадия сборки (Build Stage) ---
FROM gradle:8-jdk17-focal AS build

# Переменная с именем сервиса
ARG SERVICE_NAME

WORKDIR /app

# Копируем конфигурацию корневого Gradle
COPY build.gradle .
COPY gradle.properties .
COPY gradlew .
COPY settings.gradle .
COPY gradle gradle

# Копируем общий модуль
COPY common common

# Копируем исходный код нужного сервиса
COPY ${SERVICE_NAME} ${SERVICE_NAME}

# Собираем JAR конкретного модуля
RUN --mount=type=cache,id=gradle-cache,target=/home/gradle/.gradle/caches,sharing=locked \
    --mount=type=cache,id=gradle-wrapper,target=/home/gradle/.gradle/wrapper,sharing=locked \
    ./gradlew :${SERVICE_NAME}:bootJar --no-daemon

# --- Финальная стадия (Runtime Stage) ---
FROM eclipse-temurin:17-jre-jammy

# В многоэтапных (multi-stage) сборках ARG нужно объявлять повторно
ARG SERVICE_NAME

WORKDIR /app

# Создаем пользователя и папку логов в один слой
RUN useradd -ms /bin/bash springuser && \
    mkdir -p /app/logs

# Копируем JAR-файл
COPY --from=build /app/${SERVICE_NAME}/build/libs/*.jar app.jar

# Права доступа
RUN chown -R springuser:springuser /app
USER springuser

ENTRYPOINT [ "java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar" ]