package com.matzip.server.domain.image.exception;

import com.matzip.server.global.common.exception.ErrorType;
import com.matzip.server.global.common.exception.MatzipException;

public class UnsupportedFileExtensionException extends MatzipException.InvalidRequestException {
    public UnsupportedFileExtensionException() {
        super(ErrorType.BadRequest.UNSUPPORTED_FILE_EXTENSION, "Only image extensions are supported.");
    }
}
