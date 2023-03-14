package com.matzip.server.domain.auth.exception;

import com.matzip.server.global.common.exception.ErrorType;
import com.matzip.server.global.common.exception.MatzipException;

public class UsernameAlreadyExistsException extends MatzipException.ConflictException {
    public UsernameAlreadyExistsException(String username) {
        super(ErrorType.Conflict.USER_CONFLICT, String.format("Username '%s' already exists.", username));
    }
}
