package com.matzip.server.global.auth.exception;

import com.matzip.server.global.common.exception.ErrorType;
import com.matzip.server.global.common.exception.MatzipException;

public class InvalidJwtException extends MatzipException.InvalidRequestException {
    public InvalidJwtException() {
        super(ErrorType.BadRequest.INVALID_JWT_TOKEN, "Invalid JWT Token");
    }
}
