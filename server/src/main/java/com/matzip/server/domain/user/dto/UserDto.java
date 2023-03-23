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
        private final Integer numberOfFollowers;
        private final Integer numberOfFollowings;

        public Response(User user, User me) {
            this.username = user.getUsername();
            this.profileImageUrl = user.getUserImage();
            this.profileString = user.getProfileString();
            this.matzipLevel = user.getMatzipLevel();
            this.createdAt = user.getCreatedAt();
            this.isMyFollowing = user.getFollowers().stream().anyMatch(f -> f.getFollower() == me);
            this.isMyFollower = user.getFollowings().stream().anyMatch(f -> f.getFollowee() == me);
            this.isMe = user == me;
            this.numberOfFollowers = user.getFollowers().size();
            this.numberOfFollowings = user.getFollowings().size();
        }
    }

    @Getter
    public static class DetailedResponse extends Response {
        private final ListResponse<ReviewDto.Response> reviews;

        public DetailedResponse(User user, User me) {
            super(user, me);
            this.reviews = new ListResponse<>(user.getReviews().stream().map(r -> new ReviewDto.Response(r, me)));
        }
    }
}

