package com.matzip.server.global.common.exception;

abstract class MatzipException extends RuntimeException {
    ErrorType errorType;
    String detail;

    public MatzipException(ErrorType errorType, String detail) {
        this.errorType = errorType;
        this.detail = detail;
    }
}
