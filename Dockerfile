# Stage 1: Build Angular Frontend
FROM node:18-alpine AS frontend-build
WORKDIR /app
# Copy frontend files
COPY frontend/package*.json ./frontend/
RUN cd frontend && npm install
COPY frontend/ ./frontend/
# Build frontend - outputs to ../src/main/resources/static (relative to frontend/)
RUN cd frontend && npm run build

# Stage 2: Build Spring Boot Backend
FROM maven:3.9-eclipse-temurin-21 AS backend-build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
# Copy built frontend from previous stage
# When frontend builds, it outputs to ../src/main/resources/static from frontend/ directory
# So from /app, the path is /app/src/main/resources/static
COPY --from=frontend-build /app/src/main/resources/static ./src/main/resources/static
RUN mvn clean package -DskipTests

# Stage 3: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=backend-build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
