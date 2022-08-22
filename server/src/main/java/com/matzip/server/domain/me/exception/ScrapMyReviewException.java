package com.matzip.server.domain.me.exception;

import com.matzip.server.global.common.exception.ErrorType;
import com.matzip.server.global.common.exception.InvalidRequestException;

public class ScrapMyReviewException extends InvalidRequestException {
    public ScrapMyReviewException() {
        super(ErrorType.SCRAP_MY_REVIEW, "Cannot scrap my reviews.");
    }
}
