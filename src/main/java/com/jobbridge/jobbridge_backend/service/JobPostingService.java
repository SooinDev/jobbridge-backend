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
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// Apache POI imports
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class JobPostingService {

    private final JobPostingRepository jobPostingRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    // DTO 응답 시 사용할 날짜 포맷 (예: "2025-04-16 19:20")
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final Pattern JSON_LD_PATTERN =
            Pattern.compile("<script type=\"application/ld\\+json\">(.*?)</script>", Pattern.DOTALL);

    @Transactional
    @Scheduled(initialDelay = 0, fixedRate = 1_800_000)
    public void fetchJobPostingsFromWanted() {
        // 1) DB에 이미 저장된 Wanted 공고 URL 집합 조회
        List<JobPosting> storedJobs = jobPostingRepository.findAll();
        Set<String> existingUrls = storedJobs.stream().map(JobPosting::getUrl).collect(Collectors.toSet());

        // 2) ID 범위 하드코딩 (283223 ~ 283323)
        for (int id = 283223; id <= 284323; id++) {
            String url = "https://www.wanted.co.kr/wd/" + id;

            // 3) 중복 URL 스킵
            if (existingUrls.contains(url)) {
                System.out.println("[스킵] 이미 존재하는 URL: " + url);
                continue;
            }

            try {
                // 4) HTML 요청
                String html = restTemplate.getForObject(url, String.class);

                // 5) JSON-LD 스크립트 블록 추출
                Matcher matcher = JSON_LD_PATTERN.matcher(html);
                JsonNode jobNode = null;
                ObjectMapper mapper = new ObjectMapper();
                while (matcher.find()) {
                    String jsonText = matcher.group(1);
                    JsonNode root = mapper.readTree(jsonText);
                    if ("JobPosting".equals(root.path("@type").asText())) {
                        jobNode = root;
                        break;
                    }
                }
                if (jobNode == null) {
                    System.out.println("[스킵] JobPosting 데이터 없음: " + url);
                    continue;
                }

                // 6) JSON-LD에서 필드 추출
                String company = jobNode.path("hiringOrganization").path("name").asText("");
                String position = jobNode.path("title").asText("");
                String title = (company + " " + position).trim();
                String description = fetchFullWantedJobDescription(url);

                // 경력 요건
                JsonNode expNode = jobNode.path("experienceRequirements");
                String experienceLevel;
                if (expNode.isArray()) {
                    List<String> exps = new ArrayList<>();
                    expNode.forEach(e -> exps.add(e.asText()));
                    experienceLevel = String.join(", ", exps);
                } else {
                    experienceLevel = expNode.asText("");
                }

                // 근무 지역
                JsonNode addr = jobNode.path("jobLocation").path("address");
                String location = "";
                if (!addr.isMissingNode()) {
                    String region   = addr.path("addressRegion").asText("");
                    String locality = addr.path("addressLocality").asText("");
                    location = (region + " " + locality).trim();
                }

                // 필요 역량
                JsonNode occNode = jobNode.path("occupationalCategory");
                String requiredSkills;
                if (occNode.isArray()) {
                    List<String> occs = new ArrayList<>();
                    occNode.forEach(o -> occs.add(o.asText()));
                    requiredSkills = String.join(", ", occs);
                } else {
                    requiredSkills = occNode.asText("");
                }

                // 연봉
                JsonNode salaryNode = jobNode.path("baseSalary").path("value");
                String salary = "";
                if (!salaryNode.isMissingNode()) {
                    double minv = salaryNode.path("minValue").asDouble(0);
                    double maxv = salaryNode.path("maxValue").asDouble(0);
                    String unit = salaryNode.path("unitText").asText("");
                    if (minv > 0 || maxv > 0) {
                        salary = (minv != maxv)
                                ? String.format("%.0f-%.0f %s", minv, maxv, unit).trim()
                                : String.format("%.0f %s", minv, unit).trim();
                    }
                }

                // 마감일 파싱 (시간 포함/미포함 모두 처리)
                String deadlineStr = jobNode.path("validThrough").asText("").trim();
                LocalDateTime deadline = null;
                if (!deadlineStr.isBlank()) {
                    try {
                        deadline = LocalDateTime.parse(deadlineStr, DateTimeFormatter.ISO_DATE_TIME);
                    } catch (DateTimeParseException ex) {
                        LocalDate date = LocalDate.parse(deadlineStr, DateTimeFormatter.ISO_DATE);
                        deadline = date.atStartOfDay();
                    }
                }

                // company_id 추출
                String companyIdStr = jobNode.path("identifier").path("propertyID").asText("").trim();
                Long companyId = null;
                if (!companyIdStr.isBlank()) {
                    try {
                        companyId = Long.valueOf(companyIdStr);
                    } catch (NumberFormatException ignored) {}
                }

                // 7) 엔티티 매핑 및 저장
                JobPosting job = new JobPosting();
                job.setUrl(url);
                job.setTitle(title);
                job.setDescription(description);
                job.setPosition(position);
                job.setRequiredSkills(requiredSkills);
                job.setExperienceLevel(experienceLevel);
                job.setLocation(location);
                job.setSalary(salary);
                job.setSource("wanted");
                if (deadline != null) job.setDeadline(deadline);
                job.setCreatedAt(LocalDateTime.now());
                job.setUpdatedAt(LocalDateTime.now());


                jobPostingRepository.save(job);
                System.out.println("[DB 저장] URL=" + url + " / title=" + title);

                // 저장 후 URL 집합에 추가하여 동일 세션 내 중복 방지
                existingUrls.add(url);

            } catch (Exception e) {
                System.out.println("[에러] " + url + " → " + e.getMessage());
            }

            // 1초 대기 (서버 부하 경감)
            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
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

    // Wanted 상세공고 전체 내용 (HTML 파싱 기반) 추출 함수
    public String fetchFullWantedJobDescription(String url) {
        try {
            String html = restTemplate.getForObject(url, String.class);
            StringBuilder fullText = new StringBuilder();

            // 1. <span class="wds-h4ga6o">
            String spanClass = "wds-h4ga6o";
            String spanStart = "<span class=\"" + spanClass + "\">";
            String spanEnd = "</span>";

            int spanIdx = 0;
            while ((spanIdx = html.indexOf(spanStart, spanIdx)) != -1) {
                int start = spanIdx + spanStart.length();
                int end = html.indexOf(spanEnd, start);
                if (end == -1) break;

                String raw = html.substring(start, end);
                fullText.append(cleanHtml(raw)).append("\n\n");
                spanIdx = end + spanEnd.length();
            }

            // 2. <div class="JobDescription_JobDescription__paragraph__87w8I">
            String divClass = "JobDescription_JobDescription__paragraph__87w8I";
            String divStart = "<div class=\"" + divClass + "\">";
            String divEnd = "</div>";

            int divIdx = 0;
            while ((divIdx = html.indexOf(divStart, divIdx)) != -1) {
                int start = divIdx + divStart.length();
                int end = html.indexOf(divEnd, start);
                if (end == -1) break;

                String raw = html.substring(start, end);
                fullText.append(cleanHtml(raw)).append("\n\n");
                divIdx = end + divEnd.length();
            }

            return fullText.toString().trim();

        } catch (Exception e) {
            System.out.println("[상세공고 크롤링 실패] " + url + " → " + e.getMessage());
            return "";
        }
    }

    // HTML 태그 및 특수문자 정제용 내부 메서드
    private String cleanHtml(String html) {
        return html.replaceAll("(?i)<br\\s*/?>", "\n")
                .replaceAll("(?i)<li>", "• ")
                .replaceAll("(?i)</li>", "\n")
                .replaceAll("<[^>]+>", "")
                .replace("&nbsp;", " ")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .trim();
    }
}
