package com.matzip.server.admin.service;

import com.matzip.server.admin.model.AdminAuthenticationToken;
import com.matzip.server.domain.auth.dto.AuthDto.LoginRequest;
import com.matzip.server.domain.auth.exception.LoginException;
import com.matzip.server.domain.comment.exception.CommentNotFoundException;
import com.matzip.server.domain.comment.model.Comment;
import com.matzip.server.domain.comment.repository.CommentRepository;
import com.matzip.server.domain.image.service.ImageService;
import com.matzip.server.domain.review.exception.ReviewNotFoundException;
import com.matzip.server.domain.review.model.Review;
import com.matzip.server.domain.review.repository.HeartRepository;
import com.matzip.server.domain.review.repository.ReviewRepository;
import com.matzip.server.domain.scrap.repository.ScrapRepository;
import com.matzip.server.domain.user.exception.UserNotFoundException;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

@Service
@RequiredArgsConstructor
@Transactional(readOnly=true)
public class AdminService {
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final CommentRepository commentRepository;
    private final ScrapRepository scrapRepository;
    private final HeartRepository heartRepository;
    private final ImageService imageService;

    @Value("${matzip.admin.username}")
    private String adminUsername;

    @Value("${matzip.admin.password}")
    private String adminPassword;

    @Transactional
    public void deleteUser(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException(username));

        if (user.getUserImage() != null) imageService.deleteImage(user.getUserImage());

        userRepository.delete(user);
    }

    @Transactional
    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new ReviewNotFoundException(reviewId));

        imageService.deleteImages(review.getReviewImages());

        heartRepository.deleteAllByReviewId(reviewId);
        commentRepository.deleteAllByReviewId(reviewId);
        scrapRepository.deleteAllByReviewId(reviewId);
        reviewRepository.delete(review);
    }

    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new CommentNotFoundException(commentId));

        commentRepository.delete(comment);
    }

    public void login(LoginRequest request) {
        String username = request.username();
        String password = request.password();

        if (!username.equals(adminUsername) || !password.equals(adminPassword)) throw new LoginException();

        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new AdminAuthenticationToken());
        RequestContextHolder.currentRequestAttributes()
                .setAttribute("SPRING_SECURITY_CONTEXT", context, RequestAttributes.SCOPE_SESSION);
    }
}
