package com.matzip.server.global.auth.dto;

import com.matzip.server.global.common.validation.Password;
import com.matzip.server.global.common.validation.Username;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

public class AuthDto {
    public record LoginRequest(
            @NotBlank @Length(max=30) String username,
            @NotBlank @Length(max=30) String password
    ) {}

    public record SignupRequest(
            @Username String username,
            @Password String password
    ) {}

    public record Response(String token) {}
}
