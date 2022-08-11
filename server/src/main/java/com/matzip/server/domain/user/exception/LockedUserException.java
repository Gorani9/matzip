package com.matzip.server.domain.user.exception;

import com.matzip.server.global.common.exception.ErrorType;
import com.matzip.server.global.common.exception.InvalidRequestException;

public class LockedUserException extends InvalidRequestException {
    public LockedUserException(String username) {
        super(ErrorType.USER_LOCKED, String.format("Username '%s' is locked.", username));
    }
}
