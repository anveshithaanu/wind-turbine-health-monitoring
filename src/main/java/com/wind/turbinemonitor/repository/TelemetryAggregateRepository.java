package com.wind.turbinemonitor.repository;

import com.wind.turbinemonitor.model.TelemetryAggregate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TelemetryAggregateRepository extends JpaRepository<TelemetryAggregate, Long> {
    List<TelemetryAggregate> findByTurbineId(Long turbineId);
    
    @Query("SELECT ta FROM TelemetryAggregate ta WHERE ta.turbine.id = :turbineId AND ta.hourStart >= :startTime AND ta.hourStart <= :endTime ORDER BY ta.hourStart")
    List<TelemetryAggregate> findByTurbineAndDateRange(
        @Param("turbineId") Long turbineId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    @Query("SELECT ta FROM TelemetryAggregate ta WHERE ta.hourStart >= :startTime AND ta.hourStart <= :endTime ORDER BY ta.turbine.id, ta.hourStart")
    List<TelemetryAggregate> findByDateRange(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    Optional<TelemetryAggregate> findByTurbineIdAndHourStart(Long turbineId, LocalDateTime hourStart);
    
    @Query("SELECT ta FROM TelemetryAggregate ta WHERE ta.hasAnomaly = true AND ta.hourStart >= :startTime ORDER BY ta.hourStart DESC")
    List<TelemetryAggregate> findAnomaliesSince(@Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT ta FROM TelemetryAggregate ta WHERE ta.hourStart >= :startTime AND ta.hourStart <= :endTime " +
           "AND (:farmName IS NULL OR ta.turbine.farm.name = :farmName) " +
           "AND (:region IS NULL OR ta.turbine.farm.region = :region) " +
           "ORDER BY ta.turbine.id, ta.hourStart")
    List<TelemetryAggregate> findByDateRangeAndFilters(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime,
        @Param("farmName") String farmName,
        @Param("region") String region
    );
}

