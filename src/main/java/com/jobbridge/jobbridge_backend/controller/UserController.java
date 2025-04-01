package com.jobbridge.jobbridge_backend.controller;

import com.jobbridge.jobbridge_backend.dto.EmailRequest;
import com.jobbridge.jobbridge_backend.dto.LoginRequest;
import com.jobbridge.jobbridge_backend.dto.SignupRequest;
import com.jobbridge.jobbridge_backend.dto.VerificationRequest;
import com.jobbridge.jobbridge_backend.repository.UserRepository;
import com.jobbridge.jobbridge_backend.service.EmailService;
import com.jobbridge.jobbridge_backend.service.UserService;
import com.jobbridge.jobbridge_backend.service.EmailVerificationService;  // EmailVerificationService 임포트 추가
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final EmailVerificationService emailVerificationService; // EmailVerificationService 주입

    private final UserRepository userRepository;
    private final EmailService emailService;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {
        try {
            userService.login(request);
            return ResponseEntity.ok("로그인 성공!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 실패: " + e.getMessage());
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody SignupRequest request) {
        try {
            userService.signup(request);
            return ResponseEntity.ok("회원가입 성공!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("회원가입 실패: " + e.getMessage());
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyCode(@RequestBody VerificationRequest request) {
        try {
            String result = emailVerificationService.verifyCode(request.getEmail(), request.getCode());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("인증 실패: " + e.getMessage());
        }
    }

    @PostMapping("/send-code")
    public ResponseEntity<String> sendVerificationCode(@RequestBody EmailRequest request) {
        String email = request.getEmail();

        // 이메일 형식 간단 검증
        if (email == null || !email.matches("^[\\w-.]+@[\\w-]+\\.[a-zA-Z]{2,}$")) {
            return ResponseEntity.badRequest().body("유효한 이메일 주소를 입력해주세요.");
        }

        // 이미 가입된 이메일인지 확인
        if (userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.badRequest().body("이미 가입된 이메일입니다.");
        }

        // 인증번호 생성 및 이메일 발송
        emailService.sendVerificationCode(email);

        // 인증번호는 DB에 저장되지 않기 때문에 인증 확인 시 DB에 있어야 한다면 별도 로직 추가 필요

        return ResponseEntity.ok("인증번호가 전송되었습니다.");
    }
}