package com.matzip.server.domain.me.dto;

import com.matzip.server.domain.me.model.Scrap;
import com.matzip.server.domain.me.model.ScrapProperty;
import com.matzip.server.domain.review.dto.ReviewDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Validated
public class ScrapDto {
    @RequiredArgsConstructor
    @Getter
    public static class PostRequest {
        @NotBlank @Length(max = 100)
        private final String description;
    }

    @RequiredArgsConstructor
    @Getter
    public static class SearchRequest {
        private final String keyword;
        private final Integer page;
        private final Integer size;
        private final ScrapProperty sort;
        private final Boolean asc;
    }

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
            this.review = ReviewDto.Response.of(scrap.getReview(), scrap.getUser());
        }
    }
}
