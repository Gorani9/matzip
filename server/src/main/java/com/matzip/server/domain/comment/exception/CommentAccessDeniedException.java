package com.matzip.server.domain.comment.exception;

import com.matzip.server.global.common.exception.ErrorType;
import com.matzip.server.global.common.exception.MatzipException;

public class CommentAccessDeniedException extends MatzipException.ForbiddenException {
    public CommentAccessDeniedException() {
        super(ErrorType.Forbidden.COMMENT_ACCESS_DENIED, "Only writer can change the status of the comment.");
    }
}
