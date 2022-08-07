package com.matzip.server.domain.user.dto;

import com.matzip.server.domain.user.model.User;
import lombok.*;

import javax.validation.constraints.NotBlank;
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
    public static class PasswordChangeRequest {
        @Password
        private final String password;
    }

    @RequiredArgsConstructor
    @Getter
    public static class DuplicateResponse {
        private final Boolean isDuplicated;
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
            this.isActive = user.getActive();
        }
    }
}

