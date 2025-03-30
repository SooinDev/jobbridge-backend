package com.jobbridge.jobbridge_backend.controller;

import com.jobbridge.jobbridge_backend.dto.LoginRequest;
import com.jobbridge.jobbridge_backend.dto.SignupRequest;
import com.jobbridge.jobbridge_backend.service.UserService;
import com.jobbridge.jobbridge_backend.service.EmailVerificationService;  // EmailVerificationService 임포트 추가
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final EmailVerificationService emailVerificationService; // EmailVerificationService 주입

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

    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        try {
            String result = emailVerificationService.verifyEmail(token);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("인증 실패: " + e.getMessage());
        }
    }
}