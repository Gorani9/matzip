package com.matzip.server.domain.admin.dto;

import com.matzip.server.domain.me.dto.MeDto;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.domain.user.validation.UserProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Validated
public class AdminDto {
    @RequiredArgsConstructor
    @Getter
    public static class UserListRequest {
        @PositiveOrZero
        private final Integer pageNumber;
        @Positive
        private final Integer pageSize;
        @UserProperty
        private final String sortedBy;
        @NotNull
        private final Boolean ascending;
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
        @UserProperty
        private final String sortedBy;
        @NotNull
        private final Boolean ascending;
        @NotBlank
        private final String username;
        private final Boolean isNonLocked;
    }

    @RequiredArgsConstructor
    @Getter
    public static class UserPatchRequest {
        private final Boolean username;
        private final Boolean profileImageUrl;
        private final Boolean profileString;
        private final Integer matzipLevel;
    }

    @Getter
    public static class UserResponse extends MeDto.Response {
        private final Long id;
        private final Boolean isNonLocked;
        private final String role;

        public UserResponse(User user) {
            super(user);
            this.id = user.getId();
            this.isNonLocked = user.getIsNonLocked();
            this.role = user.getRole();
        }
    }
}
