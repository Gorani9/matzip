package com.matzip.server.domain.image.exception;

import com.matzip.server.global.common.exception.ErrorType;
import com.matzip.server.global.common.exception.InvalidRequestException;

public class OverloadReviewImagesException extends InvalidRequestException {
    public OverloadReviewImagesException() {
        super(ErrorType.OVERLOAD_REVIEW_IMAGES, "Images can be uploaded at most 10.");
    }
}
