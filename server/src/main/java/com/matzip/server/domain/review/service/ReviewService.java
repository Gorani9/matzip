package com.matzip.server.domain.review.service;

import com.matzip.server.domain.image.service.ImageService;
import com.matzip.server.domain.review.dto.ReviewDto;
import com.matzip.server.domain.review.exception.ReviewChangeByAnonymousException;
import com.matzip.server.domain.review.exception.ReviewNotFoundException;
import com.matzip.server.domain.review.model.Review;
import com.matzip.server.domain.review.repository.ReviewRepository;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Transactional(readOnly=true)
public class ReviewService {
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final ImageService imageService;

    @Transactional
    public ReviewDto.Response postReview(Long myId, ReviewDto.PostRequest postRequest) {
        User me = userRepository.findMeById(myId);
        Review review = new Review(me, postRequest);
        imageService.uploadReviewImages(me, review, postRequest.getImages());
        return new ReviewDto.Response(me, reviewRepository.save(review));
    }

    public ReviewDto.Response getReview(Long myId, Long reviewId) {
        User me = userRepository.findMeById(myId);
        return new ReviewDto.Response(
                me, reviewRepository.findAllById(reviewId).orElseThrow(() -> new ReviewNotFoundException(reviewId)));
    }

    public Slice<ReviewDto.Response> searchReviews(Long myId, ReviewDto.SearchRequest searchRequest) {
        User me = userRepository.findMeById(myId);
        Slice<Review> reviews = reviewRepository.searchReviewByKeyword(searchRequest);
        return reviews.map(r -> new ReviewDto.Response(me, r));
    }

    @Transactional
    public ReviewDto.Response patchReview(Long myId, Long reviewId, ReviewDto.PatchRequest patchRequest) {
        User me = userRepository.findMeById(myId);
        Review review = reviewRepository.findAllById(reviewId).orElseThrow(() -> new ReviewNotFoundException(reviewId));
        if (review.getUser() != me)
            throw new ReviewChangeByAnonymousException();
        if (Optional.ofNullable(patchRequest.getContent()).isPresent())
            review.setContent(patchRequest.getContent());
        if (Optional.ofNullable(patchRequest.getNewImages()).isPresent())
            imageService.uploadReviewImages(me, review, patchRequest.getNewImages());
        if (Optional.ofNullable(patchRequest.getOldUrls()).isPresent())
            imageService.deleteReviewImages(review, patchRequest.getOldUrls());
        if (Optional.ofNullable(patchRequest.getRating()).isPresent())
            review.setRating(patchRequest.getRating());
        return new ReviewDto.Response(me, reviewRepository.save(review));
    }

    @Transactional
    public void deleteReview(Long myId, Long reviewId) {
        User me = userRepository.findMeById(myId);
        Review review = reviewRepository.findAllById(reviewId).orElseThrow(() -> new ReviewNotFoundException(reviewId));
        if (review.getUser() != me)
            throw new ReviewChangeByAnonymousException();
        reviewRepository.delete(review);
        me.deleteReview(review);
    }

    public ReviewDto.HotResponse getHotReviews(Long myId) {
        User me = userRepository.findMeById(myId);
        List<ReviewDto.Response> dailyHotReviews = reviewRepository
                .fetchHotReviews(LocalDateTime.now().minusDays(1), 10)
                .stream().map(r -> new ReviewDto.Response(me, r)).collect(Collectors.toList());
        List<ReviewDto.Response> weeklyHotReviews = reviewRepository
                .fetchHotReviews(LocalDateTime.now().minusWeeks(1), 10)
                .stream().map(r -> new ReviewDto.Response(me, r)).collect(Collectors.toList());
        List<ReviewDto.Response> monthlyHotReviews = reviewRepository
                .fetchHotReviews(LocalDateTime.now().minusMonths(1), 10)
                .stream().map(r -> new ReviewDto.Response(me, r)).collect(Collectors.toList());
        return new ReviewDto.HotResponse(dailyHotReviews, weeklyHotReviews, monthlyHotReviews);
    }

    public ReviewDto.HallOfFameResponse getHallOfFameReviews(Long myId) {
        User me = userRepository.findMeById(myId);
        return new ReviewDto.HallOfFameResponse(
                reviewRepository.fetchHotReviews(null, 10)
                        .stream().map(r -> new ReviewDto.Response(me, r)).collect(Collectors.toList()));
    }
}
