package com.smartfarm.farm_management.controller;

import com.smartfarm.farm_management.entity.PlantStatusLog;
import com.smartfarm.farm_management.service.PlantStatusLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

// 이 DTO는 라즈베리 파이에서 센서 데이터를 보낼 때의 요청 본문 형식을 정의합니다.
// DTO(Data Transfer Object)는 엔티티와 분리하여 요청/응답에 사용합니다.
@Schema(description = "라즈베리 파이에서 백엔드로 전송되는 센서 데이터 요청 모델")
class SensorDataRequest {
    @Schema(description = "센서 데이터가 측정된 식물의 고유 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    public Long plantId;
    @Schema(description = "DHT-22 센서로 측정된 온도 (섭씨)", example = "25.5", nullable = true)
    public Float temperature;
    @Schema(description = "DHT-22 센서로 측정된 습도 (%)", example = "60.2", nullable = true)
    public Float humidity;
    @Schema(description = "FC-28 센서로 측정된 토양 수분 (%)", example = "45.1", nullable = true)
    public Float soilMoisture;
}

@RestController
@RequestMapping("/api/sensors")
@Tag(name = "센서 데이터 관리 (Sensor Data Management)", description = "라즈베리 파이로부터 실시간 센서 데이터를 수신하고, 기록된 센서 로그를 조회하는 API")
public class SensorController {

    @Autowired
    private PlantStatusLogService plantStatusLogService;

    @Operation(summary = "[라즈베리파이 API] 센서 데이터 수신 및 기록", description = "라즈베리 파이에서 측정된 DHT-22 (온도, 습도) 및 FC-28 (토양 수분) 센서 데이터를 수신하여 DB에 기록합니다. 데이터를 기록한 후, 시스템은 해당 식물의 적정 환경 기준과 비교하여 자동으로 경고를 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "센서 데이터가 성공적으로 수신 및 처리되어 기록되었습니다."),
            @ApiResponse(responseCode = "400", description = "요청 본문의 형식이 잘못되었거나 필수 필드가 누락되었습니다 (예: 유효하지 않은 식물 ID)."),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류 발생")
    })
    @PostMapping("/data")
    public ResponseEntity<String> receiveSensorData(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "전송할 센서 데이터 (식물 ID는 필수)", required = true,
                    content = @Content(schema = @Schema(implementation = SensorDataRequest.class),
                            examples = @ExampleObject(name = "Sensor Data Example", value = "{\"plantId\":1, \"temperature\":25.5, \"humidity\":60.2, \"soilMoisture\":45.1}")))
            @RequestBody SensorDataRequest data) {
        try {
            plantStatusLogService.createPlantStatusLog(
                    data.plantId, data.temperature, data.humidity, data.soilMoisture
            );
            return ResponseEntity.status(HttpStatus.CREATED).body("Sensor data received and processed.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error processing sensor data: " + e.getMessage());
        }
    }

    @Operation(summary = "[웹 API] 특정 식물의 최신 센서 로그 조회", description = "지정된 **식물 ID**에 대한 최신 센서 데이터 기록들을 **최신순**으로 조회합니다. 이 API는 특정 식물의 현재 상태를 빠르게 파악하는 데 유용합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "센서 로그 목록을 성공적으로 조회했습니다.",
                    content = @Content(schema = @Schema(implementation = PlantStatusLog.class),
                            examples = @ExampleObject(name = "Plant Status Log Example", value = "[{\"id\":201,\"plant\":{\"id\":1,\"name\":\"토마토\"},\"temperature\":25.5,\"humidity\":60.2,\"soilMoisture\":45.1,\"timestamp\":\"2025-06-08T15:00:00\"},{\"id\":200,\"plant\":{\"id\":1,\"name\":\"토마토\"},\"temperature\":25.0,\"humidity\":61.0,\"soilMoisture\":46.0,\"timestamp\":\"2025-06-08T14:55:00\"}]"))),
            @ApiResponse(responseCode = "404", description = "해당 ID의 식물을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류 발생")
    })
    @GetMapping("/logs/plant/{plantId}")
    public ResponseEntity<List<PlantStatusLog>> getSensorLogsByPlant(
            @Parameter(description = "센서 로그를 조회할 식물의 **고유 ID** (Long 타입)", example = "1")
            @PathVariable Long plantId) {
        try {
            List<PlantStatusLog> logs = plantStatusLogService.getLogsByPlant(plantId);
            return ResponseEntity.ok(logs);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "[웹 API] 특정 식물의 센서 로그 시간 범위 조회", description = "지정된 **식물 ID**와 **특정 시간 범위** 내에 기록된 센서 데이터 로그들을 조회합니다. `start`와 `end` 파라미터로 시간 범위를 지정할 수 있습니다. 결과는 오래된 순으로 정렬됩니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "지정된 시간 범위 내 센서 로그 목록을 성공적으로 조회했습니다.",
                    content = @Content(schema = @Schema(implementation = PlantStatusLog.class))),
            @ApiResponse(responseCode = "400", description = "요청 파라미터의 형식이 잘못되었거나 (예: 날짜 형식 오류), 유효하지 않은 식물 ID입니다."),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류 발생")
    })
    @GetMapping("/logs/plant/{plantId}/range")
    public ResponseEntity<List<PlantStatusLog>> getSensorLogsByPlantAndRange(
            @Parameter(description = "센서 로그를 조회할 식물의 **고유 ID** (Long 타입)", example = "1")
            @PathVariable Long plantId,
            @Parameter(description = "조회 시작 시간 (ISO 8601 형식: `YYYY-MM-DDTHH:MM:SS`)", example = "2025-06-01T00:00:00", required = true)
            @RequestParam String start,
            @Parameter(description = "조회 종료 시간 (ISO 8601 형식: `YYYY-MM-DDTHH:MM:SS`)", example = "2025-06-08T23:59:59", required = true)
            @RequestParam String end) {
        try {
            LocalDateTime startTime = LocalDateTime.parse(start);
            LocalDateTime endTime = LocalDateTime.parse(end);
            List<PlantStatusLog> logs = plantStatusLogService.getLogsByPlantAndTimeRange(plantId, startTime, endTime);
            return ResponseEntity.ok(logs);
        } catch (RuntimeException e) {
            // RuntimeException은 plantService.getLogsByPlantAndTimeRange에서 발생할 수 있는 식물 ID 오류
            // DateTimeParseException은 날짜 문자열 파싱 오류
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}