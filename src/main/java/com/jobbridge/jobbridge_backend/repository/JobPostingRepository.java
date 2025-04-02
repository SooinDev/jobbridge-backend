package com.jobbridge.jobbridge_backend.repository;

import com.jobbridge.jobbridge_backend.entity.JobPosting;
import com.jobbridge.jobbridge_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {
    List<JobPosting> findByCompany(User company);
    List<JobPosting> findByCompanyOrderByCreatedAtDesc(User company);
}