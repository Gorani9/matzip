package com.matzip.server.domain.comment.service;

import com.matzip.server.domain.comment.dto.CommentDto;
import com.matzip.server.domain.comment.dto.CommentDto.PatchRequest;
import com.matzip.server.domain.comment.exception.CommentAccessDeniedException;
import com.matzip.server.domain.comment.exception.CommentNotFoundException;
import com.matzip.server.domain.comment.model.Comment;
import com.matzip.server.domain.comment.repository.CommentRepository;
import com.matzip.server.domain.record.service.RecordService;
import com.matzip.server.domain.review.dto.ReviewDto.Response;
import com.matzip.server.domain.review.exception.ReviewNotFoundException;
import com.matzip.server.domain.review.model.Review;
import com.matzip.server.domain.review.repository.ReviewRepository;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly=true)
public class CommentService {
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final CommentRepository commentRepository;
    private final RecordService recordService;

    @Transactional
    public Response postComment(Long myId, CommentDto.PostRequest request) {
        User me = userRepository.findMeById(myId);
        Long reviewId = request.getReviewId();
        Review review = reviewRepository.findByIdFetchJoinComments(reviewId).orElseThrow(() -> new ReviewNotFoundException(reviewId));

        commentRepository.save(new Comment(me, review, request.getContent()));

        recordService.postComment(review, me);

        return new Response(review, me);
    }

    @Transactional
    public Response patchComment(Long myId, Long commentId, PatchRequest request) {
        User me = userRepository.findMeById(myId);
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new CommentNotFoundException(commentId));

        if (comment.getUser() != me) throw new CommentAccessDeniedException();

        comment.setContent(request.content());
        comment.update();

        return new Response(comment.getReview(), me);
    }

    @Transactional
    public Response deleteComment(Long myId, Long commentId) {
        User me = userRepository.findMeById(myId);
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new CommentNotFoundException(commentId));

        if (comment.getUser() != me) throw new CommentAccessDeniedException();

        comment.delete();
        commentRepository.delete(comment);
        recordService.deleteComment(comment.getReview(), me);

        return new Response(comment.getReview(), me);
    }
}
