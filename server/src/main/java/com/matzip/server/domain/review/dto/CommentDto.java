package com.matzip.server.domain.review.dto;

import com.matzip.server.domain.review.model.Comment;
import com.matzip.server.domain.review.validation.CommentProperty;
import com.matzip.server.domain.user.dto.UserDto;
import com.matzip.server.domain.user.model.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

public class CommentDto {
    @RequiredArgsConstructor
    @Getter
    public static class SearchRequest {
        @PositiveOrZero
        private final Integer pageNumber;
        @Positive
        private final Integer pageSize;
        @CommentProperty
        private final String sortedBy;
        @NotNull
        private final Boolean ascending;
        @NotBlank
        private final String keyword;
    }

    @RequiredArgsConstructor
    @Getter
    public static class PostRequest {
        @NotNull
        private final Long reviewId;
        @NotBlank
        @Length(max=100)
        private final String content;

    }

    @RequiredArgsConstructor
    @Getter
    public static class PutRequest {
        @NotBlank
        @Length(max=100)
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
            this.deletable = user.getId().equals(comment.getUser().getId()) || user.getRole().equals("ADMIN");
        }
    }
}
