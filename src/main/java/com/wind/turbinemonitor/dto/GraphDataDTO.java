package com.wind.turbinemonitor.dto;

public class GraphDataDTO {
    private String date;
    private double generation;
    private double efficiency;
    
    public GraphDataDTO() {}
    
    public GraphDataDTO(String date, double generation, double efficiency) {
        this.date = date;
        this.generation = generation;
        this.efficiency = efficiency;
    }
    
    public String getDate() {
        return date;
    }
    
    public void setDate(String date) {
        this.date = date;
    }
    
    public double getGeneration() {
        return generation;
    }
    
    public void setGeneration(double generation) {
        this.generation = generation;
    }
    
    public double getEfficiency() {
        return efficiency;
    }
    
    public void setEfficiency(double efficiency) {
        this.efficiency = efficiency;
    }
}

