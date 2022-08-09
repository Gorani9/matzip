package com.matzip.server.domain.admin.exception;

import com.matzip.server.global.common.exception.ErrorType;
import com.matzip.server.global.common.exception.InvalidRequestException;

public class DeactivateAdminUserException extends InvalidRequestException {
    public DeactivateAdminUserException() {
        super(ErrorType.DEACTIVATE_ADMIN_USER, "Admin user cannot be deactivated");
    }
}
