package com.smartfarm.farm_management.service;

import com.smartfarm.farm_management.entity.Plant;
import com.smartfarm.farm_management.entity.PumpCommand;
import com.smartfarm.farm_management.repository.PlantRepository;
import com.smartfarm.farm_management.repository.PumpCommandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PumpCommandService {

    @Autowired
    private PumpCommandRepository pumpCommandRepository;
    @Autowired
    private PlantRepository plantRepository;

    // 사용자/관리자가 관수 명령을 생성
    public PumpCommand createPumpCommand(Long plantId, Integer amountMl, PumpCommand.Method method, LocalDateTime scheduledTime) {
        Plant plant = plantRepository.findById(plantId)
                .orElseThrow(() -> new RuntimeException("Plant not found with id " + plantId));

        PumpCommand command = new PumpCommand();
        command.setPlant(plant);
        command.setAmountMl(amountMl);
        command.setMethod(method);
        command.setStatus(PumpCommand.CommandStatus.PENDING); // 처음 생성 시 대기 상태
        command.setScheduledTime(scheduledTime); // 예약 관수 시간 (즉시 관수면 null)
        return pumpCommandRepository.save(command);
    }

    // 라즈베리 파이가 가져갈 PENDING 상태의 명령 조회 (가장 오래된 것부터)
    public List<PumpCommand> getPendingCommands() {
        // 예약 시간을 고려하여 현재 시간이 예약 시간 이후인 PENDING 명령만 가져옴
        return pumpCommandRepository.findByStatusAndScheduledTimeBeforeOrderByCreatedAtAsc(PumpCommand.CommandStatus.PENDING, LocalDateTime.now());
    }

    // 특정 식물에 대한 PENDING 명령 조회 (선택 사항)
    public Optional<PumpCommand> getPendingCommandForPlant(Long plantId) {
        return pumpCommandRepository.findByPlant_IdAndStatusOrderByCreatedAtAsc(plantId, PumpCommand.CommandStatus.PENDING)
                .stream().findFirst(); // 여러 개 있을 수 있지만, 여기서는 가장 오래된 하나만
    }

    // 라즈베리 파이가 명령을 실행한 후 상태 업데이트
    public PumpCommand markCommandAsExecuted(Long commandId) {
        return pumpCommandRepository.findById(commandId).map(command -> {
            command.setStatus(PumpCommand.CommandStatus.EXECUTED);
            command.setExecutedAt(LocalDateTime.now());
            return pumpCommandRepository.save(command);
        }).orElseThrow(() -> new RuntimeException("Pump command not found with id " + commandId));
    }

    // 모든 명령 기록 조회
    public List<PumpCommand> getAllCommands() {
        return pumpCommandRepository.findAll();
    }

    // 특정 식물에 대한 모든 명령 기록 조회
    public List<PumpCommand> getCommandsByPlant(Long plantId) {
        Plant plant = plantRepository.findById(plantId)
                .orElseThrow(() -> new RuntimeException("Plant not found with id " + plantId));
        return pumpCommandRepository.findByPlantOrderByCreatedAtAsc(plant); // Plant 엔티티 객체를 이용한 쿼리
    }

    // PumpCommandRepository에 findByPlantOrderByCreatedAtAsc 추가해야 함
    // (JPA는 ManyToOne 관계에서 Plant 객체로도 쿼리 가능)
    // List<PumpCommand> findByPlantOrderByCreatedAtAsc(Plant plant);
}
