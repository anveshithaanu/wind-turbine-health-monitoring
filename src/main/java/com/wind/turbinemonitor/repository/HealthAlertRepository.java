package com.wind.turbinemonitor.repository;

import com.wind.turbinemonitor.model.HealthAlert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HealthAlertRepository extends JpaRepository<HealthAlert, Long> {
    List<HealthAlert> findByTurbineId(Long turbineId);
    List<HealthAlert> findByStatus(String status);
    Page<HealthAlert> findByStatus(String status, Pageable pageable);
    
    @Query("SELECT ha FROM HealthAlert ha WHERE ha.turbine.id = :turbineId AND ha.status = 'ACTIVE' ORDER BY ha.alertTime DESC")
    List<HealthAlert> findActiveAlertsByTurbine(@Param("turbineId") Long turbineId);
    
    @Query("SELECT ha FROM HealthAlert ha WHERE ha.turbine.id = :turbineId AND ha.status = 'ACTIVE' ORDER BY ha.alertTime DESC")
    Page<HealthAlert> findActiveAlertsByTurbine(@Param("turbineId") Long turbineId, Pageable pageable);
    
    @Query("SELECT ha FROM HealthAlert ha WHERE ha.status = 'ACTIVE' AND ha.alertTime >= :since ORDER BY ha.alertTime DESC")
    List<HealthAlert> findActiveAlertsSince(@Param("since") LocalDateTime since);
    
    @Query("SELECT ha FROM HealthAlert ha WHERE ha.turbine.farm.region = :region AND ha.status = 'ACTIVE' ORDER BY ha.alertTime DESC")
    List<HealthAlert> findActiveAlertsByRegion(@Param("region") String region);
    
    @Query("SELECT ha FROM HealthAlert ha WHERE ha.turbine.farm.region = :region AND ha.status = 'ACTIVE' ORDER BY ha.alertTime DESC")
    Page<HealthAlert> findActiveAlertsByRegion(@Param("region") String region, Pageable pageable);
    
    @Query("SELECT ha FROM HealthAlert ha WHERE ha.turbine.farm.name = :farmName AND ha.status = 'ACTIVE' ORDER BY ha.alertTime DESC")
    List<HealthAlert> findActiveAlertsByFarm(@Param("farmName") String farmName);
    
    @Query("SELECT ha FROM HealthAlert ha WHERE ha.turbine.farm.name = :farmName AND ha.status = 'ACTIVE' ORDER BY ha.alertTime DESC")
    Page<HealthAlert> findActiveAlertsByFarm(@Param("farmName") String farmName, Pageable pageable);
    
    @Query("SELECT ha FROM HealthAlert ha WHERE (:turbineId IS NULL OR ha.turbine.id = :turbineId) AND (:region IS NULL OR ha.turbine.farm.region = :region) AND (:farm IS NULL OR ha.turbine.farm.name = :farm) AND ha.status = 'ACTIVE' ORDER BY ha.alertTime DESC")
    Page<HealthAlert> findActiveAlertsByFilters(@Param("turbineId") Long turbineId, @Param("region") String region, @Param("farm") String farm, Pageable pageable);
}

