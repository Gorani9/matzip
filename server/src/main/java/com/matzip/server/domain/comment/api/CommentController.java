package com.matzip.server.domain.comment.api;

import com.matzip.server.domain.auth.model.CurrentUser;
import com.matzip.server.domain.auth.model.CurrentUsername;
import com.matzip.server.domain.comment.dto.CommentDto;
import com.matzip.server.domain.comment.dto.CommentDto.PatchRequest;
import com.matzip.server.domain.comment.dto.CommentDto.PostRequest;
import com.matzip.server.domain.comment.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;

@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/comments")
public class CommentController {
    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentDto.Response> postComment(
            @CurrentUser Long myId,
            @CurrentUsername String user,
            @RequestBody @Valid PostRequest request
    ) {
        log.info("""
                         [{}(id={})] POST /api/v1/comments
                         \t review ID = {}
                         \t content = {}""", user, myId, request.getReviewId(), request.getContent());
        return ResponseEntity.ok(commentService.postComment(myId, request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CommentDto.Response> patchComment(
            @CurrentUser Long myId,
            @CurrentUsername String user,
            @PathVariable("id") @Positive Long commentId,
            @RequestBody @Valid PatchRequest request
    ) {
        log.info("[{}(id={})] PATCH /api/v1/comments/{}\n" +
                 "\t content = {}", user, myId, commentId, request.content());
        return ResponseEntity.ok(commentService.patchComment(myId, commentId, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteComment(
            @CurrentUser Long myId,
            @CurrentUsername String user,
            @PathVariable("id") @Positive Long commentId
    ) {
        log.info("[{}(id={})] DELETE /api/v1/comments/{}", user, myId, commentId);
        commentService.deleteComment(myId, commentId);
        return ResponseEntity.ok().build();
    }
}
