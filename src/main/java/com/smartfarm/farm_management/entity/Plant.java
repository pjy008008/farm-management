package com.smartfarm.farm_management.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "plants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Plant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name; // 식물 이름 (예: 토마토, 고추 등)

    @Column(name = "plant_type", nullable = false, length = 50)
    private String plantType; // 식물의 종류

    @Column(name = "min_temp")
    private Float minTemp; // 적정 최저 온도

    @Column(name = "max_temp")
    private Float maxTemp; // 적정 최고 온도

    @Column(name = "min_humidity")
    private Float minHumidity; // 적정 최저 습도

    @Column(name = "max_humidity")
    private Float maxHumidity; // 적정 최고 습도

    @Column(name = "min_soil_moisture")
    private Float minSoilMoisture; // 적정 최저 토양 습도

    @Column(name = "max_soil_moisture")
    private Float maxSoilMoisture; // 적정 최고 토양 습도
}