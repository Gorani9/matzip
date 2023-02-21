package com.matzip.server.domain.review.exception;

import com.matzip.server.global.common.exception.ErrorType;
import com.matzip.server.global.common.exception.MatzipException;

public class DuplicateHeartException extends MatzipException.ConflictException {
    public DuplicateHeartException() {
        super(ErrorType.Conflict.DUPLICATE_HEART, "You already hearted this review");
    }
}
