package com.wind.turbinemonitor.controller;

import com.wind.turbinemonitor.dto.PageResponse;
import com.wind.turbinemonitor.model.HealthAlert;
import com.wind.turbinemonitor.model.Turbine;
import com.wind.turbinemonitor.service.AnomalyService;
import com.wind.turbinemonitor.service.TurbineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/health")
@CrossOrigin(origins = "*")
public class HealthController {
    @Autowired
    private AnomalyService anomalyService;
    
    @Autowired
    private TurbineService turbineService;
    
    @GetMapping("/alerts")
    public ResponseEntity<?> getActiveAlerts(
            @RequestParam(required = false) Long turbineId,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String farm,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {
        
        if (page >= 0 && size > 0) {
            PageResponse<HealthAlert> paginated = anomalyService.getActiveAlertsPaginated(page, size, turbineId, region, farm);
            return ResponseEntity.ok(paginated);
        }
        
        if (turbineId != null) {
            return ResponseEntity.ok(anomalyService.getActiveAlertsByTurbine(turbineId));
        }
        if (region != null) {
            return ResponseEntity.ok(anomalyService.getActiveAlertsByRegion(region));
        }
        if (farm != null) {
            return ResponseEntity.ok(anomalyService.getActiveAlertsByFarm(farm));
        }
        
        return ResponseEntity.ok(anomalyService.getActiveAlerts());
    }
    
    @GetMapping("/turbine/{turbineId}/status")
    public ResponseEntity<Object> getTurbineHealthStatus(@PathVariable Long turbineId) {
        Optional<Turbine> turbineOpt = turbineService.getTurbineById(turbineId);
        if (turbineOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Turbine turbine = turbineOpt.get();
        List<HealthAlert> alerts = anomalyService.getActiveAlertsByTurbine(turbineId);
        
        java.util.Map<String, Object> status = new java.util.HashMap<>();
        status.put("turbineId", turbine.getTurbineId());
        status.put("name", turbine.getName());
        status.put("status", turbine.getStatus());
        status.put("activeAlerts", alerts.size());
        status.put("alerts", alerts);
        status.put("healthStatus", alerts.isEmpty() ? "HEALTHY" : "ALERT");
        
        return ResponseEntity.ok(status);
    }
    
    @PutMapping("/alerts/{alertId}/resolve")
    public ResponseEntity<Void> resolveAlert(@PathVariable Long alertId) {
        try {
            anomalyService.resolveAlert(alertId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

