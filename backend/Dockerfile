# -------- Build Stage --------
FROM gradle:7.6-jdk17 AS builder

WORKDIR /app
COPY . .

# Build Spring Boot executable JAR without tests
RUN ./gradlew clean bootJar -x test

# -------- Runtime Stage --------
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copy only the bootable JAR and rename to app.jar
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose Spring Boot default port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]