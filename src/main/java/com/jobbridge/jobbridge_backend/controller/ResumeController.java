package com.jobbridge.jobbridge_backend.controller;

import com.jobbridge.jobbridge_backend.dto.ResumeDto;
import com.jobbridge.jobbridge_backend.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resume")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping
    public ResponseEntity<ResumeDto.Response> createResume(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody ResumeDto.Request request) {

        String email = userDetails.getUsername(); // UserDetails에서 이메일 가져오기
        ResumeDto.Response response = resumeService.createResume(email, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    public ResponseEntity<List<ResumeDto.Response>> getMyResumes(
            @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails.getUsername();
        List<ResumeDto.Response> responses = resumeService.getUserResumes(email);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResumeDto.Response> getResume(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        ResumeDto.Response response = resumeService.getResume(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResumeDto.Response> updateResume(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody ResumeDto.Request request) {

        String email = userDetails.getUsername();
        ResumeDto.Response response = resumeService.updateResume(id, email, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResume(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails.getUsername();
        resumeService.deleteResume(id, email);
        return ResponseEntity.ok().build();
    }
}