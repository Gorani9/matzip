package com.matzip.server.domain.user.dto;

import com.matzip.server.domain.user.model.User;
import com.matzip.server.domain.user.validation.Password;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Validated
public class UserDto {
    @RequiredArgsConstructor
    @Getter
    public static class DuplicateRequest {
        @NotBlank
        private final String username;
    }

    @RequiredArgsConstructor
    @Getter
    public static class SignUpRequest {
        @NotBlank
        private final String username;
        @Password
        private final String password;
    }

    @RequiredArgsConstructor
    @Getter
    public static class FindRequest {
        @NotBlank
        private final String username;
    }

    @RequiredArgsConstructor
    @Getter
    @Setter
    public static class PasswordChangeRequest {
        @Password
        private final String password;
        private String username;
    }

    @RequiredArgsConstructor
    @Getter
    public static class SearchRequest {
        @PositiveOrZero
        private final Integer pageNumber;
        @Positive
        private final Integer pageSize;
        @NotBlank
        private final String username;
    }

    @RequiredArgsConstructor
    @Getter
    public static class ModifyProfileRequest {
        @URL
        private final String profileImageUrl;
    }

    @RequiredArgsConstructor
    @Getter
    public static class DuplicateResponse {
        private final Boolean exists;
    }

    @RequiredArgsConstructor
    @Getter
    public static class SignUpResponse {
        private final Response response;
        private final String token;
    }

    @Getter
    public static class Response {
        private final String username;
        private final String profileImageUrl;

        public Response(User user) {
            this.username = user.getUsername();
            this.profileImageUrl = user.getProfileImageUrl();
        }
    }
}

