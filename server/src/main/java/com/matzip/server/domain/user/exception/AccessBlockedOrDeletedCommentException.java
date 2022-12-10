package com.matzip.server.domain.user.exception;

import com.matzip.server.global.common.exception.ErrorType;
import com.matzip.server.global.common.exception.NotAllowedException;

public class AccessBlockedOrDeletedCommentException extends NotAllowedException {
    public AccessBlockedOrDeletedCommentException(Long commentId) {
        super(ErrorType.COMMENT_BLOCKED_OR_DELETED, String.format("Review with id '%d' is blocked or deleted.", commentId));
    }
}
