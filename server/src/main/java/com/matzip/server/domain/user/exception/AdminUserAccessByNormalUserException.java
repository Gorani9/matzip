package com.matzip.server.domain.user.exception;

import com.matzip.server.global.common.exception.ErrorType;
import com.matzip.server.global.common.exception.NotAllowedException;

public class AdminUserAccessByNormalUserException extends NotAllowedException {
    public AdminUserAccessByNormalUserException() {
        super(ErrorType.ADMIN_USER_ACCESS_BY_NORMAL_USER, "Admin user information is not disclosed by normal user.");
    }
}
