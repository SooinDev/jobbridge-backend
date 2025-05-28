package com.jobbridge.jobbridge_backend.repository;

import com.jobbridge.jobbridge_backend.entity.JobPosting;
import com.jobbridge.jobbridge_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {

    List<JobPosting> findByCompany(User company);

    List<JobPosting> findByCompanyOrderByCreatedAtDesc(User company);

    List<JobPosting> findBySource(String source); // ✅ 사람인 API 구분용

    // ✅ 새로 추가: 모든 채용공고를 최신순으로 조회
    List<JobPosting> findAllByOrderByCreatedAtDesc();

    // ✅ 기존 10개에서 50개로 늘림
    List<JobPosting> findTop50ByOrderByCreatedAtDesc();

    // ✅ 기존 메서드들 유지
    List<JobPosting> findTop10ByOrderByCreatedAtDesc();

    @Query("SELECT j FROM JobPosting j WHERE " +
            "(LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(j.position) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(j.requiredSkills) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY j.createdAt DESC")
    List<JobPosting> searchByKeyword(@Param("keyword") String keyword);

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

    @Query("SELECT j FROM JobPosting j WHERE " +
            "LOWER(j.requiredSkills) LIKE LOWER(CONCAT('%', :skill, '%')) " +
            "ORDER BY j.createdAt DESC")
    List<JobPosting> findBySkill(@Param("skill") String skill);
}