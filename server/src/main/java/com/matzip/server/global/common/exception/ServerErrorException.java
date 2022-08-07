package com.matzip.server.global.common.exception;

abstract class ServerErrorException extends MatzipException {
    public ServerErrorException(ErrorType errorType, String detail) {
        super(errorType, detail);
    }
}
