package com.jobbridge.jobbridge_backend.service;

import com.jobbridge.jobbridge_backend.dto.MyApplicationDto;
import com.jobbridge.jobbridge_backend.entity.Application;
import com.jobbridge.jobbridge_backend.entity.JobPosting;
import com.jobbridge.jobbridge_backend.entity.Notification;
import com.jobbridge.jobbridge_backend.entity.User;
import com.jobbridge.jobbridge_backend.repository.ApplicationRepository;
import com.jobbridge.jobbridge_backend.repository.JobPostingRepository;
import com.jobbridge.jobbridge_backend.repository.NotificationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobPostingRepository jobPostingRepository;
    private final NotificationRepository notificationRepository;

    @Transactional
    public void applyToJob(User applicant, Long jobPostingId) {
        JobPosting job = jobPostingRepository.findById(jobPostingId)
                .orElseThrow(() -> new IllegalArgumentException("채용공고를 찾을 수 없습니다."));

        // 중복 지원 방지
        if (applicationRepository.existsByApplicantAndJobPosting(applicant, job)) {
            throw new IllegalStateException("이미 지원한 공고입니다.");
        }

        // 지원 내역 저장
        Application application = Application.builder()
                .applicant(applicant)
                .jobPosting(job)
                .appliedAt(LocalDateTime.now())
                .build();
        applicationRepository.save(application);

        // 알림 저장
        Notification notification = Notification.builder()
                .senderId(applicant.getId())
                .receiverId(job.getCompany().getId()) // 채용공고 올린 기업
                .jobPostingId(job.getId())
                .message(applicant.getName() + "님이 '" + job.getTitle() + "'에 지원했습니다.")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);
    }

    // ✅ 내가 지원한 공고 내역 불러오기
    public List<MyApplicationDto> getApplicationsByUser(User user) {
        List<Application> applications = applicationRepository.findByApplicant(user);

        return applications.stream()
                .map(app -> new MyApplicationDto(
                        app.getJobPosting().getId(),
                        app.getJobPosting().getTitle(),
                        app.getJobPosting().getCompany().getName(),
                        app.getAppliedAt()))
                .toList();
    }
}