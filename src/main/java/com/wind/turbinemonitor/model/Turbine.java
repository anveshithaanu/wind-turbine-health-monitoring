package com.wind.turbinemonitor.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "turbines")
public class Turbine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "turbine_id", nullable = false, unique = true)
    private String turbineId;
    
    @Column(nullable = false)
    private String name;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farm_id", nullable = false)
    private Farm farm;
    
    @Column(nullable = false)
    private Double ratedPower;
    
    @Column(nullable = false)
    private String status;
    
    @Column(nullable = false)
    private LocalDateTime installedDate;
    
    @Column(nullable = false)
    private LocalDateTime lastUpdated;
    
    @OneToMany(mappedBy = "turbine", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Telemetry> telemetryData;
    
    @OneToMany(mappedBy = "turbine", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<HealthAlert> alerts;
    
    public Turbine() {}
    
    public Turbine(String turbineId, String name, Farm farm, Double ratedPower, String status) {
        this.turbineId = turbineId;
        this.name = name;
        this.farm = farm;
        this.ratedPower = ratedPower;
        this.status = status;
        this.installedDate = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTurbineId() { return turbineId; }
    public void setTurbineId(String turbineId) { this.turbineId = turbineId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Farm getFarm() { return farm; }
    public void setFarm(Farm farm) { this.farm = farm; }
    
    public Double getRatedPower() { return ratedPower; }
    public void setRatedPower(Double ratedPower) { this.ratedPower = ratedPower; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public LocalDateTime getInstalledDate() { return installedDate; }
    public void setInstalledDate(LocalDateTime installedDate) { this.installedDate = installedDate; }
    
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
    
    public List<Telemetry> getTelemetryData() { return telemetryData; }
    public void setTelemetryData(List<Telemetry> telemetryData) { this.telemetryData = telemetryData; }
    
    public List<HealthAlert> getAlerts() { return alerts; }
    public void setAlerts(List<HealthAlert> alerts) { this.alerts = alerts; }
}

