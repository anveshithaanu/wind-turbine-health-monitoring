package com.wind.turbinemonitor.repository;

import com.wind.turbinemonitor.model.Farm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FarmRepository extends JpaRepository<Farm, Long> {
    Optional<Farm> findByName(String name);
    List<Farm> findByRegion(String region);
}


