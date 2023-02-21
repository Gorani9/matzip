package com.matzip.server.domain.review.api;

import com.matzip.server.domain.auth.model.CurrentUser;
import com.matzip.server.domain.auth.model.CurrentUsername;
import com.matzip.server.domain.review.dto.ReviewDto.*;
import com.matzip.server.domain.review.model.ReviewProperty;
import com.matzip.server.domain.review.service.ReviewService;
import com.matzip.server.global.common.dto.ListResponse;
import com.matzip.server.global.common.validation.NullableNotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;
import java.util.Optional;

@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    @GetMapping
    public ResponseEntity<Slice<Response>> searchReviews(
            @CurrentUser Long myId,
            @CurrentUsername String user,
            @RequestParam(value = "keyword", required = false) @NullableNotBlank @Length(max=30) String keyword,
            @RequestParam(value = "page", required = false, defaultValue = "0") @PositiveOrZero Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "20") @Positive @Max(100) Integer size,
            @RequestParam(value = "sort", required = false) ReviewProperty reviewProperty,
            @RequestParam(value = "asc", required = false, defaultValue = "false") Boolean asc
    ) {
        log.info("""
                         [{}(id={})] GET /api/v1/reviews?
                         \t keyword = {}
                         \t page = {}
                         \t size = {}
                         \t reviewProperty = {}
                         \t asc = {}""",
                 user, myId, keyword, page, size, reviewProperty, asc);
        return ResponseEntity.ok(reviewService.searchReviews(myId, new SearchRequest(keyword, page, size, reviewProperty, asc)));
    }

    @PostMapping(consumes={"multipart/form-data"})
    public ResponseEntity<DetailedResponse> postReview(
            @CurrentUser Long myId,
            @CurrentUsername String user,
            @ModelAttribute @Valid PostRequest request
    ) {
        log.info("""
                         [{}(id={})] POST /api/v1/reviews\t content = {}
                         \t #images = {}
                         \t rating = {}
                         \t location = {}
                         """,
                 user, myId, request.content(), request.images().size(), request.rating(), request.location());
        return ResponseEntity.ok(reviewService.postReview(myId, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DetailedResponse> fetchReview(
            @CurrentUser Long myId,
            @CurrentUsername String user,
            @PathVariable("id") @NotNull @Positive Long reviewId
    ) {
        log.info("[{}(id={})] GET /api/v1/reviews/{}", user, myId, reviewId);
        return ResponseEntity.ok(reviewService.fetchReview(myId, reviewId));
    }

    @PatchMapping(value="/{id}", consumes={"multipart/form-data"})
    public ResponseEntity<DetailedResponse> patchReview(
            @CurrentUser Long myId,
            @CurrentUsername String user,
            @PathVariable("id") @NotNull @Positive Long reviewId,
            @ModelAttribute @Valid PatchRequest request
    ) {
        log.info("""
                         [{}(id={})] PATCH /api/v1/reviews/{}\t content = {}
                         \t #new images = {}
                         \t old images = {}
                         \t rating = {}""",
                 user, myId, reviewId, request.content(), Optional.ofNullable(request.images()).orElse(List.of()).size(),
                 request.oldUrls(), request.rating());
        return ResponseEntity.ok(reviewService.patchReview(myId, reviewId, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteReview(
            @CurrentUser Long myId,
            @CurrentUsername String user,
            @PathVariable("id") @NotNull @Positive Long reviewId
    ) {
        log.info("[{}(id={})] DELETE /api/v1/reviews/{}", user, myId, reviewId);
        reviewService.deleteReview(myId, reviewId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/hot")
    public ResponseEntity<ListResponse<Response>> fetchHotReviews(
            @CurrentUser Long myId,
            @CurrentUsername String user
    ) {
        log.info("[{}(id={})] GET /api/v1/reviews/hot", user, myId);
        return ResponseEntity.ok(reviewService.getHotReviews(myId));
    }

    @PutMapping("/{id}/heart")
    public ResponseEntity<DetailedResponse> putHeartOnReview(
            @CurrentUser Long myId,
            @CurrentUsername String user,
            @PathVariable("id") @NotNull @Positive Long reviewId
    ) {
        log.info("[{}(id={})] PUT /api/v1/reviews/{}/heart", user, myId, reviewId);
        return ResponseEntity.ok(reviewService.heartReview(myId, reviewId));
    }

    @DeleteMapping("/{id}/heart")
    public ResponseEntity<DetailedResponse> deleteHeartFromReview(
            @CurrentUser Long myId,
            @CurrentUsername String user,
            @PathVariable("id") @NotNull @Positive Long reviewId
    ) {
        log.info("[{}(id={})] DELETE /api/v1/reviews/{}/heart", user, myId, reviewId);
        return ResponseEntity.ok(reviewService.deleteHeartFromReview(myId, reviewId));
    }
}
