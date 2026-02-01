# Real-Time Wind Turbine Health Monitoring System

Enterprise-grade Java Full Stack application for monitoring 2,200+ wind turbines with real-time health visibility, anomaly detection, and predictive maintenance insights.

## Quick Start

### Docker (Recommended)
```bash
cd frontend
npm install
npm run build
cd ..
docker-compose up --build
```
Access at: http://localhost:8080

### Manual Setup
1. Start MySQL and create database `wind_turbine_db`
2. Update `src/main/resources/application.yaml` with your MySQL credentials
3. Build backend: `./mvnw clean package && ./mvnw spring-boot:run`
4. Build frontend: `cd frontend && npm install && npm run build`
5. Access at: http://localhost:8080

## Complete Documentation

**See [COMPLETE-SYSTEM-DOCUMENTATION.md](./COMPLETE-SYSTEM-DOCUMENTATION.md) for comprehensive documentation covering:**
- Adding turbines with batch processing
- Telemetry data generation (every 10 seconds)
- Data aggregation (hourly)
- Batch processing details
- Complete data flow architecture
- API endpoints
- Troubleshooting

## Technology Stack

- **Backend**: Spring Boot 4.0.2, Java 21, Spring Data JPA
- **Frontend**: Angular 17, TypeScript
- **Database**: MySQL 8.0
- **DevOps**: Docker, Docker Compose

## Key Features

- Real-time health status monitoring
- Telemetry generation every 10 seconds
- Hourly data aggregation with batch processing
- Historical performance analytics
- Anomaly detection and alerts
- Scalable to 2,200+ turbines

---

**For detailed information, see [COMPLETE-SYSTEM-DOCUMENTATION.md](./COMPLETE-SYSTEM-DOCUMENTATION.md)**
