package com.matzip.server.global.common.exception;

public abstract class DataNotFoundException extends MatzipException {
    public DataNotFoundException(ErrorType errorType, String detail) {
        super(errorType, detail);
    }
}
