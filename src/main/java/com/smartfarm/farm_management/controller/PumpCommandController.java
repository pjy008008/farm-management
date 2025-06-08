package com.smartfarm.farm_management.controller;

import com.smartfarm.farm_management.entity.PumpCommand;
import com.smartfarm.farm_management.service.PumpCommandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// 관수 명령 요청 DTO
class PumpCommandRequest {
    public Long plantId;
    public Integer amountMl;
    public PumpCommand.Method method;
    public LocalDateTime scheduledTime; // 예약 관수 시간 (없으면 null)
}

// 라즈베리 파이가 명령 실행 후 결과를 알릴 때 사용 (WateringController의 /log와 유사)
class CommandExecutedRequest {
    public Long commandId;
    public Integer actualAmountMl; // 실제로 관수된 양
}


@RestController
@RequestMapping("/api/pump_commands")
public class PumpCommandController {

    @Autowired
    private PumpCommandService pumpCommandService;

    // 1. 사용자/관리자가 워터 펌프 작동 명령을 생성 (프론트엔드에서 호출)
    // curl -X POST -H "Content-Type: application/json" -d '{"plantId":1, "amountMl":150, "method":"manual", "scheduledTime":"2025-06-09T10:00:00"}' http://[라즈베리파이_IP]:8080/api/pump_commands
    // 즉시 관수 요청: -d '{"plantId":1, "amountMl":150, "method":"manual"}' (scheduledTime 필드 제외)
    @PostMapping
    public ResponseEntity<PumpCommand> createPumpCommand(@RequestBody PumpCommandRequest request) {
        try {
            PumpCommand command = pumpCommandService.createPumpCommand(
                    request.plantId, request.amountMl, request.method, request.scheduledTime
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(command);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // 에러 처리
        }
    }

    // 2. 라즈베리 파이가 주기적으로 "PENDING" 상태의 명령을 폴링하여 가져감
    // curl -X GET http://[라즈베리파이_IP]:8080/api/pump_commands/pending
    @GetMapping("/pending")
    public ResponseEntity<List<PumpCommand>> getPendingCommands() {
        List<PumpCommand> pendingCommands = pumpCommandService.getPendingCommands();
        if (pendingCommands.isEmpty()) {
            return ResponseEntity.noContent().build(); // 처리할 명령이 없으면 204 No Content
        }
        return ResponseEntity.ok(pendingCommands);
    }

    // 3. 라즈베리 파이가 명령을 실행한 후, 해당 명령의 상태를 EXECUTED로 업데이트 (Python에서 호출)
    // curl -X POST -H "Content-Type: application/json" -d '{"commandId":123, "actualAmountMl":145}' http://[라즈베리파이_IP]:8080/api/pump_commands/execute
    @PostMapping("/execute")
    public ResponseEntity<String> markCommandAsExecuted(@RequestBody CommandExecutedRequest request) {
        try {
            // 명령 상태 업데이트
            pumpCommandService.markCommandAsExecuted(request.commandId);
            // 실제 관수 기록은 WateringController의 /log 엔드포인트로 다시 전송하는 것이 좋습니다.
            // 여기서는 단순히 상태 업데이트만 하고, 관수 기록은 Python에서 별도로 /api/watering/log로 보내도록 합니다.
            // (WateringController의 WateringLogService를 여기에 주입해서 처리할 수도 있습니다.)
            return ResponseEntity.ok("Pump command " + request.commandId + " marked as executed.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Command not found or error: " + e.getMessage());
        }
    }

    // 모든 펌프 명령 기록 조회 (관리용)
    @GetMapping
    public ResponseEntity<List<PumpCommand>> getAllCommands() {
        List<PumpCommand> commands = pumpCommandService.getAllCommands();
        return ResponseEntity.ok(commands);
    }

    // 특정 식물에 대한 펌프 명령 기록 조회
    @GetMapping("/plant/{plantId}")
    public ResponseEntity<List<PumpCommand>> getCommandsByPlant(@PathVariable Long plantId) {
        try {
            List<PumpCommand> commands = pumpCommandService.getCommandsByPlant(plantId);
            return ResponseEntity.ok(commands);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
