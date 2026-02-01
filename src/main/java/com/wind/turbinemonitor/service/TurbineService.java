package com.wind.turbinemonitor.service;

import com.wind.turbinemonitor.dto.PageResponse;
import com.wind.turbinemonitor.model.Farm;
import com.wind.turbinemonitor.model.Turbine;
import com.wind.turbinemonitor.repository.FarmRepository;
import com.wind.turbinemonitor.repository.TurbineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TurbineService {
    @Autowired
    private TurbineRepository turbineRepository;
    
    @Autowired
    private FarmRepository farmRepository;
    
    public List<Turbine> getAllTurbines() {
        return turbineRepository.findAll();
    }
    
    public Optional<Turbine> getTurbineById(Long id) {
        return turbineRepository.findById(id);
    }
    
    public Optional<Turbine> getTurbineByTurbineId(String turbineId) {
        return turbineRepository.findByTurbineId(turbineId);
    }
    
    public List<Turbine> getTurbinesByFarm(Long farmId) {
        return turbineRepository.findByFarmId(farmId);
    }
    
    public List<Turbine> getTurbinesByFarmName(String farmName) {
        return turbineRepository.findByFarmName(farmName);
    }
    
    public List<Turbine> getTurbinesByRegion(String region) {
        return turbineRepository.findByRegion(region);
    }
    
    public List<Turbine> getTurbinesByStatus(String status) {
        return turbineRepository.findByStatus(status);
    }
    
    public Turbine createTurbine(Turbine turbine) {
        turbine.setInstalledDate(LocalDateTime.now());
        turbine.setLastUpdated(LocalDateTime.now());
        return turbineRepository.save(turbine);
    }
    
    public Turbine updateTurbine(Long id, Turbine turbineDetails) {
        Turbine turbine = turbineRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Turbine not found with id: " + id));
        turbine.setName(turbineDetails.getName());
        turbine.setTurbineId(turbineDetails.getTurbineId());
        if (turbineDetails.getFarm() != null) {
            Farm farm = farmRepository.findById(turbineDetails.getFarm().getId())
                .orElseThrow(() -> new RuntimeException("Farm not found"));
            turbine.setFarm(farm);
        }
        turbine.setRatedPower(turbineDetails.getRatedPower());
        turbine.setStatus(turbineDetails.getStatus());
        turbine.setLastUpdated(LocalDateTime.now());
        return turbineRepository.save(turbine);
    }
    
    public void deleteTurbine(Long id) {
        turbineRepository.deleteById(id);
    }
    
    public PageResponse<Turbine> getAllTurbinesPaginated(int page, int size, String farm, String region, String status) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Turbine> turbinePage;
        
        boolean hasFarm = farm != null && !farm.isEmpty();
        boolean hasRegion = region != null && !region.isEmpty();
        boolean hasStatus = status != null && !status.isEmpty();
        
        if (hasFarm || hasRegion || hasStatus) {
            turbinePage = turbineRepository.findByFilters(farm, region, status, pageable);
        } else {
            turbinePage = turbineRepository.findAll(pageable);
        }
        
        return new PageResponse<>(
            turbinePage.getContent(),
            turbinePage.getNumber(),
            turbinePage.getSize(),
            turbinePage.getTotalElements()
        );
    }
    
    public List<Turbine> getAllTurbinesWithFilters(String farm, String region, String status) {
        boolean hasFarm = farm != null && !farm.isEmpty();
        boolean hasRegion = region != null && !region.isEmpty();
        boolean hasStatus = status != null && !status.isEmpty();
        
        if (hasFarm || hasRegion || hasStatus) {
            return turbineRepository.findByFiltersList(farm, region, status);
        } else {
            return turbineRepository.findAll();
        }
    }
}

