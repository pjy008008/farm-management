package com.smartfarm.farm_management.service;

import com.smartfarm.farm_management.entity.Plant;
import com.smartfarm.farm_management.repository.PlantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PlantService {

    @Autowired
    private PlantRepository plantRepository;

    public List<Plant> getAllPlants() {
        return plantRepository.findAll();
    }

    public Optional<Plant> getPlantById(Long id) {
        return plantRepository.findById(id);
    }

    public Plant createPlant(Plant plant) {
        return plantRepository.save(plant);
    }

    public Plant updatePlant(Long id, Plant plantDetails) {
        return plantRepository.findById(id).map(plant -> {
            plant.setName(plantDetails.getName());
            plant.setPlantType(plantDetails.getPlantType());
            plant.setMinTemp(plantDetails.getMinTemp());
            plant.setMaxTemp(plantDetails.getMaxTemp());
            plant.setMinHumidity(plantDetails.getMinHumidity());
            plant.setMaxHumidity(plantDetails.getMaxHumidity());
            plant.setMinSoilMoisture(plantDetails.getMinSoilMoisture());
            plant.setMaxSoilMoisture(plantDetails.getMaxSoilMoisture());
            return plantRepository.save(plant);
        }).orElseThrow(() -> new RuntimeException("Plant not found with id " + id)); // 적절한 예외 처리 필요
    }

    public void deletePlant(Long id) {
        if (!plantRepository.existsById(id)) {
            throw new RuntimeException("Plant not found with id " + id); // 적절한 예외 처리 필요
        }
        plantRepository.deleteById(id);
    }
}
