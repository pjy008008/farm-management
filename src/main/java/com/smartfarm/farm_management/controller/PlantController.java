package com.smartfarm.farm_management.controller;

import com.smartfarm.farm_management.entity.Plant;
import com.smartfarm.farm_management.service.PlantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plants")
public class PlantController {

    @Autowired
    private PlantService plantService;

    // 모든 식물 정보 조회
    @GetMapping
    public ResponseEntity<List<Plant>> getAllPlants() {
        List<Plant> plants = plantService.getAllPlants();
        return ResponseEntity.ok(plants);
    }

    // 특정 식물 정보 조회
    @GetMapping("/{id}")
    public ResponseEntity<Plant> getPlantById(@PathVariable Long id) {
        return plantService.getPlantById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 새로운 식물 정보 추가 (관리자용)
    @PostMapping
    public ResponseEntity<Plant> createPlant(@RequestBody Plant plant) {
        Plant createdPlant = plantService.createPlant(plant);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPlant);
    }

    // 식물 정보 업데이트 (관리자용)
    @PutMapping("/{id}")
    public ResponseEntity<Plant> updatePlant(@PathVariable Long id, @RequestBody Plant plant) {
        try {
            Plant updatedPlant = plantService.updatePlant(id, plant);
            return ResponseEntity.ok(updatedPlant);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build(); // 또는 더 구체적인 오류 응답
        }
    }

    // 식물 정보 삭제 (관리자용)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlant(@PathVariable Long id) {
        try {
            plantService.deletePlant(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build(); // 또는 더 구체적인 오류 응답
        }
    }
}