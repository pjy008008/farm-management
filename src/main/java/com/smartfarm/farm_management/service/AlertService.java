package com.smartfarm.farm_management.service;

import com.smartfarm.farm_management.entity.Alert;
import com.smartfarm.farm_management.entity.Plant;
import com.smartfarm.farm_management.repository.AlertRepository;
import com.smartfarm.farm_management.repository.PlantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AlertService {

    @Autowired
    private AlertRepository alertRepository;
    @Autowired
    private PlantRepository plantRepository;

    public Alert createAlert(Long plantId, Alert.SensorType sensorType, Float value, Alert.ThresholdType thresholdType) {
        Plant plant = plantRepository.findById(plantId)
                .orElseThrow(() -> new RuntimeException("Plant not found with id " + plantId));

        Alert alert = new Alert();
        alert.setPlant(plant);
        alert.setSensorType(sensorType);
        alert.setValue(value);
        alert.setThresholdType(thresholdType);
        return alertRepository.save(alert);
    }

    public List<Alert> getAlertsByPlant(Long plantId) {
        Plant plant = plantRepository.findById(plantId)
                .orElseThrow(() -> new RuntimeException("Plant not found with id " + plantId));
        return alertRepository.findByPlantOrderByTimestampDesc(plant);
    }

    public List<Alert> getAlertsByPlantAndSensorType(Long plantId, Alert.SensorType sensorType) {
        Plant plant = plantRepository.findById(plantId)
                .orElseThrow(() -> new RuntimeException("Plant not found with id " + plantId));
        return alertRepository.findByPlantAndSensorTypeOrderByTimestampDesc(plant, sensorType);
    }
}
