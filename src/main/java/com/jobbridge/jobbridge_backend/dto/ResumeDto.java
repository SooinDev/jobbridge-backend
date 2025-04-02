package com.jobbridge.jobbridge_backend.dto;

import lombok.Getter;
import lombok.Setter;

public class ResumeDto {

    @Getter
    @Setter
    public static class Request {
        private String title;
        private String content;
    }

    @Getter
    @Setter
    public static class Response {
        private Long id;
        private String title;
        private String content;
        private String userName;
        private String createdAt;
        private String updatedAt;
    }
}