package com.matzip.server.domain.me.exception;

import com.matzip.server.global.common.exception.ErrorType;
import com.matzip.server.global.common.exception.InvalidRequestException;

public class DuplicateHeartException extends InvalidRequestException {
    public DuplicateHeartException() {
        super(ErrorType.DUPLICATE_HEART, "Duplicate Heart is not allowed.");
    }
}
