package com.smartfarm.farm_management.repository;

import com.smartfarm.farm_management.entity.Plant;
import com.smartfarm.farm_management.entity.PumpCommand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PumpCommandRepository extends JpaRepository<PumpCommand, Long> {
    // PENDING 상태의 명령 중 가장 오래된 것 (혹은 특정 식물ID에 대한)을 가져옴
    // 라즈베리 파이가 폴링할 때 사용
    List<PumpCommand> findByStatusOrderByCreatedAtAsc(PumpCommand.CommandStatus status);
    List<PumpCommand> findByStatusAndScheduledTimeBeforeOrderByCreatedAtAsc(PumpCommand.CommandStatus status, LocalDateTime now);
    List<PumpCommand> findByPlantIdAndStatusOrderByCreatedAtAsc(Long plantId, PumpCommand.CommandStatus status);
    List<PumpCommand> findByPlantOrderByCreatedAtAsc(Plant plant);
}