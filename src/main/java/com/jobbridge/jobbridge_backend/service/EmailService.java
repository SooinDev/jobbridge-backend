package com.jobbridge.jobbridge_backend.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class EmailService {

    private final JavaMailSender emailSender;

    public EmailService(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

    public void sendVerificationEmail(String toEmail, String verificationToken) {
        String verificationUrl = "http://localhost:8080/api/user/verify?token=" + verificationToken;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("이메일 인증");
        message.setText("회원가입을 완료하려면 아래 링크를 클릭해주세요:\n" + verificationUrl);

        emailSender.send(message);
    }

    public String generateVerificationToken() {
        return UUID.randomUUID().toString();
    }
}
