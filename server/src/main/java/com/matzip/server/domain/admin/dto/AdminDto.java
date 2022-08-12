package com.matzip.server.domain.admin.dto;

import com.matzip.server.domain.user.model.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;

@Validated
public class AdminDto {
    @RequiredArgsConstructor
    @Getter
    public static class UserListRequest {
        @PositiveOrZero
        private final Integer pageNumber;
        @Positive
        private final Integer pageSize;
        @NotNull
        private final Boolean withAdmin;
    }

    @RequiredArgsConstructor
    @Getter
    public static class UserSearchRequest {
        @PositiveOrZero
        private final Integer pageNumber;
        @Positive
        private final Integer pageSize;
        @NotBlank
        private final String username;
        private final Boolean isNonLocked;
    }

    @Getter
    public static class UserResponse {
        private final Long id;
        private final LocalDateTime createdAt;
        private final LocalDateTime modifiedAt;
        private final String username;
        private final String role;
        private final Boolean isNonLocked;
        private final String profileImageUrl;

        public UserResponse(User user) {
            this.id = user.getId();
            this.createdAt = user.getCreatedAt();
            this.modifiedAt = user.getModifiedAt();
            this.username = user.getUsername();
            this.role = user.getRole();
            this.isNonLocked = user.getIsNonLocked();
            this.profileImageUrl = user.getProfileImageUrl();
        }
    }
}
