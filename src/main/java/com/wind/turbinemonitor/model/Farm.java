package com.wind.turbinemonitor.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "farms")
public class Farm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    @Column(nullable = false)
    private String region;
    
    @Column(nullable = false)
    private String location;
    
    @OneToMany(mappedBy = "farm", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Turbine> turbines;
    
    public Farm() {}
    
    public Farm(String name, String region, String location) {
        this.name = name;
        this.region = region;
        this.location = location;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public List<Turbine> getTurbines() { return turbines; }
    public void setTurbines(List<Turbine> turbines) { this.turbines = turbines; }
}

