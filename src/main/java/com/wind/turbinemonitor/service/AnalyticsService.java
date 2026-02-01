package com.wind.turbinemonitor.service;

import com.wind.turbinemonitor.dto.DailyMetricsDTO;
import com.wind.turbinemonitor.dto.GraphDataDTO;
import com.wind.turbinemonitor.model.TelemetryAggregate;
import com.wind.turbinemonitor.repository.TelemetryAggregateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AnalyticsService {
    @Autowired
    private TelemetryAggregateRepository aggregateRepository;
    
    public Map<String, Object> getDailyMetrics(Long turbineId, LocalDate date) {
        LocalDateTime startTime = date.atStartOfDay();
        LocalDateTime endTime = date.atTime(LocalTime.MAX);
        
        List<TelemetryAggregate> aggregates = aggregateRepository
            .findByTurbineAndDateRange(turbineId, startTime, endTime);
        
        Map<String, Object> metrics = new HashMap<>();
        
        if (aggregates.isEmpty()) {
            metrics.put("totalGeneration", 0.0);
            metrics.put("avgEfficiency", 0.0);
            metrics.put("avgPowerOutput", 0.0);
            metrics.put("hourCount", 0);
            return metrics;
        }
        
        double totalGeneration = aggregates.stream()
            .mapToDouble(TelemetryAggregate::getTotalGeneration)
            .sum();
        
        double avgEfficiency = aggregates.stream()
            .mapToDouble(TelemetryAggregate::getAvgEfficiency)
            .average()
            .orElse(0.0);
        
        double avgPowerOutput = aggregates.stream()
            .mapToDouble(TelemetryAggregate::getAvgPowerOutput)
            .average()
            .orElse(0.0);
        
        metrics.put("totalGeneration", totalGeneration);
        metrics.put("avgEfficiency", avgEfficiency);
        metrics.put("avgPowerOutput", avgPowerOutput);
        metrics.put("hourCount", aggregates.size());
        metrics.put("date", date.toString());
        
        return metrics;
    }
    
    public Map<String, Object> getHistoricalPerformance(Long turbineId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startTime = startDate.atStartOfDay();
        LocalDateTime endTime = endDate.atTime(LocalTime.MAX);
        
        List<TelemetryAggregate> aggregates = aggregateRepository
            .findByTurbineAndDateRange(turbineId, startTime, endTime);
        
        Map<String, Object> performance = new HashMap<>();
        
        if (aggregates.isEmpty()) {
            performance.put("totalGeneration", 0.0);
            performance.put("avgEfficiency", 0.0);
            performance.put("avgPowerOutput", 0.0);
            performance.put("dataPoints", 0);
            return performance;
        }
        
        double totalGeneration = aggregates.stream()
            .mapToDouble(TelemetryAggregate::getTotalGeneration)
            .sum();
        
        double avgEfficiency = aggregates.stream()
            .mapToDouble(TelemetryAggregate::getAvgEfficiency)
            .average()
            .orElse(0.0);
        
        double avgPowerOutput = aggregates.stream()
            .mapToDouble(TelemetryAggregate::getAvgPowerOutput)
            .average()
            .orElse(0.0);
        
        performance.put("totalGeneration", totalGeneration);
        performance.put("avgEfficiency", avgEfficiency);
        performance.put("avgPowerOutput", avgPowerOutput);
        performance.put("dataPoints", aggregates.size());
        performance.put("startDate", startDate.toString());
        performance.put("endDate", endDate.toString());
        
        return performance;
    }
    
    public List<DailyMetricsDTO> getDailyMetrics(LocalDateTime startTime, LocalDateTime endTime, String farmName, String region) {
        // Get all aggregates for the date range with filters
        List<TelemetryAggregate> aggregates;
        if (farmName != null || region != null) {
            aggregates = aggregateRepository.findByDateRangeAndFilters(startTime, endTime, farmName, region);
        } else {
            aggregates = aggregateRepository.findByDateRange(startTime, endTime);
        }
        
        // Group by date and farm
        Map<String, Map<String, DailyFarmData>> dailyFarmMap = new HashMap<>();
        
        for (TelemetryAggregate agg : aggregates) {
            String dateKey = agg.getHourStart().toLocalDate().toString();
            String farm = agg.getTurbine().getFarm().getName();
            
            dailyFarmMap.putIfAbsent(dateKey, new HashMap<>());
            Map<String, DailyFarmData> farmMap = dailyFarmMap.get(dateKey);
            farmMap.putIfAbsent(farm, new DailyFarmData());
            
            DailyFarmData farmData = farmMap.get(farm);
            farmData.totalGeneration += agg.getTotalGeneration() != null ? agg.getTotalGeneration() : 0.0;
            farmData.sumEfficiency += agg.getAvgEfficiency() != null ? agg.getAvgEfficiency() : 0.0;
            farmData.count += 1;
            farmData.maxPower += (agg.getAvgPowerOutput() != null ? agg.getAvgPowerOutput() : 0.0) * 1000; // Convert MW to kW
        }
        
        // Create DTOs: one per farm per day, plus "All Farms" per day
        List<DailyMetricsDTO> result = new ArrayList<>();
        
        // Get all unique dates
        Set<String> dates = new TreeSet<>(dailyFarmMap.keySet());
        
        for (String dateKey : dates) {
            Map<String, DailyFarmData> farmMap = dailyFarmMap.get(dateKey);
            
            // Calculate "All Farms" totals
            DailyFarmData allFarmsData = new DailyFarmData();
            for (DailyFarmData farmData : farmMap.values()) {
                allFarmsData.totalGeneration += farmData.totalGeneration;
                allFarmsData.sumEfficiency += farmData.sumEfficiency;
                allFarmsData.count += farmData.count;
                allFarmsData.maxPower += farmData.maxPower;
            }
            
            // Add "All Farms" row first
            if (allFarmsData.count > 0) {
                result.add(new DailyMetricsDTO(
                    dateKey,
                    "All Farms",
                    allFarmsData.totalGeneration,
                    allFarmsData.count > 0 ? allFarmsData.sumEfficiency / allFarmsData.count : 0.0,
                    allFarmsData.count,
                    allFarmsData.maxPower
                ));
            }
            
            // Add individual farm rows
            List<String> sortedFarms = new ArrayList<>(farmMap.keySet());
            Collections.sort(sortedFarms);
            
            for (String farm : sortedFarms) {
                DailyFarmData farmData = farmMap.get(farm);
                if (farmData.count > 0) {
                    result.add(new DailyMetricsDTO(
                        dateKey,
                        farm,
                        farmData.totalGeneration,
                        farmData.sumEfficiency / farmData.count,
                        farmData.count,
                        farmData.maxPower
                    ));
                }
            }
        }
        
        return result;
    }
    
    public List<GraphDataDTO> getGraphData(LocalDateTime startTime, LocalDateTime endTime, String farmName, String region) {
        // Get daily metrics
        List<DailyMetricsDTO> dailyMetrics = getDailyMetrics(startTime, endTime, farmName, region);
        
        // Filter to only "All Farms" rows and extract for graph
        Map<String, GraphDataDTO> graphMap = new LinkedHashMap<>();
        
        // First, populate with "All Farms" data
        for (DailyMetricsDTO metric : dailyMetrics) {
            if ("All Farms".equals(metric.getFarm())) {
                graphMap.put(metric.getDate(), new GraphDataDTO(
                    metric.getDate(),
                    metric.getTotalGeneration(),
                    metric.getAvgEfficiency()
                ));
            }
        }
        
        // Fill in missing dates with zeros
        LocalDate startDate = startTime.toLocalDate();
        LocalDate endDate = endTime.toLocalDate();
        LocalDate currentDate = startDate;
        
        List<GraphDataDTO> result = new ArrayList<>();
        while (!currentDate.isAfter(endDate)) {
            String dateKey = currentDate.toString();
            GraphDataDTO data = graphMap.get(dateKey);
            if (data != null) {
                result.add(data);
            } else {
                result.add(new GraphDataDTO(dateKey, 0.0, 0.0));
            }
            currentDate = currentDate.plusDays(1);
        }
        
        return result;
    }
    
    // Helper class for intermediate calculations
    private static class DailyFarmData {
        double totalGeneration = 0.0;
        double sumEfficiency = 0.0;
        int count = 0;
        double maxPower = 0.0;
    }
}

