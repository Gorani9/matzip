package com.matzip.server.domain.review.repository;

import com.matzip.server.domain.review.dto.ReviewDto;
import com.matzip.server.domain.review.model.Review;
import org.springframework.data.domain.Slice;

import java.time.LocalDateTime;
import java.util.List;

public interface ReviewRepositoryCustom {
    Slice<Review> searchReviewByKeyword(ReviewDto.SearchRequest searchRequest);

    List<Review> fetchHotReviews(LocalDateTime from, int size);
}
