package com.matzip.server.domain.comment.dto;

import com.matzip.server.domain.comment.model.Comment;
import com.matzip.server.domain.user.dto.UserDto;
import com.matzip.server.domain.user.model.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.LocalDateTime;

@Validated
public class CommentDto {
    public record SearchRequest(String keyword, Integer page, Integer size, Boolean asc) {}

    @Getter
    @RequiredArgsConstructor
    public static class PostRequest {
        @NotNull @Positive
        private final Long reviewId;
        @NotBlank @Length(max=100)
        private final String content;
    }

    public record PatchRequest(@NotBlank @Length(max=100) String content) {}

    @Getter
    public static class Response {
        private final Long id;
        private final LocalDateTime createdAt;
        private final LocalDateTime modifiedAt;
        private final Long reviewId;
        private final UserDto.Response user;
        private final String content;

        public Response(Comment comment, User user) {
            this.id = comment.getId();
            this.createdAt = comment.getCreatedAt();
            this.modifiedAt = comment.getModifiedAt();
            this.reviewId = comment.getReview().getId();
            this.user = new UserDto.Response(comment.getUser(), user);
            this.content = comment.getContent();
        }
    }
}
