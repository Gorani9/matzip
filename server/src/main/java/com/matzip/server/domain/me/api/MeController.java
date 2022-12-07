package com.matzip.server.domain.me.api;

import com.matzip.server.domain.me.dto.HeartDto;
import com.matzip.server.domain.me.dto.MeDto;
import com.matzip.server.domain.me.dto.ScrapDto;
import com.matzip.server.domain.me.model.ScrapProperty;
import com.matzip.server.domain.me.service.MeService;
import com.matzip.server.domain.review.dto.CommentDto;
import com.matzip.server.domain.review.dto.ReviewDto;
import com.matzip.server.domain.review.model.CommentProperty;
import com.matzip.server.domain.review.model.ReviewProperty;
import com.matzip.server.domain.user.dto.UserDto;
import com.matzip.server.domain.user.model.UserProperty;
import com.matzip.server.global.auth.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Validated
@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/me")
public class MeController {
    private final MeService meService;

    @GetMapping
    public ResponseEntity<MeDto.Response> getMe(@CurrentUser Long myId) {
        return ResponseEntity.ok().body(meService.getMe(myId));
    }

    @PatchMapping(consumes={"multipart/form-data"})
    public ResponseEntity<MeDto.Response> patchMe(
            @CurrentUser Long myId,
            @ModelAttribute @Valid MeDto.PatchProfileRequest patchProfileRequest) {
        return ResponseEntity.ok().body(meService.patchMe(myId, patchProfileRequest));
    }

    @DeleteMapping
    public ResponseEntity<Object> deleteMe(@CurrentUser Long myId) {
        meService.deleteMe(myId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/username")
    public ResponseEntity<MeDto.Response> changeUsername(
            @CurrentUser Long myId, @RequestBody @Valid MeDto.UsernameChangeRequest usernameChangeRequest) {
        return ResponseEntity.ok().body(meService.changeUsername(myId, usernameChangeRequest));
    }

    @PutMapping("/password")
    public ResponseEntity<MeDto.Response> changePassword(
            @CurrentUser Long myId, @RequestBody @Valid MeDto.PasswordChangeRequest passwordChangeRequest) {
        return ResponseEntity.ok().body(meService.changePassword(myId, passwordChangeRequest));
    }

    @GetMapping("/followers")
    public ResponseEntity<Slice<UserDto.Response>> searchMyFollowers(
            @CurrentUser Long myId,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "page", required = false, defaultValue ="0") @PositiveOrZero Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "20") @Positive @Max(100) Integer size,
            @RequestParam(value = "sort", required = false) String userProperty,
            @RequestParam(value = "asc", required = false, defaultValue = "false") Boolean asc) {
        return ResponseEntity.ok()
                .body(meService.searchMyFollowers(
                        myId, new UserDto.SearchRequest(username, page, size, UserProperty.from(userProperty), asc)));
    }

    @GetMapping("/followings")
    public ResponseEntity<Slice<UserDto.Response>> searchMyFollowings(
            @CurrentUser Long myId,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "page", required = false, defaultValue ="0") @PositiveOrZero Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "20") @Positive @Max(100) Integer size,
            @RequestParam(value = "sort", required = false) String userProperty,
            @RequestParam(value = "asc", required = false, defaultValue = "false") Boolean asc) {
        return ResponseEntity.ok()
                .body(meService.searchMyFollowings(
                        myId, new UserDto.SearchRequest(username, page, size, UserProperty.from(userProperty), asc)));
    }

    @PutMapping("/follows/{username}")
    public ResponseEntity<MeDto.Response> followAnotherUser(
            @CurrentUser Long myId,
            @PathVariable("username") String username) {
        return ResponseEntity.ok().body(meService.followUser(myId, username));
    }

    @DeleteMapping("/follows/{username}")
    public ResponseEntity<MeDto.Response> unfollowAnotherUser(
            @CurrentUser Long myId,
            @PathVariable("username") String username) {
        return ResponseEntity.ok().body(meService.unfollowUser(myId, username));
    }

    @GetMapping("/reviews")
    public ResponseEntity<Slice<ReviewDto.Response>> searchMyReviews(
            @CurrentUser Long myId,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "page", required = false, defaultValue ="0") @PositiveOrZero Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "20") @Positive @Max(100) Integer size,
            @RequestParam(value = "sort", required = false) String reviewProperty,
            @RequestParam(value = "asc", required = false, defaultValue = "false") Boolean asc) {
        return ResponseEntity.ok()
                .body(meService.searchMyReviews(
                        myId, new ReviewDto.SearchRequest(content, page, size, ReviewProperty.from(reviewProperty), asc)));
    }

    @GetMapping("/comments")
    public ResponseEntity<Slice<CommentDto.Response>> searchMyComments(
            @CurrentUser Long myId,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "page", required = false, defaultValue ="0") @PositiveOrZero Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "20") @Positive @Max(100) Integer size,
            @RequestParam(value = "sort", required = false) String commentProperty,
            @RequestParam(value = "asc", required = false, defaultValue = "false") Boolean asc) {
        return ResponseEntity.ok()
                .body(meService.searchMyComments(
                        myId, new CommentDto.SearchRequest(content, page, size, CommentProperty.from(commentProperty), asc)));
    }

    @PutMapping("/hearts/{review-id}")
    public ResponseEntity<HeartDto.Response> putHeartOnReview(
            @CurrentUser Long myId,
            @PathVariable("review-id") Long reviewId) {
        return ResponseEntity.ok().body(meService.heartReview(myId, reviewId));
    }

    @DeleteMapping("/hearts/{review-id}")
    public ResponseEntity<HeartDto.Response> deleteHeartFromReview(
            @CurrentUser Long myId,
            @PathVariable("review-id") Long reviewId) {
        return ResponseEntity.ok().body(meService.deleteHeartFromReview(myId, reviewId));
    }

    @GetMapping("/scraps")
    public ResponseEntity<Slice<ScrapDto.Response>> searchMyScraps(
            @CurrentUser Long myId,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "page", required = false, defaultValue ="0") @PositiveOrZero Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "20") @Positive @Max(100) Integer size,
            @RequestParam(value = "sort", required = false) String scrapProperty,
            @RequestParam(value = "asc", required = false, defaultValue = "false") Boolean asc) {
        return ResponseEntity.ok()
                .body(meService.searchMyScraps(
                        myId, new ScrapDto.SearchRequest(content, page, size, ScrapProperty.from(scrapProperty), asc)));
    }

    @PutMapping("/scraps/{review-id}")
    public ResponseEntity<ScrapDto.Response> putMyScrap(
            @CurrentUser Long myId, @PathVariable("review-id") Long reviewId, @RequestBody @Valid ScrapDto.PostRequest postRequest) {
        return ResponseEntity.ok().body(meService.scrapReview(myId, reviewId, postRequest));
    }

    @DeleteMapping("/scraps/{review-id}")
    public ResponseEntity<Object> deleteMyScrap(@CurrentUser Long myId, @PathVariable("review-id") Long reviewId) {
        meService.deleteMyScrap(myId, reviewId);
        return ResponseEntity.ok().build();
    }
}
