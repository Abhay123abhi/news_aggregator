# -------- Build Stage --------
FROM gradle:7.6-jdk17-alpine AS builder

WORKDIR /app
COPY backend/ .

# Build Spring Boot executable JAR without tests
# Use the container's Gradle CLI instead of the wrapper (alpine /bin/sh may be BusyBox)
RUN gradle clean bootJar -x test --no-daemon

# -------- Runtime Stage --------
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copy only the bootable JAR and rename to app.jar
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose Spring Boot default port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]