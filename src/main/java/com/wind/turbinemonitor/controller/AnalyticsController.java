package com.wind.turbinemonitor.controller;

import com.wind.turbinemonitor.model.TelemetryAggregate;
import com.wind.turbinemonitor.service.AnalyticsService;
import com.wind.turbinemonitor.service.AggregationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*")
public class AnalyticsController {
    @Autowired
    private AnalyticsService analyticsService;
    
    @Autowired
    private AggregationService aggregationService;
    
    @GetMapping("/turbine/{turbineId}/daily")
    public ResponseEntity<Map<String, Object>> getDailyMetrics(
            @PathVariable Long turbineId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(analyticsService.getDailyMetrics(turbineId, date));
    }
    
    @GetMapping("/turbine/{turbineId}/historical")
    public ResponseEntity<Map<String, Object>> getHistoricalPerformance(
            @PathVariable Long turbineId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(analyticsService.getHistoricalPerformance(turbineId, startDate, endDate));
    }
    
    @GetMapping("/turbine/{turbineId}/aggregates")
    public ResponseEntity<List<TelemetryAggregate>> getAggregatesByTurbine(
            @PathVariable Long turbineId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return ResponseEntity.ok(aggregationService.getAggregatesByTurbine(turbineId, startTime, endTime));
    }
    
    @GetMapping("/aggregates")
    public ResponseEntity<List<TelemetryAggregate>> getAggregatesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) String farm,
            @RequestParam(required = false) String region) {
        if (farm != null || region != null) {
            return ResponseEntity.ok(aggregationService.getAggregatesByFilters(startTime, endTime, farm, region));
        }
        return ResponseEntity.ok(aggregationService.getAggregatesByDateRange(startTime, endTime));
    }
    
    @GetMapping("/daily")
    public ResponseEntity<List<com.wind.turbinemonitor.dto.DailyMetricsDTO>> getDailyMetrics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) String farm,
            @RequestParam(required = false) String region) {
        return ResponseEntity.ok(analyticsService.getDailyMetrics(startTime, endTime, farm, region));
    }
    
    @GetMapping("/graph")
    public ResponseEntity<List<com.wind.turbinemonitor.dto.GraphDataDTO>> getGraphData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) String farm,
            @RequestParam(required = false) String region) {
        return ResponseEntity.ok(analyticsService.getGraphData(startTime, endTime, farm, region));
    }
}

