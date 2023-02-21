package com.matzip.server.domain.review.exception;

import com.matzip.server.global.common.exception.ErrorType;
import com.matzip.server.global.common.exception.MatzipException;

public class ReviewImageUrlNotFound extends MatzipException.NotFoundException {
    public ReviewImageUrlNotFound(String url) {
        super(ErrorType.NotFound.REVIEW_IMAGE_URL_NOT_FOUND,
              String.format("Url '%s' is not found in this review images", url));
    }
}
