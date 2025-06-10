package com.smartfarm.farm_management.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails { // UserDetails 인터페이스 구현
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username; // 사용자 ID (이메일 또는 아이디)

    @Column(nullable = false)
    private String password; // 암호화된 비밀번호

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; // 사용자 권한 (예: USER, ADMIN)

    public enum Role {
        USER, ADMIN
    }

    // --- UserDetails 인터페이스 구현 메서드 ---
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name())); // 권한 반환 (예: ROLE_USER)
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // 계정 만료 여부 (true = 만료되지 않음)
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // 계정 잠금 여부 (true = 잠기지 않음)
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 비밀번호 만료 여부 (true = 만료되지 않음)
    }

    @Override
    public boolean isEnabled() {
        return true; // 계정 활성화 여부 (true = 활성화됨)
    }
}