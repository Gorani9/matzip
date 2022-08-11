package com.matzip.server.domain.admin.exception;

import com.matzip.server.global.common.exception.ErrorType;
import com.matzip.server.global.common.exception.InvalidRequestException;

public class LockAdminUserException extends InvalidRequestException {
    public LockAdminUserException() {
        super(ErrorType.LOCK_ADMIN_USER, "Admin user cannot be deactivated");
    }
}
