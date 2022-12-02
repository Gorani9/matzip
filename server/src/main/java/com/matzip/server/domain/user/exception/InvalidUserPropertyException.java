package com.matzip.server.domain.user.exception;

import com.matzip.server.global.common.exception.ErrorType;
import com.matzip.server.global.common.exception.InvalidRequestException;

public class InvalidUserPropertyException extends InvalidRequestException {
    public InvalidUserPropertyException(String property) {
        super(ErrorType.INVALID_USER_PROPERTY, String.format("This property '%s' is not allowed to be used in sort.", property));
    }
}
