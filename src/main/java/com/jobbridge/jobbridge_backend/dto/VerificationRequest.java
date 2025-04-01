package com.jobbridge.jobbridge_backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerificationRequest {
    private String email;
    private String code;
}