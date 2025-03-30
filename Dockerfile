FROM openjdk:21-jdk-slim

WORKDIR /app

COPY target/auth-service-0.0.1-SNAPSHOT.jar backend-service.jar

ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "backend-service.jar"]