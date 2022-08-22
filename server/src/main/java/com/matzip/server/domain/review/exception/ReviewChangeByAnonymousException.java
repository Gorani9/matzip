package com.matzip.server.domain.review.exception;

import com.matzip.server.global.common.exception.ErrorType;
import com.matzip.server.global.common.exception.NotAllowedException;

public class ReviewChangeByAnonymousException extends NotAllowedException {
    public ReviewChangeByAnonymousException() {
        super(ErrorType.REVIEW_CHANGE_BY_ANONYMOUS, "Only writer can change the status of the review.");
    }
}
