package com.jobbridge.jobbridge_backend.controller;

import com.jobbridge.jobbridge_backend.service.CareerRecommendationService;
import com.jobbridge.jobbridge_backend.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/match")
@CrossOrigin(origins = "http://localhost:5173")
public class CareerRecommendationController {

    private final CareerRecommendationService recommendationService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/career")
    public ResponseEntity<List<String>> recommendCareer(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam Long resumeId,
            @RequestParam Long jobPostingId) {

        // JWT 검증 (기존 방식 재사용)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().build();
        }
        String token = authHeader.substring(7);
        if (!jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.status(401).build();
        }

        // 서비스 호출
        List<String> path = recommendationService.getRecommendationPath(resumeId, jobPostingId);
        return ResponseEntity.ok(path);
    }
}
