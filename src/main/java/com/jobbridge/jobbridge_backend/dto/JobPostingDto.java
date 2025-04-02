package com.jobbridge.jobbridge_backend.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

public class JobPostingDto {

    @Getter
    @Setter
    public static class Request {
        private String title;
        private String description;
        private String position;
        private String requiredSkills;
        private String experienceLevel;
        private String location;
        private String salary;
        private LocalDateTime deadline;
    }

    @Getter
    @Setter
    public static class Response {
        private Long id;
        private String title;
        private String description;
        private String position;
        private String requiredSkills;
        private String experienceLevel;
        private String location;
        private String salary;
        private String deadline;
        private String companyName;
        private String createdAt;
    }
}