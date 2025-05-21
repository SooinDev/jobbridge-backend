// PasswordResetService.java
package com.jobbridge.jobbridge_backend.service;

import com.jobbridge.jobbridge_backend.entity.PasswordResetToken;
import com.jobbridge.jobbridge_backend.entity.User;
import com.jobbridge.jobbridge_backend.repository.PasswordResetTokenRepository;
import com.jobbridge.jobbridge_backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    /**
     * 비밀번호 재설정 요청 처리
     * @param email 사용자 이메일
     * @return 생성된 토큰
     */
    @Transactional
    public String createPasswordResetTokenForUser(String email) {
        // 사용자 존재 여부 확인
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일의 사용자가 존재하지 않습니다."));

        // 기존 토큰 삭제
        passwordResetTokenRepository.findByEmail(email)
                .ifPresent(token -> passwordResetTokenRepository.delete(token));

        // 6자리 숫자 코드 생성
        String token = generateSixDigitCode();

        // 토큰 저장
        PasswordResetToken resetToken = new PasswordResetToken(token, email);
        passwordResetTokenRepository.save(resetToken);

        // 이메일 발송
        String subject = "JobBridge 비밀번호 재설정 코드";
        String body = "안녕하세요, " + user.getName() + "님.\n\n" +
                "비밀번호 재설정 코드는 다음과 같습니다: " + token + "\n\n" +
                "이 코드는 15분 동안 유효합니다.\n" +
                "코드를 요청하지 않으셨다면 이 이메일을 무시하세요.";

        emailService.sendEmail(email, subject, body);

        return token;
    }

    /**
     * 6자리 숫자 코드 생성
     */
    private String generateSixDigitCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // 100000-999999 범위의 숫자
        return String.valueOf(code);
    }

    /**
     * 토큰 검증 및 비밀번호 재설정
     */
    @Transactional
    public void resetPassword(String token, String newPassword) {
        // 토큰 유효성 검사
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 토큰입니다."));

        if (resetToken.isExpired()) {
            passwordResetTokenRepository.delete(resetToken);
            throw new IllegalArgumentException("만료된 토큰입니다. 새로운 코드를 요청해주세요.");
        }

        // 사용자 찾기 및 비밀번호 업데이트
        User user = userRepository.findByEmail(resetToken.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 비밀번호 변경
        user.setPw(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // 사용된 토큰 삭제
        passwordResetTokenRepository.delete(resetToken);
    }
}