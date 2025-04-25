package com.jobbridge.jobbridge_backend.repository;

import com.jobbridge.jobbridge_backend.entity.Resume;
import com.jobbridge.jobbridge_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
    List<Resume> findByUser(User user);
    List<Resume> findByUserOrderByCreatedAtDesc(User user);
    // 이메일로 사용자 찾은 뒤 최신 이력서 조회
    Optional<Resume> findTopByUserEmailOrderByCreatedAtDesc(String email);
}