package com.smartfarm.farm_management.controller;

import com.smartfarm.farm_management.entity.PumpCommand;
import com.smartfarm.farm_management.service.PumpCommandService;
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
import java.util.List;
import java.util.Optional;

// 관수 명령 요청 DTO
@Schema(description = "워터 펌프 작동 명령 생성 요청 모델 (웹 프론트엔드 -> 백엔드)")
class PumpCommandRequest {
    @Schema(description = "명령 대상 식물의 고유 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    public Long plantId;
    @Schema(description = "관수량 (밀리리터)", example = "150", requiredMode = Schema.RequiredMode.REQUIRED)
    public Integer amountMl;
    @Schema(description = "관수 방법 ('auto': 자동, 'manual': 수동)", example = "manual", allowableValues = {"auto", "manual"}, requiredMode = Schema.RequiredMode.REQUIRED)
    public PumpCommand.Method method;
    @Schema(description = "예약 관수 시간 (ISO 8601 형식: YYYY-MM-DDTHH:MM:SS), 즉시 관수 시 생략 또는 null", example = "2025-06-09T10:00:00", nullable = true)
    public LocalDateTime scheduledTime;
}

// 라즈베리 파이가 명령 실행 후 결과를 알릴 때 사용 (WateringController의 /log와 유사)
@Schema(description = "워터 펌프 명령 실행 완료 알림 모델 (라즈베리 파이 -> 백엔드)")
class CommandExecutedRequest {
    @Schema(description = "실행이 완료된 펌프 명령의 고유 ID", example = "123", requiredMode = Schema.RequiredMode.REQUIRED)
    public Long commandId;
    @Schema(description = "실제로 관수된 양 (밀리리터), 펌프 오류 등으로 인해 요청량과 다를 수 있음", example = "145", nullable = true)
    public Integer actualAmountMl;
}


@RestController
@RequestMapping("/api/pump_commands")
@Tag(name = "워터 펌프 명령 관리 (Pump Command Management)", description = "웹에서 워터 펌프 작동 명령을 생성하고, 라즈베리 파이가 이를 받아 실행 상태를 관리하는 API")
public class PumpCommandController {

    @Autowired
    private PumpCommandService pumpCommandService;

    @Operation(summary = "워터 펌프 작동 명령 생성", description = "사용자 또는 관리자가 웹을 통해 워터 펌프에 관수 명령을 요청합니다. 생성된 명령은 `PENDING` 상태로 저장되며, 라즈베리 파이가 주기적으로 확인하여 처리합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "새로운 워터 펌프 명령 정보. 관수량, 방법, 대상 식물 ID가 필요하며, 예약 관수 시간을 지정할 수 있습니다.", required = true,
                    content = @Content(schema = @Schema(implementation = PumpCommandRequest.class),
                            examples = {
                                    @ExampleObject(name = "즉시 관수 요청 예시", value = "{\"plantId\":1, \"amountMl\":150, \"method\":\"manual\"}"),
                                    @ExampleObject(name = "예약 관수 요청 예시", value = "{\"plantId\":2, \"amountMl\":100, \"method\":\"auto\", \"scheduledTime\":\"2025-06-10T09:00:00\"}")
                            })))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "펌프 명령이 성공적으로 생성되어 대기 목록에 추가되었습니다.",
                    content = @Content(schema = @Schema(implementation = PumpCommand.class))),
            @ApiResponse(responseCode = "400", description = "요청 본문의 형식이 잘못되었거나 필수 필드가 누락되었습니다 (예: 유효하지 않은 식물 ID)."),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류 발생")
    })
    @PostMapping
    public ResponseEntity<PumpCommand> createPumpCommand(@RequestBody PumpCommandRequest request) {
        try {
            PumpCommand command = pumpCommandService.createPumpCommand(
                    request.plantId, request.amountMl, request.method, request.scheduledTime
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(command);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // 더 구체적인 오류 메시지 반환 가능
        }
    }

    @Operation(summary = "대기 중인 펌프 명령 조회 (라즈베리 파이 폴링용)", description = "라즈베리 파이가 주기적으로 호출하여 현재 `PENDING` 상태의 관수 명령 목록을 가져갑니다. **예약 시간이 현재 시간을 지난 명령만 반환**됩니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "처리해야 할 대기 중인 펌프 명령 목록 반환",
                    content = @Content(schema = @Schema(implementation = PumpCommand.class))),
            @ApiResponse(responseCode = "204", description = "현재 처리할 대기 중인 펌프 명령이 없습니다 (No Content)."),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류 발생")
    })
    @GetMapping("/pending")
    public ResponseEntity<List<PumpCommand>> getPendingCommands() {
        List<PumpCommand> pendingCommands = pumpCommandService.getPendingCommands();
        if (pendingCommands.isEmpty()) {
            return ResponseEntity.noContent().build(); // 처리할 명령이 없으면 204 No Content
        }
        return ResponseEntity.ok(pendingCommands);
    }

    @Operation(summary = "펌프 명령 실행 완료 상태 업데이트", description = "라즈베리 파이가 특정 펌프 명령을 성공적으로 실행한 후, 해당 명령의 상태를 `EXECUTED`로 업데이트합니다. 이 API는 명령의 상태만 변경하며, 실제 관수 기록은 별도로 `/api/watering/log` 엔드포인트로 전송해야 합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "실행이 완료된 펌프 명령의 ID 및 실제 관수된 양 (선택 사항)", required = true,
                    content = @Content(schema = @Schema(implementation = CommandExecutedRequest.class),
                            examples = @ExampleObject(name = "Command Execution Example", value = "{\"commandId\":123, \"actualAmountMl\":145}"))))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "펌프 명령 상태가 성공적으로 `EXECUTED`로 업데이트되었습니다."),
            @ApiResponse(responseCode = "404", description = "업데이트하려는 펌프 명령 ID를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "400", description = "요청 본문의 형식이 잘못되었습니다."),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류 발생")
    })
    @PostMapping("/execute")
    public ResponseEntity<String> markCommandAsExecuted(@RequestBody CommandExecutedRequest request) {
        try {
            pumpCommandService.markCommandAsExecuted(request.commandId);
            return ResponseEntity.ok("Pump command " + request.commandId + " marked as executed.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Command not found or error: " + e.getMessage());
        }
    }

    @Operation(summary = "모든 펌프 명령 기록 조회", description = "시스템에 생성된 **모든 펌프 명령의 기록**을 조회합니다. 이 API는 주로 관리자 또는 시스템 모니터링을 위해 사용됩니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "모든 펌프 명령 기록을 성공적으로 조회했습니다.",
                    content = @Content(schema = @Schema(implementation = PumpCommand.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류 발생")
    })
    @GetMapping
    public ResponseEntity<List<PumpCommand>> getAllCommands() {
        List<PumpCommand> commands = pumpCommandService.getAllCommands();
        return ResponseEntity.ok(commands);
    }

    @Operation(summary = "특정 식물의 펌프 명령 기록 조회", description = "지정된 **식물 ID**에 대해 생성된 모든 펌프 명령 기록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "특정 식물의 펌프 명령 기록을 성공적으로 조회했습니다.",
                    content = @Content(schema = @Schema(implementation = PumpCommand.class))),
            @ApiResponse(responseCode = "404", description = "해당 ID의 식물을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류 발생")
    })
    @GetMapping("/plant/{plantId}")
    public ResponseEntity<List<PumpCommand>> getCommandsByPlant(
            @Parameter(description = "펌프 명령 기록을 조회할 식물의 **고유 ID** (Long 타입)", example = "1")
            @PathVariable Long plantId) {
        try {
            List<PumpCommand> commands = pumpCommandService.getCommandsByPlant(plantId);
            return ResponseEntity.ok(commands);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}