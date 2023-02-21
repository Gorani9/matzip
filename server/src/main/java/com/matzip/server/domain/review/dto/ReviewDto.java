package com.matzip.server.domain.review.dto;

import com.matzip.server.domain.comment.dto.CommentDto;
import com.matzip.server.domain.review.model.Review;
import com.matzip.server.domain.review.model.ReviewProperty;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Validated
public class ReviewDto {
    public record SearchRequest(String keyword, Integer page, Integer size, ReviewProperty sort, Boolean asc) {}

    public record PostRequest(
            @NotBlank @Length(max=3000) String content,
            @NotEmpty @Size(min=1, max=10) List<MultipartFile> images,
            @NotNull @Range(min=0, max=10) Integer rating,
            @NotBlank String location
    ) {}

    public record PatchRequest (
            @NullableNotBlank @Length(max=3000) String content,
            @Size(min=1, max=10) List<MultipartFile> images,
            @Size(min=1, max=10) List<@NotBlank @URL String> oldUrls,
            @Range(min=0, max=10) Integer rating
    ) {}

    @Getter
    public static class Response {
        private final Long id;
        private final LocalDateTime createdAt;
        private final LocalDateTime modifiedAt;
        private final UserDto.Response user;
        private final String content;
        private final String imageUrl;
        private final Integer rating;
        private final String location;
        private final Long views;
        private final Boolean isDeletable;
        private final Boolean isHearted;
        private final Boolean isScraped;

        public Response(Review review, User user) {
            this.id = review.getId();
            this.createdAt = review.getCreatedAt();
            this.modifiedAt = review.getModifiedAt();
            this.user = new UserDto.Response(review.getUser(), user);
            this.content = review.getContent();
            this.imageUrl = review.getReviewImages().stream().findFirst().orElse(null);
            this.rating = review.getRating();
            this.location = review.getLocation();
            this.views = review.getViews();
            this.isDeletable = user == review.getUser();
            this.isHearted = user.getHearts().stream().anyMatch(h -> h.getReview() == review);
            this.isScraped = user.getScraps().stream().anyMatch(s -> s.getReview() == review);
        }
    }

    @Getter
    public static class DetailedResponse extends Response {
        private final List<String> imageUrls;
        private final List<CommentDto.Response> comments;
        private final Integer numberOfScraps;
        private final Integer numberOfHearts;

        public DetailedResponse(Review review, User user) {
            super(review, user);
            this.imageUrls = new ArrayList<>(review.getReviewImages());
            this.comments = review.getComments().stream().map(c -> new CommentDto.Response(c, user)).collect(Collectors.toList());
            this.numberOfScraps = review.getScraps().size();
            this.numberOfHearts = review.getHearts().size();
        }
    }
}
