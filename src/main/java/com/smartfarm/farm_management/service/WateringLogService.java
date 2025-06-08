package com.smartfarm.farm_management.service;

import com.smartfarm.farm_management.entity.Plant;
import com.smartfarm.farm_management.entity.WateringLog;
import com.smartfarm.farm_management.repository.PlantRepository;
import com.smartfarm.farm_management.repository.WateringLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class WateringLogService {

    @Autowired
    private WateringLogRepository wateringLogRepository;
    @Autowired
    private PlantRepository plantRepository;

    public WateringLog createWateringLog(Long plantId, Integer amountMl, WateringLog.Method method) {
        Plant plant = plantRepository.findById(plantId)
                .orElseThrow(() -> new RuntimeException("Plant not found with id " + plantId));

        WateringLog log = new WateringLog();
        log.setPlant(plant);
        log.setAmountMl(amountMl);
        log.setMethod(method);
        return wateringLogRepository.save(log);
    }

    public List<WateringLog> getLogsByPlant(Long plantId) {
        Plant plant = plantRepository.findById(plantId)
                .orElseThrow(() -> new RuntimeException("Plant not found with id " + plantId));
        return wateringLogRepository.findByPlantOrderByTimestampDesc(plant);
    }
}
