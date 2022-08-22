package com.matzip.server.domain.me.dto;

import com.matzip.server.domain.me.model.Scrap;
import com.matzip.server.domain.review.dto.ReviewDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Validated
public class ScrapDto {
    @RequiredArgsConstructor
    @Getter
    public static class Request {
        @NotBlank
        private final String description;
    }

    @Getter
    public static class Response {
        private final Long id;
        private final String description;
        private final ReviewDto.Response review;

        public Response(Scrap scrap) {
            this.id = scrap.getId();
            this.description = scrap.getDescription();
            this.review = new ReviewDto.Response(scrap.getUser(), scrap.getReview());
        }
    }
}
