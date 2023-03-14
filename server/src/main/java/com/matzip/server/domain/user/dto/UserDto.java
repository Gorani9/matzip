package com.matzip.server.domain.user.dto;

import com.matzip.server.domain.review.dto.ReviewDto;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.domain.user.model.UserProperty;
import com.matzip.server.global.common.dto.ListResponse;
import lombok.Getter;

import java.time.LocalDateTime;

public class UserDto {
    public record SearchRequest(String username, Integer page, Integer size, UserProperty sort, Boolean asc) {}

    @Getter
    public static class Response {
        protected final String username;
        protected final String profileImageUrl;
        protected final String profileString;
        protected final Integer matzipLevel;
        protected final LocalDateTime createdAt;
        private final Boolean isMyFollowing;
        private final Boolean isMyFollower;
        private final Boolean isMe;

        public Response(User user, User me) {
            this.username = user.getUsername();
            this.profileImageUrl = user.getUserImage();
            this.profileString = user.getProfileString();
            this.matzipLevel = user.getMatzipLevel();
            this.createdAt = user.getCreatedAt();
            this.isMyFollowing = me.getFollowings().stream().anyMatch(f -> f.getFollowee() == user);
            this.isMyFollower = me.getFollowers().stream().anyMatch(f -> f.getFollower() == user);
            this.isMe = user == me;
        }
    }

    @Getter
    public static class DetailedResponse extends Response {
        private final Integer numberOfFollowers;
        private final Integer numberOfFollowings;
        private final ListResponse<ReviewDto.Response> reviews;

        public DetailedResponse(User user, User me) {
            super(user, me);
            this.numberOfFollowers = user.getFollowers().size();
            this.numberOfFollowings = user.getFollowings().size();
            this.reviews = new ListResponse<>(user.getReviews().stream().map(r -> new ReviewDto.Response(r, me)));
        }
    }
}

