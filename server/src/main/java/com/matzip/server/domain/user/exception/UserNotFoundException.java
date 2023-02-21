package com.matzip.server.domain.user.exception;

import com.matzip.server.global.common.exception.MatzipException;

import static com.matzip.server.global.common.exception.ErrorType.NotFound.USER_NOT_FOUND;

public class UserNotFoundException extends MatzipException.NotFoundException {
    public UserNotFoundException(String username) {
        super(USER_NOT_FOUND, String.format("User with username '%s' not found.", username));
    }
}
