package com.jobbridge.jobbridge_backend.controller;

import com.jobbridge.jobbridge_backend.dto.MyApplicationDto;
import com.jobbridge.jobbridge_backend.entity.User;
import com.jobbridge.jobbridge_backend.security.UserDetailsImpl;
import com.jobbridge.jobbridge_backend.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    // ✅ 지원하기
    @PostMapping("/apply/{jobPostingId}")
    public ResponseEntity<String> applyToJob(
            @PathVariable Long jobPostingId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        User user = userDetails.getUser();
        applicationService.applyToJob(user, jobPostingId);
        return ResponseEntity.ok("지원이 완료되었습니다.");
    }

    // ✅ 내가 지원한 내역 보기
    @GetMapping("/applications/mine")
    public ResponseEntity<List<MyApplicationDto>> getMyApplications(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        User user = userDetails.getUser();
        List<MyApplicationDto> result = applicationService.getApplicationsByUser(user);
        return ResponseEntity.ok(result);
    }
}