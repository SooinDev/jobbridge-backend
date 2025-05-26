package com.jobbridge.jobbridge_backend.repository;

import com.jobbridge.jobbridge_backend.entity.Application;
import com.jobbridge.jobbridge_backend.entity.JobPosting;
import com.jobbridge.jobbridge_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    boolean existsByApplicantAndJobPosting(User applicant, JobPosting jobPosting);

    List<Application> findByJobPostingId(Long jobPostingId);
    List<Application> findByJobPosting_Company(User company);

    List<Application> findByApplicant(User user);
}