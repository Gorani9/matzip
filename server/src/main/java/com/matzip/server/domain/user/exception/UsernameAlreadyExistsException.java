package com.matzip.server.domain.user.exception;

import com.matzip.server.global.common.exception.ConflictException;
import com.matzip.server.global.common.exception.ErrorType;

public class UsernameAlreadyExistsException extends ConflictException {
    public UsernameAlreadyExistsException(String username) {
        super(ErrorType.USER_CONFLICT, String.format("Username '%s' already exists.", username));
    }
}
