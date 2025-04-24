package com.jobbridge.jobbridge_backend.service;

import com.jobbridge.jobbridge_backend.dto.JobPostingDto.MatchResponse;
import com.jobbridge.jobbridge_backend.entity.JobPosting;
import com.jobbridge.jobbridge_backend.entity.Resume;
import com.jobbridge.jobbridge_backend.repository.JobPostingRepository;
import com.jobbridge.jobbridge_backend.repository.ResumeRepository;
import com.jobbridge.jobbridge_backend.util.AiHttpClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobMatchService {

    private final ResumeRepository resumeRepository;
    private final JobPostingRepository jobPostingRepository;
    private final AiHttpClient aiHttpClient;

    public List<MatchResponse> matchTop5(String email) {
        // 1) 최신 이력서 조회
        Resume resume = resumeRepository.findTopByUserEmailOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new IllegalArgumentException("이력서가 없습니다"));
        String resumeText = resume.getContent();

        // 2) 전체 채용 공고 조회
        List<JobPosting> allJobs = jobPostingRepository.findAll();

        // 3) AI 서버에 보낼 텍스트 리스트 준비 (company null 방어)
        List<String> jobTexts = allJobs.stream()
                .map(j -> {
                    String company = (j.getCompany() != null)
                            ? j.getCompany().getName()
                            : "외부공고";
                    return company + " - " + j.getTitle() + "\n" + j.getDescription();
                })
                .collect(Collectors.toList());

        // 4) 공고 ID 리스트
        List<Long> jobIds = allJobs.stream()
                .map(JobPosting::getId)
                .collect(Collectors.toList());

        // 5) AI 서버 호출
        List<Map<String, Object>> aiResults =
                aiHttpClient.getMatches(resumeText, jobTexts, jobIds);

        // 6) 결과 매핑 및 반환 (company null 방어)
        return aiResults.stream().map(result -> {
            Long jobId = ((Number) result.get("job_id")).longValue();
            Double score = ((Number) result.get("score")).doubleValue();

            JobPosting jp = allJobs.stream()
                    .filter(job -> job.getId().equals(jobId))
                    .findFirst()
                    .orElseThrow();

            MatchResponse dto = new MatchResponse();
            dto.setJobId(jobId);
            dto.setTitle(jp.getTitle());
            dto.setCompanyName(
                    (jp.getCompany() != null)
                            ? jp.getCompany().getName()
                            : "외부공고"
            );
            dto.setMatchRate(score);
            return dto;
        }).collect(Collectors.toList());
    }
}
