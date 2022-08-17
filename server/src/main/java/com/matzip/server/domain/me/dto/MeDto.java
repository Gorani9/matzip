package com.matzip.server.domain.me.dto;

import com.matzip.server.domain.user.dto.UserDto;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.domain.user.validation.Password;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Validated
public class MeDto {
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
    public static class ModifyProfileRequest {
        private final MultipartFile profileImage;
        private final String profileString;
    }

    @Getter
    public static class Response extends UserDto.Response {
        protected final LocalDateTime createdAt;
        protected final LocalDateTime modifiedAt;

        public Response(User user) {
            super(user);
            this.createdAt = user.getCreatedAt();
            this.modifiedAt = user.getModifiedAt();
        }
    }
}
