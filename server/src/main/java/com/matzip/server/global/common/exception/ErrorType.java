package com.matzip.server.global.common.exception;

public interface ErrorType {
    int getCode();

    enum BadRequest implements ErrorType {
        BAD_REQUEST_DEFAULT(0),
        INVALID_REQUEST_BODY(1),
        INVALID_PARAMETER(2),
        FILE_SIZE_LIMIT_EXCEEDED(3),
        INVALID_JWT_TOKEN(4),

        FOLLOW_ME(102),
        HEART_MY_REVIEW(103),
        SCRAP_MY_REVIEW(104),
        DELETE_REVIEW_LAST_IMAGE(109),
        UNSUPPORTED_FILE_EXTENSION(901),
        ;

        private final int errorCode;

        BadRequest(int errorCode) {
            this.errorCode = errorCode;
        }

        @Override
        public int getCode() {
            return this.errorCode;
        }
    }

    enum Unauthorized implements ErrorType {
        UNAUTHORIZED_DEFAULT(1000),
        LOGIN_FAILED(1001),
        ;

        private final int errorCode;

        Unauthorized(int errorCode) {
            this.errorCode = errorCode;
        }

        @Override
        public int getCode() {
            return this.errorCode;
        }
    }

    enum Forbidden implements ErrorType {
        FORBIDDEN_DEFAULT(3000),
        REVIEW_ACCESS_DENIED(3200),
        COMMENT_ACCESS_DENIED(3300),
        ;

        private final int errorCode;

        Forbidden(int errorCode) {
            this.errorCode = errorCode;
        }

        @Override
        public int getCode() {
            return this.errorCode;
        }
    }

    enum NotFound implements ErrorType {
        NOT_FOUND_DEFAULT(4000),
        USER_NOT_FOUND(4100),
        REVIEW_NOT_FOUND(4200),
        REVIEW_IMAGE_URL_NOT_FOUND(4201),
        COMMENT_NOT_FOUND(4300),
        ;

        private final int errorCode;

        NotFound(int errorCode) {
            this.errorCode = errorCode;
        }

        @Override
        public int getCode() {
            return this.errorCode;
        }
    }

    enum Conflict implements ErrorType {
        CONFLICT_DEFAULT(9000),
        USER_CONFLICT(9100),
        DUPLICATE_HEART(9200),
        ;

        private final int errorCode;

        Conflict(int errorCode) {
            this.errorCode = errorCode;
        }

        @Override
        public int getCode() {
            return this.errorCode;
        }
    }

    enum ServerError implements ErrorType {
        SERVER_ERROR_DEFAULT(10000),
        FILE_UPLOAD_FAIL(10100),
        FILE_DELETE_FAIL(10101),
        ;

        private final int errorCode;

        ServerError(int errorCode) {
            this.errorCode = errorCode;
        }

        @Override
        public int getCode() {
            return this.errorCode;
        }
    }
}
