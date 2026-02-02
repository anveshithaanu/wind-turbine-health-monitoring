# Stage 1: Build Angular Frontend
FROM node:18-alpine AS frontend-build
WORKDIR /app
# Copy frontend files
COPY frontend/package*.json ./frontend/
RUN cd frontend && npm install
COPY frontend/ ./frontend/
# Create output directory before building
RUN mkdir -p /app/src/main/resources/static
# Build frontend - outputs to ../src/main/resources/static (relative to frontend/)
RUN cd frontend && npm run build -- --configuration production
# Verify build output exists (check both possible locations)
RUN echo "Checking build output..." && \
    ls -la /app/src/main/resources/static/ && \
    (test -f /app/src/main/resources/static/index.html && echo "✓ Found index.html in static/") || \
    (test -f /app/src/main/resources/static/browser/index.html && echo "✓ Found index.html in static/browser/") || \
    (echo "ERROR: index.html not found!" && \
     echo "Contents of /app/src/main/resources:" && \
     ls -la /app/src/main/resources/ || true && \
     echo "Contents of /app/src/main/resources/static:" && \
     ls -la /app/src/main/resources/static/ || true && \
     exit 1)

# Stage 2: Build Spring Boot Backend
FROM maven:3.9-eclipse-temurin-21 AS backend-build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
# Ensure static directory exists
RUN mkdir -p ./src/main/resources/static
# Copy built frontend from previous stage
# Try to copy from static/ first, then from static/browser/ if needed
COPY --from=frontend-build /app/src/main/resources/static/ ./src/main/resources/static-temp/
RUN if [ -f ./src/main/resources/static-temp/index.html ]; then \
        echo "Copying from static/ directory" && \
        cp -r ./src/main/resources/static-temp/* ./src/main/resources/static/; \
    elif [ -f ./src/main/resources/static-temp/browser/index.html ]; then \
        echo "Copying from static/browser/ directory" && \
        cp -r ./src/main/resources/static-temp/browser/* ./src/main/resources/static/; \
    else \
        echo "ERROR: No frontend build found in expected locations!" && \
        ls -la ./src/main/resources/static-temp/ && \
        exit 1; \
    fi && \
    rm -rf ./src/main/resources/static-temp
# Verify files were copied
RUN echo "Verifying copied files..." && \
    ls -la ./src/main/resources/static/ && \
    (test -f ./src/main/resources/static/index.html && echo "✓ Found index.html") || \
    (echo "ERROR: index.html not found after copy!" && exit 1)
RUN mvn clean package -DskipTests

# Stage 3: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=backend-build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
