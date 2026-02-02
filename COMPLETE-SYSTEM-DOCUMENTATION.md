# Wind Turbine Health Monitoring System - Complete Documentation

## Table of Contents
1. [System Overview](#system-overview)
2. [Adding Turbines](#adding-turbines)
3. [Telemetry Data Generation](#telemetry-data-generation)
4. [Data Aggregation](#data-aggregation)
5. [Batch Processing](#batch-processing)
6. [Data Flow Architecture](#data-flow-architecture)
7. [API Endpoints](#api-endpoints)
8. [Troubleshooting](#troubleshooting)

---

## System Overview

This is a **real-time wind turbine health monitoring system** built with:
- **Backend**: Spring Boot (Java) with MySQL database
- **Frontend**: Angular (TypeScript)
- **Architecture**: RESTful API with scheduled jobs for data generation and aggregation

### Key Components

1. **Turbines**: Individual wind turbines with properties (ID, name, farm, rated power, status)
2. **Farms**: Groups of turbines located in regions
3. **Telemetry**: Raw sensor data collected every 10 seconds from active turbines
4. **Aggregates**: Hourly aggregated telemetry data for analytics

---

## Adding Turbines

### How It Works

The system uses **batch processing** to efficiently add large numbers of turbines.

**Endpoint**: `POST /api/init/data?count=<number>`

**Important**: This endpoint **ADDS** turbines to existing ones; it does **NOT** replace them.

### Process Flow

1. **Farm Creation** (if needed):
   - Checks if farms exist
   - Creates "Wind Farm Alpha" (North Region) and "Wind Farm Beta" (South Region) if missing
   - Ensures at least 2 farms exist

2. **Turbine Creation** (Batch Process):
   ```java
   long existingTurbines = turbineRepository.count();  // Get current count
   int batchSize = 500;  // Process 500 turbines at a time
   
   for (int i = 1; i <= count; i++) {
       // Round-robin distribution across farms
       Farm farm = farms.get((i - 1) % farms.size());
       
       // Every 5th turbine is in MAINTENANCE, others are ACTIVE
       String status = (i % 5 == 0) ? "MAINTENANCE" : "ACTIVE";
       
       // Unique ID: TURB-00001, TURB-00002, etc.
       String turbineId = "TURB-" + String.format("%05d", (existingTurbines + i));
       
       // Random rated power: 2.5-4.5 MW
       double ratedPower = 2.5 + (random.nextDouble() * 2.0);
       
       Turbine turbine = new Turbine(turbineId, name, farm, ratedPower, status);
       turbines.add(turbine);
       
       // Batch save every 500 turbines
       if (turbines.size() >= batchSize) {
           turbineRepository.saveAll(turbines);  // Single DB operation
           turbines.clear();
       }
   }
   ```

3. **Initial Telemetry Generation**:
   - Creates 10 telemetry records per turbine
   - Each record has timestamp going back 10 minutes (10 records × 10 minutes = 100 minutes of history)
   - Uses same batch processing (500 records per batch)

### Example Commands

**Add 2200 turbines** (if you have 140 existing, this adds 2200 more = 2340 total):
```bash
curl -X POST "http://localhost:8080/api/init/data?count=2200"
```

**Add exactly 2200 total** (if you have 140, add 2060):
```bash
curl -X POST "http://localhost:8080/api/init/data?count=2060"
```

**Clear all data and start fresh**:
```bash
curl -X DELETE "http://localhost:8080/api/init/data"
curl -X POST "http://localhost:8080/api/init/data?count=2200"
```

### Batch Processing Benefits

- **Performance**: Instead of 2200 individual database inserts, only ~5 batch operations (2200 ÷ 500 = 4.4)
- **Transaction Efficiency**: Fewer database round-trips
- **Memory Management**: Processes in chunks, preventing memory overflow

---

## Telemetry Data Generation

### Automatic Generation (Scheduled Job)

**Frequency**: Every 10 seconds (`@Scheduled(fixedRate = 10000)`)

**Process**:
```java
@Scheduled(fixedRate = 10000)  // 10 seconds = 10000 milliseconds
public void generateTelemetryData() {
    // 1. Get all ACTIVE turbines (excludes MAINTENANCE turbines)
    List<Turbine> activeTurbines = turbineRepository.findAll()
        .stream()
        .filter(t -> "ACTIVE".equals(t.getStatus()))
        .toList();
    
    // 2. Generate telemetry for each active turbine
    for (Turbine turbine : activeTurbines) {
        // Random realistic values
        double windSpeed = 8.0 + random.nextDouble() * 12.0;      // 8-20 m/s
        double powerOutput = 1.5 + random.nextDouble() * 2.0;     // 1.5-3.5 MW
        double rotorSpeed = 10.0 + random.nextDouble() * 10.0;   // 10-20 RPM
        double temperature = 15.0 + random.nextDouble() * 20.0;  // 15-35°C
        double vibration = 2.0 + random.nextDouble() * 5.0;       // 2-7 mm/s
        
        // Calculate efficiency: (actual power / rated power) × 100%
        double efficiency = (powerOutput / turbine.getRatedPower()) * 100.0;
        
        Telemetry telemetry = new Telemetry(
            turbine, 
            LocalDateTime.now(),  // Current timestamp
            windSpeed, 
            powerOutput, 
            rotorSpeed, 
            temperature, 
            vibration, 
            efficiency
        );
        telemetry.setIsAggregated(false);  // Mark as unaggregated
        
        telemetryBatch.add(telemetry);
        
        // Batch save every 500 records
        if (telemetryBatch.size() >= BATCH_SIZE) {
            telemetryRepository.saveAll(telemetryBatch);
            telemetryBatch.clear();
        }
    }
}
```

### Telemetry Data Structure

Each telemetry record contains:
- **Turbine**: Reference to the turbine
- **Timestamp**: Exact time of measurement
- **Wind Speed**: 8-20 m/s (random)
- **Power Output**: 1.5-3.5 MW (random)
- **Rotor Speed**: 10-20 RPM (random)
- **Temperature**: 15-35°C (random)
- **Vibration**: 2-7 mm/s (random)
- **Efficiency**: Calculated as `(powerOutput / ratedPower) × 100%`
- **IsAggregated**: `false` initially, set to `true` after aggregation

### Generation Rate

- **Per Turbine**: 1 record every 10 seconds
- **Per Hour**: 360 records per turbine (60 minutes × 6 records/minute)
- **For 2200 Turbines**: 792,000 records per hour (2200 × 360)

### Batch Processing

- **Batch Size**: 500 records
- **Save Frequency**: Every 500 records or at the end
- **Why Batch**: Reduces database operations from thousands to dozens

---

## Data Aggregation

### Purpose

Raw telemetry data is too granular for analytics. Aggregation:
- **Reduces Data Volume**: 360 records/hour → 1 aggregate record/hour
- **Improves Query Performance**: Faster analytics queries
- **Enables Historical Analysis**: Daily, weekly, monthly trends

### Aggregation Process

**Frequency**: Every hour (runs at minute 0 of each hour)

**Process Flow**:

1. **Trigger**: `TelemetryAggregationScheduler` runs every hour
   ```java
   @Scheduled(fixedRate = 3600000)  // 1 hour = 3600000 milliseconds
   public void aggregatePreviousHour() {
       LocalDateTime previousHour = LocalDateTime.now()
           .truncatedTo(ChronoUnit.HOURS)
           .minusHours(1);
       
       aggregationService.aggregateTelemetryForHourParallel(previousHour);
   }
   ```

2. **Initial Aggregation on Startup**:
   - Aggregates last 24 hours of unaggregated telemetry
   - Ensures system has historical data when starting

3. **Aggregation Logic** (`AggregationService.aggregateTelemetryForHourParallel`):

   ```java
   public void aggregateTelemetryForHourParallel(LocalDateTime hourStart) {
       LocalDateTime hourEnd = hourStart.plusHours(1);
       
       // 1. Get all turbines
       List<Turbine> turbines = turbineRepository.findAll();
       
       // 2. Group telemetry by turbine
       Map<Long, List<Telemetry>> telemetryByTurbine = new HashMap<>();
       for (Turbine turbine : turbines) {
           // Get unaggregated telemetry for this hour
           List<Telemetry> telemetryList = telemetryRepository
               .findUnaggregatedByTurbineAndTimeRange(
                   turbine.getId(), 
                   hourStart, 
                   hourEnd
               );
           if (!telemetryList.isEmpty()) {
               telemetryByTurbine.put(turbine.getId(), telemetryList);
           }
       }
       
       // 3. Create aggregates (batch process)
       List<TelemetryAggregate> aggregatesToSave = new ArrayList<>();
       List<Telemetry> telemetryToUpdate = new ArrayList<>();
       int batchSize = 500;
       
       for (Map.Entry<Long, List<Telemetry>> entry : telemetryByTurbine.entrySet()) {
           Turbine turbine = turbineRepository.findById(entry.getKey()).get();
           List<Telemetry> telemetryList = entry.getValue();
           
           // Calculate aggregate values
           TelemetryAggregate aggregate = createAggregate(turbine, hourStart, telemetryList);
           aggregatesToSave.add(aggregate);
           
           // Mark telemetry as aggregated
           telemetryList.forEach(t -> t.setIsAggregated(true));
           telemetryToUpdate.addAll(telemetryList);
           
           // Batch save
           if (aggregatesToSave.size() >= batchSize) {
               aggregateRepository.saveAll(aggregatesToSave);
               aggregatesToSave.clear();
           }
           
           if (telemetryToUpdate.size() >= batchSize) {
               telemetryRepository.saveAll(telemetryToUpdate);
               telemetryToUpdate.clear();
           }
       }
       
       // Save remaining
       if (!aggregatesToSave.isEmpty()) {
           aggregateRepository.saveAll(aggregatesToSave);
       }
       if (!telemetryToUpdate.isEmpty()) {
           telemetryRepository.saveAll(telemetryToUpdate);
       }
   }
   ```

4. **Aggregate Calculation** (`createAggregate`):

   ```java
   private TelemetryAggregate createAggregate(Turbine turbine, LocalDateTime hourStart, List<Telemetry> telemetryList) {
       int count = telemetryList.size();
       
       // Sum all values
       double sumWindSpeed = telemetryList.stream().mapToDouble(Telemetry::getWindSpeed).sum();
       double sumPowerOutput = telemetryList.stream().mapToDouble(Telemetry::getPowerOutput).sum();
       double sumRotorSpeed = telemetryList.stream().mapToDouble(Telemetry::getRotorSpeed).sum();
       double sumTemperature = telemetryList.stream().mapToDouble(Telemetry::getTemperature).sum();
       double sumVibration = telemetryList.stream().mapToDouble(Telemetry::getVibration).sum();
       double sumEfficiency = telemetryList.stream().mapToDouble(Telemetry::getEfficiency).sum();
       
       // Calculate averages
       aggregate.setAvgWindSpeed(sumWindSpeed / count);
       aggregate.setAvgPowerOutput(sumPowerOutput / count);
       aggregate.setAvgRotorSpeed(sumRotorSpeed / count);
       aggregate.setAvgTemperature(sumTemperature / count);
       aggregate.setAvgVibration(sumVibration / count);
       aggregate.setAvgEfficiency(sumEfficiency / count);
       
       // Total generation: sum of power outputs × (10 seconds / 3600 seconds per hour)
       // Each telemetry record represents 10 seconds of operation
       double totalGeneration = sumPowerOutput * (10.0 / 3600.0);  // Convert to kWh
       aggregate.setTotalGeneration(totalGeneration);
       
       aggregate.setDataPointCount(count);  // Number of telemetry records aggregated
       
       // Anomaly detection
       boolean hasAnomaly = anomalyService.detectAnomaly(aggregate);
       aggregate.setHasAnomaly(hasAnomaly);
       
       return aggregate;
   }
   ```

### Aggregate Data Structure

Each `TelemetryAggregate` contains:
- **Turbine**: Reference to the turbine
- **Hour Start**: Beginning of the hour (e.g., 2026-02-01 14:00:00)
- **Avg Wind Speed**: Average of all telemetry records in that hour
- **Avg Power Output**: Average power output (MW)
- **Avg Rotor Speed**: Average rotor speed (RPM)
- **Avg Temperature**: Average temperature (°C)
- **Avg Vibration**: Average vibration (mm/s)
- **Avg Efficiency**: Average efficiency (%)
- **Total Generation**: Sum of power outputs converted to kWh
- **Data Point Count**: Number of telemetry records aggregated (typically 360)
- **Has Anomaly**: Boolean flag for anomaly detection

### Aggregation Timeline

- **Hour 0**: Telemetry generated every 10 seconds
- **Hour 1**: At minute 0, aggregate Hour 0's telemetry
- **Hour 2**: At minute 0, aggregate Hour 1's telemetry
- **And so on...**

### Manual Aggregation

You can manually trigger aggregation for the previous hour:
```bash
curl -X POST "http://localhost:8080/api/init/aggregate"
```

---

## Batch Processing

### What is Batch Processing?

Instead of saving records one-by-one, batch processing groups multiple records and saves them together in a single database operation.

### Why Use Batch Processing?

**Without Batch Processing** (2200 turbines):
- 2200 individual `INSERT` statements
- 2200 database round-trips
- Slow performance (several minutes)

**With Batch Processing** (2200 turbines, batch size 500):
- 5 batch `INSERT` operations (2200 ÷ 500 = 4.4, rounded up to 5)
- 5 database round-trips
- Fast performance (seconds)

### Batch Size Configuration

**In Code**:
```java
int batchSize = 500;  // Process 500 records at a time
```

**In Database** (`application.yaml`):
```yaml
spring:
  jpa:
    properties:
      hibernate:
        jdbc:
          batch_size: 500        # Number of records per batch
        order_inserts: true      # Optimize INSERT statements
        order_updates: true      # Optimize UPDATE statements
```

### Batch Processing Locations

1. **Turbine Creation**: 500 turbines per batch
2. **Telemetry Generation**: 500 telemetry records per batch
3. **Aggregation**: 500 aggregates per batch, 500 telemetry updates per batch

### Performance Impact

| Operation | Without Batch | With Batch (500) | Improvement |
|-----------|---------------|------------------|-------------|
| Add 2200 turbines | ~2200 DB calls | ~5 DB calls | **440x faster** |
| Generate telemetry (2200 turbines) | ~2200 DB calls | ~5 DB calls | **440x faster** |
| Aggregate 1 hour (2200 turbines) | ~4400 DB calls | ~9 DB calls | **489x faster** |

---

## Data Flow Architecture

### Complete Data Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                    TURBINE CREATION                              │
│  POST /api/init/data?count=2200                                 │
│  └─> Creates 2200 turbines (batch: 500)                         │
│      └─> Creates 10 initial telemetry records per turbine       │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│              TELEMETRY GENERATION (Every 10 seconds)            │
│  TelemetryGeneratorScheduler                                     │
│  └─> Generates 1 telemetry record per ACTIVE turbine            │
│      └─> Saves in batches of 500                                │
│          └─> Sets isAggregated = false                           │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│              DATA STORAGE (MySQL)                                │
│  telemetry table:                                               │
│  - Raw sensor data (every 10 seconds)                           │
│  - isAggregated = false (new records)                           │
│  - isAggregated = true (aggregated records)                     │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│              AGGREGATION (Every hour)                             │
│  TelemetryAggregationScheduler                                   │
│  └─> Finds unaggregated telemetry (isAggregated = false)        │
│      └─> Groups by turbine and hour                             │
│          └─> Calculates averages and totals                     │
│              └─> Creates TelemetryAggregate records              │
│                  └─> Marks telemetry as aggregated (true)        │
│                      └─> Saves in batches of 500                 │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│              AGGREGATE STORAGE (MySQL)                            │
│  telemetry_aggregate table:                                     │
│  - Hourly aggregated data                                       │
│  - 1 record per turbine per hour                                │
│  - Used for analytics and reporting                             │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│              FRONTEND ANALYTICS                                  │
│  GET /api/analytics/aggregates                                  │
│  └─> Fetches aggregates for date range                          │
│      └─> Frontend groups by date and farm                       │
│          └─> Displays in table and graph                         │
└─────────────────────────────────────────────────────────────────┘
```

### Data Volume Example (2200 Turbines)

**Raw Telemetry** (every 10 seconds):
- Per hour: 2200 turbines × 360 records = **792,000 records**
- Per day: 792,000 × 24 = **19,008,000 records**

**Aggregates** (hourly):
- Per hour: 2200 turbines × 1 record = **2,200 records**
- Per day: 2,200 × 24 = **52,800 records**

**Reduction**: 19,008,000 → 52,800 = **99.7% reduction** in data volume for analytics!

---

## API Endpoints

### Initialization Endpoints

**Add Turbines**:
```
POST /api/init/data?count=<number>
```
- Adds turbines to existing ones (doesn't replace)
- Creates farms if needed
- Generates initial telemetry (10 records per turbine)
- Returns: `{ success: true, turbinesCreated: <count>, telemetryRecordsCreated: <count> }`

**Clear All Data**:
```
DELETE /api/init/data
```
- Deletes all telemetry, turbines, and farms
- Returns: `{ success: true, deleted: { telemetry: <count>, turbines: <count>, farms: <count> } }`

**Manual Aggregation**:
```
POST /api/init/aggregate
```
- Manually triggers aggregation for the previous hour
- Useful if aggregation didn't run automatically
- Returns: `{ success: true, message: "...", hour: "..." }`

### Analytics Endpoints

**Get Aggregates**:
```
GET /api/analytics/aggregates?startTime=<ISO>&endTime=<ISO>&farmName=<name>&region=<region>
```
- Fetches hourly aggregates for date range
- Optional filters: farm name, region
- Returns: Array of `TelemetryAggregate` objects

**Get Turbines**:
```
GET /api/turbines?page=<number>&size=<number>&farm=<name>&region=<region>&status=<status>
```
- Paginated list of turbines
- Optional filters: farm, region, status

**Get Alerts**:
```
GET /api/health/alerts?page=<number>&size=<number>
```
- Paginated list of health alerts
- Returns active alerts with pagination

---

## Troubleshooting

### Issue: Analytics showing all zeros

**Cause**: Aggregation hasn't run yet for new telemetry data.

**Solution**:
1. Wait for the next hour (aggregation runs automatically)
2. Or manually trigger: `curl -X POST "http://localhost:8080/api/init/aggregate"`

### Issue: Graph not showing data

**Cause**: 
- No aggregates exist for the selected date range
- Date range doesn't include days with data

**Solution**:
1. Check if aggregates exist: `GET /api/analytics/aggregates?startTime=...&endTime=...`
2. Ensure aggregation has run for the time period
3. Check browser console for errors

### Issue: Telemetry not generating

**Cause**: 
- No ACTIVE turbines exist
- Scheduler not running

**Solution**:
1. Check turbine status: `GET /api/turbines`
2. Ensure some turbines have status "ACTIVE"
3. Check application logs for scheduler execution

### Issue: Slow performance with many turbines

**Cause**: Batch processing not configured correctly.

**Solution**:
1. Verify `application.yaml` has batch settings:
   ```yaml
   hibernate:
     jdbc:
       batch_size: 500
   ```
2. Check batch size in code matches configuration
3. Monitor database connection pool size

### Issue: Duplicate turbines after adding

**Cause**: Endpoint adds to existing, doesn't replace.

**Solution**:
- Calculate: `count = desiredTotal - existingCount`
- Or clear data first: `DELETE /api/init/data`, then `POST /api/init/data?count=<desired>`

---

## Summary

### Key Concepts

1. **Batch Processing**: Groups operations (500 records) for performance
2. **Telemetry Generation**: Every 10 seconds for ACTIVE turbines
3. **Aggregation**: Every hour, converts 360 telemetry records → 1 aggregate
4. **Data Reduction**: 99.7% reduction in data volume for analytics
5. **Scalability**: Handles thousands of turbines efficiently

### Performance Metrics

- **Turbine Creation**: ~5 seconds for 2200 turbines (with batch processing)
- **Telemetry Generation**: ~1 second per batch (500 records)
- **Aggregation**: ~2-5 seconds per hour (2200 turbines)
- **Query Performance**: Milliseconds for aggregate queries (vs seconds for raw telemetry)

### Best Practices

1. **Always use batch processing** for bulk operations
2. **Monitor aggregation** - ensure it runs every hour
3. **Use aggregates for analytics** - don't query raw telemetry
4. **Clear data before large imports** if you want exact counts
5. **Check turbine status** - only ACTIVE turbines generate telemetry

---

**End of Documentation**


