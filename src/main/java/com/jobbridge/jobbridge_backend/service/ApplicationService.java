package com.jobbridge.jobbridge_backend.service;

import com.jobbridge.jobbridge_backend.dto.MyApplicationDto;
import com.jobbridge.jobbridge_backend.entity.*;
import com.jobbridge.jobbridge_backend.repository.ApplicationRepository;
import com.jobbridge.jobbridge_backend.repository.JobPostingRepository;
import com.jobbridge.jobbridge_backend.repository.NotificationRepository;
import com.jobbridge.jobbridge_backend.repository.ResumeRepository;
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
    private final ResumeRepository resumeRepository;
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

    // ApplicationService.java에 추가/수정할 메서드

    @Transactional
    public void applyToJob(User applicant, Long jobPostingId, Long resumeId) {
        // 1. 채용공고 조회
        JobPosting job = jobPostingRepository.findById(jobPostingId)
                .orElseThrow(() -> new IllegalArgumentException("채용공고를 찾을 수 없습니다."));

        // 2. 이력서 조회 및 소유권 확인
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다."));

        if (!resume.getUser().getId().equals(applicant.getId())) {
            throw new IllegalArgumentException("본인의 이력서만 사용할 수 있습니다.");
        }

        // 3. 중복 지원 방지
        if (applicationRepository.existsByApplicantAndJobPosting(applicant, job)) {
            throw new IllegalStateException("이미 지원한 공고입니다.");
        }

        // 4. 지원 내역 저장
        Application application = Application.builder()
                .applicant(applicant)
                .jobPosting(job)
                .resume(resume)  // 선택된 이력서
                .appliedAt(LocalDateTime.now())
                .status("PENDING")
                .build();
        applicationRepository.save(application);

        // 5. 알림 생성 (회사가 있는 경우만)
        if (job.getCompany() != null) {
            Notification notification = Notification.builder()
                    .senderId(applicant.getId())
                    .receiverId(job.getCompany().getId())
                    .jobPostingId(job.getId())
                    .message(applicant.getName() + "님이 '" + job.getTitle() + "'에 지원했습니다.")
                    .isRead(false)
                    .createdAt(LocalDateTime.now())
                    .build();
            notificationRepository.save(notification);

            System.out.println("✅ 알림 생성 완료 - 기업 ID: " + job.getCompany().getId());
        } else {
            System.out.println("⚠️ 채용공고 ID " + jobPostingId + "에 회사 정보가 없어 알림을 생성하지 않습니다.");
        }

        System.out.println("✅ 지원 완료 - 지원자: " + applicant.getName() +
                ", 공고: " + job.getTitle() +
                ", 이력서: " + resume.getTitle());
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
}