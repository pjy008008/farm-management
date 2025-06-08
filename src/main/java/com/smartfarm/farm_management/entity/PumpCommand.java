package com.smartfarm.farm_management.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

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
}