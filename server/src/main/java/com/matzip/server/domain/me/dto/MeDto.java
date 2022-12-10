package com.matzip.server.domain.me.dto;

import com.matzip.server.domain.user.dto.UserDto;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.domain.user.validation.Password;
import com.matzip.server.domain.user.validation.Username;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Validated
public class MeDto {
    @RequiredArgsConstructor
    @Getter
    public static class PasswordChangeRequest {
        @Password
        private final String password;
    }

    @RequiredArgsConstructor
    @Getter
    public static class UsernameChangeRequest {
        @Username
        private final String username;
    }


    @RequiredArgsConstructor
    @Getter
    public static class PatchProfileRequest {
        private final MultipartFile image;
        @Length(max=50)
        private final String profile;
    }

    @RequiredArgsConstructor
    @Getter
    public static class FollowRequest {
        @NotBlank
        private final String username;
    }

    @Getter
    public static class Response extends UserDto.Response {
        private final LocalDateTime createdAt;
        private final LocalDateTime modifiedAt;

        public Response(User user) {
            super(user, user);
            this.createdAt = user.getCreatedAt();
            this.modifiedAt = user.getModifiedAt();
        }
    }
}
