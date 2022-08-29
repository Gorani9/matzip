package com.matzip.server.domain.user.dto;

import com.matzip.server.domain.user.model.User;
import com.matzip.server.domain.user.validation.Password;
import com.matzip.server.domain.user.validation.UserProperty;
import com.matzip.server.domain.user.validation.Username;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.Objects;

@Validated
public class UserDto {
    @RequiredArgsConstructor
    @Getter
    public static class SignUpRequest {
        @Username
        private final String username;
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
        @UserProperty
        private final String sortedBy;
        @NotNull
        private final Boolean ascending;
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
        private final Boolean isMyFollowing;
        private final Boolean isMyFollower;
        private final Boolean isMe;

        public Response(User user, User me) {
            this.username = user.getUsername();
            this.profileImageUrl = user.getProfileImageUrl();
            this.profileString = user.getProfileString();
            this.matzipLevel = user.getMatzipLevel();
            this.numberOfFollowers = user.getFollowers().size();
            this.numberOfFollowings = user.getFollowings().size();
            this.isMyFollowing = me.getFollowings().contains(user);
            this.isMyFollower = me.getFollowers().contains(user);
            this.isMe = Objects.equals(user.getId(), me.getId());
        }
    }
}

