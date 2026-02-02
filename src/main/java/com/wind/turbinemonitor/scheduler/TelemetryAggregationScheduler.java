package com.wind.turbinemonitor.scheduler;

import com.wind.turbinemonitor.service.AggregationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
public class TelemetryAggregationScheduler {
    @Autowired
    private AggregationService aggregationService;
    
    // Run after application is fully started, in background thread to not block startup
    @EventListener(ApplicationReadyEvent.class)
    public void aggregateExistingTelemetryOnStartup() {
        // Run in background thread so it doesn't block application startup
        new Thread(() -> {
            try {
                LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS);
                LocalDateTime startHour = now.minusHours(24);
                
                System.out.println("Starting initial aggregation for hours from " + startHour + " to " + now);
                
                LocalDateTime currentHour = startHour;
                while (currentHour.isBefore(now)) {
                    aggregationService.aggregateTelemetryForHourParallel(currentHour);
                    currentHour = currentHour.plusHours(1);
                }
                
                System.out.println("Initial aggregation completed");
            } catch (Exception e) {
                System.err.println("Error during initial aggregation: " + e.getMessage());
                e.printStackTrace();
            }
        }, "InitialAggregationThread").start();
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

