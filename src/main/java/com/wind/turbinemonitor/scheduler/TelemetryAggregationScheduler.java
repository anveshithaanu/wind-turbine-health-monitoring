package com.wind.turbinemonitor.scheduler;

import com.wind.turbinemonitor.service.AggregationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
public class TelemetryAggregationScheduler {
    @Autowired
    private AggregationService aggregationService;
    
    @PostConstruct
    public void aggregateExistingTelemetryOnStartup() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS);
        LocalDateTime startHour = now.minusHours(24);
        
        System.out.println("Starting initial aggregation for hours from " + startHour + " to " + now);
        
        LocalDateTime currentHour = startHour;
        while (currentHour.isBefore(now)) {
            aggregationService.aggregateTelemetryForHourParallel(currentHour);
            currentHour = currentHour.plusHours(1);
        }
        
        System.out.println("Initial aggregation completed");
    }
    
    @Scheduled(fixedRate = 3600000)
    public void aggregatePreviousHour() {
        LocalDateTime previousHour = LocalDateTime.now()
            .truncatedTo(ChronoUnit.HOURS)
            .minusHours(1);
        
        System.out.println("Aggregating telemetry for hour: " + previousHour);
        aggregationService.aggregateTelemetryForHourParallel(previousHour);
        System.out.println("Aggregation completed for hour: " + previousHour);
    }
}

