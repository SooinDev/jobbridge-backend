package com.jobbridge.jobbridge_backend.service;

import com.jobbridge.jobbridge_backend.dto.MatchDto;
import com.jobbridge.jobbridge_backend.dto.ResumeDto;
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
public class ResumeMatchService {

    private final JobPostingRepository jobPostingRepository;
    private final ResumeRepository resumeRepository;
    private final AiHttpClient aiHttpClient;

    /**
     * 채용공고 ID로 상위 5개 이력서를 추천
     */
    public List<ResumeDto.Response> findTopMatchingResumes(Long jobPostingId) {
        // 1) 채용공고 조회
        JobPosting jobPost = jobPostingRepository.findById(jobPostingId)
                .orElseThrow(() -> new NoSuchElementException("Job posting not found: " + jobPostingId));
        String jobContent = jobPost.getDescription();  // JobPosting 엔티티의 description 필드 사용

        // 2) 이력서 전체 조회
        List<Resume> allResumes = resumeRepository.findAll();
        if (allResumes.isEmpty()) {
            return Collections.emptyList();
        }

        // 3) AI 요청 DTO 구성
        List<MatchDto.Item> items = allResumes.stream()
                .map(r -> new MatchDto.Item(r.getId(), r.getContent()))
                .collect(Collectors.toList());
        MatchDto requestDto = new MatchDto(jobContent, items);

        // 4) AI 서버 호출 (/api/match) - 기존 AiHttpClient.getMatches 사용
        // 'resume' 파라미터로 채용공고, 'job_listings'로 이력서 내용, 'job_ids'로 이력서 ID 리스트 전달
        List<Map<String, Object>> responses = aiHttpClient.getMatches(
                jobContent,
                allResumes.stream().map(Resume::getContent).collect(Collectors.toList()),
                allResumes.stream().map(Resume::getId).collect(Collectors.toList())
        );

        // 🐛 디버깅: AI 응답 로그 추가
        log.info("AI 서버 응답 수: {}", responses.size());
        for (int i = 0; i < Math.min(responses.size(), 3); i++) {
            Map<String, Object> response = responses.get(i);
            log.info("응답 {}번: resumeId={}, score={}, scoreType={}",
                    i+1,
                    response.get("job_id"),
                    response.get("score"),
                    response.get("score") != null ? response.get("score").getClass().getSimpleName() : "null"
            );
        }

        // 5) 유사도 내림차순 정렬 후 상위 5개 추출
        List<Map<String, Object>> top5 = responses.stream()
                .sorted((a, b) -> Double.compare(
                        ((Number) b.get("score")).doubleValue(),
                        ((Number) a.get("score")).doubleValue()
                ))
                .limit(5)
                .collect(Collectors.toList());

        // 6) Resume 엔티티에 matchRate 주입 및 결과 리스트 구성 - 🔧 점수 정규화 추가
        Map<Long, Resume> resumeMap = allResumes.stream()
                .collect(Collectors.toMap(Resume::getId, r -> r));

        List<ResumeDto.Response> result = new ArrayList<>();
        for (Map<String, Object> entry : top5) {
            Long resumeId = ((Number) entry.get("job_id")).longValue();
            Double rawScore = ((Number) entry.get("score")).doubleValue();

            // 🔧 점수 정규화: 0~1 범위를 0~100으로 변환
            Double matchRate = normalizeScore(rawScore);

            log.info("resumeId={}, rawScore={}, normalizedScore={}", resumeId, rawScore, matchRate);

            Resume r = resumeMap.get(resumeId);
            if (r != null) {
                ResumeDto.Response dto = new ResumeDto.Response();
                dto.setId(r.getId());
                dto.setTitle(r.getTitle());
                dto.setContent(r.getContent());
                dto.setUserName(r.getUser().getName()); // user가 null일 가능성 주의
                dto.setCreatedAt(r.getCreatedAt().toString());
                dto.setUpdatedAt(r.getUpdatedAt().toString());
                dto.setMatchRate(matchRate);
                result.add(dto);
            }
        }

        return result;
    }

    /**
     * 점수 정규화 메서드
     * AI에서 받은 점수를 의미있는 퍼센트로 변환
     */
    private Double normalizeScore(Double rawScore) {
        if (rawScore == null) {
            return 0.0;
        }

        // 점수가 이미 0~100 범위인 경우
        if (rawScore >= 1.0 && rawScore <= 100.0) {
            return Math.round(rawScore * 100.0) / 100.0; // 소수점 2자리까지
        }

        // 점수가 0~1 범위인 경우 (유사도 점수)
        if (rawScore >= 0.0 && rawScore <= 1.0) {
            return Math.round(rawScore * 100.0 * 100.0) / 100.0; // 퍼센트로 변환
        }

        // 점수가 1보다 큰 경우 (코사인 유사도나 다른 메트릭)
        if (rawScore > 1.0) {
            // 로그 스케일로 정규화 또는 최대값으로 나누기
            return Math.min(Math.round(rawScore * 10.0 * 100.0) / 100.0, 100.0);
        }

        // 음수인 경우 0으로 처리
        return Math.max(0.0, rawScore);
    }
}