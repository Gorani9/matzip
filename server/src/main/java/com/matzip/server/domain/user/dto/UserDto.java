package com.matzip.server.domain.user.dto;

import com.matzip.server.domain.user.model.User;
import com.matzip.server.domain.user.validation.Password;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;

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
        private String username;
        @Password
        private final String password;
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
        private final Long id;
        private final LocalDateTime createdAt;
        private final LocalDateTime modifiedAt;
        private final String username;
        private final Boolean isActive;

        public Response(User user) {
            this.id = user.getId();
            this.createdAt = user.getCreatedAt();
            this.modifiedAt = user.getModifiedAt();
            this.username = user.getUsername();
            this.isActive = user.getIsActive();
        }
    }
}

