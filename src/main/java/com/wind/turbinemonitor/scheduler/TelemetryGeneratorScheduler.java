package com.wind.turbinemonitor.scheduler;

import com.wind.turbinemonitor.model.Telemetry;
import com.wind.turbinemonitor.model.Turbine;
import com.wind.turbinemonitor.repository.TelemetryRepository;
import com.wind.turbinemonitor.repository.TurbineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class TelemetryGeneratorScheduler {
    @Autowired
    private TurbineRepository turbineRepository;
    
    @Autowired
    private TelemetryRepository telemetryRepository;
    
    private static final int BATCH_SIZE = 500;
    private final Random random = new Random();
    
    @Scheduled(fixedRate = 10000)
    @Transactional
    public void generateTelemetryData() {
        List<Turbine> activeTurbines = turbineRepository.findAll()
            .stream()
            .filter(t -> "ACTIVE".equals(t.getStatus()))
            .toList();
        
        if (activeTurbines.isEmpty()) {
            return;
        }
        
        LocalDateTime now = LocalDateTime.now();
        List<Telemetry> telemetryBatch = new ArrayList<>();
        
        for (Turbine turbine : activeTurbines) {
            double windSpeed = 8.0 + random.nextDouble() * 12.0;
            double powerOutput = 1.5 + random.nextDouble() * 2.0;
            double rotorSpeed = 10.0 + random.nextDouble() * 10.0;
            double temperature = 15.0 + random.nextDouble() * 20.0;
            double vibration = 2.0 + random.nextDouble() * 5.0;
            double efficiency = (powerOutput / turbine.getRatedPower()) * 100.0;
            
            Telemetry telemetry = new Telemetry(turbine, now, windSpeed, 
                powerOutput, rotorSpeed, temperature, vibration, efficiency);
            telemetry.setIsAggregated(false);
            telemetryBatch.add(telemetry);
            
            if (telemetryBatch.size() >= BATCH_SIZE) {
                telemetryRepository.saveAll(telemetryBatch);
                telemetryBatch.clear();
            }
        }
        
        if (!telemetryBatch.isEmpty()) {
            telemetryRepository.saveAll(telemetryBatch);
        }
        
        System.out.println("Generated telemetry for " + activeTurbines.size() + " turbines at " + now);
    }
}

