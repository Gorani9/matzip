package com.matzip.server.global.auth.exception;

import com.matzip.server.global.common.exception.ErrorType;
import com.matzip.server.global.common.exception.MatzipException;

public class MatzipAccessDeniedException extends MatzipException.ForbiddenException {
    public MatzipAccessDeniedException() {
        super(ErrorType.Forbidden.FORBIDDEN_DEFAULT, "You are not allowed to access this resource.");
    }
}
