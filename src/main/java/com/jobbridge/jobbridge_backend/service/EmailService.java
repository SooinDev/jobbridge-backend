package com.jobbridge.jobbridge_backend.service;


import com.jobbridge.jobbridge_backend.entity.EmailVerification;
import com.jobbridge.jobbridge_backend.repository.EmailVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender emailSender;
    private final EmailVerificationRepository emailVerificationRepository;

    public void sendVerificationCode(String toEmail) {
        // 1. 인증번호 생성
        String code = generateVerificationCode();

        // 2. 기존 인증 요청이 있다면 덮어쓰기
        Optional<EmailVerification> existing = emailVerificationRepository.findByEmail(toEmail);
        EmailVerification verification = existing.orElse(new EmailVerification());
        verification.setEmail(toEmail);
        verification.setCode(code);
        verification.setVerified(false); // 항상 재요청 시 미인증 처리
        verification.setCreatedAt(java.time.LocalDateTime.now());

        emailVerificationRepository.save(verification);

        // 3. 이메일 발송
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("JobBridge 이메일 인증번호");
        message.setText("아래 인증번호를 입력해주세요:\n\n" + code + "\n\n5분 내로 입력해주세요.");
        emailSender.send(message);
    }

    public String generateVerificationCode() {
        int code = (int)(Math.random() * 900000) + 100000; // 100000~999999
        return String.valueOf(code);
    }

    // EmailService.java에 추가할 메서드
    public void sendEmail(String toEmail, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);
        emailSender.send(message);
    }
}