package com.wind.turbinemonitor.dto;

import java.time.LocalDate;

public class DailyMetricsDTO {
    private String date;
    private String farm;
    private double totalGeneration;
    private double avgEfficiency;
    private double operatingHours;
    private double maxPower;
    
    public DailyMetricsDTO() {}
    
    public DailyMetricsDTO(String date, String farm, double totalGeneration, double avgEfficiency, double operatingHours, double maxPower) {
        this.date = date;
        this.farm = farm;
        this.totalGeneration = totalGeneration;
        this.avgEfficiency = avgEfficiency;
        this.operatingHours = operatingHours;
        this.maxPower = maxPower;
    }
    
    public String getDate() {
        return date;
    }
    
    public void setDate(String date) {
        this.date = date;
    }
    
    public String getFarm() {
        return farm;
    }
    
    public void setFarm(String farm) {
        this.farm = farm;
    }
    
    public double getTotalGeneration() {
        return totalGeneration;
    }
    
    public void setTotalGeneration(double totalGeneration) {
        this.totalGeneration = totalGeneration;
    }
    
    public double getAvgEfficiency() {
        return avgEfficiency;
    }
    
    public void setAvgEfficiency(double avgEfficiency) {
        this.avgEfficiency = avgEfficiency;
    }
    
    public double getOperatingHours() {
        return operatingHours;
    }
    
    public void setOperatingHours(double operatingHours) {
        this.operatingHours = operatingHours;
    }
    
    public double getMaxPower() {
        return maxPower;
    }
    
    public void setMaxPower(double maxPower) {
        this.maxPower = maxPower;
    }
}


