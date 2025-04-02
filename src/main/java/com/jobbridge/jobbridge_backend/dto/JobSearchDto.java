package com.jobbridge.jobbridge_backend.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class JobSearchDto {
    private String keyword;
    private String location;
    private String experienceLevel;
    private LocalDateTime deadlineAfter;
    private String[] skills;
}