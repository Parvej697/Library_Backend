package com.library.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class AuthDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        private String username;
        private String password;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginResponse {
        private String token;
        private String username;
        private String name;

        @JsonProperty("isAdmin")
        private boolean isAdmin;

        private String message;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApiResponse {
        private boolean success;
        private String message;
        private Object data;
    }

    // ─── Member Signup Request ──────────────────────
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberSignupRequest {
        private String membershipId;
        private String name;
        private String password;
    }

    // ─── Member Login Request ───────────────────────
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberLoginRequest {
        private String membershipId;
        private String password;
    }

    // ─── Member Login Response ──────────────────────
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberLoginResponse {
        private String token;
        private String membershipId;
        private String name;
        private String message;
    }
}