package com.matzip.server.domain.admin.exception;

import com.matzip.server.global.common.exception.ErrorType;
import com.matzip.server.global.common.exception.InvalidRequestException;

public class UserAlreadyInactiveException extends InvalidRequestException {
    public UserAlreadyInactiveException(Long id) {
        super(ErrorType.USER_ALREADY_INACTIVE, String.format("User with id '%d' is already inactive.", id));
    }
}
