package com.wind.turbinemonitor.service;

import com.wind.turbinemonitor.model.Farm;
import com.wind.turbinemonitor.repository.FarmRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class FarmService {
    @Autowired
    private FarmRepository farmRepository;
    
    public List<Farm> getAllFarms() {
        return farmRepository.findAll();
    }
    
    public Optional<Farm> getFarmById(Long id) {
        return farmRepository.findById(id);
    }
    
    public Optional<Farm> getFarmByName(String name) {
        return farmRepository.findByName(name);
    }
    
    public List<Farm> getFarmsByRegion(String region) {
        return farmRepository.findByRegion(region);
    }
    
    public Farm createFarm(Farm farm) {
        return farmRepository.save(farm);
    }
    
    public Farm updateFarm(Long id, Farm farmDetails) {
        Farm farm = farmRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Farm not found with id: " + id));
        farm.setName(farmDetails.getName());
        farm.setRegion(farmDetails.getRegion());
        farm.setLocation(farmDetails.getLocation());
        return farmRepository.save(farm);
    }
    
    public void deleteFarm(Long id) {
        farmRepository.deleteById(id);
    }
}

