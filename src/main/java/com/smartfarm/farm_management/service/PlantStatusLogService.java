package com.smartfarm.farm_management.service;

import com.smartfarm.farm_management.entity.Alert;
import com.smartfarm.farm_management.entity.Plant;
import com.smartfarm.farm_management.entity.PlantStatusLog;
import com.smartfarm.farm_management.repository.AlertRepository;
import com.smartfarm.farm_management.repository.PlantRepository;
import com.smartfarm.farm_management.repository.PlantStatusLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PlantStatusLogService {

    @Autowired
    private PlantStatusLogRepository plantStatusLogRepository;
    @Autowired
    private PlantRepository plantRepository; // Plant 엔티티 참조를 위해 필요
    @Autowired
    private AlertRepository alertRepository; // 경고 기록을 위해 AlertRepository 주입

    public PlantStatusLog createPlantStatusLog(Long plantId, Float temperature, Float humidity, Float soilMoisture) {
        Plant plant = plantRepository.findById(plantId)
                .orElseThrow(() -> new RuntimeException("Plant not found with id " + plantId));

        PlantStatusLog log = new PlantStatusLog();
        log.setPlant(plant);
        log.setTemperature(temperature);
        log.setHumidity(humidity);
        log.setSoilMoisture(soilMoisture);
        // timestamp는 @PrePersist에서 자동으로 설정됩니다.
        PlantStatusLog createdLog = plantStatusLogRepository.save(log);

        // --- 센서 데이터 저장 후 임계값 확인 및 경고 기록 로직 추가 ---
        // (이 로직은 SensorController에서 수행할 수도 있지만, 서비스 계층에서 처리하는 것이 비즈니스 로직 응집도에 좋습니다.)
        checkAndCreateAlert(plant, temperature, humidity, soilMoisture);
        // --- End of 경고 로직 ---

        return createdLog;
    }

    private void checkAndCreateAlert(Plant plant, Float temperature, Float humidity, Float soilMoisture) {
        // 온도 임계값 확인
        if (plant.getMinTemp() != null && temperature < plant.getMinTemp()) {
            alertRepository.save(new Alert(null, plant, Alert.SensorType.temp, temperature, Alert.ThresholdType.min, null));
        } else if (plant.getMaxTemp() != null && temperature > plant.getMaxTemp()) {
            alertRepository.save(new Alert(null, plant, Alert.SensorType.temp, temperature, Alert.ThresholdType.max, null));
        }

        // 습도 임계값 확인
        if (plant.getMinHumidity() != null && humidity < plant.getMinHumidity()) {
            alertRepository.save(new Alert(null, plant, Alert.SensorType.humidity, humidity, Alert.ThresholdType.min, null));
        } else if (plant.getMaxHumidity() != null && humidity > plant.getMaxHumidity()) {
            alertRepository.save(new Alert(null, plant, Alert.SensorType.humidity, humidity, Alert.ThresholdType.max, null));
        }

        // 토양 수분 임계값 확인
        if (plant.getMinSoilMoisture() != null && soilMoisture < plant.getMinSoilMoisture()) {
            alertRepository.save(new Alert(null, plant, Alert.SensorType.soil_moisture, soilMoisture, Alert.ThresholdType.min, null));
        } else if (plant.getMaxSoilMoisture() != null && soilMoisture > plant.getMaxSoilMoisture()) {
            alertRepository.save(new Alert(null, plant, Alert.SensorType.soil_moisture, soilMoisture, Alert.ThresholdType.max, null));
        }
    }


    public List<PlantStatusLog> getLogsByPlant(Long plantId) {
        Plant plant = plantRepository.findById(plantId)
                .orElseThrow(() -> new RuntimeException("Plant not found with id " + plantId));
        return plantStatusLogRepository.findByPlantOrderByTimestampDesc(plant);
    }

    public List<PlantStatusLog> getLogsByPlantAndTimeRange(Long plantId, LocalDateTime startTime, LocalDateTime endTime) {
        Plant plant = plantRepository.findById(plantId)
                .orElseThrow(() -> new RuntimeException("Plant not found with id " + plantId));
        return plantStatusLogRepository.findByPlantAndTimestampBetweenOrderByTimestampAsc(plant, startTime, endTime);
    }
}