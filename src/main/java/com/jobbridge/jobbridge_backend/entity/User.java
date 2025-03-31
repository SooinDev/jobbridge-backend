package com.jobbridge.jobbridge_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String pw;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 255)
    private String address;

    private Integer age;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(length = 20)
    private String phonenumber;

    @Column(name = "user_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserType userType;

    @Column(name = "verification_token")
    private String verificationToken;  // 인증 토큰 저장

    @Column(name = "verified")
    private boolean verified = false;  // 이메일 인증 상태 (기본값은 false)

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now(); // 현재 시간 자동 입력
        }
    }

    public enum UserType {
        INDIVIDUAL, COMPANY;

        @Override
        public String toString() {
            return name(); // 무조건 대문자 반환
        }
    }
}