package com.smartfarm.farm_management.controller;

import com.smartfarm.farm_management.entity.PlantStatusLog;
import com.smartfarm.farm_management.service.PlantStatusLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

// 이 DTO는 라즈베리 파이에서 센서 데이터를 보낼 때의 요청 본문 형식을 정의합니다.
// DTO(Data Transfer Object)는 엔티티와 분리하여 요청/응답에 사용합니다.
class SensorDataRequest {
    public Long plantId;
    public Float temperature;
    public Float humidity;
    public Float soilMoisture;
}

@RestController
@RequestMapping("/api/sensors")
public class SensorController {

    @Autowired
    private PlantStatusLogService plantStatusLogService;

    // 라즈베리 파이에서 센서 데이터를 전송받는 엔드포인트
    // curl -X POST -H "Content-Type: application/json" -d '{"plantId":1, "temperature":25.5, "humidity":60.2, "soilMoisture":45.1}' http://[라즈베리파이_IP]:8080/api/sensors/data
    @PostMapping("/data")
    public ResponseEntity<String> receiveSensorData(@RequestBody SensorDataRequest data) {
        try {
            // 센서 데이터를 저장하고, 내부적으로 임계값 확인 및 경고 기록도 처리됩니다.
            plantStatusLogService.createPlantStatusLog(
                    data.plantId, data.temperature, data.humidity, data.soilMoisture
            );
            return ResponseEntity.status(HttpStatus.CREATED).body("Sensor data received and processed.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error processing sensor data: " + e.getMessage());
        }
    }

    // 특정 식물의 센서 로그 조회 (최신순)
    @GetMapping("/logs/plant/{plantId}")
    public ResponseEntity<List<PlantStatusLog>> getSensorLogsByPlant(@PathVariable Long plantId) {
        try {
            List<PlantStatusLog> logs = plantStatusLogService.getLogsByPlant(plantId);
            return ResponseEntity.ok(logs);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 특정 식물의 센서 로그를 시간 범위로 조회
    // curl -X GET "http://[라즈베리파이_IP]:8080/api/sensors/logs/plant/1/range?start=2025-06-01T00:00:00&end=2025-06-08T23:59:59"
    @GetMapping("/logs/plant/{plantId}/range")
    public ResponseEntity<List<PlantStatusLog>> getSensorLogsByPlantAndRange(
            @PathVariable Long plantId,
            @RequestParam String start,
            @RequestParam String end) {
        try {
            LocalDateTime startTime = LocalDateTime.parse(start);
            LocalDateTime endTime = LocalDateTime.parse(end);
            List<PlantStatusLog> logs = plantStatusLogService.getLogsByPlantAndTimeRange(plantId, startTime, endTime);
            return ResponseEntity.ok(logs);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}
