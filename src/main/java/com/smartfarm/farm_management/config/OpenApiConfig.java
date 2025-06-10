package com.smartfarm.farm_management.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth"; // 스웨거 UI에 표시될 보안 스키마 이름

        return new OpenAPI()
                .info(new Info()
                        .title("스마트 팜 관리 시스템 API") // API 제목
                        .description("라즈베리 파이 기반 스마트 팜의 센서 데이터 수집 및 펌프 제어를 위한 백엔드 API입니다.") // API 설명
                        .version("1.0.0")) // API 버전
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName)) // 전역 보안 요구 사항 추가
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP) // HTTP 기반 보안 스키마
                                .scheme("bearer") // Bearer 토큰 사용
                                .bearerFormat("JWT") // JWT 형식 지정
                                .description("JWT 토큰을 입력해주세요. (Bearer 접두사 없이 토큰만 입력)")) // 입력 필드 설명
                );
    }
}