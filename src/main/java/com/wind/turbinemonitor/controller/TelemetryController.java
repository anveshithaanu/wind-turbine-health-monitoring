package com.wind.turbinemonitor.controller;

import com.wind.turbinemonitor.model.Telemetry;
import com.wind.turbinemonitor.service.TelemetryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/telemetry")
@CrossOrigin(origins = "*")
public class TelemetryController {
    @Autowired
    private TelemetryService telemetryService;
    
    @PostMapping
    public ResponseEntity<Telemetry> createTelemetry(@RequestBody Telemetry telemetry) {
        return ResponseEntity.status(HttpStatus.CREATED).body(telemetryService.saveTelemetry(telemetry));
    }
    
    @PostMapping("/batch")
    public ResponseEntity<List<Telemetry>> createTelemetryBatch(@RequestBody List<Telemetry> telemetryList) {
        return ResponseEntity.status(HttpStatus.CREATED).body(telemetryService.saveTelemetryBatch(telemetryList));
    }
    
    @PostMapping("/turbine/{turbineId}")
    public ResponseEntity<Telemetry> createTelemetryForTurbine(
            @PathVariable Long turbineId,
            @RequestParam Double windSpeed,
            @RequestParam Double powerOutput,
            @RequestParam Double rotorSpeed,
            @RequestParam Double temperature,
            @RequestParam Double vibration) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(telemetryService.createTelemetry(turbineId, windSpeed, powerOutput, rotorSpeed, temperature, vibration));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/turbine/{turbineId}")
    public ResponseEntity<List<Telemetry>> getTelemetryByTurbine(@PathVariable Long turbineId) {
        return ResponseEntity.ok(telemetryService.getTelemetryByTurbine(turbineId));
    }
    
    @GetMapping("/turbine/{turbineId}/range")
    public ResponseEntity<List<Telemetry>> getTelemetryByTurbineAndDateRange(
            @PathVariable Long turbineId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return ResponseEntity.ok(telemetryService.getTelemetryByTurbineAndDateRange(turbineId, startTime, endTime));
    }
}


