# Stage 1: build
FROM maven:3.9.9-eclipse-temurin-23 AS build
WORKDIR /app

COPY .mvn .mvn
COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# Stage 2: run
FROM openjdk:23-ea-18-jdk-slim
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]
