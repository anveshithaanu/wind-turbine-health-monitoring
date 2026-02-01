package com.wind.turbinemonitor.repository;

import com.wind.turbinemonitor.model.Telemetry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TelemetryRepository extends JpaRepository<Telemetry, Long> {
    List<Telemetry> findByTurbineId(Long turbineId);
    
    @Query("SELECT t FROM Telemetry t WHERE t.turbine.id = :turbineId AND t.timestamp >= :startTime AND t.timestamp < :endTime AND t.isAggregated = false ORDER BY t.timestamp")
    List<Telemetry> findUnaggregatedByTurbineAndTimeRange(
        @Param("turbineId") Long turbineId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    @Query("SELECT t FROM Telemetry t WHERE t.timestamp >= :startTime AND t.timestamp < :endTime AND t.isAggregated = false ORDER BY t.turbine.id, t.timestamp")
    List<Telemetry> findUnaggregatedByTimeRange(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    @Query("SELECT t FROM Telemetry t WHERE t.turbine.id = :turbineId AND t.timestamp >= :startTime AND t.timestamp <= :endTime ORDER BY t.timestamp")
    List<Telemetry> findByTurbineAndDateRange(
        @Param("turbineId") Long turbineId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
}

