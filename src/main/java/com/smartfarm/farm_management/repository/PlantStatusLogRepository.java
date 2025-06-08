package com.smartfarm.farm_management.repository;

import com.smartfarm.farm_management.entity.Plant;
import com.smartfarm.farm_management.entity.PlantStatusLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PlantStatusLogRepository extends JpaRepository<PlantStatusLog, Long> {
    List<PlantStatusLog> findByPlantOrderByTimestampDesc(Plant plant);
    List<PlantStatusLog> findByPlantAndTimestampBetweenOrderByTimestampAsc(Plant plant, LocalDateTime start, LocalDateTime end);
}