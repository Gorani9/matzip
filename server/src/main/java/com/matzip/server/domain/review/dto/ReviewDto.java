package com.matzip.server.domain.review.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.matzip.server.domain.comment.dto.CommentDto;
import com.matzip.server.domain.review.model.Review;
import com.matzip.server.domain.review.model.Scrap;
import com.matzip.server.domain.user.dto.UserDto;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.global.common.validation.NullableNotBlank;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import org.hibernate.validator.constraints.URL;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Validated
public class ReviewDto {
    public record PostRequest(
            @NotBlank @Length(max=3000) String content,
            @NotEmpty @Size(min=1, max=10) List<MultipartFile> images,
            @NotNull @Range(min=0, max=5) Integer rating,
            @NotBlank String restaurant
    ) {
        @Override
        public String toString() {
            return "PostRequest{" +
                   "content='" + content + '\'' +
                   ", #images=" + images.size() +
                   ", rating=" + rating +
                   ", restaurant='" + restaurant + '\'' +
                   '}';
        }
    }

    public record PatchRequest (
            @NullableNotBlank @Length(max=3000) String content,
            @Size(min=1, max=10) List<MultipartFile> images,
            @JsonProperty("old_urls")
            @Size(min=1, max=10) List<@NotBlank @URL String> oldUrls,
            @Range(min=0, max=5) Integer rating
    ) {
        @Override
        public String toString() {
            return "PatchRequest{" +
                   "content='" + content + '\'' +
                   ", #images=" + (images == null ? "null" : images.size()) +
                   ", oldUrls=" + (oldUrls == null ? "null" : oldUrls.stream()
                           .map(s -> s.substring(s.lastIndexOf('/') + 1))
                           .collect(Collectors.joining(", "))) +
                   ", rating=" + rating +
                   '}';
        }
    }

    public record ScrapRequest(@NotBlank @Length(max=100) String description) {}

    @Getter
    public static class Response {
        private final Long id;
        private final LocalDateTime createdAt;
        private final LocalDateTime modifiedAt;
        private final UserDto.Response user;
        private final String content;
        private final List<String> imageUrls;
        private final Integer rating;
        private final String restaurant;
        private final Long views;
        private final Boolean isDeletable;
        private final Boolean isHearted;
        private final Boolean isScraped;
        private final String scrapDescription;
        private final Integer numberOfScraps;
        private final Integer numberOfHearts;
        private final List<CommentDto.Response> comments;

        public Response(Review review, User user) {
            Scrap scrap = user.getScraps().stream().filter(s -> s.getReview() == review).findFirst().orElse(null);

            this.id = review.getId();
            this.createdAt = review.getCreatedAt();
            this.modifiedAt = review.getModifiedAt();
            this.user = new UserDto.Response(review.getUser(), user);
            this.content = review.getContent();
            this.imageUrls = review.getReviewImages();
            this.rating = review.getRating();
            this.restaurant = review.getRestaurant();
            this.views = review.getViews();
            this.isDeletable = user == review.getUser();
            this.isHearted = user.getHearts().stream().anyMatch(h -> h.getReview() == review);
            this.isScraped = scrap != null;
            this.numberOfScraps = review.getScraps().size();
            this.numberOfHearts = review.getHearts().size();
            this.comments = review.getComments().stream().map(c -> new CommentDto.Response(c, user)).collect(Collectors.toList());
            this.scrapDescription = isScraped ? scrap.getDescription() : null;
        }

        public Response(Scrap scrap) {
            this(scrap.getReview(), scrap.getUser());
        }
    }
}
