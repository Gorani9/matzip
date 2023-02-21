package com.matzip.server.global.auth;

import com.matzip.server.global.common.exception.ErrorType;
import com.matzip.server.global.common.exception.MatzipException;

public class UnauthorizedException extends MatzipException.UnauthorizedException {
    public UnauthorizedException() {
        super(ErrorType.Unauthorized.UNAUTHORIZED_DEFAULT, "You have to login first.");
    }
}
