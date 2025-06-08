package com.smartfarm.farm_management.repository;

import com.smartfarm.farm_management.entity.Plant;
import com.smartfarm.farm_management.entity.WateringLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WateringLogRepository extends JpaRepository<WateringLog, Long> {
    List<WateringLog> findByPlantOrderByTimestampDesc(Plant plant);
}