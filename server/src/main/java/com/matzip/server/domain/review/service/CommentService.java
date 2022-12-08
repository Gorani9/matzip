package com.matzip.server.domain.review.service;

import com.matzip.server.domain.review.dto.CommentDto;
import com.matzip.server.domain.review.dto.CommentDto.Response;
import com.matzip.server.domain.review.exception.AccessBlockedOrDeletedReviewException;
import com.matzip.server.domain.review.exception.CommentChangeByAnonymousException;
import com.matzip.server.domain.review.exception.CommentNotFoundException;
import com.matzip.server.domain.review.exception.ReviewNotFoundException;
import com.matzip.server.domain.review.model.Comment;
import com.matzip.server.domain.review.model.Review;
import com.matzip.server.domain.review.repository.CommentRepository;
import com.matzip.server.domain.review.repository.ReviewRepository;
import com.matzip.server.domain.user.exception.AccessBlockedOrDeletedCommentException;
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

    @Transactional
    public Response postComment(Long myId, CommentDto.PostRequest postRequest) {
        User me = userRepository.findMeById(myId);
        Long reviewId = postRequest.getReviewId();
        Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new ReviewNotFoundException(reviewId));
        if (review.isBlocked() || review.isDeleted()) throw new AccessBlockedOrDeletedReviewException(postRequest.getReviewId());
        return new Response(commentRepository.save(new Comment(me, review, postRequest.getContent())), me);
    }

    public Response fetchComment(Long myId, Long commentId) {
        User me = userRepository.findMeById(myId);
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new CommentNotFoundException(commentId));
        if (comment.isBlocked() || comment.isDeleted()) throw new AccessBlockedOrDeletedCommentException(commentId);
        return new Response(comment, me);
    }

    @Transactional
    public Response putComment(Long myId, Long commentId, CommentDto.PutRequest putRequest) {
        User me = userRepository.findMeById(myId);
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new CommentNotFoundException(commentId));
        if (comment.isBlocked() || comment.isDeleted()) throw new AccessBlockedOrDeletedCommentException(commentId);
        if (comment.getUser() != me)
            throw new CommentChangeByAnonymousException();
        comment.setContent(putRequest.getContent());
        return new Response(comment, me);
    }

    @Transactional
    public void deleteComment(Long myId, Long commentId) {
        User me = userRepository.findMeById(myId);
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new CommentNotFoundException(commentId));
        if (comment.isBlocked() || comment.isDeleted()) throw new AccessBlockedOrDeletedCommentException(commentId);
        if (comment.getUser() != me)
            throw new CommentChangeByAnonymousException();
        comment.delete();
    }
}
