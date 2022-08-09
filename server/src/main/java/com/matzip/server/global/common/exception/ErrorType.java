package com.matzip.server.global.common.exception;

public enum ErrorType {
    INVALID_REQUEST(0),

    USER_NOT_ACTIVE(100),
    USER_ALREADY_ACTIVE(101),
    USER_ALREADY_INACTIVE(102),
    DELETE_ACTIVE_USER(103),
    DEACTIVATE_ADMIN_USER(107),


    NOT_ALLOWED(3000),

    USER_ACCESS_DENIED(3100),


    DATA_NOT_FOUND(4000),

    USER_NOT_FOUND(4100),


    CONFLICT(9000),

    USER_CONFLICT(9100),


    SERVER_ERROR(10000),
    ;

    private final int errorCode;
    ErrorType(int errorCode) {
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
