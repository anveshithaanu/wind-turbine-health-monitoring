package com.wind.turbinemonitor.config;

import com.wind.turbinemonitor.model.Farm;
import com.wind.turbinemonitor.model.Telemetry;
import com.wind.turbinemonitor.model.Turbine;
import com.wind.turbinemonitor.repository.FarmRepository;
import com.wind.turbinemonitor.repository.TelemetryRepository;
import com.wind.turbinemonitor.repository.TurbineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class DataInitializer implements CommandLineRunner {
    @Autowired
    private FarmRepository farmRepository;
    
    @Autowired
    private TurbineRepository turbineRepository;
    
    @Autowired
    private TelemetryRepository telemetryRepository;
    
    @Override
    public void run(String... args) throws Exception {
        if (farmRepository.count() == 0) {
            Farm farm1 = new Farm("Wind Farm Alpha", "North Region", "Location A");
            Farm farm2 = new Farm("Wind Farm Beta", "South Region", "Location B");
            farm1 = farmRepository.save(farm1);
            farm2 = farmRepository.save(farm2);
            
            if (turbineRepository.count() == 0) {
                List<Turbine> turbines = new ArrayList<>();
                
                for (int i = 1; i <= 10; i++) {
                    Farm farm = i <= 5 ? farm1 : farm2;
                    String status = i % 5 == 0 ? "MAINTENANCE" : "ACTIVE";
                    Turbine turbine = new Turbine("TURB-" + String.format("%04d", i), 
                        "Turbine " + i, farm, 2.5 + (i * 0.1), status);
                    turbines.add(turbineRepository.save(turbine));
                }
                
                Random random = new Random();
                LocalDateTime now = LocalDateTime.now();
                List<Telemetry> allTelemetry = new ArrayList<>();
                int batchSize = 500;
                
                for (Turbine turbine : turbines) {
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
                        
                        if (allTelemetry.size() >= batchSize) {
                            telemetryRepository.saveAll(allTelemetry);
                            allTelemetry.clear();
                        }
                    }
                }
                
                if (!allTelemetry.isEmpty()) {
                    telemetryRepository.saveAll(allTelemetry);
                }
            }
        }
    }
}

