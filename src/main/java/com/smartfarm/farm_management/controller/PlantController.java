package com.smartfarm.farm_management.controller;

import com.smartfarm.farm_management.entity.Plant;
import com.smartfarm.farm_management.service.PlantService;
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

@RestController
@RequestMapping("/api/plants")
@Tag(name = "식물 정보 관리 (Plant Management)", description = "스마트 팜 시스템에 등록된 식물 정보를 관리하고 조회하는 API")
public class PlantController {

    @Autowired
    private PlantService plantService;

    @Operation(summary = "모든 식물 정보 조회", description = "시스템에 등록된 **모든 식물**의 상세 정보를 목록 형태로 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "모든 식물 정보를 성공적으로 조회했습니다.",
                    content = @Content(schema = @Schema(implementation = Plant.class),
                            examples = @ExampleObject(name = "Plant List Example", value = "[{\"id\":1,\"name\":\"토마토\",\"plantType\":\"과채류\",\"minTemp\":20.0,\"maxTemp\":30.0,\"minHumidity\":60.0,\"maxHumidity\":80.0,\"minSoilMoisture\":30.0,\"maxSoilMoisture\":70.0},{\"id\":2,\"name\":\"상추\",\"plantType\":\"엽채류\",\"minTemp\":15.0,\"maxTemp\":25.0,\"minHumidity\":70.0,\"maxHumidity\":90.0,\"minSoilMoisture\":40.0,\"maxSoilMoisture\":80.0}]"))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류 발생")
    })
    @GetMapping
    public ResponseEntity<List<Plant>> getAllPlants() {
        List<Plant> plants = plantService.getAllPlants();
        return ResponseEntity.ok(plants);
    }

    @Operation(summary = "특정 식물 정보 조회", description = "**식물 ID**를 사용하여 특정 식물의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청한 식물 정보를 성공적으로 찾았습니다.",
                    content = @Content(schema = @Schema(implementation = Plant.class),
                            examples = @ExampleObject(name = "Single Plant Example", value = "{\"id\":1,\"name\":\"토마토\",\"plantType\":\"과채류\",\"minTemp\":20.0,\"maxTemp\":30.0,\"minHumidity\":60.0,\"maxHumidity\":80.0,\"minSoilMoisture\":30.0,\"maxSoilMoisture\":70.0}"))),
            @ApiResponse(responseCode = "404", description = "해당 ID에 해당하는 식물을 찾을 수 없습니다.")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Plant> getPlantById(
            @Parameter(description = "조회할 식물의 **고유 ID** (Long 타입)", example = "1")
            @PathVariable Long id) {
        return plantService.getPlantById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "새로운 식물 정보 추가", description = "**새로운 식물 정보**를 시스템에 등록합니다. 이 작업은 주로 관리자에 의해 수행됩니다. `id` 필드는 자동 생성되므로 요청 본문에 포함하지 마세요.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "새롭게 등록할 식물의 상세 정보. 적정 환경 기준(온도, 습도, 토양 수분)을 포함할 수 있습니다.", required = true,
                    content = @Content(schema = @Schema(implementation = Plant.class),
                            examples = @ExampleObject(name = "New Plant Example", value = "{\"name\":\"딸기\", \"plantType\":\"과채류\", \"minTemp\":18.0, \"maxTemp\":28.0, \"minHumidity\":65.0, \"maxHumidity\":85.0, \"minSoilMoisture\":35.0, \"maxSoilMoisture\":75.0}"))))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "식물 정보가 성공적으로 생성되어 DB에 등록되었습니다.",
                    content = @Content(schema = @Schema(implementation = Plant.class))),
            @ApiResponse(responseCode = "400", description = "요청 본문의 형식이 잘못되었거나 필수 필드가 누락되었습니다.")
    })
    @PostMapping
    public ResponseEntity<Plant> createPlant(@RequestBody Plant plant) {
        Plant createdPlant = plantService.createPlant(plant);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPlant);
    }

    @Operation(summary = "식물 정보 업데이트", description = "기존에 등록된 식물의 **부분 또는 전체 정보**를 업데이트합니다. 식물 ID를 사용하여 대상을 지정합니다. 이 작업은 주로 관리자에 의해 수행됩니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "업데이트할 식물의 새로운 정보. 변경하고자 하는 필드만 포함할 수 있습니다.", required = true,
                    content = @Content(schema = @Schema(implementation = Plant.class),
                            examples = @ExampleObject(name = "Update Plant Example", value = "{\"name\":\"토마토 (품종 개량)\", \"maxTemp\":32.0, \"minSoilMoisture\":40.0}"))))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "식물 정보가 성공적으로 업데이트되었습니다.",
                    content = @Content(schema = @Schema(implementation = Plant.class))),
            @ApiResponse(responseCode = "404", description = "업데이트하려는 식물 ID를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "400", description = "요청 본문의 형식이 잘못되었습니다.")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Plant> updatePlant(
            @Parameter(description = "업데이트할 식물의 **고유 ID**", example = "1")
            @PathVariable Long id,
            @RequestBody Plant plant) {
        try {
            Plant updatedPlant = plantService.updatePlant(id, plant);
            return ResponseEntity.ok(updatedPlant);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build(); // 더 구체적인 오류 응답을 반환할 수 있습니다.
        }
    }

    @Operation(summary = "식물 정보 삭제", description = "**식물 ID**를 통해 특정 식물의 모든 정보를 시스템에서 영구적으로 삭제합니다. 이 작업은 신중하게 수행해야 합니다. (관리자용)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "식물 정보가 성공적으로 삭제되었습니다. (No Content)"),
            @ApiResponse(responseCode = "404", description = "삭제하려는 식물 ID를 찾을 수 없습니다.")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlant(
            @Parameter(description = "삭제할 식물의 **고유 ID**", example = "1")
            @PathVariable Long id) {
        try {
            plantService.deletePlant(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build(); // 더 구체적인 오류 응답을 반환할 수 있습니다.
        }
    }
}