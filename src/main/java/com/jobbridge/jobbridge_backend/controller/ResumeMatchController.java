package com.jobbridge.jobbridge_backend.controller;

import com.jobbridge.jobbridge_backend.dto.ResumeDto;
import com.jobbridge.jobbridge_backend.entity.Resume;
import com.jobbridge.jobbridge_backend.security.JwtTokenProvider;
import com.jobbridge.jobbridge_backend.service.ResumeMatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/match")
@CrossOrigin(origins = "http://localhost:5173")
public class ResumeMatchController {

    private final ResumeMatchService resumeMatchService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/resumes")
    public ResponseEntity<List<ResumeDto.Response>> matchResumes(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam Long jobPostingId) {

        System.out.println("[Debug] Authorization 헤더: " + authorizationHeader);

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            System.out.println("[Debug] Authorization 헤더 형식 문제");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        String token = authorizationHeader.substring(7);
        System.out.println("[Debug] 토큰 추출: " + token);

        if (!jwtTokenProvider.validateToken(token)) {
            System.out.println("[Debug] 토큰 유효성 검사 실패");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        System.out.println("[Debug] 채용공고 ID: " + jobPostingId);
        List<ResumeDto.Response> resumes = resumeMatchService.findTopMatchingResumes(jobPostingId);

        // ✅ DTO 변환
        List<ResumeDto.Response> responseList = resumes.stream().map(resume -> {
            ResumeDto.Response dto = new ResumeDto.Response();
            dto.setId(resume.getId());
            dto.setTitle(resume.getTitle());
            dto.setContent(resume.getContent());
            dto.setUserName(resume.getUserName());  // user는 반드시 EAGER fetch이거나 이미 로딩된 상태여야 함
            dto.setCreatedAt(resume.getCreatedAt());
            dto.setUpdatedAt(resume.getUpdatedAt());
            dto.setMatchRate(resume.getMatchRate());
            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(responseList);
    }
}
