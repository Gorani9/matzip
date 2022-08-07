package com.matzip.server.global.auth.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.NotBlank;

@RequiredArgsConstructor
@Getter
public class LoginRequest {
    @NotBlank
    private final String username;
    @NotBlank
    private final String password;
}
