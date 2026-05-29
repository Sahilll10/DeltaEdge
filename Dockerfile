# Stage 1: Build the application using Maven
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run the application using a lightweight Java runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# We use a wildcard *.jar so it works no matter what you named the artifact
COPY --from=build /app/target/*.jar app.jar
EXPOSE 5454
ENTRYPOINT ["java", "-jar", "app.jar"]
