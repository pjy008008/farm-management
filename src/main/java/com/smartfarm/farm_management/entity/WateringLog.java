package com.smartfarm.farm_management.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "watering_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WateringLog {
    public enum Method {
        auto, manual
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plant_id", nullable = false)
    private Plant plant; // 대상 식물 ID

    @Column(name = "amount_ml")
    private Integer amountMl; // 관수량 (ml)

    @Enumerated(EnumType.STRING) // Enum 타입을 문자열로 저장
    @Column(nullable = false)
    private Method method; // 자동/수동 여부

    @Column(nullable = false)
    private LocalDateTime timestamp; // 관수 시간

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}