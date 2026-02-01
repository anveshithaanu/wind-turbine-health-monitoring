package com.wind.turbinemonitor.controller;

import com.wind.turbinemonitor.dto.PageResponse;
import com.wind.turbinemonitor.model.Turbine;
import com.wind.turbinemonitor.service.TurbineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/turbines")
@CrossOrigin(origins = "*")
public class TurbineController {
    @Autowired
    private TurbineService turbineService;
    
    @GetMapping
    public ResponseEntity<?> getAllTurbines(
            @RequestParam(required = false) String farm,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        
        if (page != null && size != null && page >= 0 && size > 0) {
            PageResponse<Turbine> paginated = turbineService.getAllTurbinesPaginated(page, size, farm, region, status);
            return ResponseEntity.ok(paginated);
        }
        
        List<Turbine> turbines = turbineService.getAllTurbinesWithFilters(farm, region, status);
        return ResponseEntity.ok(turbines);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Turbine> getTurbineById(@PathVariable Long id) {
        Optional<Turbine> turbine = turbineService.getTurbineById(id);
        return turbine.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/turbine-id/{turbineId}")
    public ResponseEntity<Turbine> getTurbineByTurbineId(@PathVariable String turbineId) {
        Optional<Turbine> turbine = turbineService.getTurbineByTurbineId(turbineId);
        return turbine.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/farm/{farmId}")
    public ResponseEntity<List<Turbine>> getTurbinesByFarm(@PathVariable Long farmId) {
        return ResponseEntity.ok(turbineService.getTurbinesByFarm(farmId));
    }
    
    @PostMapping
    public ResponseEntity<Turbine> createTurbine(@RequestBody Turbine turbine) {
        return ResponseEntity.status(HttpStatus.CREATED).body(turbineService.createTurbine(turbine));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Turbine> updateTurbine(@PathVariable Long id, @RequestBody Turbine turbine) {
        try {
            return ResponseEntity.ok(turbineService.updateTurbine(id, turbine));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTurbine(@PathVariable Long id) {
        turbineService.deleteTurbine(id);
        return ResponseEntity.noContent().build();
    }
}

