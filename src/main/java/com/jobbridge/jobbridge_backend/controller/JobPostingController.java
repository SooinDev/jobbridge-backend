package com.jobbridge.jobbridge_backend.controller;

import com.jobbridge.jobbridge_backend.dto.JobPostingDto;
import com.jobbridge.jobbridge_backend.entity.JobPosting;
import com.jobbridge.jobbridge_backend.security.JwtTokenProvider;
import com.jobbridge.jobbridge_backend.service.JobPostingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/job-posting")
@CrossOrigin(origins = "http://localhost:5173")
public class JobPostingController {

    private final JobPostingService jobPostingService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping
    public ResponseEntity<JobPostingDto.Response> createJobPosting(
            @RequestHeader("Authorization") String authorization,
            @RequestBody JobPostingDto.Request request) {
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

    private String getUserEmailFromToken(String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            return jwtTokenProvider.getUserEmail(token);
        }
        throw new IllegalArgumentException("유효한 토큰이 아닙니다.");
    }
}