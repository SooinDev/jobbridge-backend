package com.jobbridge.jobbridge_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ApplicantInfoDto {
    private String name;
    private String email;
    private LocalDateTime appliedAt;
}