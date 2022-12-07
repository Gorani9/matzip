package com.matzip.server.domain.user.exception;

import com.matzip.server.global.common.exception.ErrorType;
import com.matzip.server.global.common.exception.NotAllowedException;

public class AccessBlockedUserException extends NotAllowedException {
    public AccessBlockedUserException(String username) {
        super(ErrorType.USER_BLOCKED, String.format("User '%s' is blocked.", username));
    }
}
