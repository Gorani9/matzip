package com.matzip.server.domain.review.repository;

import com.matzip.server.domain.review.dto.ReviewDto;
import com.matzip.server.domain.review.model.Review;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface ReviewRepositoryCustom {
    Slice<Review> searchReviewsByKeyword(ReviewDto.SearchRequest searchRequest);
    List<Review> fetchHotReviews();
}
