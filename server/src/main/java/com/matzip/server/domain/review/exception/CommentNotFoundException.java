package com.matzip.server.domain.review.exception;

import com.matzip.server.global.common.exception.DataNotFoundException;
import com.matzip.server.global.common.exception.ErrorType;

public class CommentNotFoundException extends DataNotFoundException {
    public CommentNotFoundException(Long id) {
        super(ErrorType.COMMENT_NOT_FOUND, "Comment with id '" + id + "' not found.");
    }
}
