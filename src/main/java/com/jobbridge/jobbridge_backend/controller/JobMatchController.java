package com.jobbridge.jobbridge_backend.controller;

import com.jobbridge.jobbridge_backend.security.JwtTokenProvider;
import com.jobbridge.jobbridge_backend.dto.JobPostingDto.MatchResponse;
import com.jobbridge.jobbridge_backend.service.JobMatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/job-matches")
@CrossOrigin(origins = "http://localhost:5173")
public class JobMatchController {

    private final JobMatchService jobMatchService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping
    public ResponseEntity<List<MatchResponse>> getMatches(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        // 헤더 형식 검증
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("유효한 Authorization 헤더가 필요합니다.");
        }
        // 토큰에서 이메일 추출 및 검증
        String token = authorizationHeader.substring(7);
        if (!jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String email = jwtTokenProvider.getUserEmail(token);
        // 이메일 기반으로 추천 로직 실행
        List<MatchResponse> matches = jobMatchService.matchTop5(email);
        return ResponseEntity.ok(matches);
    }
}
