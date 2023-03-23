package com.matzip.server.domain.review.service;

import com.matzip.server.domain.comment.repository.CommentRepository;
import com.matzip.server.domain.image.service.ImageService;
import com.matzip.server.domain.record.service.RecordService;
import com.matzip.server.domain.review.dto.ReviewDto.*;
import com.matzip.server.domain.review.exception.*;
import com.matzip.server.domain.review.model.Heart;
import com.matzip.server.domain.review.model.Review;
import com.matzip.server.domain.review.model.Scrap;
import com.matzip.server.domain.review.repository.HeartRepository;
import com.matzip.server.domain.review.repository.ReviewRepository;
import com.matzip.server.domain.review.repository.ScrapRepository;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.domain.user.repository.UserRepository;
import com.matzip.server.global.common.dto.ListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly=true)
public class ReviewService {
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final CommentRepository commentRepository;
    private final ScrapRepository scrapRepository;
    private final HeartRepository heartRepository;
    private final ImageService imageService;
    private final RecordService recordService;

    @Transactional
    public Response postReview(Long myId, PostRequest request) {
        User me = userRepository.findMeById(myId);
        Review review = reviewRepository.save(new Review(me, request));

        review.getReviewImages().addAll(imageService.uploadImages(me.getUsername(), request.images()));

        recordService.postReview(me);

        return new Response(review, me);
    }

    @Transactional
    public Response fetchReview(Long myId, Long reviewId) {
        User me = userRepository.findMeById(myId);
        Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new ReviewNotFoundException(reviewId));

        recordService.view(review, me);

        return new Response(review, me);
    }

    public Slice<Response> searchReviews(Long myId, SearchRequest request) {
        User me = userRepository.findMeById(myId);
        Slice<Review> reviews = reviewRepository.searchReviewsByKeyword(request);
        return reviews.map(r -> new Response(r, me));
    }

    @Transactional
    public Response patchReview(Long myId, Long reviewId, PatchRequest request) {
        User me = userRepository.findMeById(myId);
        Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new ReviewNotFoundException(reviewId));

        if (review.getUser() != me) throw new ReviewAccessDeniedException();

        if (request.oldUrls() != null) {
            for (String oldUrl : request.oldUrls())
                if (!review.getReviewImages().contains(oldUrl)) throw new ReviewImageUrlNotFound(oldUrl);
        }

        int imageCount = review.getReviewImages().size();
        imageCount += request.images() == null ? 0 : request.images().size();
        imageCount -= request.oldUrls() == null ? 0 : request.oldUrls().size();
        if (imageCount < 1) throw new DeleteLastImageException();

        if (request.images() != null)
            review.getReviewImages().addAll(imageService.uploadImages(me.getUsername(), request.images()));
        if (request.oldUrls() != null)
            review.getReviewImages().removeAll(imageService.deleteImages(request.oldUrls()));
        if (request.content() != null) review.setContent(request.content());
        if (request.rating() != null) review.setRating(request.rating());

        if (request.images() != null || request.oldUrls() != null || request.content() != null || request.rating() != null)
            review.update();

        return new Response(review, me);
    }

    @Transactional
    public void deleteReview(Long myId, Long reviewId) {
        User me = userRepository.findMeById(myId);
        Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new ReviewNotFoundException(reviewId));

        if (review.getUser() != me) throw new ReviewAccessDeniedException();

        imageService.deleteImages(review.getReviewImages());

        heartRepository.deleteAllByReviewId(reviewId);
        commentRepository.deleteAllByReviewId(reviewId);
        scrapRepository.deleteAllByReviewId(reviewId);

        review.delete();
        reviewRepository.delete(review);
        recordService.deleteReview(me);
    }

    public ListResponse<Response> getHotReviews(Long myId) {
        User me = userRepository.findMeById(myId);

        return new ListResponse<>(reviewRepository.fetchHotReviews().stream().map(r -> new Response(r, me)));
    }

    @Transactional
    public Response heartReview(Long myId, Long reviewId) {
        User me = userRepository.findMeById(myId);
        Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new ReviewNotFoundException(reviewId));

        if (review.getUser() == me) throw new HeartMyReviewException();
        if (review.getHearts().stream().anyMatch(h -> h.getUser().equals(me))) throw new DuplicateHeartException();

        heartRepository.save(new Heart(me, review));

        recordService.likeReview(review, me);

        return new Response(review, me);
    }

    @Transactional
    public Response deleteHeartFromReview(Long myId, Long reviewId) {
        User me = userRepository.findMeById(myId);
        Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new ReviewNotFoundException(reviewId));

        heartRepository.findByUserIdAndReviewId(myId, reviewId).ifPresent(
                h -> {
                    h.delete();
                    heartRepository.delete(h);
                    recordService.deleteLike(review, me);
                }
        );

        return new Response(review, me);
    }

    @Transactional
    public Response putScrap(Long myId, Long reviewId, ScrapRequest request) {
        User me = userRepository.findMeById(myId);
        Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new ReviewNotFoundException(reviewId));

        if (review.getUser() == me) throw new ScrapMyReviewException();

        Optional<Scrap> scrapOptional = review.getScraps().stream().filter(s -> s.getUser() == me).findAny();

        if (scrapOptional.isPresent()) {
            Scrap scrap = scrapOptional.get();
            scrap.setDescription(request.description());
            scrap.update();
            return new Response(scrapRepository.save(scrap));
        } else {
            Scrap scrap = scrapRepository.save(new Scrap(me, review, request.description()));
            recordService.scrapReview(review, me);
            return new Response(scrap);
        }
    }

    @Transactional
    public Response deleteScrap(Long myId, Long reviewId) {
        User me = userRepository.findMeById(myId);
        Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new ReviewNotFoundException(reviewId));

        scrapRepository.findByUserIdAndReviewId(myId, reviewId).ifPresent(
                s -> {
                    s.delete();
                    scrapRepository.delete(s);
                    recordService.deleteScrap(review, me);
                }
        );

        return new Response(review, me);
    }
}
