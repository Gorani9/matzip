package com.matzip.server.domain.user.exception;

import com.matzip.server.global.common.exception.DataNotFoundException;
import com.matzip.server.global.common.exception.ErrorType;

public class UserNotFoundException extends DataNotFoundException {
    public UserNotFoundException(String identifier) {
        super(ErrorType.USER_NOT_FOUND, String.format("User with %s not found.", identifier));
    }
}
