package com.wind.turbinemonitor.service;

import com.wind.turbinemonitor.model.Telemetry;
import com.wind.turbinemonitor.model.Turbine;
import com.wind.turbinemonitor.repository.TelemetryRepository;
import com.wind.turbinemonitor.repository.TurbineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TelemetryService {
    @Autowired
    private TelemetryRepository telemetryRepository;
    
    @Autowired
    private TurbineRepository turbineRepository;
    
    public Telemetry saveTelemetry(Telemetry telemetry) {
        return telemetryRepository.save(telemetry);
    }
    
    public List<Telemetry> saveTelemetryBatch(List<Telemetry> telemetryList) {
        return telemetryRepository.saveAll(telemetryList);
    }
    
    @Transactional
    public void saveTelemetryBatchOptimized(List<Telemetry> telemetryList) {
        int batchSize = 500;
        List<Telemetry> batch = new ArrayList<>();
        
        for (Telemetry telemetry : telemetryList) {
            batch.add(telemetry);
            
            if (batch.size() >= batchSize) {
                telemetryRepository.saveAll(batch);
                batch.clear();
            }
        }
        
        if (!batch.isEmpty()) {
            telemetryRepository.saveAll(batch);
        }
    }
    
    public List<Telemetry> getTelemetryByTurbine(Long turbineId) {
        return telemetryRepository.findByTurbineId(turbineId);
    }
    
    public List<Telemetry> getTelemetryByTurbineAndDateRange(Long turbineId, LocalDateTime startTime, LocalDateTime endTime) {
        return telemetryRepository.findByTurbineAndDateRange(turbineId, startTime, endTime);
    }
    
    public List<Telemetry> getUnaggregatedTelemetryByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return telemetryRepository.findUnaggregatedByTimeRange(startTime, endTime);
    }
    
    public List<Telemetry> getUnaggregatedTelemetryByTurbineAndTimeRange(Long turbineId, LocalDateTime startTime, LocalDateTime endTime) {
        return telemetryRepository.findUnaggregatedByTurbineAndTimeRange(turbineId, startTime, endTime);
    }
    
    
    public Telemetry createTelemetry(Long turbineId, Double windSpeed, Double powerOutput, 
                                    Double rotorSpeed, Double temperature, Double vibration) {
        Optional<Turbine> turbineOpt = turbineRepository.findById(turbineId);
        if (turbineOpt.isEmpty()) {
            throw new RuntimeException("Turbine not found with id: " + turbineId);
        }
        
        Turbine turbine = turbineOpt.get();
        Double efficiency = (powerOutput / turbine.getRatedPower()) * 100.0;
        
        Telemetry telemetry = new Telemetry(turbine, LocalDateTime.now(), windSpeed, 
                                           powerOutput, rotorSpeed, temperature, vibration, efficiency);
        return telemetryRepository.save(telemetry);
    }
}

