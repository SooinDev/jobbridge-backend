package com.jobbridge.jobbridge_backend.controller;

import com.jobbridge.jobbridge_backend.dto.JobPostingDto;
import com.jobbridge.jobbridge_backend.dto.JobSearchDto;
import com.jobbridge.jobbridge_backend.service.JobSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/jobs")
public class JobSearchController {

    private final JobSearchService jobSearchService;

    @GetMapping("/search")
    public ResponseEntity<List<JobPostingDto.Response>> searchJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String experienceLevel,
            @RequestParam(required = false) Boolean activeOnly) {

        JobSearchDto searchDto = new JobSearchDto();
        searchDto.setKeyword(keyword);
        searchDto.setLocation(location);
        searchDto.setExperienceLevel(experienceLevel);

        if (activeOnly != null && activeOnly) {
            searchDto.setDeadlineAfter(LocalDateTime.now());
        }

        List<JobPostingDto.Response> results = jobSearchService.searchJobs(searchDto);
        return ResponseEntity.ok(results);
    }

    @PostMapping("/advanced-search")
    public ResponseEntity<List<JobPostingDto.Response>> advancedSearch(
            @RequestBody JobSearchDto searchDto) {
        List<JobPostingDto.Response> results = jobSearchService.searchJobs(searchDto);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<JobPostingDto.Response>> getRecentJobs() {
        List<JobPostingDto.Response> recentJobs = jobSearchService.getRecentJobs();
        return ResponseEntity.ok(recentJobs);
    }

    // ✅ 새로 추가: 모든 채용공고 조회
    @GetMapping("/all")
    public ResponseEntity<List<JobPostingDto.Response>> getAllJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        List<JobPostingDto.Response> allJobs = jobSearchService.getAllJobs(page, size, sortBy, sortDir);
        return ResponseEntity.ok(allJobs);
    }

    @GetMapping("/skill/{skill}")
    public ResponseEntity<List<JobPostingDto.Response>> searchBySkill(
            @PathVariable String skill) {
        List<JobPostingDto.Response> results = jobSearchService.searchBySkill(skill);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/quick-search")
    public ResponseEntity<List<JobPostingDto.Response>> quickSearch(
            @RequestParam String keyword) {
        List<JobPostingDto.Response> results = jobSearchService.quickSearch(keyword);
        return ResponseEntity.ok(results);
    }
}