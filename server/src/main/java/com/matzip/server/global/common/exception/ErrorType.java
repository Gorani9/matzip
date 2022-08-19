package com.matzip.server.global.common.exception;

public enum ErrorType {
    INVALID_REQUEST(0),

    USER_LOCKED(100),
    LOCK_ADMIN_USER(101),
    DELETE_ADMIN_USER(102),
    FOLLOW_ME(103),

    FILE_TOO_LARGE(900),
    UNSUPPORTED_FILE_EXTENSION(901),


    NOT_ALLOWED(3000),

    USER_ACCESS_DENIED(3100),
    ADMIN_USER_ACCESS_BY_NORMAL_USER(3101),


    DATA_NOT_FOUND(4000),

    USER_NOT_FOUND(4100),


    CONFLICT(9000),

    USER_CONFLICT(9100),


    SERVER_ERROR(10000),

    FILE_UPLOAD_FAIL(10100),
    FILE_DELETE_FAIL(10101),
    ;

    private final int errorCode;

    ErrorType(int errorCode) {
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
