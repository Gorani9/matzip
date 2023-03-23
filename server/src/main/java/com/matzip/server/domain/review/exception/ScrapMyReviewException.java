package com.matzip.server.domain.review.exception;

import com.matzip.server.global.common.exception.ErrorType;
import com.matzip.server.global.common.exception.MatzipException;

public class ScrapMyReviewException extends MatzipException.InvalidRequestException {
    public ScrapMyReviewException() {
        super(ErrorType.BadRequest.SCRAP_MY_REVIEW, "Cannot scrap my reviews.");
    }
}
