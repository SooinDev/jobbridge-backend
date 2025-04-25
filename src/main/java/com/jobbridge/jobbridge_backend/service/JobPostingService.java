// JobPostingService.java
package com.jobbridge.jobbridge_backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobbridge.jobbridge_backend.dto.JobPostingDto;
import com.jobbridge.jobbridge_backend.entity.JobPosting;
import com.jobbridge.jobbridge_backend.entity.User;
import com.jobbridge.jobbridge_backend.repository.JobPostingRepository;
import com.jobbridge.jobbridge_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobPostingService {

    private final JobPostingRepository jobPostingRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    // DTO 응답 시 사용할 날짜 포맷 (예: "2025-04-16 19:20")
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Value("${saramin.api.key}")
    private String apiKey;

    @Scheduled(initialDelay = 0, fixedRate = 1800000)
    public void fetchJobPostingsFromSaramin() {
        // 필수: access-key, keywords, 반환 필드(posting-date, expiration-date) 지정
        String url = "https://oapi.saramin.co.kr/job-search?access-key=" + apiKey
                + "&count=110&keywords=개발자&fields=posting-date,expiration-date";
        System.out.println("[DEBUG] API 호출 URL: " + url);
        try {
            String response = restTemplate.getForObject(url, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response);
            // 채용공고 데이터는 job-search > jobs > job 배열에 존재함
            JsonNode jobs = rootNode.path("jobs").path("job");

            List<JobPosting> postings = new ArrayList<>();

            // 마감일은 ISO 오프셋 형식을 사용: "yyyy-MM-dd'T'HH:mm:ssZ"
            DateTimeFormatter expirationFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");
            // 단순 날짜 형식(예: "yyyy-MM-dd")도 대비
            DateTimeFormatter simpleFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            for (JsonNode job : jobs) {
                try {
                    // 마감일 파싱: expiration-date
                    String expirationDateStr = job.path("expiration-date").asText();
                    LocalDateTime deadline = parseDeadline(expirationDateStr, expirationFormatter, simpleFormatter);

                    // 공고 제목 및 직무명: position.title
                    String title = job.path("position").path("title").asText();

                    // 필요 기술: keyword (쉼표로 구분된 문자열)
                    String requiredSkills = job.path("keyword").asText();

                    // 연봉: salary.name; 값이 없으면 salary 필드 그대로 사용
                    String salary = job.path("salary").path("name").asText();
                    if (salary == null || salary.isBlank()) {
                        salary = job.path("salary").asText("");
                    }

                    // 경력 수준: position.experience-level.name
                    String experienceLevel = job.path("position").path("experience-level").path("name").asText();

                    // 근무지: position.location.name
                    String location = job.path("position").path("location").path("name").asText();

                    // 회사 정보: company.detail.name와 company.detail.href
                    String companyName = job.path("company").path("detail").path("name").asText();
                    String companyHref = job.path("company").path("detail").path("href").asText();
                    // 외부 데이터이므로 내부 DB 연동은 null 처리
                    User company = null;

                    // 채용 공고 URL
                    String jobUrl = job.path("url").asText();

                    // JobPosting 객체 생성 및 추가
                    JobPosting posting = JobPosting.builder()
                            .title(title)
                            .description("사람인에서 수집된 채용 공고입니다.")
                            // position 필드에 공고 제목(또는 직무명)을 저장
                            .position(title)
                            .requiredSkills(requiredSkills)
                            .experienceLevel(experienceLevel)
                            .location(location)
                            .salary(salary)
                            .deadline(deadline)
                            .company(company)
                            .source("SARAMIN")
                            .url(jobUrl)
                            .build();

                    postings.add(posting);
                } catch (Exception innerEx) {
                    System.out.println("[DEBUG] 개별 공고 처리 중 오류 발생");
                    innerEx.printStackTrace();
                }
            }
            System.out.println("[DEBUG] 총 파싱된 공고 수: " + postings.size());
            jobPostingRepository.saveAll(postings);
            System.out.println("[DEBUG] jobPostingRepository.saveAll 실행 완료");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private LocalDateTime parseDeadline(String expirationDateStr, DateTimeFormatter expirationFormatter, DateTimeFormatter simpleFormatter) {
        if (expirationDateStr == null || expirationDateStr.isBlank()) {
            System.out.println("[DEBUG] expiration-date 값이 비어 있습니다.");
            return null;
        }
        try {
            // "yyyy-MM-dd'T'HH:mm:ssZ" 형식으로 파싱 후 OffsetDateTime에서 LocalDateTime 추출
            return OffsetDateTime.parse(expirationDateStr, expirationFormatter).toLocalDateTime();
        } catch (Exception ex) {
            // 위 형식으로 파싱되지 않으면, "yyyy-MM-dd" 형식으로 파싱한 후 자정 추가
            return LocalDateTime.of(LocalDate.parse(expirationDateStr, simpleFormatter), LocalTime.MIDNIGHT);
        }
    }

    @Transactional
    public JobPostingDto.Response createJobPosting(String email, JobPostingDto.Request request) {
        User company = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        if (company.getUserType() != User.UserType.COMPANY) {
            throw new IllegalArgumentException("기업 회원만 채용공고를 등록할 수 있습니다.");
        }
        JobPosting jobPosting = JobPosting.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .position(request.getPosition())
                .requiredSkills(request.getRequiredSkills())
                .experienceLevel(request.getExperienceLevel())
                .location(request.getLocation())
                .salary(request.getSalary())
                .deadline(request.getDeadline())
                .company(company)
                .source("USER")
                .build();
        JobPosting savedJobPosting = jobPostingRepository.save(jobPosting);
        return convertToDto(savedJobPosting);
    }

    @Transactional(readOnly = true)
    public List<JobPostingDto.Response> getCompanyJobPostings(String email) {
        User company = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return jobPostingRepository.findByCompanyOrderByCreatedAtDesc(company).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public JobPostingDto.Response getJobPosting(Long id) {
        JobPosting jobPosting = jobPostingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("채용공고를 찾을 수 없습니다."));
        return convertToDto(jobPosting);
    }

    @Transactional
    public JobPostingDto.Response updateJobPosting(Long id, String email, JobPostingDto.Request request) {
        JobPosting jobPosting = jobPostingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("채용공고를 찾을 수 없습니다."));
        if (!jobPosting.getCompany().getEmail().equals(email)) {
            throw new IllegalArgumentException("자신의 회사 채용공고만 수정할 수 있습니다.");
        }
        jobPosting.setTitle(request.getTitle());
        jobPosting.setDescription(request.getDescription());
        jobPosting.setPosition(request.getPosition());
        jobPosting.setRequiredSkills(request.getRequiredSkills());
        jobPosting.setExperienceLevel(request.getExperienceLevel());
        jobPosting.setLocation(request.getLocation());
        jobPosting.setSalary(request.getSalary());
        jobPosting.setDeadline(request.getDeadline());
        JobPosting updatedJobPosting = jobPostingRepository.save(jobPosting);
        return convertToDto(updatedJobPosting);
    }

    @Transactional
    public void deleteJobPosting(Long id, String email) {
        JobPosting jobPosting = jobPostingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("채용공고를 찾을 수 없습니다."));
        if (!jobPosting.getCompany().getEmail().equals(email)) {
            throw new IllegalArgumentException("자신의 회사 채용공고만 삭제할 수 있습니다.");
        }
        jobPostingRepository.delete(jobPosting);
    }

    public List<JobPosting> getAllJobPostings() {
        return jobPostingRepository.findAll();
    }

    public List<JobPosting> getFilteredJobPostings(String source) {
        return jobPostingRepository.findBySource(source);
    }

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
        if (jobPosting.getCompany() != null) {
            response.setCompanyName(jobPosting.getCompany().getName());
            response.setCompanyEmail(jobPosting.getCompany().getEmail());
        }
        else {
            // company가 null인 경우 (예: SARAMIN 공고), 기본 값 또는 별도 처리
            response.setCompanyName(null);  // 또는 "외부공고" 등의 표시
            response.setCompanyEmail(null);
        }
        response.setCreatedAt(jobPosting.getCreatedAt().format(formatter));
        return response;
    }
}
