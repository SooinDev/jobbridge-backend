package com.jobbridge.jobbridge_backend.dto;

import lombok.Getter;
import lombok.Setter;

public class JobDto {

    @Getter
    @Setter
    public static class Request {
        // (채용공고 생성/수정 시 사용할 필드 - 필요 시 정의)
        private String title;
        private String description;
    }

    @Getter
    @Setter
    public static class Response {
        private Long id;
        private String title;
        private String description;  // 채용공고 설명
        private String createdAt;    // 생성일자 (예: "2023-05-01T12:34:56")
        private String updatedAt;    // 수정일자
        private Double matchRate;    // AI 유사도 점수 (예: 0.85)
    }
}
