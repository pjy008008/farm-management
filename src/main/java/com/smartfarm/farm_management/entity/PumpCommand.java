package com.smartfarm.farm_management.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore; // JsonIgnore 임포트

@Entity
@Table(name = "pump_commands")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PumpCommand {
    public enum CommandStatus {
        PENDING, EXECUTED // 대기 중, 실행됨
    }

    public enum Method { // WateringLog의 Method와 동일하게 사용
        auto, manual
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plant_id", nullable = false)
    @JsonIgnore // Plant 필드를 JSON 직렬화 시 무시
    private Plant plant;

    @Column(name = "amount_ml", nullable = false)
    private Integer amountMl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Method method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommandStatus status = CommandStatus.PENDING; // 기본값 PENDING

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "executed_at")
    private LocalDateTime executedAt; // 펌프 작동이 완료된 시간

    @Column(name = "scheduled_time")
    private LocalDateTime scheduledTime; // 예약 관수 시 관수 시작 희망 시간 (즉시 관수는 null)

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // --- 추가된 부분: plantId를 반환하는 헬퍼 메서드 ---
    // 이 메서드를 통해 JSON 응답에 plantId를 포함시킬 수 있습니다.
    // Jackson은 기본적으로 getter 메서드를 필드로 인식하여 직렬화합니다.
    public Long getPlantId() {
        if (plant != null) {
            // plant 객체가 로드된 상태라면 ID를 반환
            // LAZY 로딩 상태에서 초기화되지 않은 프록시 객체에 getId()를 호출하면
            // N+1 쿼리가 발생할 수 있으나, 일반적으로는 DTO 변환 시에 처리하거나
            // 서비스 레이어에서 fetch join으로 미리 로딩하는 것이 좋습니다.
            // 여기서는 getPlant() 호출 시에 프록시가 초기화될 수 있음을 가정합니다.
            return plant.getId();
        }
        return null;
    }
}