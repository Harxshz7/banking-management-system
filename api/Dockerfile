# Stage 1: Build the core + api
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app
# We need to copy the entire repository because the api module depends on the root project's source code
COPY . .
WORKDIR /app/api
RUN mvn clean package -DskipTests

# Stage 2: Minimal runtime
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy the built jar from the builder stage
COPY --from=builder /app/api/target/bankpro-api-1.0.0.jar app.jar

# Expose port
EXPOSE 8080

# The data directory where SQLite database will be mounted
RUN mkdir -p /app/banking_data

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
