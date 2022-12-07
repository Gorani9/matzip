package com.matzip.server.domain.admin.dto;

import com.matzip.server.domain.me.dto.MeDto;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.domain.user.model.UserProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;

@Validated
public class AdminDto {
    @RequiredArgsConstructor
    @Getter
    public static class UserSearchRequest {
        private final String username;
        private final Integer page;
        private final Integer size;
        private final UserProperty sort;
        private final Boolean asc;
        private final Boolean withBlocked;
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

        public UserResponse(User user) {
            super(user);
            this.id = user.getId();
            this.isNonLocked = user.isBlocked();
        }
    }
}
