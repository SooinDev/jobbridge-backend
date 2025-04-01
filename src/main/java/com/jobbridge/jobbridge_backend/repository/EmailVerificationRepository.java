package com.jobbridge.jobbridge_backend.repository;

import com.jobbridge.jobbridge_backend.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

    Optional<EmailVerification> findByEmail(String email);

    Optional<EmailVerification> findByEmailAndCode(String email, String code);
}