package com.matzip.server.domain.review.api;

import com.matzip.server.domain.review.dto.ReviewDto.PatchRequest;
import com.matzip.server.domain.review.dto.ReviewDto.PostRequest;
import com.matzip.server.domain.review.dto.ReviewDto.Response;
import com.matzip.server.domain.review.dto.ReviewDto.ScrapRequest;
import com.matzip.server.domain.review.service.ReviewService;
import com.matzip.server.global.auth.model.CurrentUser;
import com.matzip.server.global.auth.model.CurrentUsername;
import com.matzip.server.global.common.logger.Logging;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping(consumes={"multipart/form-data"})
    @Logging(endpoint="POST /api/v1/reviews")
    public ResponseEntity<Response> postReview(
            @CurrentUser Long myId,
            @CurrentUsername String user,
            @ModelAttribute @Valid PostRequest request
    ) {
        return ResponseEntity.ok(reviewService.postReview(myId, request));
    }

    @GetMapping("/{id}")
    @Logging(endpoint="GET /api/v1/reviews/{pathVariable}", pathVariable = true)
    public ResponseEntity<Response> fetchReview(
            @CurrentUser Long myId,
            @CurrentUsername String user,
            @PathVariable("id") @NotNull @Positive Long reviewId
    ) {
        return ResponseEntity.ok(reviewService.fetchReview(myId, reviewId));
    }

    @PatchMapping(value="/{id}", consumes={"multipart/form-data"})
    @Logging(endpoint="PATCH /api/v1/reviews/{pathVariable}", pathVariable = true)
    public ResponseEntity<Response> patchReview(
            @CurrentUser Long myId,
            @CurrentUsername String user,
            @PathVariable("id") @NotNull @Positive Long reviewId,
            @ModelAttribute @Valid PatchRequest request
    ) {
        return ResponseEntity.ok(reviewService.patchReview(myId, reviewId, request));
    }

    @DeleteMapping("/{id}")
    @Logging(endpoint="DELETE /api/v1/reviews/{pathVariable}", pathVariable = true)
    public ResponseEntity<Object> deleteReview(
            @CurrentUser Long myId,
            @CurrentUsername String user,
            @PathVariable("id") @NotNull @Positive Long reviewId
    ) {
        reviewService.deleteReview(myId, reviewId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/heart")
    @Logging(endpoint="PUT /api/v1/reviews/{pathVariable}/heart", pathVariable = true)
    public ResponseEntity<Response> putHeartOnReview(
            @CurrentUser Long myId,
            @CurrentUsername String user,
            @PathVariable("id") @NotNull @Positive Long reviewId
    ) {
        return ResponseEntity.ok(reviewService.heartReview(myId, reviewId));
    }

    @DeleteMapping("/{id}/heart")
    @Logging(endpoint="DELETE /api/v1/reviews/{pathVariable}/heart", pathVariable = true)
    public ResponseEntity<Response> deleteHeartFromReview(
            @CurrentUser Long myId,
            @CurrentUsername String user,
            @PathVariable("id") @NotNull @Positive Long reviewId
    ) {
        return ResponseEntity.ok(reviewService.deleteHeartFromReview(myId, reviewId));
    }

    @PutMapping("/{id}/scrap")
    @Logging(endpoint="PUT /api/v1/reviews/{pathVariable}/scrap", pathVariable = true)
    public ResponseEntity<Response> putScrap(
            @CurrentUser Long myId,
            @CurrentUsername String user,
            @PathVariable("id") @Positive Long reviewId,
            @RequestBody @Valid ScrapRequest request
    ) {
        return ResponseEntity.ok(reviewService.putScrap(myId, reviewId, request));
    }

    @DeleteMapping("/{id}/scrap")
    @Logging(endpoint="DELETE /api/v1/reviews/{pathVariable}/scrap", pathVariable = true)
    public ResponseEntity<Response> deleteScrap(
            @CurrentUser Long myId,
            @CurrentUsername String user,
            @PathVariable("id") @Positive Long reviewId
    ) {
        return ResponseEntity.ok(reviewService.deleteScrap(myId, reviewId));
    }
}
