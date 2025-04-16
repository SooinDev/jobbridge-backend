package com.jobbridge.jobbridge_backend.repository;

import com.jobbridge.jobbridge_backend.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByReceiverId(Long receiverId); // 받은 알림 (기업용)
    List<Notification> findBySenderId(Long senderId);     // 보낸 알림 (개인용)
}