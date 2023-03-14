package com.matzip.server.domain.me.dto;

import com.matzip.server.domain.auth.validation.Password;
import com.matzip.server.domain.auth.validation.Username;
import com.matzip.server.domain.comment.dto.CommentDto;
import com.matzip.server.domain.review.dto.ReviewDto;
import com.matzip.server.domain.scrap.dto.ScrapDto;
import com.matzip.server.domain.user.dto.UserDto;
import com.matzip.server.domain.user.model.Follow;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.global.common.dto.ListResponse;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

@Validated
public class MeDto {
    public record PasswordChangeRequest(@Password String password) {}
    public record UsernameChangeRequest(@Username String username) {}
    public record PatchRequest(MultipartFile image, @Length(max=50) String profile) {}

    @Getter
    public static class Response extends UserDto.DetailedResponse {
        private final ListResponse<UserDto.Response> myFollowers;
        private final ListResponse<UserDto.Response> myFollowings;
        private final ListResponse<CommentDto.Response> comments;
        private final ListResponse<ScrapDto.Response> scraps;
        private final ListResponse<ReviewDto.Response> heartedReviews;

        public Response(User me) {
            super(me, me);
            myFollowers = new ListResponse<>(me.getFollowers().stream().map(Follow::getFollower).map(u -> new UserDto.Response(u, me)));
            myFollowings = new ListResponse<>(me.getFollowings().stream().map(Follow::getFollowee).map(u -> new UserDto.Response(u, me)));
            comments = new ListResponse<>(me.getComments().stream().map(c -> new CommentDto.Response(c, me)));
            scraps = new ListResponse<>(me.getScraps().stream().map(ScrapDto.Response::new));
            heartedReviews = new ListResponse<>(me.getHearts().stream().map(c -> new ReviewDto.Response(c.getReview(), me)));
        }
    }
}
