package com.jobbridge.jobbridge_backend.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {
    private String token;
    private String name;
    private String email;
    private String userType;
}