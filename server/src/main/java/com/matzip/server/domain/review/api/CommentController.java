package com.matzip.server.domain.review.api;

import com.matzip.server.domain.review.dto.CommentDto;
import com.matzip.server.domain.review.service.CommentService;
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
@RequestMapping("/api/v1/comments")
public class CommentController {
    private final CommentService commentService;

    @GetMapping
    public ResponseEntity<Page<CommentDto.Response>> searchComments(
            @CurrentUser User user,
            @RequestParam(defaultValue="0") @Valid @PositiveOrZero Integer pageNumber,
            @RequestParam(defaultValue="15") @Valid @Positive Integer pageSize,
            @RequestParam(defaultValue="createdAt") @Valid String sortedBy,
            @RequestParam(defaultValue="false") Boolean ascending,
            @RequestParam @Valid @NotBlank String keyword) {
        return ResponseEntity.ok()
                .body(commentService.searchComment(
                        user.getUsername(),
                        new CommentDto.SearchRequest(pageNumber, pageSize, sortedBy, ascending, keyword)));
    }

    @PostMapping
    public ResponseEntity<CommentDto.Response> postComment(
            @CurrentUser User user,
            @RequestBody @Valid CommentDto.PostRequest postRequest) {
        return ResponseEntity.ok().body(commentService.postComment(user.getUsername(), postRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommentDto.Response> getComment(@CurrentUser User user, @PathVariable("id") Long id) {
        return ResponseEntity.ok().body(commentService.getComment(user.getUsername(), id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommentDto.Response> patchComment(
            @CurrentUser User user,
            @PathVariable("id") Long id,
            @RequestBody @Valid CommentDto.PutRequest putRequest) {
        return ResponseEntity.ok().body(commentService.putComment(user.getUsername(), id, putRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteComment(@CurrentUser User user, @PathVariable("id") Long id) {
        commentService.deleteComment(user, id);
        return ResponseEntity.ok().build();
    }

}
