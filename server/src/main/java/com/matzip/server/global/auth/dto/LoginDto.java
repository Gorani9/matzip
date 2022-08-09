package com.matzip.server.global.auth.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.NotBlank;

public class LoginDto {
    @RequiredArgsConstructor
    @Getter
    public static class LoginRequest {
        @NotBlank
        private final String username;
        @NotBlank
        private final String password;
    }

    @RequiredArgsConstructor
    @Getter
    public static class LoginResponse {
        private final String role;
    }
}
