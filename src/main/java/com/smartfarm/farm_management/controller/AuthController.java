package com.smartfarm.farm_management.controller;

import com.smartfarm.farm_management.entity.User;
import com.smartfarm.farm_management.payload.request.AuthRequest;
import com.smartfarm.farm_management.payload.response.JwtResponse;
import com.smartfarm.farm_management.security.JwtTokenProvider;
import com.smartfarm.farm_management.service.UserService;
import io.swagger.v3.oas.annotations.Operation; // Swagger 어노테이션 임포트
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag; // Tag 어노테이션 임포트
import jakarta.validation.Valid; // @Valid 임포트
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth") // 인증 관련 API는 /api/auth 경로로 시작
@Tag(name = "인증 및 사용자 관리 API", description = "회원가입, 로그인 등 사용자 인증 관련 기능을 제공합니다.") // 컨트롤러 전체에 대한 태그 추가
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
    }

    @Operation(summary = "새로운 사용자 회원가입", description = "사용자 이름과 비밀번호를 받아 새로운 계정을 생성합니다.") // API 요약 및 설명
    @ApiResponses(value = { // 다양한 응답에 대한 설명
            @ApiResponse(responseCode = "200", description = "회원가입 성공",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (예: 이미 존재하는 사용자 이름)",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string")))
    })
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody AuthRequest signUpRequest) { // @Valid 추가하여 DTO 유효성 검사
        try {
            User newUser = userService.registerNewUser(signUpRequest.getUsername(), signUpRequest.getPassword());
            return ResponseEntity.ok("User registered successfully: " + newUser.getUsername());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error registering user: " + e.getMessage());
        }
    }

    @Operation(summary = "사용자 로그인 및 JWT 토큰 발급", description = "사용자 이름과 비밀번호로 로그인하여 JWT 토큰을 발급받습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공 및 JWT 토큰 발급",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = JwtResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (잘못된 사용자 이름 또는 비밀번호)",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string")))
    })
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody AuthRequest loginRequest) { // @Valid 추가
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtTokenProvider.generateToken(authentication);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userService.findByUsername(userDetails.getUsername()); // ID와 Role을 가져오기 위해 사용자 조회

        return ResponseEntity.ok(new JwtResponse(jwt, "Bearer", user.getId(), user.getUsername(), user.getRole().name()));
    }
}