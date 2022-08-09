package com.matzip.server.domain.admin.exception;

import com.matzip.server.global.common.exception.ErrorType;
import com.matzip.server.global.common.exception.InvalidRequestException;

public class UserAlreadyActiveException extends InvalidRequestException {
    public UserAlreadyActiveException(Long id) {
        super(ErrorType.USER_ALREADY_ACTIVE, String.format("User with id '%d' is already active.", id));
    }
}
