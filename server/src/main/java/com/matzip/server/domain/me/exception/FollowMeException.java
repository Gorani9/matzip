package com.matzip.server.domain.me.exception;

import com.matzip.server.global.common.exception.ErrorType;
import com.matzip.server.global.common.exception.InvalidRequestException;

public class FollowMeException extends InvalidRequestException {
    public FollowMeException() {
        super(ErrorType.FOLLOW_ME, "Following myself is not allowed.");
    }
}
