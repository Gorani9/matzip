package com.matzip.server.domain.scrap.dto;

import com.matzip.server.domain.review.dto.ReviewDto;
import com.matzip.server.domain.scrap.model.Scrap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.LocalDateTime;

@Validated
public class ScrapDto {
    @Getter
    @RequiredArgsConstructor
    public static class PostRequest {
        @NotNull @Positive
        private final Long reviewId;
        @NotBlank @Length(max=100)
        private final String description;
    }

    public record PatchRequest(@NotBlank @Length(max=100) String description) {}

    @Getter
    public static class Response {
        private final LocalDateTime createdAt;
        private final LocalDateTime modifiedAt;
        private final String description;
        private final ReviewDto.Response review;

        public Response(Scrap scrap) {
            this.createdAt = scrap.getCreatedAt();
            this.modifiedAt = scrap.getModifiedAt();
            this.description = scrap.getDescription();
            this.review = new ReviewDto.Response(scrap.getReview(), scrap.getUser());
        }
    }
}
