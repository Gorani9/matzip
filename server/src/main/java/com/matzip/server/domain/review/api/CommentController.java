package com.matzip.server.domain.review.api;

import com.matzip.server.domain.review.dto.CommentDto;
import com.matzip.server.domain.review.model.CommentProperty;
import com.matzip.server.domain.review.service.CommentService;
import com.matzip.server.global.auth.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/comments")
public class CommentController {
    private final CommentService commentService;

    @GetMapping
    public ResponseEntity<Slice<CommentDto.Response>> searchComments(
            @CurrentUser Long myId,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", required = false, defaultValue = "0") @PositiveOrZero Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "20") @Positive @Max(100) Integer size,
            @RequestParam(value = "sort", required = false) String commentProperty,
            @RequestParam(value = "asc", required = false, defaultValue = "false") Boolean asc) {
        return ResponseEntity.ok()
                .body(commentService.searchComment(
                        myId,
                        new CommentDto.SearchRequest(keyword, page, size, CommentProperty.from(commentProperty), asc)));
    }

    @PostMapping
    public ResponseEntity<CommentDto.Response> postComment(
            @CurrentUser Long myId,
            @RequestBody @Valid CommentDto.PostRequest postRequest) {
        return ResponseEntity.ok().body(commentService.postComment(myId, postRequest));
    }

    @GetMapping("/{comment-id}")
    public ResponseEntity<CommentDto.Response> getComment(
            @CurrentUser Long myId, @PathVariable("comment-id") Long commentId) {
        return ResponseEntity.ok().body(commentService.getComment(myId, commentId));
    }

    @PutMapping("/{comment-id}")
    public ResponseEntity<CommentDto.Response> patchComment(
            @CurrentUser Long myId,
            @PathVariable("comment-id") Long commentId,
            @RequestBody @Valid CommentDto.PutRequest putRequest) {
        return ResponseEntity.ok().body(commentService.putComment(myId, commentId, putRequest));
    }

    @DeleteMapping("/{comment-id}")
    public ResponseEntity<Object> deleteComment(@CurrentUser Long myId, @PathVariable("comment-id") Long commentId) {
        commentService.deleteComment(myId, commentId);
        return ResponseEntity.ok().build();
    }

}
