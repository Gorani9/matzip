package com.matzip.server.domain.user.exception;

import com.matzip.server.global.common.exception.ErrorType;
import com.matzip.server.global.common.exception.NotAllowedException;

public class AccessBlockedOrDeletedUserException extends NotAllowedException {
    public AccessBlockedOrDeletedUserException(String username) {
        super(ErrorType.USER_BLOCKED_OR_DELETED, String.format("User '%s' is blocked or deleted.", username));
    }
}
