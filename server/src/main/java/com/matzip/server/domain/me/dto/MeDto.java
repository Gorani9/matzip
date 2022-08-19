package com.matzip.server.domain.me.dto;

import com.matzip.server.domain.me.validation.FollowType;
import com.matzip.server.domain.user.dto.UserDto;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.domain.user.validation.Password;
import com.matzip.server.domain.user.validation.UserProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;

@Validated
public class MeDto {
    @RequiredArgsConstructor
    @Getter
    @Setter
    public static class PasswordChangeRequest {
        @Password
        private final String password;
    }

    @RequiredArgsConstructor
    @Getter
    public static class ModifyProfileRequest {
        private final MultipartFile profileImage;
        private final String profileString;
    }

    @RequiredArgsConstructor
    @Getter
    public static class FollowRequest {
        @NotBlank
        private final String username;
    }

    @RequiredArgsConstructor
    @Getter
    public static class FindFollowRequest {
        @PositiveOrZero
        private final Integer pageNumber;
        @Positive
        private final Integer pageSize;
        @UserProperty
        private final String sortedBy;
        @NotNull
        private final Boolean ascending;
        @FollowType
        private final String type;
    }

    @Getter
    public static class Response extends UserDto.Response {
        private final LocalDateTime createdAt;
        private final LocalDateTime modifiedAt;

        public Response(User user) {
            super(user);
            this.createdAt = user.getCreatedAt();
            this.modifiedAt = user.getModifiedAt();
        }
    }
}
