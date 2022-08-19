package com.matzip.server.domain.user.dto;

import com.matzip.server.domain.user.model.User;
import com.matzip.server.domain.user.validation.Password;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;
import java.util.Optional;

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
        protected final String username;
        protected final String profileImageUrl;
        protected final String profileString;
        protected final Integer matzipLevel;
        private final Integer numberOfFollowers;
        private final Integer numberOfFollowings;

        public Response(User user) {
            this.username = user.getUsername();
            this.profileImageUrl = user.getProfileImageUrl();
            this.profileString = user.getProfileString();
            this.matzipLevel = user.getMatzipLevel();
            this.numberOfFollowers = Optional.ofNullable(user.getFollowers()).orElse(List.of()).size();
            this.numberOfFollowings = Optional.ofNullable(user.getFollowings()).orElse(List.of()).size();
        }
    }
}

