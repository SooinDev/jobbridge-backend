package com.jobbridge.jobbridge_backend.repository;

import com.jobbridge.jobbridge_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일로 사용자 찾기
    Optional<User> findByEmail(String email);

    // 인증 토큰으로 사용자 찾기
    //Optional<User> findByVerificationToken(String verificationToken);

    // 이메일 인증 여부로 사용자 찾기
    Optional<User> findByVerified(boolean verified);
}