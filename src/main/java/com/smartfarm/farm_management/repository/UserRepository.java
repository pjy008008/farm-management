package com.smartfarm.farm_management.repository;

import com.smartfarm.farm_management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username); // 사용자 이름으로 조회
    boolean existsByUsername(String username); // 사용자 이름 중복 확인
}