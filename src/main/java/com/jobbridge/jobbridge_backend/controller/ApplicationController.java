package com.jobbridge.jobbridge_backend.controller;

import com.jobbridge.jobbridge_backend.dto.MyApplicationDto;
import com.jobbridge.jobbridge_backend.entity.Application;
import com.jobbridge.jobbridge_backend.entity.User;
import com.jobbridge.jobbridge_backend.repository.ApplicationRepository;
import com.jobbridge.jobbridge_backend.security.UserDetailsImpl;
import com.jobbridge.jobbridge_backend.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;
    private final ApplicationRepository applicationRepository;

    // ✅ 새로 추가: 특정 채용공고에 이미 지원했는지 확인
    @GetMapping("/applications/check/{jobPostingId}")
    public ResponseEntity<Map<String, Boolean>> checkIfAlreadyApplied(
            @PathVariable Long jobPostingId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        try {
            User user = userDetails.getUser();
            System.out.println("➡️ 지원 여부 확인 요청 - 사용자 ID: " + user.getId() +
                    ", 채용공고 ID: " + jobPostingId);

            // 사용자 유형 확인
            if (user.getUserType() != User.UserType.INDIVIDUAL) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("applied", false, "error", true));
            }

            // 지원 여부 확인
            boolean hasApplied = applicationService.hasUserAppliedToJob(user, jobPostingId);

            System.out.println("⬅️ 지원 여부 확인 결과 - 지원 여부: " + hasApplied);

            return ResponseEntity.ok(Map.of("applied", hasApplied));

        } catch (IllegalArgumentException e) {
            System.out.println("❌ 지원 여부 확인 실패 (입력 오류) - " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("applied", false, "error", true));
        } catch (Exception e) {
            System.out.println("❌ 지원 여부 확인 실패 (서버 오류) - " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("applied", false, "error", true));
        }
    }

    // ✅ 지원하기
    @PostMapping("/apply/{jobPostingId}")
    public ResponseEntity<String> applyToJob(
            @PathVariable Long jobPostingId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        try {
            User user = userDetails.getUser();
            System.out.println("➡️ 지원 요청 - 사용자 ID: " + user.getId() +
                    ", 이메일: " + user.getEmail() +
                    ", 채용공고 ID: " + jobPostingId);

            // 사용자 유형 확인
            if (user.getUserType() != User.UserType.INDIVIDUAL) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("개인 회원만 지원할 수 있습니다. 현재 회원 유형: " + user.getUserType());
            }

            // 지원 처리 - 서비스 메소드 호출
            applicationService.applyToJob(user, jobPostingId);
            System.out.println("✅ 지원 처리 완료 - 사용자 ID: " + user.getId() +
                    ", 채용공고 ID: " + jobPostingId);

            return ResponseEntity.ok("지원이 완료되었습니다.");
        } catch (IllegalArgumentException e) {
            System.out.println("❌ 지원 처리 실패 (입력 오류) - " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalStateException e) {
            System.out.println("❌ 지원 처리 실패 (상태 오류) - " + e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ 지원 처리 실패 (서버 오류) - " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("서버 오류: " + e.getMessage());
        }
    }

    // ✅ 내가 지원한 내역 보기
    @GetMapping("/applications/mine")
    public ResponseEntity<List<MyApplicationDto>> getMyApplications(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        User user = userDetails.getUser();
        System.out.println("➡️ 지원 내역 조회 요청 - 사용자 ID: " + user.getId() +
                ", 이메일: " + user.getEmail());

        List<MyApplicationDto> result = applicationService.getApplicationsByUser(user);
        System.out.println("⬅️ 지원 내역 반환 - 건수: " + result.size());

        if (result.isEmpty()) {
            System.out.println("⚠️ 주의: 지원 내역이 없습니다!");
            // 디버깅을 위해 데이터베이스 직접 조회
            List<Application> applications = applicationRepository.findAll();
            System.out.println("📊 전체 지원 내역 수: " + applications.size());

            if (!applications.isEmpty()) {
                System.out.println("📝 첫 번째 지원 내역 정보:");
                Application first = applications.get(0);
                System.out.println("   - 지원자 ID: " + first.getApplicant().getId());
                System.out.println("   - 지원자 이메일: " + first.getApplicant().getEmail());
                System.out.println("   - 채용공고 ID: " + first.getJobPosting().getId());
            }
        }

        return ResponseEntity.ok(result);
    }

    // ✅ 테스트용 API - 모든 지원 내역 조회 (개발용)
    @GetMapping("/applications/all")
    public ResponseEntity<List<Application>> getAllApplications() {
        List<Application> applications = applicationRepository.findAll();
        System.out.println("📊 전체 지원 내역 수: " + applications.size());
        return ResponseEntity.ok(applications);
    }
}