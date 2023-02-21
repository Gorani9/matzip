package com.matzip.server.domain.scrap.exception;

import com.matzip.server.global.common.exception.ErrorType;
import com.matzip.server.global.common.exception.MatzipException;

public class ScrapNotFoundException extends MatzipException.NotFoundException {
    public ScrapNotFoundException(Long reviewId) {
        super(ErrorType.NotFound.SCRAP_NOT_FOUND,
              String.format("Scrap for Review with id '%d' does not exists.", reviewId));
    }
}
