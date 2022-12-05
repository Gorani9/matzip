package com.matzip.server.domain.review.exception;

import com.matzip.server.global.common.exception.ErrorType;
import com.matzip.server.global.common.exception.InvalidRequestException;

public class InvalidCommentPropertyException extends InvalidRequestException {
    public InvalidCommentPropertyException(String property) {
        super(ErrorType.INVALID_COMMENT_PROPERTY, String.format("This property '%s' is not allowed to be used in sort.", property));
    }
}
