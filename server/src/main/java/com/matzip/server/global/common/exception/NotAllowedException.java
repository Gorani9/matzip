package com.matzip.server.global.common.exception;

public abstract class NotAllowedException extends MatzipException {
    public NotAllowedException(ErrorType errorType, String detail) {
        super(errorType, detail);
    }
}
