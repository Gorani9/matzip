package com.matzip.server.domain.review.api;

import com.matzip.server.domain.review.dto.ReviewDto;
import com.matzip.server.domain.review.service.ReviewService;
import com.matzip.server.domain.review.validation.ReviewProperty;
import com.matzip.server.domain.review.validation.ReviewSearchType;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.global.auth.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    @GetMapping
    public ResponseEntity<Page<ReviewDto.Response>> searchReviews(
            @CurrentUser User user,
            @RequestParam(defaultValue="0") @Valid @PositiveOrZero Integer pageNumber,
            @RequestParam(defaultValue="15") @Valid @Positive Integer pageSize,
            @RequestParam(defaultValue="createdAt") @Valid @ReviewProperty String sortedBy,
            @RequestParam(defaultValue="false") Boolean ascending,
            @RequestParam(defaultValue="content") @ReviewSearchType String searchType,
            @RequestParam @Valid @NotBlank String keyword) {
        return ResponseEntity.ok()
                .body(reviewService.searchReviews(
                        user.getUsername(),
                        new ReviewDto.SearchRequest(
                                pageNumber,
                                pageSize,
                                sortedBy,
                                ascending,
                                searchType,
                                keyword)));
    }

    @PostMapping(consumes={"multipart/form-data"})
    public ResponseEntity<ReviewDto.Response> postReview(
            @CurrentUser User user,
            @ModelAttribute ReviewDto.PostRequest postRequest) {
        return ResponseEntity.ok().body(reviewService.postReview(user.getUsername(), postRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewDto.Response> getReview(@CurrentUser User user, @PathVariable("id") Long id) {
        return ResponseEntity.ok().body(reviewService.getReview(user.getUsername(), id));
    }

    @PatchMapping(value="/{id}", consumes={"multipart/form-data"})
    public ResponseEntity<ReviewDto.Response> patchReview(
            @CurrentUser User user,
            @PathVariable("id") Long id,
            @ModelAttribute ReviewDto.PatchRequest patchRequest) {
        return ResponseEntity.ok().body(reviewService.patchReview(user.getUsername(), id, patchRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteReview(@CurrentUser User user, @PathVariable("id") Long id) {
        reviewService.deleteReview(user, id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/hot")
    public ResponseEntity<ReviewDto.HotResponse> getHotReviews(@CurrentUser User user) {
        return ResponseEntity.ok().body(reviewService.getHotReviews(user.getUsername()));
    }

    @GetMapping("/hall-of-fame")
    public ResponseEntity<ReviewDto.HallOfFameResponse> getHallOfFameReviews(@CurrentUser User user) {
        return ResponseEntity.ok().body(reviewService.getHallOfFameReviews(user.getUsername()));
    }
}
