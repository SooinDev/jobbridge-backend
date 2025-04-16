package com.jobbridge.jobbridge_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class MyApplicationDto {
    private Long jobPostingId;
    private String jobTitle;
    private String companyName;
    private LocalDateTime appliedAt;
}