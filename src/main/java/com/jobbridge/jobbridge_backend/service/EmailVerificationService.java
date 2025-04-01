package com.jobbridge.jobbridge_backend.service;

import com.jobbridge.jobbridge_backend.entity.EmailVerification;
import com.jobbridge.jobbridge_backend.repository.EmailVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final EmailVerificationRepository emailVerificationRepository;

    public String verifyCode(String email, String code) {
        // 1. 이메일 + 코드로 인증 요청 조회
        EmailVerification verification = emailVerificationRepository
                .findByEmailAndCode(email, code)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 인증번호 또는 이메일입니다."));

        // 2. 이미 인증된 경우
        if (verification.isVerified()) {
            return "이미 인증이 완료된 이메일입니다.";
        }

        // 3. 유효 시간 확인 (5분 이내)
        if (verification.getCreatedAt().isBefore(LocalDateTime.now().minusMinutes(5))) {
            throw new IllegalArgumentException("인증번호가 만료되었습니다.");
        }

        // 4. 인증 완료 처리
        verification.setVerified(true);
        emailVerificationRepository.save(verification);

        return "이메일 인증이 완료되었습니다!";
    }

    public boolean isEmailVerified(String email) {
        return emailVerificationRepository.findByEmail(email)
                .map(EmailVerification::isVerified)
                .orElse(false);
    }
}