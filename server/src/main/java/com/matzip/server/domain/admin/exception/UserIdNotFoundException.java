package com.matzip.server.domain.admin.exception;

import com.matzip.server.domain.user.exception.UserNotFoundException;

public class UserIdNotFoundException extends UserNotFoundException {
    public UserIdNotFoundException(Long id) {
        super("id " + id);
    }
}
