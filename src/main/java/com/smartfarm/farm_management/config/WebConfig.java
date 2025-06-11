package com.smartfarm.farm_management.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 모든 경로에 대해 CORS 허용
                .allowedOrigins("http://localhost:3000", "http://192.168.0.100:3000") // React 앱의 Origin 명시 (필요시 추가)
                // .allowedOrigins("*") // 모든 Origin 허용 (주의: 개발 단계에서만 사용 권장)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 허용할 HTTP 메서드
                .allowedHeaders("*") // 모든 헤더 허용
                .allowCredentials(true) // 자격 증명(쿠키, 인증 헤더 등) 허용
                .maxAge(3600); // Pre-flight 요청 캐시 시간 (초 단위)
    }
}