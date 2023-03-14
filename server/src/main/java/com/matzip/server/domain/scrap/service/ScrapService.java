package com.matzip.server.domain.scrap.service;

import com.matzip.server.domain.review.exception.ReviewNotFoundException;
import com.matzip.server.domain.review.model.Review;
import com.matzip.server.domain.review.repository.ReviewRepository;
import com.matzip.server.domain.scrap.dto.ScrapDto;
import com.matzip.server.domain.scrap.dto.ScrapDto.Response;
import com.matzip.server.domain.scrap.exception.DuplicateScrapException;
import com.matzip.server.domain.scrap.exception.ScrapMyReviewException;
import com.matzip.server.domain.scrap.exception.ScrapNotFoundException;
import com.matzip.server.domain.scrap.model.Scrap;
import com.matzip.server.domain.scrap.repository.ScrapRepository;
import com.matzip.server.domain.user.model.User;
import com.matzip.server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScrapService {
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final ScrapRepository scrapRepository;

    @Transactional
    public Response postScrap(Long myId, ScrapDto.PostRequest postRequest) {
        User me = userRepository.findMeById(myId);
        Long reviewId = postRequest.getReviewId();
        Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new ReviewNotFoundException(reviewId));

        if (review.getUser() == me) throw new ScrapMyReviewException();

        if (scrapRepository.existsByUserIdAndReviewId(myId, reviewId)) throw new DuplicateScrapException();

        Scrap scrap = scrapRepository.save(new Scrap(me, review, postRequest.getDescription()));

        return new Response(scrap);
    }

    @Transactional
    public Response patchScrap(Long myId, Long reviewId, ScrapDto.PatchRequest request) {
        Scrap scrap = scrapRepository.findByUserIdAndReviewId(myId, reviewId)
                .orElseThrow(() -> new ScrapNotFoundException(reviewId));

        if (request.description() != null) scrap.setDescription(request.description());

        return new Response(scrap);
    }

    @Transactional
    public void deleteScrap(Long myId, Long reviewId) {
        Scrap scrap = scrapRepository.findByUserIdAndReviewId(myId, reviewId)
                .orElseThrow(() -> new ScrapNotFoundException(reviewId));

        scrap.delete();
        scrapRepository.delete(scrap);
    }
}
