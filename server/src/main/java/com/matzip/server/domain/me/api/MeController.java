package com.matzip.server.domain.me.api;

import com.matzip.server.domain.me.dto.MeDto;
import com.matzip.server.domain.me.dto.ScrapDto;
import com.matzip.server.domain.me.service.MeService;
import com.matzip.server.domain.me.validation.FollowType;
import com.matzip.server.domain.review.dto.CommentDto;
import com.matzip.server.domain.review.dto.ReviewDto;
import com.matzip.server.domain.review.validation.CommentProperty;
import com.matzip.server.domain.review.validation.ReviewProperty;
import com.matzip.server.domain.user.dto.UserDto;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.domain.user.validation.UserProperty;
import com.matzip.server.global.auth.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Validated
@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/me")
public class MeController {
    private final MeService meService;

    @GetMapping
    public ResponseEntity<MeDto.Response> getMe(@CurrentUser User user) {
        return ResponseEntity.ok().body(meService.getMe(user.getUsername()));
    }

    @PatchMapping
    public ResponseEntity<MeDto.Response> patchMe(
            @CurrentUser User user,
            @RequestPart(required=false) MultipartFile profileImage,
            @RequestPart(required=false) @Valid @Length(max=50) String profileString) {
        return ResponseEntity.ok()
                .body(meService.patchMe(
                        user.getUsername(),
                        new MeDto.PatchProfileRequest(profileImage, profileString)));
    }

    @DeleteMapping
    public ResponseEntity<Object> deleteMe(@CurrentUser User user) {
        meService.deleteMe(user.getUsername());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/username")
    public ResponseEntity<MeDto.Response> changeUsername(
            @CurrentUser User user, @RequestBody @Valid MeDto.UsernameChangeRequest usernameChangeRequest) {
        return ResponseEntity.ok().body(meService.changeUsername(user.getUsername(), usernameChangeRequest));
    }

    @PutMapping("/password")
    public ResponseEntity<MeDto.Response> changePassword(
            @CurrentUser User user, @RequestBody @Valid MeDto.PasswordChangeRequest passwordChangeRequest) {
        return ResponseEntity.ok().body(meService.changePassword(user.getUsername(), passwordChangeRequest));
    }

    @GetMapping("/follows")
    public ResponseEntity<Page<UserDto.Response>> getMyFollows(
            @CurrentUser User user,
            @RequestParam(defaultValue="0") @Valid @PositiveOrZero Integer pageNumber,
            @RequestParam(defaultValue="15") @Valid @Positive Integer pageSize,
            @RequestParam(defaultValue="createdAt") @Valid @UserProperty String sortedBy,
            @RequestParam(defaultValue="false") Boolean ascending,
            @RequestParam(defaultValue="following") @Valid @FollowType String type) {
        return ResponseEntity.ok()
                .body(meService.getMyFollows(
                        user,
                        new MeDto.FindFollowRequest(pageNumber, pageSize, sortedBy, ascending, type)));
    }

    @PutMapping("/follows/{username}")
    public ResponseEntity<MeDto.Response> followAnotherUser(
            @CurrentUser User user,
            @PathVariable("username") String username) {
        return ResponseEntity.ok().body(meService.followUser(user.getUsername(), username));
    }

    @DeleteMapping("/follows/{username}")
    public ResponseEntity<MeDto.Response> unfollowAnotherUser(
            @CurrentUser User user,
            @PathVariable("username") String username) {
        return ResponseEntity.ok().body(meService.unfollowUser(user.getUsername(), username));
    }

    @GetMapping("/reviews")
    public ResponseEntity<Page<ReviewDto.Response>> getMyReviews(
            @CurrentUser User user,
            @RequestParam(defaultValue="0") @Valid @PositiveOrZero Integer pageNumber,
            @RequestParam(defaultValue="15") @Valid @Positive Integer pageSize,
            @RequestParam(defaultValue="createdAt") @Valid @ReviewProperty String sortedBy,
            @RequestParam(defaultValue="false") Boolean ascending) {
        return ResponseEntity.ok()
                .body(meService.getMyReviews(
                        user,
                        new MeDto.FindReviewRequest(pageNumber, pageSize, sortedBy, ascending)));
    }

    @GetMapping("/comments")
    public ResponseEntity<Page<CommentDto.Response>> getMyComments(
            @CurrentUser User user,
            @RequestParam(defaultValue="0") @Valid @PositiveOrZero Integer pageNumber,
            @RequestParam(defaultValue="15") @Valid @Positive Integer pageSize,
            @RequestParam(defaultValue="createdAt") @Valid @CommentProperty String sortedBy,
            @RequestParam(defaultValue="false") Boolean ascending) {
        return ResponseEntity.ok()
                .body(meService.getMyComments(
                        user,
                        new MeDto.FindCommentRequest(pageNumber, pageSize, sortedBy, ascending)));
    }

    @PutMapping("/hearts/{review-id}")
    public ResponseEntity<Object> putHeartOnReview(@CurrentUser User user, @PathVariable("review-id") Long reviewId) {
        meService.heartReview(user, reviewId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/hearts/{review-id}")
    public ResponseEntity<Object> deleteHeartFromReview(
            @CurrentUser User user,
            @PathVariable("review-id") Long reviewId) {
        meService.deleteHeartFromReview(user, reviewId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/scraps")
    public ResponseEntity<Page<ScrapDto.Response>> getMyScraps(
            @CurrentUser User user,
            @RequestParam(defaultValue="0") @Valid @PositiveOrZero Integer pageNumber,
            @RequestParam(defaultValue="15") @Valid @Positive Integer pageSize,
            @RequestParam(defaultValue="createdAt") @Valid @ReviewProperty String sortedBy,
            @RequestParam(defaultValue="false") Boolean ascending) {
        return ResponseEntity.ok()
                .body(meService.getMyScraps(
                        user,
                        new MeDto.FindReviewRequest(pageNumber, pageSize, sortedBy, ascending)));
    }

    @PutMapping("/scraps/{id}")
    public ResponseEntity<ScrapDto.Response> updateMyScrap(
            @CurrentUser User user, @PathVariable("id") Long reviewId,
            @RequestBody @Valid ScrapDto.Request request) {
        return ResponseEntity.ok().body(meService.scrapReview(user.getUsername(), reviewId, request));
    }

    @DeleteMapping("/scraps/{id}")
    public ResponseEntity<Object> deleteMyScrap(@CurrentUser User user, @PathVariable("id") Long reviewId) {
        meService.deleteMyScrap(user, reviewId);
        return ResponseEntity.ok().build();
    }
}
