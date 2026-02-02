package com.wind.turbinemonitor.controller;

import com.wind.turbinemonitor.model.Farm;
import com.wind.turbinemonitor.service.FarmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/farms")
@CrossOrigin(origins = "*")
public class FarmController {
    @Autowired
    private FarmService farmService;
    
    @GetMapping
    public ResponseEntity<List<Farm>> getAllFarms() {
        return ResponseEntity.ok(farmService.getAllFarms());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Farm> getFarmById(@PathVariable Long id) {
        Optional<Farm> farm = farmService.getFarmById(id);
        return farm.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/region/{region}")
    public ResponseEntity<List<Farm>> getFarmsByRegion(@PathVariable String region) {
        return ResponseEntity.ok(farmService.getFarmsByRegion(region));
    }
    
    @PostMapping
    public ResponseEntity<Farm> createFarm(@RequestBody Farm farm) {
        return ResponseEntity.status(HttpStatus.CREATED).body(farmService.createFarm(farm));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Farm> updateFarm(@PathVariable Long id, @RequestBody Farm farm) {
        try {
            return ResponseEntity.ok(farmService.updateFarm(id, farm));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFarm(@PathVariable Long id) {
        farmService.deleteFarm(id);
        return ResponseEntity.noContent().build();
    }
}


