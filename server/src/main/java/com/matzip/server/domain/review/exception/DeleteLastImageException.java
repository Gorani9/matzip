package com.matzip.server.domain.review.exception;

import com.matzip.server.global.common.exception.ErrorType;
import com.matzip.server.global.common.exception.MatzipException;

public class DeleteLastImageException extends MatzipException.InvalidRequestException {
    public DeleteLastImageException() {
        super(ErrorType.BadRequest.DELETE_REVIEW_LAST_IMAGE, "A review must have at least 1 image.");
    }
}
