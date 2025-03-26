package com.jobbridge.jobbridge_backend.service;

import com.jobbridge.jobbridge_backend.dto.LoginRequest;
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

    public void login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("이메일이 존재하지 않습니다."));

        if (!passwordEncoder.matches(request.getPw(), user.getPw())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 로그인 성공 처리 (세션 저장 or 토큰 발급 등은 추후)
    }
}
