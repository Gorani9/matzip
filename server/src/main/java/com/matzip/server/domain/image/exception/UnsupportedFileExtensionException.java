package com.matzip.server.domain.image.exception;

import com.matzip.server.global.common.exception.ErrorType;
import com.matzip.server.global.common.exception.InvalidRequestException;

public class UnsupportedFileExtensionException extends InvalidRequestException {
    public UnsupportedFileExtensionException() {
        super(ErrorType.UNSUPPORTED_FILE_EXTENSION, "Only image extensions are supported.");
    }
}
