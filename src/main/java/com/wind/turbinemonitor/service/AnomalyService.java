package com.wind.turbinemonitor.service;

import com.wind.turbinemonitor.dto.PageResponse;
import com.wind.turbinemonitor.model.HealthAlert;
import com.wind.turbinemonitor.model.TelemetryAggregate;
import com.wind.turbinemonitor.model.Turbine;
import com.wind.turbinemonitor.repository.HealthAlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class AnomalyService {
    @Autowired
    private HealthAlertRepository alertRepository;
    
    private static final double EFFICIENCY_THRESHOLD_LOW = 30.0;
    private static final double EFFICIENCY_THRESHOLD_HIGH = 95.0;
    private static final double VIBRATION_THRESHOLD = 10.0;
    private static final double TEMPERATURE_THRESHOLD_HIGH = 80.0;
    private static final double TEMPERATURE_THRESHOLD_LOW = -20.0;
    private static final double POWER_OUTPUT_THRESHOLD_LOW = 0.1;
    
    public boolean detectAnomaly(TelemetryAggregate aggregate) {
        List<String> anomalies = new ArrayList<>();
        
        if (aggregate.getAvgEfficiency() < EFFICIENCY_THRESHOLD_LOW) {
            anomalies.add("Low efficiency: " + String.format("%.2f", aggregate.getAvgEfficiency()) + "%");
        }
        
        if (aggregate.getAvgEfficiency() > EFFICIENCY_THRESHOLD_HIGH) {
            anomalies.add("Abnormally high efficiency: " + String.format("%.2f", aggregate.getAvgEfficiency()) + "%");
        }
        
        if (aggregate.getAvgVibration() > VIBRATION_THRESHOLD) {
            anomalies.add("High vibration: " + String.format("%.2f", aggregate.getAvgVibration()));
        }
        
        if (aggregate.getAvgTemperature() > TEMPERATURE_THRESHOLD_HIGH) {
            anomalies.add("High temperature: " + String.format("%.2f", aggregate.getAvgTemperature()) + "°C");
        }
        
        if (aggregate.getAvgTemperature() < TEMPERATURE_THRESHOLD_LOW) {
            anomalies.add("Low temperature: " + String.format("%.2f", aggregate.getAvgTemperature()) + "°C");
        }
        
        if (aggregate.getAvgPowerOutput() < POWER_OUTPUT_THRESHOLD_LOW && aggregate.getAvgWindSpeed() > 5.0) {
            anomalies.add("Low power output despite sufficient wind");
        }
        
        if (!anomalies.isEmpty()) {
            String message = String.join("; ", anomalies);
            String severity = determineSeverity(aggregate);
            createAlert(aggregate.getTurbine(), "ANOMALY_DETECTED", severity, message);
            return true;
        }
        
        return false;
    }
    
    private String determineSeverity(TelemetryAggregate aggregate) {
        int criticalCount = 0;
        
        if (aggregate.getAvgVibration() > VIBRATION_THRESHOLD * 1.5) criticalCount++;
        if (aggregate.getAvgTemperature() > TEMPERATURE_THRESHOLD_HIGH + 10) criticalCount++;
        if (aggregate.getAvgEfficiency() < EFFICIENCY_THRESHOLD_LOW / 2) criticalCount++;
        
        if (criticalCount >= 2) return "CRITICAL";
        if (criticalCount >= 1) return "HIGH";
        return "MEDIUM";
    }
    
    public void createAlert(Turbine turbine, String alertType, String severity, String message) {
        HealthAlert alert = new HealthAlert(turbine, alertType, severity, message);
        alertRepository.save(alert);
    }
    
    public List<HealthAlert> getActiveAlerts() {
        return alertRepository.findByStatus("ACTIVE");
    }
    
    public List<HealthAlert> getActiveAlertsByTurbine(Long turbineId) {
        return alertRepository.findActiveAlertsByTurbine(turbineId);
    }
    
    public List<HealthAlert> getActiveAlertsByRegion(String region) {
        return alertRepository.findActiveAlertsByRegion(region);
    }
    
    public List<HealthAlert> getActiveAlertsByFarm(String farmName) {
        return alertRepository.findActiveAlertsByFarm(farmName);
    }
    
    public PageResponse<HealthAlert> getActiveAlertsPaginated(int page, int size, Long turbineId, String region, String farm) {
        Pageable pageable = PageRequest.of(page, size);
        Page<HealthAlert> alertPage = alertRepository.findActiveAlertsByFilters(turbineId, region, farm, pageable);
        
        return new PageResponse<>(
            alertPage.getContent(),
            alertPage.getNumber(),
            alertPage.getSize(),
            alertPage.getTotalElements()
        );
    }
    
    public void resolveAlert(Long alertId) {
        HealthAlert alert = alertRepository.findById(alertId)
            .orElseThrow(() -> new RuntimeException("Alert not found"));
        alert.setStatus("RESOLVED");
        alert.setResolvedAt(LocalDateTime.now());
        alertRepository.save(alert);
    }
}

