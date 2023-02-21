package com.matzip.server.domain.image.exception;

import com.matzip.server.global.common.exception.ErrorType;
import com.matzip.server.global.common.exception.MatzipException;

public class FileDeleteException extends MatzipException.ServerErrorException {
    public FileDeleteException() {
        super(ErrorType.ServerError.FILE_DELETE_FAIL, "Failed to delete files.");
    }
}
