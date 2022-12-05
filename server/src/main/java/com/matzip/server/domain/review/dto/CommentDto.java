package com.matzip.server.domain.review.dto;

import com.matzip.server.domain.review.model.Comment;
import com.matzip.server.domain.review.model.CommentProperty;
import com.matzip.server.domain.user.dto.UserDto;
import com.matzip.server.domain.user.model.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Validated
public class CommentDto {
    @RequiredArgsConstructor
    @Getter
    public static class SearchRequest {
        private final String keyword;
        private final Integer page;
        private final Integer size;
        private final CommentProperty sort;
        private final Boolean asc;
    }

    @RequiredArgsConstructor
    @Getter
    public static class PostRequest {
        @NotNull
        private final Long reviewId;
        @NotBlank @Length(max=100)
        private final String content;

    }

    @RequiredArgsConstructor
    @Getter
    public static class PutRequest {
        @NotBlank @Length(max=100)
        private final String content;

    }

    @Getter
    public static class Response {
        private final Long id;
        private final Long reviewId;
        private final UserDto.Response user;
        private final String content;
        private final Boolean deletable;

        public Response(User user, Comment comment) {
            this.id = comment.getId();
            this.reviewId = comment.getReview().getId();
            this.user = new UserDto.Response(comment.getUser(), user);
            this.content = comment.getContent();
            this.deletable = user == comment.getUser() || user.getRole().equals("ADMIN");
        }
    }
}
