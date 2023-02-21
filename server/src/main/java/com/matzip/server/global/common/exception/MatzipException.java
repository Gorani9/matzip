package com.matzip.server.global.common.exception;

import lombok.Getter;

@Getter
public abstract class MatzipException extends RuntimeException {
    int errorCode;
    ErrorType errorType;
    String detail;

    public MatzipException(ErrorType errorType, String detail) {
        this.errorCode = errorType.getCode();
        this.errorType = errorType;
        this.detail = detail;
    }

    public abstract static class InvalidRequestException extends MatzipException {
        public InvalidRequestException(ErrorType.BadRequest errorType, String detail) {
            super(errorType, detail);
        }
    }

    public abstract static class UnauthorizedException extends MatzipException {
        public UnauthorizedException(ErrorType.Unauthorized errorType, String detail) {
            super(errorType, detail);
        }
    }

    public abstract static class ForbiddenException extends MatzipException {
        public ForbiddenException(ErrorType.Forbidden errorType, String detail) {
            super(errorType, detail);
        }
    }

    public abstract static class NotFoundException extends MatzipException {
        public NotFoundException(ErrorType.NotFound errorType, String detail) {
            super(errorType, detail);
        }
    }

    public abstract static class ConflictException extends MatzipException {
        public ConflictException(ErrorType.Conflict errorType, String detail) {
            super(errorType, detail);
        }
    }

    public abstract static class ServerErrorException extends MatzipException {
        public ServerErrorException(ErrorType.ServerError errorType, String detail) {
            super(errorType, detail);
        }
    }
}
