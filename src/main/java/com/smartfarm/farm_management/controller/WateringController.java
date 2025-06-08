package com.smartfarm.farm_management.controller;

import com.smartfarm.farm_management.entity.WateringLog;
import com.smartfarm.farm_management.service.WateringLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 이 DTO는 라즈베리 파이에서 워터 펌프 작동을 요청할 때의 요청 본문 형식을 정의합니다.
class WateringRequest {
    public Long plantId;
    public Integer amountMl;
    public WateringLog.Method method; // "auto" 또는 "manual"
}

@RestController
@RequestMapping("/api/watering")
public class WateringController {

    @Autowired
    private WateringLogService wateringLogService;

    // 라즈베리 파이에서 워터 펌프 작동 후 기록을 전송받는 엔드포인트
    // curl -X POST -H "Content-Type: application/json" -d '{"plantId":1, "amountMl":200, "method":"auto"}' http://[라즈베리파이_IP]:8080/api/watering/log
    @PostMapping("/log")
    public ResponseEntity<String> logWatering(@RequestBody WateringRequest request) {
        try {
            wateringLogService.createWateringLog(request.plantId, request.amountMl, request.method);
            return ResponseEntity.status(HttpStatus.CREATED).body("Watering logged successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error logging watering: " + e.getMessage());
        }
    }

    // 특정 식물의 관수 기록 조회 (최신순)
    @GetMapping("/logs/plant/{plantId}")
    public ResponseEntity<List<WateringLog>> getWateringLogsByPlant(@PathVariable Long plantId) {
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