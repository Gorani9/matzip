package com.matzip.server.domain.review.api;

import com.matzip.server.domain.review.dto.CommentDto;
import com.matzip.server.domain.review.service.CommentService;
import com.matzip.server.global.auth.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/comments")
public class CommentController {
    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentDto.Response> postComment(
            @CurrentUser Long myId,
            @RequestBody @Valid CommentDto.PostRequest postRequest) {
        return ResponseEntity.ok().body(commentService.postComment(myId, postRequest));
    }

    @GetMapping("/{comment-id}")
    public ResponseEntity<CommentDto.Response> getComment(
            @CurrentUser Long myId, @PathVariable("comment-id") @Positive Long commentId) {
        return ResponseEntity.ok().body(commentService.fetchComment(myId, commentId));
    }

    @PutMapping("/{comment-id}")
    public ResponseEntity<CommentDto.Response> patchComment(
            @CurrentUser Long myId,
            @PathVariable("comment-id") @Positive Long commentId,
            @RequestBody @Valid CommentDto.PutRequest putRequest) {
        return ResponseEntity.ok().body(commentService.putComment(myId, commentId, putRequest));
    }

    @DeleteMapping("/{comment-id}")
    public ResponseEntity<Object> deleteComment(
            @CurrentUser Long myId,
            @PathVariable("comment-id") @Positive Long commentId) {
        commentService.deleteComment(myId, commentId);
        return ResponseEntity.ok().build();
    }
}
