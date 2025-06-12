package com.jobbridge.jobbridge_backend.service;

import com.jobbridge.jobbridge_backend.dto.ApplicantInfoDto;
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

    // 새로 추가: 특정 사용자가 특정 채용공고에 지원했는지 확인
    public boolean hasUserAppliedToJob(User user, Long jobPostingId) {
        // 1. 채용공고 존재 여부 확인
        JobPosting jobPosting = jobPostingRepository.findById(jobPostingId)
                .orElseThrow(() -> new IllegalArgumentException("채용공고를 찾을 수 없습니다. ID: " + jobPostingId));

        // 2. 지원 여부 확인
        boolean hasApplied = applicationRepository.existsByApplicantAndJobPosting(user, jobPosting);

        System.out.println("🔍 지원 여부 확인 - 사용자: " + user.getEmail() +
                ", 채용공고: " + jobPosting.getTitle() +
                ", 결과: " + hasApplied);

        return hasApplied;
    }

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

        // 회사 정보가 있는 경우만 알림 생성
        if (job.getCompany() != null) {
            Notification notification = Notification.builder()
                    .senderId(applicant.getId())
                    .receiverId(job.getCompany().getId()) // 채용공고 올린 기업
                    .jobPostingId(job.getId())
                    .message(applicant.getName() + "님이 '" + job.getTitle() + "'에 지원했습니다.")
                    .isRead(false)
                    .createdAt(LocalDateTime.now())
                    .build();
            notificationRepository.save(notification);
        } else {
            System.out.println("⚠️ 채용공고 ID " + jobPostingId + "에 회사 정보가 없어 알림을 생성하지 않습니다.");
        }
    }

    // 내가 지원한 공고 내역 불러오기
    public List<MyApplicationDto> getApplicationsByUser(User user) {
        List<Application> applications = applicationRepository.findByApplicant(user);

        return applications.stream()
                .map(app -> {
                    JobPosting job = app.getJobPosting();
                    String companyName = (job.getCompany() != null) ? job.getCompany().getName() : "외부 공고";

                    return new MyApplicationDto(
                            job.getId(),
                            job.getTitle(),
                            companyName,
                            app.getAppliedAt());
                })
                .toList();
    }

    public List<ApplicantInfoDto> getApplicantsByJobPosting(Long jobPostingId) {
        List<Application> applications = applicationRepository.findByJobPosting_Id(jobPostingId);

        return applications.stream()
                .map(app -> new ApplicantInfoDto(
                        app.getApplicant().getName(),
                        app.getApplicant().getEmail(),
                        app.getAppliedAt()
                )).toList();
    }

    public JobPosting getJobPostingById(Long id) {
        return jobPostingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("채용공고를 찾을 수 없습니다."));
    }
}