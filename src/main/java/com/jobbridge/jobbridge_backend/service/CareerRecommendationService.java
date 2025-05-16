package com.jobbridge.jobbridge_backend.service;

import com.jobbridge.jobbridge_backend.entity.JobPosting;
import com.jobbridge.jobbridge_backend.entity.Resume;
import com.jobbridge.jobbridge_backend.repository.JobPostingRepository;
import com.jobbridge.jobbridge_backend.repository.ResumeRepository;
import com.jobbridge.jobbridge_backend.util.AiHttpClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CareerRecommendationService {

    private final ResumeRepository resumeRepository;
    private final JobPostingRepository jobPostingRepository;
    private final AiHttpClient aiHttpClient;

    public List<String> getRecommendationPath(Long resumeId, Long jobPostingId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid resumeId: " + resumeId));
        JobPosting job = jobPostingRepository.findById(jobPostingId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid jobPostingId: " + jobPostingId));

        String resumeText = resume.getContent();
        String jobDesc = job.getDescription() + job.getTitle() + job.getExperienceLevel() + job.getRequiredSkills();

        // AI 서버 호출
        return aiHttpClient.getCareerPath(resumeText, jobDesc);
    }
}
