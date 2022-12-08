package com.matzip.server.domain.user.dto;

import com.matzip.server.domain.me.dto.MeDto;
import com.matzip.server.domain.me.model.Follow;
import com.matzip.server.domain.review.dto.ReviewDto;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.domain.user.model.UserProperty;
import com.matzip.server.domain.user.validation.Password;
import com.matzip.server.domain.user.validation.Username;
import com.matzip.server.global.common.dto.BaseResponse;
import com.matzip.server.global.common.dto.BlockedResponse;
import com.matzip.server.global.common.dto.DeletedResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.stream.Collectors;

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
        private final MeDto.Response response;
        private final String token;
    }

    @Getter
    public static class Response extends BaseResponse {
        protected final String username;
        protected final String profileImageUrl;
        protected final String profileString;
        protected final Integer matzipLevel;
        private final Boolean isMyFollowing;
        private final Boolean isMyFollower;
        private final Boolean isMe;

        protected Response(User user, User me) {
            super(true);
            this.username = user.getUsername();
            this.profileImageUrl = user.getUserImage() == null ? null : user.getUserImage().getImageUrl();
            this.profileString = user.getProfileString();
            this.matzipLevel = user.getMatzipLevel();
            this.isMyFollowing = me.isFollowing(user);
            this.isMyFollower = me.isFollowedBy(user);
            this.isMe = user == me;
        }

        public static BaseResponse ofNullable(User user, User me) {
            if (user.isBlocked()) return BlockedResponse.ofBlockedUser();
            else if (user.isDeleted()) return DeletedResponse.ofDeletedUser();
            else return new Response(user, me);
        }

        public static Response of(User user, User me) {
            return new Response(user, me);
        }
    }

    @Getter
    public static class DetailedResponse extends Response {
        private final List<Response> followers;
        private final Integer numberOfFollowers;
        private final List<Response> followings;
        private final Integer numberOfFollowings;
        private final List<ReviewDto.Response> reviews;

        public DetailedResponse(User user, User me) {
            super(user, me);
            this.followers = user.getFollowers().stream().map(Follow::getFollower).filter(u -> !u.isBlocked())
                    .map(u -> Response.of(u, me)).collect(Collectors.toList());
            this.numberOfFollowers = this.followers.size();
            this.followings = user.getFollowings().stream().map(Follow::getFollowee).filter(u -> !u.isBlocked())
                    .map(u -> Response.of(u, me)).collect(Collectors.toList());
            this.numberOfFollowings = this.followings.size();
            this.reviews = user.getReviews().stream().filter(r -> !r.isBlocked())
                    .map(r -> ReviewDto.Response.of(r, me)).collect(Collectors.toList());
        }
    }
}

