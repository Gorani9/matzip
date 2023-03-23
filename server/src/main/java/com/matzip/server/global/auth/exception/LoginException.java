package com.matzip.server.global.auth.exception;

import com.matzip.server.global.common.exception.ErrorType;
import com.matzip.server.global.common.exception.MatzipException;

public class LoginException extends MatzipException.UnauthorizedException {
    public LoginException() {
        super(ErrorType.Unauthorized.LOGIN_FAILED, "User does not exist or invalid password.");
    }
}
