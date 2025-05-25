package com.jobbridge.jobbridge_backend.service;

import com.jobbridge.jobbridge_backend.dto.JobDto;
import com.jobbridge.jobbridge_backend.entity.JobPosting;
import com.jobbridge.jobbridge_backend.entity.Resume;
import com.jobbridge.jobbridge_backend.repository.JobPostingRepository;
import com.jobbridge.jobbridge_backend.repository.ResumeRepository;
import com.jobbridge.jobbridge_backend.util.AiHttpClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobMatchService {

    private final ResumeRepository resumeRepository;
    private final JobPostingRepository jobPostingRepository;
    private final AiHttpClient aiHttpClient;

    /**
     * 이력서 ID로 상위 5개 채용공고를 추천
     */
    public List<JobDto.Response> findTopMatchingJobs(Long resumeId) {
        // 1) 이력서 조회
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new NoSuchElementException("Resume not found: " + resumeId));
        String resumeContent = resume.getContent();

        // 2) 전체 채용공고 조회
        List<JobPosting> allJobs = jobPostingRepository.findAll();
        if (allJobs.isEmpty()) {
            return Collections.emptyList();
        }

        // 3) jobContents: 각 채용공고의 position, skills, description을 하나의 텍스트로 묶기
        List<String> jobContents = allJobs.stream()
                .map(job -> String.join("\n",
                        "채용포지션: " + job.getPosition(),
                        "요구역량: " + job.getRequiredSkills(),
                        "상세내용: " + job.getDescription()
                ))
                .collect(Collectors.toList());

        // 4) jobIds 추출
        List<Long> jobIds = allJobs.stream()
                .map(JobPosting::getId)
                .collect(Collectors.toList());

        // 🔍 로그로 요청 크기 확인
        log.info("매칭 요청: resume.length={}, jobIds.size={}, jobContents.size={}",
                resumeContent.length(), jobIds.size(), jobContents.size());

        // 5) AI 서버 호출
        List<Map<String, Object>> responses = aiHttpClient.getMatches(
                resumeContent,
                jobContents,
                jobIds
        );

        // 6) 점수 기준 정렬 후 상위 5개 추출
        List<Map<String, Object>> top5 = responses.stream()
                .sorted((a, b) -> Double.compare(
                        ((Number) b.get("score")).doubleValue(),
                        ((Number) a.get("score")).doubleValue()
                ))
                .limit(5)
                .collect(Collectors.toList());

        // 7) 결과 조립
        Map<Long, JobPosting> jobMap = allJobs.stream()
                .collect(Collectors.toMap(JobPosting::getId, job -> job));

        List<JobDto.Response> result = new ArrayList<>();
        for (Map<String, Object> entry : top5) {
            Long jobId = ((Number) entry.get("job_id")).longValue();
            Double matchRate = ((Number) entry.get("score")).doubleValue();

            JobPosting job = jobMap.get(jobId);
            if (job != null) {
                JobDto.Response dto = new JobDto.Response();
                dto.setId(job.getId());
                dto.setTitle(job.getTitle());
                dto.setDescription(job.getDescription());
                dto.setCreatedAt(job.getCreatedAt().toString());
                dto.setUpdatedAt(job.getUpdatedAt().toString());
                dto.setMatchRate(matchRate);
                result.add(dto);
            }
        }

        return result;
    }
}