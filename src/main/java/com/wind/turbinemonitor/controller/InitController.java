package com.wind.turbinemonitor.controller;

import com.wind.turbinemonitor.model.Farm;
import com.wind.turbinemonitor.model.Telemetry;
import com.wind.turbinemonitor.model.Turbine;
import com.wind.turbinemonitor.repository.FarmRepository;
import com.wind.turbinemonitor.repository.TelemetryRepository;
import com.wind.turbinemonitor.repository.TurbineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/init")
@CrossOrigin(origins = "*")
public class InitController {
    
    @GetMapping
    public ResponseEntity<Map<String, String>> info() {
        Map<String, String> info = new HashMap<>();
        info.put("message", "Use POST /api/init/data to initialize 10 turbines");
        info.put("endpoint", "/api/init/data");
        return ResponseEntity.ok(info);
    }
    @Autowired
    private FarmRepository farmRepository;
    
    @Autowired
    private TurbineRepository turbineRepository;
    
    @Autowired
    private TelemetryRepository telemetryRepository;
    
    @Autowired
    private com.wind.turbinemonitor.service.AggregationService aggregationService;
    
    @PostMapping("/aggregate")
    public ResponseEntity<Map<String, Object>> triggerAggregation() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            java.time.LocalDateTime now = java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.HOURS);
            java.time.LocalDateTime previousHour = now.minusHours(1);
            
            aggregationService.aggregateTelemetryForHourParallel(previousHour);
            
            response.put("success", true);
            response.put("message", "Aggregation triggered for hour: " + previousHour);
            response.put("hour", previousHour.toString());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @PostMapping("/data")
    public ResponseEntity<Map<String, Object>> initializeData(@RequestParam(required = false, defaultValue = "10") int count) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (count < 1 || count > 10000) {
                response.put("success", false);
                response.put("error", "Count must be between 1 and 10000");
                return ResponseEntity.badRequest().body(response);
            }
            
            List<Farm> farms = farmRepository.findAll();
            
            // Ensure we have at least 2 farms
            if (farms.isEmpty()) {
                Farm farm1 = new Farm("Wind Farm Alpha", "North Region", "Location A");
                Farm farm2 = new Farm("Wind Farm Beta", "South Region", "Location B");
                farm1 = farmRepository.save(farm1);
                farm2 = farmRepository.save(farm2);
                farms.add(farm1);
                farms.add(farm2);
                response.put("farmsCreated", 2);
            } else if (farms.size() == 1) {
                Farm farm2 = new Farm("Wind Farm Beta", "South Region", "Location B");
                farm2 = farmRepository.save(farm2);
                farms.add(farm2);
                response.put("farmsCreated", 1);
            } else {
                response.put("farmsCreated", 0);
            }
            
            long existingTurbines = turbineRepository.count();
            List<Turbine> turbines = new ArrayList<>();
            int batchSize = 500;
            Random random = new Random();
            
            // Distribute turbines across farms
            for (int i = 1; i <= count; i++) {
                Farm farm = farms.get((i - 1) % farms.size()); // Round-robin distribution
                String status = (i % 5 == 0) ? "MAINTENANCE" : "ACTIVE";
                String turbineId = "TURB-" + String.format("%05d", (int)(existingTurbines + i));
                double ratedPower = 2.5 + (random.nextDouble() * 2.0); // Random between 2.5-4.5 MW
                
                Turbine turbine = new Turbine(turbineId, 
                    "Turbine " + (int)(existingTurbines + i), farm, ratedPower, status);
                turbines.add(turbine);
                
                // Batch save for performance
                if (turbines.size() >= batchSize) {
                    turbineRepository.saveAll(turbines);
                    turbines.clear();
                }
            }
            
            // Save remaining turbines and get all newly created turbines
            if (!turbines.isEmpty()) {
                turbines = new ArrayList<>(turbineRepository.saveAll(turbines));
            }
            
            // Get all newly created turbines (those created in this batch)
            List<Turbine> newlyCreatedTurbines = turbineRepository.findAll().stream()
                .skip(existingTurbines)
                .limit(count)
                .collect(java.util.stream.Collectors.toList());
            
            // Generate initial telemetry data (10 records per turbine)
            LocalDateTime now = LocalDateTime.now();
            List<Telemetry> allTelemetry = new ArrayList<>();
            int telemetryCount = 0;
            
            for (Turbine turbine : newlyCreatedTurbines) {
                for (int j = 0; j < 10; j++) {
                    LocalDateTime timestamp = now.minusMinutes(j * 10);
                    double windSpeed = 8.0 + random.nextDouble() * 12.0;
                    double powerOutput = 1.5 + random.nextDouble() * 2.0;
                    double rotorSpeed = 10.0 + random.nextDouble() * 10.0;
                    double temperature = 15.0 + random.nextDouble() * 20.0;
                    double vibration = 2.0 + random.nextDouble() * 5.0;
                    double efficiency = (powerOutput / turbine.getRatedPower()) * 100.0;
                    
                    Telemetry telemetry = new Telemetry(turbine, timestamp, windSpeed, 
                        powerOutput, rotorSpeed, temperature, vibration, efficiency);
                    allTelemetry.add(telemetry);
                    telemetryCount++;
                    
                    // Batch save telemetry
                    if (allTelemetry.size() >= batchSize) {
                        telemetryRepository.saveAll(allTelemetry);
                        allTelemetry.clear();
                    }
                }
            }
            
            if (!allTelemetry.isEmpty()) {
                telemetryRepository.saveAll(allTelemetry);
            }
            
            response.put("success", true);
            response.put("turbinesCreated", count);
            response.put("telemetryRecordsCreated", telemetryCount);
            response.put("message", "Successfully created " + count + " turbines with telemetry data");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @DeleteMapping("/data")
    public ResponseEntity<Map<String, Object>> clearData() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            long telemetryCount = telemetryRepository.count();
            long turbineCount = turbineRepository.count();
            long farmCount = farmRepository.count();
            
            telemetryRepository.deleteAll();
            turbineRepository.deleteAll();
            farmRepository.deleteAll();
            
            response.put("success", true);
            response.put("deleted", Map.of(
                "telemetry", telemetryCount,
                "turbines", turbineCount,
                "farms", farmCount
            ));
            response.put("message", "All data cleared successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}

