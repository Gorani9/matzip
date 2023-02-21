package com.matzip.server.domain.scrap.exception;

import com.matzip.server.global.common.exception.ErrorType;
import com.matzip.server.global.common.exception.MatzipException;

public class DuplicateScrapException extends MatzipException.ConflictException {
    public DuplicateScrapException() {
        super(ErrorType.Conflict.DUPLICATE_SCRAP, "You already scrapped this review.");
    }
}
