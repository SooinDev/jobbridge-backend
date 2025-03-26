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

    // tt
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("이메일이 존재하지 않습니다."));

        if (!passwordEncoder.matches(request.getPw(), user.getPw())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
    }

    public void signup(SignupRequest request) {
        // 이미 존재하는 이메일인지 확인
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        // 비밀번호 암호화
        String encodedPw = passwordEncoder.encode(request.getPw());

        // 유저 생성 및 저장
        User user = User.builder()
                .pw(encodedPw)
                .name(request.getName())
                .address(request.getAddress())
                .age(request.getAge())
                .email(request.getEmail())
                .phonenumber(request.getPhonenumber())
                .build();

        userRepository.save(user);
    }
}
