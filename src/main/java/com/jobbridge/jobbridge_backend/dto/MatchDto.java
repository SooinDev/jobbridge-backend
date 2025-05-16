package com.jobbridge.jobbridge_backend.dto;

import java.util.List;

/**
 * AI 매칭 요청 및 응답에 모두 사용하는 DTO
 */
public class MatchDto {

    // --- 요청 필드 ---
    private String content;         // 비교 기준 텍스트 (채용공고 설명)
    private List<Item> items;       // 비교 대상 목록 (이력서 ID + 본문)

    // --- 응답 필드 ---
    private Long id;                // 매칭된 이력서 ID
    private Double score;           // 매칭 점수 (퍼센트 또는 0~1 스케일)

    public MatchDto() {}

    // 요청용 생성자
    public MatchDto(String content, List<Item> items) {
        this.content = content;
        this.items = items;
    }

    // 응답용 생성자
    public MatchDto(Long id, Double score) {
        this.id = id;
        this.score = score;
    }

    // 내부 아이템 클래스
    public static class Item {
        private Long id;
        private String content;

        public Item() {}
        public Item(Long id, String content) {
            this.id = id;
            this.content = content;
        }
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    // getters / setters
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public List<Item> getItems() { return items; }
    public void setItems(List<Item> items) { this.items = items; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }
}