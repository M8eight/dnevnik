FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY build/libs/*.jar app.jar

EXPOSE 9004

ENTRYPOINT ["java", "-jar", "app.jar"]
