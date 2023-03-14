package com.matzip.server.domain.review.exception;

import com.matzip.server.global.common.exception.ErrorType;
import com.matzip.server.global.common.exception.MatzipException;

public class HeartMyReviewException extends MatzipException.InvalidRequestException {
    public HeartMyReviewException() {
        super(ErrorType.BadRequest.HEART_MY_REVIEW, "Cannot put heart on my reviews.");
    }
}
