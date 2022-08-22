package com.matzip.server.domain.me.exception;

import com.matzip.server.global.common.exception.ErrorType;
import com.matzip.server.global.common.exception.InvalidRequestException;

public class HeartMyReviewException extends InvalidRequestException {
    public HeartMyReviewException() {
        super(ErrorType.HEART_MY_REVIEW, "Cannot put heart on my reviews.");
    }
}
