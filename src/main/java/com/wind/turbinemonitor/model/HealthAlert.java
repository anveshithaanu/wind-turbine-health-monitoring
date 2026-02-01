package com.wind.turbinemonitor.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "health_alerts")
public class HealthAlert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turbine_id", nullable = false)
    private Turbine turbine;
    
    @Column(nullable = false)
    private LocalDateTime alertTime;
    
    @Column(nullable = false)
    private String alertType;
    
    @Column(nullable = false)
    private String severity;
    
    @Column(nullable = false, length = 1000)
    private String message;
    
    @Column(nullable = false)
    private String status;
    
    @Column
    private LocalDateTime resolvedAt;
    
    public HealthAlert() {
        this.status = "ACTIVE";
    }
    
    public HealthAlert(Turbine turbine, String alertType, String severity, String message) {
        this.turbine = turbine;
        this.alertTime = LocalDateTime.now();
        this.alertType = alertType;
        this.severity = severity;
        this.message = message;
        this.status = "ACTIVE";
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Turbine getTurbine() { return turbine; }
    public void setTurbine(Turbine turbine) { this.turbine = turbine; }
    
    public LocalDateTime getAlertTime() { return alertTime; }
    public void setAlertTime(LocalDateTime alertTime) { this.alertTime = alertTime; }
    
    public String getAlertType() { return alertType; }
    public void setAlertType(String alertType) { this.alertType = alertType; }
    
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
}

