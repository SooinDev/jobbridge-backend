package com.jobbridge.jobbridge_backend.service;

import com.jobbridge.jobbridge_backend.entity.User;
import com.jobbridge.jobbridge_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final UserRepository userRepository;

    public String verifyEmail(String token) {
        Optional<User> userOptional = userRepository.findByVerificationToken(token);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setVerified(true);  // 이메일 인증 완료
            user.setVerificationToken(null);  // 인증 후 토큰 삭제
            userRepository.save(user);

            return "이메일 인증이 완료되었습니다!";
        } else {
            return "잘못된 인증 토큰입니다.";
        }
    }
}