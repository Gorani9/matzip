package com.matzip.server.global.common.exception;

public abstract class ConflictException extends MatzipException {
    public ConflictException(ErrorType errorType, String detail) {
        super(errorType, detail);
    }
}
