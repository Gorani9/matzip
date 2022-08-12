package com.matzip.server.domain.image.exception;

import com.matzip.server.global.common.exception.ErrorType;
import com.matzip.server.global.common.exception.ServerErrorException;

public class FileDeleteException extends ServerErrorException {
    public FileDeleteException() {
        super(ErrorType.FILE_DELETE_FAIL, "Failed to delete files.");
    }
}
