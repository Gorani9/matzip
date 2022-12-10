package com.matzip.server.domain.image.exception;

import com.matzip.server.global.common.exception.ErrorType;
import com.matzip.server.global.common.exception.InvalidRequestException;

public class DeleteReviewLastImageException extends InvalidRequestException {
    public DeleteReviewLastImageException() {
        super(ErrorType.DELETE_REVIEW_LAST_IMAGE, "A review must contain at least 1 image");
    }
}
