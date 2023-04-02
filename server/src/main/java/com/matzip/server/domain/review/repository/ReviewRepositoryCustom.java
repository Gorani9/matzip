package com.matzip.server.domain.review.repository;

import com.matzip.server.domain.review.model.Review;
import com.matzip.server.domain.search.dto.SearchDto.ReviewSearch;
import org.springframework.data.domain.Slice;

public interface ReviewRepositoryCustom {
    Slice<Review> searchReviewsByKeyword(ReviewSearch searchRequest);
    Slice<Review> searchReviewsByKeywordUsingFullText(ReviewSearch searchRequest);
}
