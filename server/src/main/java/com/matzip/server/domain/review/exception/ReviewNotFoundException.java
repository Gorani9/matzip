package com.matzip.server.domain.review.exception;

import com.matzip.server.global.common.exception.ErrorType;
import com.matzip.server.global.common.exception.MatzipException;

public class ReviewNotFoundException extends MatzipException.NotFoundException {
    public ReviewNotFoundException(Long id) {
        super(ErrorType.NotFound.REVIEW_NOT_FOUND, "Review with id '" + id + "' not found.");
    }
}
