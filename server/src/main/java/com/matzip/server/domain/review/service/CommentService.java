package com.matzip.server.domain.review.service;

import com.matzip.server.domain.review.dto.CommentDto;
import com.matzip.server.domain.review.dto.CommentDto.Response;
import com.matzip.server.domain.review.exception.CommentChangeByAnonymousException;
import com.matzip.server.domain.review.exception.CommentNotFoundException;
import com.matzip.server.domain.review.exception.ReviewNotFoundException;
import com.matzip.server.domain.review.model.Comment;
import com.matzip.server.domain.review.model.Review;
import com.matzip.server.domain.review.repository.CommentRepository;
import com.matzip.server.domain.review.repository.ReviewRepository;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly=true)
public class CommentService {
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final CommentRepository commentRepository;

    public Slice<Response> searchComment(Long myId, CommentDto.SearchRequest searchRequest) {
        User me = userRepository.findMeById(myId);
        return commentRepository.searchCommentsByKeyword(searchRequest).map(c -> Response.of(c, me));
    }

    @Transactional
    public Response postComment(Long myId, CommentDto.PostRequest postRequest) {
        User me = userRepository.findMeById(myId);
        Long reviewId = postRequest.getReviewId();
        Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new ReviewNotFoundException(reviewId));
        return Response.of(commentRepository.save(new Comment(me, review, postRequest)), me);
    }

    public Response getComment(Long myId, Long id) {
        User me = userRepository.findMeById(myId);
        Comment comment = commentRepository.findById(id).orElseThrow(() -> new CommentNotFoundException(id));
        return Response.of(comment, me);
    }

    @Transactional
    public Response putComment(Long myId, Long commentId, CommentDto.PutRequest putRequest) {
        User me = userRepository.findMeById(myId);
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new CommentNotFoundException(commentId));
        if (comment.getUser() != me)
            throw new CommentChangeByAnonymousException();
        comment.setContent(putRequest.getContent());
        return Response.of(commentRepository.save(comment), me);
    }

    @Transactional
    public void deleteComment(Long myId, Long commentId) {
        User me = userRepository.findMeById(myId);
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new CommentNotFoundException(commentId));
        if (comment.getUser() != me)
            throw new CommentChangeByAnonymousException();
        commentRepository.delete(comment);
        me.deleteComment(comment);
        comment.getReview().deleteComment(comment);
    }
}
