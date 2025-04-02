package com.jobbridge.jobbridge_backend.service;

import com.jobbridge.jobbridge_backend.dto.LoginRequest;
import com.jobbridge.jobbridge_backend.dto.SignupRequest;
import com.jobbridge.jobbridge_backend.entity.User;
import com.jobbridge.jobbridge_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final EmailVerificationService emailVerificationService;

    public void login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("이메일이 존재하지 않습니다."));

        if (!user.isVerified()) {
            throw new IllegalArgumentException("이메일 인증이 필요합니다.");
        }

        if (!passwordEncoder.matches(request.getPw(), user.getPw())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
    }

    // 로그인 시 사용자 정보 반환하는 메소드 추가
    public User loginAndGetUser(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("이메일이 존재하지 않습니다."));

        if (!user.isVerified()) {
            throw new IllegalArgumentException("이메일 인증이 필요합니다.");
        }

        if (!passwordEncoder.matches(request.getPw(), user.getPw())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        return user;
    }

    public void signup(SignupRequest request) {
        // 이미 가입된 이메일인지 확인
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        // 이메일 인증 여부 확인
        if (!emailVerificationService.isEmailVerified(request.getEmail())) {
            throw new IllegalArgumentException("이메일 인증이 필요합니다.");
        }

        // 비밀번호 암호화
        String encodedPw = passwordEncoder.encode(request.getPw());

        // userType을 ENUM으로 변환
        User.UserType userType = User.UserType.valueOf(request.getUserType().toUpperCase());

        // 유저 생성
        User user = User.builder()
                .pw(encodedPw)
                .name(request.getName())
                .address(request.getAddress())
                .age(request.getAge())
                .email(request.getEmail())
                .phonenumber(request.getPhonenumber())
                .userType(userType)
                .verified(true) // 이메일 인증은 이미 완료된 상태
                .build();

        userRepository.save(user);
    }
}