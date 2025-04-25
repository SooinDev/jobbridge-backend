package com.jobbridge.jobbridge_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "job_posting")
public class JobPosting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 내부 PK 또는 사라민 ID 그대로 사용 가능

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description; // 사람인 공고라면 "사람인에서 수집된 공고입니다." 로 설정

    @Column(nullable = false)
    private String position;

    @Column(name = "required_skills")
    private String requiredSkills;

    @Column(name = "experience_level")
    private String experienceLevel;

    @Column
    private String location;

    @Column
    private String salary;

    @Column(name = "deadline")
    private LocalDateTime deadline;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private User company; // 사람인 데이터는 null

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(nullable = false)
    private String source = "USER"; // "USER" 또는 "SARAMIN" 으로 구분

    @Column
    private String url; // 사람인 상세 공고 URL

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}