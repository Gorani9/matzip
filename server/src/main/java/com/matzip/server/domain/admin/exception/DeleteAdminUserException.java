package com.matzip.server.domain.admin.exception;

import com.matzip.server.global.common.exception.ErrorType;
import com.matzip.server.global.common.exception.InvalidRequestException;

public class DeleteAdminUserException extends InvalidRequestException {
    public DeleteAdminUserException() {
        super(ErrorType.DELETE_ADMIN_USER, "Admin user cannot be deleted");
    }
}
