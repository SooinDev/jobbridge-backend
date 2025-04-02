package com.jobbridge.jobbridge_backend.repository;

import com.jobbridge.jobbridge_backend.entity.JobPosting;
import com.jobbridge.jobbridge_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {
    List<JobPosting> findByCompany(User company);
    List<JobPosting> findByCompanyOrderByCreatedAtDesc(User company);

    // Basic search by keyword in title or description
    @Query("SELECT j FROM JobPosting j WHERE " +
            "(LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(j.position) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(j.requiredSkills) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY j.createdAt DESC")
    List<JobPosting> searchByKeyword(@Param("keyword") String keyword);

    // Advanced search with multiple filters
    @Query("SELECT j FROM JobPosting j WHERE " +
            "(:keyword IS NULL OR " +
            "(LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(j.position) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(j.requiredSkills) LIKE LOWER(CONCAT('%', :keyword, '%')))) " +
            "AND (:location IS NULL OR LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%'))) " +
            "AND (:experienceLevel IS NULL OR LOWER(j.experienceLevel) LIKE LOWER(CONCAT('%', :experienceLevel, '%'))) " +
            "AND (:deadlineAfter IS NULL OR j.deadline >= :deadlineAfter) " +
            "ORDER BY j.createdAt DESC")
    List<JobPosting> advancedSearch(
            @Param("keyword") String keyword,
            @Param("location") String location,
            @Param("experienceLevel") String experienceLevel,
            @Param("deadlineAfter") LocalDateTime deadlineAfter);

    // Find recent job postings
    List<JobPosting> findTop10ByOrderByCreatedAtDesc();

    // Find jobs by specific skills
    @Query("SELECT j FROM JobPosting j WHERE " +
            "LOWER(j.requiredSkills) LIKE LOWER(CONCAT('%', :skill, '%')) " +
            "ORDER BY j.createdAt DESC")
    List<JobPosting> findBySkill(@Param("skill") String skill);
}