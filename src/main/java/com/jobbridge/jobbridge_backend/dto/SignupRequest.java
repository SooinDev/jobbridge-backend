package com.jobbridge.jobbridge_backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequest {
    private String pw;
    private String name;
    private String address;
    private Integer age;
    private String email;
    private String phonenumber;
    private String userType;  // 기업 또는 개인 구분
}