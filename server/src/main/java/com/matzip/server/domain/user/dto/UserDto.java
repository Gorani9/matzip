package com.matzip.server.domain.user.dto;

import com.matzip.server.domain.user.model.User;
import com.matzip.server.domain.user.model.UserProperty;
import com.matzip.server.domain.user.validation.Password;
import com.matzip.server.domain.user.validation.Username;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;

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
        private final String username;
        private final Integer page;
        private final Integer size;
        private final UserProperty sort;
        private final Boolean asc;
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
            this.isMyFollowing = user.hasFollower(me);
            this.isMyFollower = user.hasFollowing(me);
            this.isMe = Objects.equals(user.getId(), me.getId());
        }
    }
}

