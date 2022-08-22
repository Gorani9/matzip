package com.matzip.server.domain.review.exception;

import com.matzip.server.global.common.exception.DataNotFoundException;
import com.matzip.server.global.common.exception.ErrorType;

public class ReviewNotFoundException extends DataNotFoundException {
    public ReviewNotFoundException(Long id) {
        super(ErrorType.REVIEW_NOT_FOUND, "Review with id '" + id + "' not found.");
    }
}
