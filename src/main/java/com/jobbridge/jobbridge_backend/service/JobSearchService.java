package com.jobbridge.jobbridge_backend.service;

import com.jobbridge.jobbridge_backend.dto.JobPostingDto;
import com.jobbridge.jobbridge_backend.dto.JobSearchDto;
import com.jobbridge.jobbridge_backend.entity.JobPosting;
import com.jobbridge.jobbridge_backend.repository.JobPostingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobSearchService {

    private final JobPostingRepository jobPostingRepository;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Transactional(readOnly = true)
    public List<JobPostingDto.Response> searchJobs(JobSearchDto searchDto) {
        // Set default search criteria if none specified
        String keyword = searchDto.getKeyword();
        if (keyword != null && keyword.trim().isEmpty()) {
            keyword = null;
        }

        String location = searchDto.getLocation();
        if (location != null && location.trim().isEmpty()) {
            location = null;
        }

        String experienceLevel = searchDto.getExperienceLevel();
        if (experienceLevel != null && experienceLevel.trim().isEmpty()) {
            experienceLevel = null;
        }

        // If all search parameters are null, return recent jobs
        if (keyword == null && location == null && experienceLevel == null && searchDto.getDeadlineAfter() == null) {
            return getRecentJobs();
        }

        LocalDateTime deadlineAfter = searchDto.getDeadlineAfter();

        // Perform advanced search with all filters
        List<JobPosting> results = jobPostingRepository.advancedSearch(
                keyword, location, experienceLevel, deadlineAfter
        );

        return results.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<JobPostingDto.Response> getRecentJobs() {
        List<JobPosting> recentJobs = jobPostingRepository.findTop10ByOrderByCreatedAtDesc();
        return recentJobs.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<JobPostingDto.Response> searchBySkill(String skill) {
        List<JobPosting> results = jobPostingRepository.findBySkill(skill);
        return results.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Quick search by keyword
    @Transactional(readOnly = true)
    public List<JobPostingDto.Response> quickSearch(String keyword) {
        List<JobPosting> results = jobPostingRepository.searchByKeyword(keyword);
        return results.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Helper method to convert JobPosting entity to DTO
    private JobPostingDto.Response convertToDto(JobPosting jobPosting) {
        JobPostingDto.Response response = new JobPostingDto.Response();
        response.setId(jobPosting.getId());
        response.setTitle(jobPosting.getTitle());
        response.setDescription(jobPosting.getDescription());
        response.setPosition(jobPosting.getPosition());
        response.setRequiredSkills(jobPosting.getRequiredSkills());
        response.setExperienceLevel(jobPosting.getExperienceLevel());
        response.setLocation(jobPosting.getLocation());
        response.setSalary(jobPosting.getSalary());

        if (jobPosting.getDeadline() != null) {
            response.setDeadline(jobPosting.getDeadline().format(formatter));
        }

        response.setCompanyName(jobPosting.getCompany().getName());
        response.setCreatedAt(jobPosting.getCreatedAt().format(formatter));
        return response;
    }
}