// CompanyApplicationController.java - 기업용 지원자 관리 API
package com.jobbridge.jobbridge_backend.controller;

import com.jobbridge.jobbridge_backend.dto.MyApplicationDto;
import com.jobbridge.jobbridge_backend.entity.Application;
import com.jobbridge.jobbridge_backend.entity.User;
import com.jobbridge.jobbridge_backend.repository.ApplicationRepository;
import com.jobbridge.jobbridge_backend.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/company/applications")
@RequiredArgsConstructor
public class CompanyApplicationController {

    private final ApplicationRepository applicationRepository;

    /**
     * 기업의 특정 채용공고에 대한 지원자 목록 조회
     */
    @GetMapping("/job/{jobPostingId}")
    public ResponseEntity<List<Map<String, Object>>> getApplicationsForJob(
            @PathVariable Long jobPostingId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        try {
            User company = userDetails.getUser();

            // 기업 회원 여부 확인
            if (company.getUserType() != User.UserType.COMPANY) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(null);
            }

            // 해당 채용공고의 지원자 목록 조회 (기본 Application 엔티티 사용)
            List<Application> applications = applicationRepository.findByJobPostingId(jobPostingId);

            // DTO로 변환 (기존 엔티티 활용)
            List<Map<String, Object>> result = applications.stream()
                    .map(app -> {
                        Map<String, Object> applicationData = new java.util.HashMap<>();
                        applicationData.put("id", app.getId());
                        applicationData.put("jobPostingId", app.getJobPosting().getId());
                        applicationData.put("applicantId", app.getApplicant().getId());
                        applicationData.put("applicantName", app.getApplicant().getName());
                        applicationData.put("applicantEmail", app.getApplicant().getEmail());
                        applicationData.put("appliedAt", app.getAppliedAt().toString());
                        applicationData.put("status", "PENDING"); // 기본 상태
                        return applicationData;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.out.println("❌ 지원자 목록 조회 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    /**
     * 기업의 모든 채용공고에 대한 지원자 통계 조회
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getApplicationStats(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        try {
            User company = userDetails.getUser();

            if (company.getUserType() != User.UserType.COMPANY) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
            }

            // 기업의 모든 지원서 조회 (기존 repository 메소드 활용)
            List<Application> allApplications = applicationRepository.findByJobPosting_Company(company);

            Map<String, Object> stats = new java.util.HashMap<>();
            stats.put("totalApplications", allApplications.size());
            stats.put("pendingApplications", allApplications.size()); // 모두 대기 상태로 처리
            stats.put("thisMonthApplications", allApplications.size()); // 간단히 전체로 처리

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}