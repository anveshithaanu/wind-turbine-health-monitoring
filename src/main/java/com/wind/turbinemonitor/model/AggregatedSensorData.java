package com.wind.turbinemonitor.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "telemetry_aggregates")
public class TelemetryAggregate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turbine_id", nullable = false)
    private Turbine turbine;
    
    @Column(nullable = false)
    private LocalDateTime hourStart;
    
    @Column(nullable = false)
    private Double avgWindSpeed;
    
    @Column(nullable = false)
    private Double avgPowerOutput;
    
    @Column(nullable = false)
    private Double avgRotorSpeed;
    
    @Column(nullable = false)
    private Double avgTemperature;
    
    @Column(nullable = false)
    private Double avgVibration;
    
    @Column(nullable = false)
    private Double avgEfficiency;
    
    @Column(nullable = false)
    private Double totalGeneration;
    
    @Column(nullable = false)
    private Integer dataPointCount;
    
    @Column(nullable = false)
    private Boolean hasAnomaly;
    
    public TelemetryAggregate() {}
    
    public TelemetryAggregate(Turbine turbine, LocalDateTime hourStart) {
        this.turbine = turbine;
        this.hourStart = hourStart;
        this.hasAnomaly = false;
        this.dataPointCount = 0;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Turbine getTurbine() { return turbine; }
    public void setTurbine(Turbine turbine) { this.turbine = turbine; }
    
    public LocalDateTime getHourStart() { return hourStart; }
    public void setHourStart(LocalDateTime hourStart) { this.hourStart = hourStart; }
    
    public Double getAvgWindSpeed() { return avgWindSpeed; }
    public void setAvgWindSpeed(Double avgWindSpeed) { this.avgWindSpeed = avgWindSpeed; }
    
    public Double getAvgPowerOutput() { return avgPowerOutput; }
    public void setAvgPowerOutput(Double avgPowerOutput) { this.avgPowerOutput = avgPowerOutput; }
    
    public Double getAvgRotorSpeed() { return avgRotorSpeed; }
    public void setAvgRotorSpeed(Double avgRotorSpeed) { this.avgRotorSpeed = avgRotorSpeed; }
    
    public Double getAvgTemperature() { return avgTemperature; }
    public void setAvgTemperature(Double avgTemperature) { this.avgTemperature = avgTemperature; }
    
    public Double getAvgVibration() { return avgVibration; }
    public void setAvgVibration(Double avgVibration) { this.avgVibration = avgVibration; }
    
    public Double getAvgEfficiency() { return avgEfficiency; }
    public void setAvgEfficiency(Double avgEfficiency) { this.avgEfficiency = avgEfficiency; }
    
    public Double getTotalGeneration() { return totalGeneration; }
    public void setTotalGeneration(Double totalGeneration) { this.totalGeneration = totalGeneration; }
    
    public Integer getDataPointCount() { return dataPointCount; }
    public void setDataPointCount(Integer dataPointCount) { this.dataPointCount = dataPointCount; }
    
    public Boolean getHasAnomaly() { return hasAnomaly; }
    public void setHasAnomaly(Boolean hasAnomaly) { this.hasAnomaly = hasAnomaly; }
}

