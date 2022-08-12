package com.matzip.server.domain.image.exception;

import com.matzip.server.global.common.exception.ErrorType;
import com.matzip.server.global.common.exception.ServerErrorException;

public class FileUploadException extends ServerErrorException {
    public FileUploadException() {
        super(ErrorType.FILE_UPLOAD_FAIL, "Failed to upload files.");
    }
}
