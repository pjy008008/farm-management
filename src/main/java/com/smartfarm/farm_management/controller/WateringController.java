package com.smartfarm.farm_management.controller;

import com.smartfarm.farm_management.entity.WateringLog;
import com.smartfarm.farm_management.service.WateringLogService;
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

import java.util.List;

// 이 DTO는 라즈베리 파이에서 워터 펌프 작동 후 기록을 백엔드로 전송할 때의 요청 본문 형식을 정의합니다.
@Schema(description = "워터 펌프 관수 작동 후 기록을 위한 요청 모델")
class WateringRequest {
    @Schema(description = "관수 대상 식물의 고유 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    public Long plantId;
    @Schema(description = "실제로 관수된 양 (밀리리터)", example = "200", requiredMode = Schema.RequiredMode.REQUIRED)
    public Integer amountMl;
    @Schema(description = "관수 방법 ('auto': 자동 관수, 'manual': 수동 관수)", example = "auto", allowableValues = {"auto", "manual"}, requiredMode = Schema.RequiredMode.REQUIRED)
    public WateringLog.Method method; // "auto" 또는 "manual"
}

@RestController
@RequestMapping("/api/watering")
@Tag(name = "관수 기록 관리 (Watering Log Management)", description = "워터 펌프의 관수 이력을 기록하고 조회하는 API")
public class WateringController {

    @Autowired
    private WateringLogService wateringLogService;

    @Operation(summary = "워터 펌프 작동 기록", description = "라즈베리 파이에서 워터 펌프 작동이 완료된 후, 해당 관수 이력을 백엔드에 기록합니다. 이 기록은 관수량, 관수 방법, 대상 식물 정보를 포함합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "관수 기록이 성공적으로 저장되었습니다."),
            @ApiResponse(responseCode = "400", description = "요청 본문의 형식이 잘못되었거나 필수 필드가 누락되었습니다 (예: 유효하지 않은 식물 ID)."),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류 발생")
    })
    @PostMapping("/log")
    public ResponseEntity<String> logWatering(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "워터 펌프 작동 후 전송할 기록 데이터", required = true,
                    content = @Content(schema = @Schema(implementation = WateringRequest.class),
                            examples = @ExampleObject(name = "Watering Log Example", value = "{\"plantId\":1, \"amountMl\":200, \"method\":\"auto\"}")))
            @RequestBody WateringRequest request) {
        try {
            wateringLogService.createWateringLog(request.plantId, request.amountMl, request.method);
            return ResponseEntity.status(HttpStatus.CREATED).body("Watering logged successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error logging watering: " + e.getMessage());
        }
    }

    @Operation(summary = "특정 식물의 관수 기록 조회", description = "지정된 **식물 ID**에 대해 모든 관수 기록을 **최신순**으로 조회합니다. 이 API는 특정 식물의 관수 이력을 추적하는 데 사용됩니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "관수 기록 목록을 성공적으로 조회했습니다.",
                    content = @Content(schema = @Schema(implementation = WateringLog.class),
                            examples = @ExampleObject(name = "Watering Log List Example", value = "[{\"id\":301,\"plant\":{\"id\":1,\"name\":\"상추\"},\"amountMl\":150,\"method\":\"manual\",\"timestamp\":\"2025-06-08T16:00:00\"},{\"id\":300,\"plant\":{\"id\":1,\"name\":\"상추\"},\"amountMl\":200,\"method\":\"auto\",\"timestamp\":\"2025-06-08T12:00:00\"}]"))),
            @ApiResponse(responseCode = "404", description = "해당 ID의 식물을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류 발생")
    })
    @GetMapping("/logs/plant/{plantId}")
    public ResponseEntity<List<WateringLog>> getWateringLogsByPlant(
            @Parameter(description = "관수 기록을 조회할 식물의 **고유 ID** (Long 타입)", example = "1")
            @PathVariable Long plantId) {
        try {
            List<WateringLog> logs = wateringLogService.getLogsByPlant(plantId);
            return ResponseEntity.ok(logs);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // TODO: 라즈베리파이에서 워터펌프를 직접 제어하는 API가 필요할 수 있습니다.
    // 이 API는 Spring Boot가 라즈베리 파이에게 특정 액츄에이터를 작동시키라는 명령을 내리는 구조일 때 유용합니다.
    // 구현 방식은 여러 가지가 있습니다:
    // 1. Spring Boot에서 SSH를 통해 Python 스크립트를 원격 실행 (복잡하고 보안 이슈)
    // 2. Python 스크립트에서 Flask/FastAPI 같은 경량 웹 서버를 띄우고, Spring에서 해당 웹 서버로 HTTP 요청을 보내 제어
    // 3. MQTT와 같은 메시지 브로커를 사용하여 통신 (Spring은 MQTT 발행, Python은 MQTT 구독하여 명령 수신)
    // 현재 `curl` 통신 구조를 고려하면, Python에서 펌프 작동 후 백엔드에 기록하는 방식이 더 적합할 수 있습니다.
    // 즉, 펌프 작동은 Python에서 독립적으로 판단하고, 작동 완료 후 /api/watering/log 로 기록을 전송하는 것이죠.
    // 만약 Spring에서 직접 펌프를 켜고 끄는 명령을 내리려면, 위 2번 또는 3번 방법을 고려해야 합니다.
}