// PasswordResetDto.java
package com.jobbridge.jobbridge_backend.dto;

import lombok.Getter;
import lombok.Setter;

public class PasswordResetDto {

    @Getter
    @Setter
    public static class Request {
        private String email;
    }

    @Getter
    @Setter
    public static class ConfirmRequest {
        private String token;
        private String newPassword;
    }

    @Getter
    @Setter
    public static class Response {
        private String message;

        public Response(String message) {
            this.message = message;
        }
    }
}