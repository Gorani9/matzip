package com.matzip.server.global.common.exception;

public abstract class InvalidRequestException extends MatzipException {
    public InvalidRequestException(ErrorType errorType, String detail) {
        super(errorType, detail);
    }
}
