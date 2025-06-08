package com.smartfarm.farm_management.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "plant_status_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlantStatusLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // 지연 로딩
    @JoinColumn(name = "plant_id", nullable = false)
    private Plant plant; // 어떤 식물의 데이터인지 (plants.id 참조)

    private Float temperature; // 온도 측정값
    private Float humidity;    // 습도 측정값
    @Column(name = "soil_moisture")
    private Float soilMoisture; // 토양 습도 측정값

    @Column(nullable = false)
    private LocalDateTime timestamp; // 데이터 측정 시간

    @PrePersist // 엔티티가 저장되기 전에 실행
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}
