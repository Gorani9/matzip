package com.matzip.server.domain.user.exception;

import com.matzip.server.global.common.exception.ErrorType;
import com.matzip.server.global.common.exception.InvalidRequestException;

public class UserNotActiveException extends InvalidRequestException {
    public UserNotActiveException(String username) {
        super(ErrorType.USER_NOT_ACTIVE, String.format("Username '%s' is not active.", username));
    }
}
