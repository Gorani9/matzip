package com.matzip.server.domain.review.exception;

import com.matzip.server.global.common.exception.ErrorType;
import com.matzip.server.global.common.exception.NotAllowedException;

public class CommentChangeByAnonymousException extends NotAllowedException {
    public CommentChangeByAnonymousException() {
        super(ErrorType.COMMENT_CHANGE_BY_ANONYMOUS, "Only writer can change the status of the comment.");
    }
}
