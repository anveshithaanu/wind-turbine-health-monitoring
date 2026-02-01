package com.wind.turbinemonitor.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "telemetry")
public class Telemetry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turbine_id", nullable = false)
    private Turbine turbine;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column(nullable = false)
    private Double windSpeed;
    
    @Column(nullable = false)
    private Double powerOutput;
    
    @Column(nullable = false)
    private Double rotorSpeed;
    
    @Column(nullable = false)
    private Double temperature;
    
    @Column(nullable = false)
    private Double vibration;
    
    @Column(nullable = false)
    private Double efficiency;
    
    @Column(nullable = false)
    private Boolean isAggregated;
    
    public Telemetry() {
        this.isAggregated = false;
    }
    
    public Telemetry(Turbine turbine, LocalDateTime timestamp, Double windSpeed, 
                    Double powerOutput, Double rotorSpeed, Double temperature, 
                    Double vibration, Double efficiency) {
        this.turbine = turbine;
        this.timestamp = timestamp;
        this.windSpeed = windSpeed;
        this.powerOutput = powerOutput;
        this.rotorSpeed = rotorSpeed;
        this.temperature = temperature;
        this.vibration = vibration;
        this.efficiency = efficiency;
        this.isAggregated = false;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Turbine getTurbine() { return turbine; }
    public void setTurbine(Turbine turbine) { this.turbine = turbine; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public Double getWindSpeed() { return windSpeed; }
    public void setWindSpeed(Double windSpeed) { this.windSpeed = windSpeed; }
    
    public Double getPowerOutput() { return powerOutput; }
    public void setPowerOutput(Double powerOutput) { this.powerOutput = powerOutput; }
    
    public Double getRotorSpeed() { return rotorSpeed; }
    public void setRotorSpeed(Double rotorSpeed) { this.rotorSpeed = rotorSpeed; }
    
    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }
    
    public Double getVibration() { return vibration; }
    public void setVibration(Double vibration) { this.vibration = vibration; }
    
    public Double getEfficiency() { return efficiency; }
    public void setEfficiency(Double efficiency) { this.efficiency = efficiency; }
    
    public Boolean getIsAggregated() { return isAggregated; }
    public void setIsAggregated(Boolean isAggregated) { this.isAggregated = isAggregated; }
}

