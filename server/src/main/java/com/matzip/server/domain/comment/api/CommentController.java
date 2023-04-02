package com.matzip.server.domain.comment.api;

import com.matzip.server.domain.comment.dto.CommentDto.PatchRequest;
import com.matzip.server.domain.comment.dto.CommentDto.PostRequest;
import com.matzip.server.domain.comment.service.CommentService;
import com.matzip.server.domain.review.dto.ReviewDto.Response;
import com.matzip.server.global.auth.model.CurrentUser;
import com.matzip.server.global.auth.model.CurrentUsername;
import com.matzip.server.global.common.logger.Logging;
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
    @Logging(endpoint="POST /api/v1/comments")
    public ResponseEntity<Response> postComment(
            @CurrentUser Long myId,
            @CurrentUsername String user,
            @RequestBody @Valid PostRequest request
    ) {
        return ResponseEntity.ok(commentService.postComment(myId, request));
    }

    @PatchMapping("/{id}")
    @Logging(endpoint="PATCH /api/v1/comments/{pathVariable}", pathVariable=true)
    public ResponseEntity<Response> patchComment(
            @CurrentUser Long myId,
            @CurrentUsername String user,
            @PathVariable("id") @Positive Long commentId,
            @RequestBody @Valid PatchRequest request
    ) {
        return ResponseEntity.ok(commentService.patchComment(myId, commentId, request));
    }

    @DeleteMapping("/{id}")
    @Logging(endpoint="DELETE /api/v1/comments/{pathVariable}", pathVariable=true)
    public ResponseEntity<Response> deleteComment(
            @CurrentUser Long myId,
            @CurrentUsername String user,
            @PathVariable("id") @Positive Long commentId
    ) {
        return ResponseEntity.ok(commentService.deleteComment(myId, commentId));
    }
}
