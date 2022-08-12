package com.matzip.server.domain.image.exception;

import com.matzip.server.global.common.exception.DataNotFoundException;
import com.matzip.server.global.common.exception.ErrorType;

public class ImageDeleteByUnauthorizedUserException extends DataNotFoundException {
    public ImageDeleteByUnauthorizedUserException() {
        super(ErrorType.IMAGE_DELETE_BY_UNAUTHORIZED_USER, "Unauthorized to delete this file.");
    }
}
