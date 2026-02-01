package com.wind.turbinemonitor.service;

import com.wind.turbinemonitor.model.Telemetry;
import com.wind.turbinemonitor.model.TelemetryAggregate;
import com.wind.turbinemonitor.model.Turbine;
import com.wind.turbinemonitor.repository.TelemetryAggregateRepository;
import com.wind.turbinemonitor.repository.TelemetryRepository;
import com.wind.turbinemonitor.repository.TurbineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AggregationService {
    @Autowired
    private TelemetryRepository telemetryRepository;
    
    @Autowired
    private TelemetryAggregateRepository aggregateRepository;
    
    @Autowired
    private TurbineRepository turbineRepository;
    
    @Autowired
    private AnomalyService anomalyService;
    
    public void aggregateTelemetryForHour(LocalDateTime hourStart) {
        LocalDateTime hourEnd = hourStart.plusHours(1);
        List<Turbine> turbines = turbineRepository.findAll();
        
        for (Turbine turbine : turbines) {
            List<Telemetry> telemetryList = telemetryRepository
                .findUnaggregatedByTurbineAndTimeRange(turbine.getId(), hourStart, hourEnd);
            
            if (!telemetryList.isEmpty()) {
                TelemetryAggregate aggregate = createAggregate(turbine, hourStart, telemetryList);
                aggregateRepository.save(aggregate);
                telemetryList.forEach(t -> t.setIsAggregated(true));
                telemetryRepository.saveAll(telemetryList);
            }
        }
    }
    
    public void aggregateTelemetryForHourParallel(LocalDateTime hourStart) {
        LocalDateTime hourEnd = hourStart.plusHours(1);
        List<Turbine> turbines = turbineRepository.findAll();
        
        Map<Long, List<Telemetry>> telemetryByTurbine = new HashMap<>();
        int totalTelemetryCount = 0;
        
        for (Turbine turbine : turbines) {
            List<Telemetry> telemetryList = telemetryRepository
                .findUnaggregatedByTurbineAndTimeRange(turbine.getId(), hourStart, hourEnd);
            if (!telemetryList.isEmpty()) {
                telemetryByTurbine.put(turbine.getId(), telemetryList);
                totalTelemetryCount += telemetryList.size();
            }
        }
        
        if (telemetryByTurbine.isEmpty()) {
            System.out.println("No unaggregated telemetry found for hour: " + hourStart);
            return;
        }
        
        System.out.println("Aggregating " + totalTelemetryCount + " telemetry records for " + 
                          telemetryByTurbine.size() + " turbines for hour: " + hourStart);
        
        List<TelemetryAggregate> aggregatesToSave = new ArrayList<>();
        List<Telemetry> telemetryToUpdate = new ArrayList<>();
        int batchSize = 500;
        
        for (Map.Entry<Long, List<Telemetry>> entry : telemetryByTurbine.entrySet()) {
            Long turbineId = entry.getKey();
            List<Telemetry> telemetryList = entry.getValue();
            Optional<Turbine> turbineOpt = turbineRepository.findById(turbineId);
            
            if (turbineOpt.isPresent()) {
                TelemetryAggregate aggregate = createAggregate(turbineOpt.get(), hourStart, telemetryList);
                aggregatesToSave.add(aggregate);
                telemetryList.forEach(t -> t.setIsAggregated(true));
                telemetryToUpdate.addAll(telemetryList);
                
                if (aggregatesToSave.size() >= batchSize) {
                    aggregateRepository.saveAll(aggregatesToSave);
                    aggregatesToSave.clear();
                }
                
                if (telemetryToUpdate.size() >= batchSize) {
                    telemetryRepository.saveAll(telemetryToUpdate);
                    telemetryToUpdate.clear();
                }
            }
        }
        
        int totalAggregates = aggregatesToSave.size();
        
        if (!aggregatesToSave.isEmpty()) {
            aggregateRepository.saveAll(aggregatesToSave);
        }
        
        if (!telemetryToUpdate.isEmpty()) {
            telemetryRepository.saveAll(telemetryToUpdate);
        }
        
        System.out.println("Created " + totalAggregates + " aggregates for hour: " + hourStart);
    }
    
    private TelemetryAggregate createAggregate(Turbine turbine, LocalDateTime hourStart, List<Telemetry> telemetryList) {
        TelemetryAggregate aggregate = new TelemetryAggregate(turbine, hourStart);
        
        int count = telemetryList.size();
        double sumWindSpeed = telemetryList.stream().mapToDouble(Telemetry::getWindSpeed).sum();
        double sumPowerOutput = telemetryList.stream().mapToDouble(Telemetry::getPowerOutput).sum();
        double sumRotorSpeed = telemetryList.stream().mapToDouble(Telemetry::getRotorSpeed).sum();
        double sumTemperature = telemetryList.stream().mapToDouble(Telemetry::getTemperature).sum();
        double sumVibration = telemetryList.stream().mapToDouble(Telemetry::getVibration).sum();
        double sumEfficiency = telemetryList.stream().mapToDouble(Telemetry::getEfficiency).sum();
        double totalGeneration = sumPowerOutput * (10.0 / 3600.0);
        
        aggregate.setAvgWindSpeed(sumWindSpeed / count);
        aggregate.setAvgPowerOutput(sumPowerOutput / count);
        aggregate.setAvgRotorSpeed(sumRotorSpeed / count);
        aggregate.setAvgTemperature(sumTemperature / count);
        aggregate.setAvgVibration(sumVibration / count);
        aggregate.setAvgEfficiency(sumEfficiency / count);
        aggregate.setTotalGeneration(totalGeneration);
        aggregate.setDataPointCount(count);
        
        boolean hasAnomaly = anomalyService.detectAnomaly(aggregate);
        aggregate.setHasAnomaly(hasAnomaly);
        
        return aggregate;
    }
    
    public List<TelemetryAggregate> getAggregatesByTurbine(Long turbineId, LocalDateTime startTime, LocalDateTime endTime) {
        return aggregateRepository.findByTurbineAndDateRange(turbineId, startTime, endTime);
    }
    
    public List<TelemetryAggregate> getAggregatesByDateRange(LocalDateTime startTime, LocalDateTime endTime) {
        return aggregateRepository.findByDateRange(startTime, endTime);
    }
    
    public List<TelemetryAggregate> getAggregatesByFilters(LocalDateTime startTime, LocalDateTime endTime, String farmName, String region) {
        return aggregateRepository.findByDateRangeAndFilters(startTime, endTime, farmName, region);
    }
}

