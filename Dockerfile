
# Build stage
FROM maven:3.9.6-eclipse-temurin-17

WORKDIR /app

# Copy project files
COPY pom.xml .
COPY src ./src

# Build the app (skip tests for speed)
RUN mvn clean install -DskipTests

# Run stage
FROM openjdk:17-jdk-slim
WORKDIR /app

# Copy the jar from the build stage
COPY --from=0 /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]

