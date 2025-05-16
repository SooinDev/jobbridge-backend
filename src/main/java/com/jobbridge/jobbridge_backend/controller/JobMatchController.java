package com.jobbridge.jobbridge_backend.controller;

import com.jobbridge.jobbridge_backend.dto.JobDto;
import com.jobbridge.jobbridge_backend.entity.JobPosting;
import com.jobbridge.jobbridge_backend.security.JwtTokenProvider;
import com.jobbridge.jobbridge_backend.service.JobMatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/match")
@CrossOrigin(origins = "http://localhost:5173")
public class JobMatchController {

    private final JobMatchService jobMatchService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/jobs")
    public ResponseEntity<List<JobDto.Response>> matchJobs(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam Long resumeId) {

        // 1) JWT 토큰 검증
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        String token = authorizationHeader.substring(7);
        if (!jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 2) 매칭 서비스 호출
        List<JobDto.Response> jobs = jobMatchService.findTopMatchingJobs(resumeId);

        // 3) (필요 시) DTO 변환 - 여기서는 서비스에서 이미 DTO로 반환하므로 그대로 반환 가능
        List<JobDto.Response> responseList = jobs.stream().map(job -> {
            JobDto.Response dto = new JobDto.Response();
            dto.setId(job.getId());
            dto.setTitle(job.getTitle());
            dto.setDescription(job.getDescription());
            dto.setCreatedAt(job.getCreatedAt());
            dto.setUpdatedAt(job.getUpdatedAt());
            dto.setMatchRate(job.getMatchRate());
            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(responseList);
    }
}
