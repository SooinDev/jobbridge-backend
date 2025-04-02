package com.jobbridge.jobbridge_backend.controller;

import com.jobbridge.jobbridge_backend.dto.EmailRequest;
import com.jobbridge.jobbridge_backend.dto.LoginRequest;
import com.jobbridge.jobbridge_backend.dto.SignupRequest;
import com.jobbridge.jobbridge_backend.dto.VerificationRequest;
import com.jobbridge.jobbridge_backend.entity.User;
import com.jobbridge.jobbridge_backend.repository.UserRepository;
import com.jobbridge.jobbridge_backend.service.EmailService;
import com.jobbridge.jobbridge_backend.service.UserService;
import com.jobbridge.jobbridge_backend.service.EmailVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final EmailVerificationService emailVerificationService;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // 로그인 시 사용자 정보 반환
            User user = userService.loginAndGetUser(request);

            // 응답 데이터 구성
            Map<String, Object> response = new HashMap<>();
            response.put("message", "로그인 성공!");
            response.put("name", user.getName());
            response.put("email", user.getEmail());
            response.put("userType", user.getUserType().toString());

            return ResponseEntity.ok(response);
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

        return ResponseEntity.ok("인증번호가 전송되었습니다.");
    }

    // 사용자 정보 조회 API 추가
    @GetMapping("/{email}")
    public ResponseEntity<?> getUserInfo(@PathVariable String email) {
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            // 민감한 정보 제외하고 반환
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("name", user.getName());
            userInfo.put("email", user.getEmail());
            userInfo.put("address", user.getAddress());
            userInfo.put("age", user.getAge());
            userInfo.put("phonenumber", user.getPhonenumber());
            userInfo.put("userType", user.getUserType().toString());
            userInfo.put("verified", user.isVerified());

            return ResponseEntity.ok(userInfo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}