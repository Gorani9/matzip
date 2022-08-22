package com.matzip.server.domain.review.service;

import com.matzip.server.domain.review.dto.CommentDto;
import com.matzip.server.domain.review.exception.CommentChangeByAnonymousException;
import com.matzip.server.domain.review.exception.CommentNotFoundException;
import com.matzip.server.domain.review.exception.ReviewNotFoundException;
import com.matzip.server.domain.review.model.Comment;
import com.matzip.server.domain.review.model.Review;
import com.matzip.server.domain.review.repository.CommentRepository;
import com.matzip.server.domain.review.repository.ReviewRepository;
import com.matzip.server.domain.user.exception.UsernameNotFoundException;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@RequiredArgsConstructor
@Service
@Transactional(readOnly=true)
public class CommentService {
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final CommentRepository commentRepository;

    public Page<CommentDto.Response> searchComment(String username, CommentDto.SearchRequest searchRequest) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
        Sort sort = searchRequest.getAscending() ? Sort.by(searchRequest.getSortedBy()).ascending()
                                                 : Sort.by(searchRequest.getSortedBy()).descending();
        Pageable pageable = PageRequest.of(searchRequest.getPageNumber(), searchRequest.getPageSize(), sort);
        return commentRepository.findAllByContentContaining(pageable, searchRequest.getKeyword())
                .map(c -> new CommentDto.Response(user, c));
    }

    @Transactional
    public CommentDto.Response postComment(String username, CommentDto.PostRequest postRequest) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
        Long reviewId = postRequest.getReviewId();
        Review review = reviewRepository.findAllById(reviewId).orElseThrow(() -> new ReviewNotFoundException(reviewId));
        return new CommentDto.Response(user, commentRepository.save(new Comment(user, review, postRequest)));
    }

    public CommentDto.Response getComment(String username, Long id) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
        Comment comment = commentRepository.findById(id).orElseThrow(() -> new CommentNotFoundException(id));
        return new CommentDto.Response(user, comment);
    }

    @Transactional
    public CommentDto.Response putComment(String username, Long id, CommentDto.PutRequest putRequest) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));

        Comment comment = commentRepository.findById(id).orElseThrow(() -> new CommentNotFoundException(id));
        if (!Objects.equals(comment.getUser().getId(), user.getId()) && !user.getRole().equals("ADMIN"))
            throw new CommentChangeByAnonymousException();
        comment.setContent(putRequest.getContent());
        return new CommentDto.Response(user, commentRepository.save(comment));
    }

    @Transactional
    public void deleteComment(User user, Long id) {
        Comment comment = commentRepository.findById(id).orElseThrow(() -> new CommentNotFoundException(id));
        if (!Objects.equals(comment.getUser().getId(), user.getId()) && !user.getRole().equals("ADMIN"))
            throw new CommentChangeByAnonymousException();
        commentRepository.delete(comment);
        commentRepository.flush();
    }
}
