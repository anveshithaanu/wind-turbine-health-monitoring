package com.wind.turbinemonitor.repository;

import com.wind.turbinemonitor.model.Turbine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TurbineRepository extends JpaRepository<Turbine, Long> {
    Optional<Turbine> findByTurbineId(String turbineId);
    List<Turbine> findByFarmId(Long farmId);
    List<Turbine> findByStatus(String status);
    Page<Turbine> findByStatus(String status, Pageable pageable);
    
    @Query("SELECT t FROM Turbine t WHERE t.farm.region = :region")
    List<Turbine> findByRegion(@Param("region") String region);
    
    @Query("SELECT t FROM Turbine t WHERE t.farm.region = :region")
    Page<Turbine> findByRegion(@Param("region") String region, Pageable pageable);
    
    @Query("SELECT t FROM Turbine t WHERE t.farm.name = :farmName")
    List<Turbine> findByFarmName(@Param("farmName") String farmName);
    
    @Query("SELECT t FROM Turbine t WHERE t.farm.name = :farmName")
    Page<Turbine> findByFarmName(@Param("farmName") String farmName, Pageable pageable);
    
    @Query("SELECT t FROM Turbine t WHERE (:farmName IS NULL OR :farmName = '' OR t.farm.name = :farmName) AND (:region IS NULL OR :region = '' OR t.farm.region = :region) AND (:status IS NULL OR :status = '' OR t.status = :status)")
    Page<Turbine> findByFilters(@Param("farmName") String farmName, @Param("region") String region, @Param("status") String status, Pageable pageable);
    
    @Query("SELECT t FROM Turbine t WHERE (:farmName IS NULL OR :farmName = '' OR t.farm.name = :farmName) AND (:region IS NULL OR :region = '' OR t.farm.region = :region) AND (:status IS NULL OR :status = '' OR t.status = :status)")
    List<Turbine> findByFiltersList(@Param("farmName") String farmName, @Param("region") String region, @Param("status") String status);
}

