package com.matzip.server.domain.comment.exception;

import com.matzip.server.global.common.exception.ErrorType;
import com.matzip.server.global.common.exception.MatzipException;

public class CommentNotFoundException extends MatzipException.NotFoundException {
    public CommentNotFoundException(Long id) {
        super(ErrorType.NotFound.COMMENT_NOT_FOUND, "Comment with id '" + id + "' not found.");
    }
}
