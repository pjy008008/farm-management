package com.smartfarm.farm_management.repository;

import com.smartfarm.farm_management.entity.Alert;
import com.smartfarm.farm_management.entity.Plant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByPlantOrderByTimestampDesc(Plant plant);
    List<Alert> findByPlantAndSensorTypeOrderByTimestampDesc(Plant plant, Alert.SensorType sensorType);
}
