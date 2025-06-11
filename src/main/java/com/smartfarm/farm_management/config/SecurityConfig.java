package com.smartfarm.farm_management.config;

import com.smartfarm.farm_management.security.JwtAuthenticationFilter;
import com.smartfarm.farm_management.security.JwtTokenProvider;
import com.smartfarm.farm_management.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtTokenProvider jwtTokenProvider;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService, JwtTokenProvider jwtTokenProvider) {
        this.customUserDetailsService = customUserDetailsService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // 비밀번호 암호화에 BCrypt 사용
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // CSRF 보호 비활성화 (JWT 사용 시)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 사용 안 함 (JWT 스테이트리스)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/auth/**").permitAll() // /api/auth/** 경로는 인증 없이 접근 허용 (회원가입, 로그인)
                        .requestMatchers("/api/sensors/data").permitAll()
                        .requestMatchers("/api/watering/log").permitAll()
                        .requestMatchers("/api/pump_commands").permitAll()
                        .requestMatchers("/api/pump_commands/execute").permitAll()
                        .requestMatchers(
                                "/v3/api-docs/**",           // OpenAPI 3.0 docs
                                "/swagger-ui/**",            // Swagger UI
                                "/swagger-ui.html",          // Swagger UI HTML
                                "/webjars/**"                // Webjars (Swagger UI dependencies)
                        ).permitAll()
                        .requestMatchers("/api/public/**").permitAll() // 필요한 경우 공개 API 경로 추가
                        .requestMatchers("/api/admin/**").hasRole("ADMIN") // ADMIN 권한 필요
                        .anyRequest().authenticated() // 그 외 모든 요청은 인증 필요
                )
                .authenticationProvider(authenticationProvider()) // 커스텀 인증 프로바이더 등록
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, customUserDetailsService), UsernamePasswordAuthenticationFilter.class); // JWT 필터 추가

        return http.build();
    }
}