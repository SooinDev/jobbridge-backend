// PasswordResetController.java
package com.jobbridge.jobbridge_backend.controller;

import com.jobbridge.jobbridge_backend.dto.PasswordResetDto;
import com.jobbridge.jobbridge_backend.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/password-reset")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    /**
     * 비밀번호 재설정 요청 (이메일로 코드 발송)
     */
    @PostMapping
    public ResponseEntity<PasswordResetDto.Response> requestPasswordReset(
            @RequestBody PasswordResetDto.Request request) {
        try {
            passwordResetService.createPasswordResetTokenForUser(request.getEmail());
            return ResponseEntity.ok(new PasswordResetDto.Response(
                    "비밀번호 재설정 코드가 이메일로 발송되었습니다. 이메일을 확인해주세요."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new PasswordResetDto.Response(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    new PasswordResetDto.Response("비밀번호 재설정 요청 중 오류가 발생했습니다."));
        }
    }

    /**
     * 비밀번호 재설정 (토큰으로 비밀번호 변경)
     */
    @PostMapping("/confirm")
    public ResponseEntity<PasswordResetDto.Response> resetPassword(
            @RequestBody PasswordResetDto.ConfirmRequest request) {
        try {
            // 비밀번호 유효성 검사
            if (request.getNewPassword().length() < 8) {
                return ResponseEntity.badRequest().body(
                        new PasswordResetDto.Response("비밀번호는 8자 이상이어야 합니다."));
            }

            passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok(new PasswordResetDto.Response(
                    "비밀번호가 성공적으로 변경되었습니다. 새 비밀번호로 로그인해주세요."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new PasswordResetDto.Response(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    new PasswordResetDto.Response("비밀번호 변경 중 오류가 발생했습니다."));
        }
    }
}