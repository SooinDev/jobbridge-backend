package com.jobbridge.jobbridge_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Application {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User applicant;

    @ManyToOne(fetch = FetchType.LAZY)
    private JobPosting jobPosting;

    private LocalDateTime appliedAt;
}