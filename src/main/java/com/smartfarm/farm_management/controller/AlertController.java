package com.smartfarm.farm_management.controller;

import com.smartfarm.farm_management.entity.Alert;
import com.smartfarm.farm_management.service.AlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    @Autowired
    private AlertService alertService;

    // 특정 식물의 모든 경고 조회 (최신순)
    @GetMapping("/plant/{plantId}")
    public ResponseEntity<List<Alert>> getAlertsByPlant(@PathVariable Long plantId) {
        try {
            List<Alert> alerts = alertService.getAlertsByPlant(plantId);
            return ResponseEntity.ok(alerts);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 특정 식물의 특정 센서 유형에 대한 경고 조회 (최신순)
    // curl -X GET "http://[라즈베리파이_IP]:8080/api/alerts/plant/1/sensor/temp"
    @GetMapping("/plant/{plantId}/sensor/{sensorType}")
    public ResponseEntity<List<Alert>> getAlertsByPlantAndSensorType(
            @PathVariable Long plantId,
            @PathVariable Alert.SensorType sensorType) {
        try {
            List<Alert> alerts = alertService.getAlertsByPlantAndSensorType(plantId, sensorType);
            return ResponseEntity.ok(alerts);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

