package com.jobbridge.jobbridge_backend.controller;

import com.jobbridge.jobbridge_backend.dto.JobPostingDto;
import com.jobbridge.jobbridge_backend.security.JwtTokenProvider;
import com.jobbridge.jobbridge_backend.service.JobPostingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/job-posting")
public class JobPostingController {

    private final JobPostingService jobPostingService;
    private final JwtTokenProvider jwtTokenProvider; // JwtTokenProvider 주입 필요

    @PostMapping
    public ResponseEntity<JobPostingDto.Response> createJobPosting(
            @RequestHeader("Authorization") String authorization,
            @RequestBody JobPostingDto.Request request) {
        // 토큰에서 이메일을 추출하는 로직 필요 (임시로 이메일을 직접 받는 것으로 대체)
        String email = getUserEmailFromToken(authorization);
        JobPostingDto.Response response = jobPostingService.createJobPosting(email, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    public ResponseEntity<List<JobPostingDto.Response>> getMyJobPostings(
            @RequestHeader("Authorization") String authorization) {
        String email = getUserEmailFromToken(authorization);
        List<JobPostingDto.Response> responses = jobPostingService.getCompanyJobPostings(email);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobPostingDto.Response> getJobPosting(@PathVariable Long id) {
        JobPostingDto.Response response = jobPostingService.getJobPosting(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobPostingDto.Response> updateJobPosting(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authorization,
            @RequestBody JobPostingDto.Request request) {
        String email = getUserEmailFromToken(authorization);
        JobPostingDto.Response response = jobPostingService.updateJobPosting(id, email, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJobPosting(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authorization) {
        String email = getUserEmailFromToken(authorization);
        jobPostingService.deleteJobPosting(id, email);
        return ResponseEntity.ok().build();
    }

    // JWT 토큰에서 이메일을 추출하는 메소드 (실제로는 JWT 라이브러리 사용)
    private String getUserEmailFromToken(String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            return jwtTokenProvider.getUserEmail(token);
        }
        throw new IllegalArgumentException("유효한 토큰이 아닙니다.");
    }
}