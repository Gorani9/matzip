package com.matzip.server.domain.review.dto;

import com.matzip.server.domain.image.model.ReviewImage;
import com.matzip.server.domain.review.model.Review;
import com.matzip.server.domain.review.model.ReviewProperty;
import com.matzip.server.domain.user.dto.UserDto;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.global.common.dto.BaseResponse;
import com.matzip.server.global.common.dto.BlockedResponse;
import com.matzip.server.global.common.dto.DeletedResponse;
import com.matzip.server.global.common.model.BaseTimeEntity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Validated
public class ReviewDto {
    @RequiredArgsConstructor
    @Getter
    public static class SearchRequest {
        private final String keyword;
        private final Integer page;
        private final Integer size;
        private final ReviewProperty sort;
        private final Boolean asc;
    }

    @RequiredArgsConstructor
    @Getter
    public static class PostRequest {
        @NotBlank @Length(max=3000)
        private final String content;
        @NotEmpty
        private final List<MultipartFile> images;
        @NotNull @Range(min=0, max=10)
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
    public static class Response extends BaseResponse {
        private final Long id;
        private final LocalDateTime createdAt;
        private final LocalDateTime modifiedAt;
        private final BaseResponse user;
        private final String content;
        private final String imageUrl;
        private final Integer rating;
        private final String location;
        private final Boolean isDeletable;
        private final Boolean isHearted;
        private final Boolean isScraped;

        private Response(Review review, User user) {
            super(true);
            this.id = review.getId();
            this.createdAt = review.getCreatedAt();
            this.modifiedAt = review.getModifiedAt();
            this.user = UserDto.Response.ofNullable(review.getUser(), user);
            this.content = review.getContent();
            this.imageUrl = review.getReviewImages().stream().sorted(Comparator.comparing(BaseTimeEntity::getCreatedAt))
                    .filter(i -> !i.isBlocked()).map(ReviewImage::getImageUrl).findFirst().orElse(null);
            this.rating = review.getRating();
            this.location = review.getLocation();
            this.isDeletable = user == review.getUser();
            this.isHearted = user.getHearts().stream().anyMatch(h -> h.getReview() == review);
            this.isScraped = user.getScraps().stream().anyMatch(s -> s.getReview() == review);
        }

        public static BaseResponse ofNullable(Review review, User user) {
            if (review.isBlocked()) return BlockedResponse.ofBlockedReview();
            else if (review.isDeleted()) return DeletedResponse.ofDeletedReview();
            else return new Response(review, user);
        }

        public static Response of(Review review, User user) {
            return new Response(review, user);
        }
    }

    @Getter
    public static class DetailedResponse extends Response {
        private final List<String> imageUrls;
        private final List<BaseResponse> comments;
        private final Integer numberOfScraps;
        private final Integer numberOfHearts;

        public DetailedResponse(Review review, User user) {
            super(review, user);
            this.imageUrls = review.getReviewImages().stream().map(ReviewImage::getImageUrl).collect(Collectors.toList());
            this.comments = review.getComments().stream()
                    .filter(c -> !c.isBlocked()).map(c -> CommentDto.Response.ofBlockable(c, user)).collect(Collectors.toList());
            this.numberOfScraps = review.getScraps().size();
            this.numberOfHearts = review.getHearts().size();
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
