package com.jobbridge.jobbridge_backend.service;

import com.jobbridge.jobbridge_backend.dto.MatchDto;
import com.jobbridge.jobbridge_backend.dto.JobDto;
import com.jobbridge.jobbridge_backend.entity.JobPosting;
import com.jobbridge.jobbridge_backend.entity.Resume;
import com.jobbridge.jobbridge_backend.repository.JobPostingRepository;
import com.jobbridge.jobbridge_backend.repository.ResumeRepository;
import com.jobbridge.jobbridge_backend.util.AiHttpClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        String resumeContent = resume.getContent();  // 이력서 내용

        // 2) 채용공고 전체 조회
        List<JobPosting> allJobs = jobPostingRepository.findAll();
        if (allJobs.isEmpty()) {
            return Collections.emptyList();
        }

        // 3) AI 요청용 MatchDto 구성
        List<MatchDto.Item> items = allJobs.stream()
                .map(job -> new MatchDto.Item(job.getId(), job.getDescription()))
                .collect(Collectors.toList());
        // (필요 시 요청 DTO 생성; AiHttpClient 내부에서 리스트만 사용하기도 함)
        // MatchDto requestDto = new MatchDto(resumeContent, items);

        // 4) AI 서버 호출: 'resume' 파라미터로 이력서 내용, 'jobContents'로 공고 설명 리스트, 'job_ids'로 공고 ID 리스트 전달
        List<String> jobContents = new ArrayList<>();
        List<String> getDescription = allJobs.stream()
                .map(JobPosting::getDescription)
                .collect(Collectors.toList());

        List<String> getPosition = allJobs.stream()
                .map(JobPosting::getPosition)
                .collect(Collectors.toList());

        List<String> required_skills = allJobs.stream()
                .map(JobPosting::getRequiredSkills)
                .collect(Collectors.toList());

        jobContents.addAll(getDescription);
        jobContents.addAll(getPosition);
        jobContents.addAll(required_skills);

        List<Long> jobIds = allJobs.stream()
                .map(JobPosting::getId)
                .collect(Collectors.toList());
        List<Map<String, Object>> responses = aiHttpClient.getMatches(
                resumeContent,
                jobContents,
                jobIds
        );

        // 5) 유사도(score) 내림차순 정렬 후 상위 5개 선택
        List<Map<String, Object>> top5 = responses.stream()
                .sorted((a, b) -> Double.compare(
                        ((Number) b.get("score")).doubleValue(),
                        ((Number) a.get("score")).doubleValue()
                ))
                .limit(5)
                .collect(Collectors.toList());

        // 6) JobPosting 엔티티에 matchRate 주입 및 결과 리스트 구성
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
                // 생성/수정일자를 문자열로 변환
                dto.setCreatedAt(job.getCreatedAt().toString());
                dto.setUpdatedAt(job.getUpdatedAt().toString());
                dto.setMatchRate(matchRate);
                result.add(dto);
            }
        }

        return result;
    }
}
