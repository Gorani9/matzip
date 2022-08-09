package com.matzip.server.domain.admin.exception;

import com.matzip.server.global.common.exception.ErrorType;
import com.matzip.server.global.common.exception.InvalidRequestException;

public class DeleteActiveUserException extends InvalidRequestException {
    public DeleteActiveUserException() {
        super(ErrorType.DELETE_ACTIVE_USER, "Active user cannot be deleted.");
    }
}
