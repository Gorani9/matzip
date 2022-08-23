package com.matzip.server.domain.review.dto;

import com.matzip.server.domain.me.model.Heart;
import com.matzip.server.domain.me.model.Scrap;
import com.matzip.server.domain.review.model.Review;
import com.matzip.server.domain.review.validation.ReviewProperty;
import com.matzip.server.domain.review.validation.ReviewSearchType;
import com.matzip.server.domain.user.dto.UserDto;
import com.matzip.server.domain.user.model.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;
import java.util.stream.Collectors;

public class ReviewDto {
    @RequiredArgsConstructor
    @Getter
    public static class SearchRequest {
        @PositiveOrZero
        private final Integer pageNumber;
        @Positive
        private final Integer pageSize;
        @ReviewProperty
        private final String sortedBy;
        @NotNull
        private final Boolean ascending;
        @ReviewSearchType
        private final String searchType;
        @NotBlank
        private final String keyword;
    }

    @RequiredArgsConstructor
    @Getter
    public static class PostRequest {
        @NotBlank
        @Length(max=3000)
        private final String content;
        private final List<MultipartFile> images;
        @Range(min=0, max=10)
        private final Integer rating;
        @NotBlank
        private final String location;
    }

    @RequiredArgsConstructor
    @Getter
    public static class PatchRequest {
        @Length(max=3000)
        private final String content;
        private final List<MultipartFile> newImages;
        private final List<String> oldUrls;
        @Range(min=0, max=10)
        private final Integer rating;
    }

    @Getter
    public static class Response {
        private final Long id;
        private final UserDto.Response user;
        private final String content;
        private final List<String> imageUrls;
        private final Integer rating;
        private final String location;
        private final List<CommentDto.Response> comments;
        private final Integer numberOfScraps;
        private final Integer numberOfHearts;
        private final Boolean isDeletable;
        private final Boolean isHearted;
        private final Boolean isScraped;

        public Response(User user, Review review) {
            this.id = review.getId();
            this.user = new UserDto.Response(review.getUser());
            this.content = review.getContent();
            this.imageUrls = review.getImageUrls();
            this.rating = review.getRating();
            this.location = review.getLocation();
            this.comments = review.getComments()
                    .stream()
                    .map(c -> new CommentDto.Response(user, c))
                    .collect(Collectors.toList());
            this.numberOfScraps = review.getScraps().size();
            this.numberOfHearts = review.getHearts().size();
            this.isDeletable = user.getId().equals(review.getUser().getId()) || user.getRole().equals("ADMIN");
            this.isHearted = review.getHearts().contains(new Heart(user, review));
            this.isScraped = review.getScraps().contains(new Scrap(user, review));
        }
    }

    @Getter
    public static class HotResponse {
        private final List<Response> dailyHotReviews;
        private final List<Response> weeklyHotReviews;
        private final List<Response> monthlyHotReviews;

        public HotResponse(
                List<Response> dailyHotReviews,
                List<Response> weeklyHotReviews,
                List<Response> monthlyHotReviews) {
            this.dailyHotReviews = dailyHotReviews;
            this.weeklyHotReviews = weeklyHotReviews;
            this.monthlyHotReviews = monthlyHotReviews;
        }
    }

    @Getter
    public static class HallOfFameResponse {
        private final List<Response> hallOfFameReviews;

        public HallOfFameResponse(List<Response> hallOfFameReviews) {
            this.hallOfFameReviews = hallOfFameReviews;
        }
    }
}
