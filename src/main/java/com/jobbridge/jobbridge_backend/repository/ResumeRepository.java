package com.jobbridge.jobbridge_backend.repository;

import com.jobbridge.jobbridge_backend.entity.Resume;
import com.jobbridge.jobbridge_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
    List<Resume> findByUser(User user);
    List<Resume> findByUserOrderByCreatedAtDesc(User user);
}