package com.matzip.server.domain.user.exception;

import com.matzip.server.global.common.exception.ErrorType;
import com.matzip.server.global.common.exception.MatzipException;

public class FollowMeException extends MatzipException.InvalidRequestException {
    public FollowMeException() {
        super(ErrorType.BadRequest.FOLLOW_ME, "Following myself is not allowed.");
    }
}
