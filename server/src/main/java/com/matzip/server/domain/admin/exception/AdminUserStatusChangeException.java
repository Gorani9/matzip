package com.matzip.server.domain.admin.exception;

import com.matzip.server.global.common.exception.ErrorType;
import com.matzip.server.global.common.exception.InvalidRequestException;

public class AdminUserStatusChangeException extends InvalidRequestException {
    public AdminUserStatusChangeException() {
        super(ErrorType.CHANGE_ADMIN_USER_STATUS, "Admin user status cannot be changed here.");
    }
}
