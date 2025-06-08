package com.smartfarm.farm_management.controller;

import com.smartfarm.farm_management.entity.Alert;
import com.smartfarm.farm_management.service.AlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@Tag(name = "경고 로그 관리 (Alert Log Management)", description = "식물의 센서 데이터가 적정 범위를 벗어났을 때 발생하는 경고 이력을 조회하는 API")
public class AlertController {

    @Autowired
    private AlertService alertService;

    @Operation(summary = "특정 식물의 모든 경고 조회", description = "지정된 **식물 ID**에 대해 발생한 모든 경고 기록을 **최신순**으로 조회합니다. 이는 온도, 습도, 토양 수분 등 모든 센서 유형의 경고를 포함합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "경고 로그 목록을 성공적으로 조회했습니다.",
                    content = @Content(schema = @Schema(implementation = Alert.class),
                            examples = @ExampleObject(name = "Alert List Example", value = "[{\"id\":101,\"plant\":{\"id\":1,\"name\":\"토마토\"},\"sensorType\":\"temp\",\"value\":18.5,\"thresholdType\":\"min\",\"timestamp\":\"2025-06-08T14:30:00\"},{\"id\":102,\"plant\":{\"id\":1,\"name\":\"토마토\"},\"sensorType\":\"humidity\",\"value\":92.0,\"thresholdType\":\"max\",\"timestamp\":\"2025-06-08T14:00:00\"}]"))),
            @ApiResponse(responseCode = "404", description = "해당 ID의 식물을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류 발생")
    })
    @GetMapping("/plant/{plantId}")
    public ResponseEntity<List<Alert>> getAlertsByPlant(
            @Parameter(description = "경고 로그를 조회할 식물의 **고유 ID** (Long 타입)", example = "1")
            @PathVariable Long plantId) {
        try {
            List<Alert> alerts = alertService.getAlertsByPlant(plantId);
            return ResponseEntity.ok(alerts);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "특정 식물의 센서 유형별 경고 조회", description = "지정된 **식물 ID**와 **센서 유형**에 대해 발생한 경고 기록을 **최신순**으로 조회합니다. 센서 유형은 'temp', 'humidity', 'soil_moisture' 중 하나입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "센서 유형별 경고 로그 목록을 성공적으로 조회했습니다.",
                    content = @Content(schema = @Schema(implementation = Alert.class),
                            examples = @ExampleObject(name = "Temperature Alerts Example", value = "[{\"id\":101,\"plant\":{\"id\":1,\"name\":\"토마토\"},\"sensorType\":\"temp\",\"value\":18.5,\"thresholdType\":\"min\",\"timestamp\":\"2025-06-08T14:30:00\"}]"))),
            @ApiResponse(responseCode = "404", description = "해당 ID의 식물을 찾을 수 없거나, 유효하지 않은 센서 유형입니다."),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류 발생")
    })
    @GetMapping("/plant/{plantId}/sensor/{sensorType}")
    public ResponseEntity<List<Alert>> getAlertsByPlantAndSensorType(
            @Parameter(description = "경고 로그를 조회할 식물의 **고유 ID** (Long 타입)", example = "1")
            @PathVariable Long plantId,
            @Parameter(description = "조회할 센서의 **유형** ('temp', 'humidity', 'soil_moisture' 중 하나)", example = "temp",
                    schema = @Schema(type = "string", allowableValues = {"temp", "humidity", "soil_moisture"}))
            @PathVariable Alert.SensorType sensorType) {
        try {
            List<Alert> alerts = alertService.getAlertsByPlantAndSensorType(plantId, sensorType);
            return ResponseEntity.ok(alerts);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}