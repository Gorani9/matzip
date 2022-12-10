package com.matzip.server.domain.review.exception;

import com.matzip.server.global.common.exception.ErrorType;
import com.matzip.server.global.common.exception.NotAllowedException;

public class AccessBlockedOrDeletedReviewException extends NotAllowedException {
    public AccessBlockedOrDeletedReviewException(Long reviewId) {
        super(ErrorType.REVIEW_BLOCKED_OR_DELETED, String.format("Review with id '%d' is blocked or deleted.", reviewId));
    }
}
