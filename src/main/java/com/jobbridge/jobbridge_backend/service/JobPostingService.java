package com.jobbridge.jobbridge_backend.service;

import com.jobbridge.jobbridge_backend.dto.JobPostingDto;
import com.jobbridge.jobbridge_backend.entity.JobPosting;
import com.jobbridge.jobbridge_backend.entity.User;
import com.jobbridge.jobbridge_backend.repository.JobPostingRepository;
import com.jobbridge.jobbridge_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobPostingService {

    private final JobPostingRepository jobPostingRepository;
    private final UserRepository userRepository;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

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