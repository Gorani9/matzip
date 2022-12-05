package com.matzip.server.domain.review.service;

import com.matzip.server.domain.review.dto.CommentDto;
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

    public Slice<CommentDto.Response> searchComment(Long myId, CommentDto.SearchRequest searchRequest) {
        User me = userRepository.findMeById(myId);
        return commentRepository.searchCommentByKeyword(searchRequest).map(c -> new CommentDto.Response(me, c));
    }

    @Transactional
    public CommentDto.Response postComment(Long myId, CommentDto.PostRequest postRequest) {
        User me = userRepository.findMeById(myId);
        Long reviewId = postRequest.getReviewId();
        Review review = reviewRepository.findAllById(reviewId).orElseThrow(() -> new ReviewNotFoundException(reviewId));
        return new CommentDto.Response(me, commentRepository.save(new Comment(me, review, postRequest)));
    }

    public CommentDto.Response getComment(Long myId, Long id) {
        User me = userRepository.findMeById(myId);
        Comment comment = commentRepository.findById(id).orElseThrow(() -> new CommentNotFoundException(id));
        return new CommentDto.Response(me, comment);
    }

    @Transactional
    public CommentDto.Response putComment(Long myId, Long commentId, CommentDto.PutRequest putRequest) {
        User me = userRepository.findMeById(myId);
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new CommentNotFoundException(commentId));
        if (comment.getUser() != me)
            throw new CommentChangeByAnonymousException();
        comment.setContent(putRequest.getContent());
        return new CommentDto.Response(me, commentRepository.save(comment));
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
