package com.matzip.server.domain.review.exception;

import com.matzip.server.global.common.exception.ErrorType;
import com.matzip.server.global.common.exception.MatzipException;

public class ReviewAccessDeniedException extends MatzipException.ForbiddenException {
    public ReviewAccessDeniedException() {
        super(ErrorType.Forbidden.REVIEW_ACCESS_DENIED, "Only writer can change the status of the review.");
    }
}
