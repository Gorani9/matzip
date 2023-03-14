package com.matzip.server.domain.image.exception;

import com.matzip.server.global.common.exception.ErrorType;
import com.matzip.server.global.common.exception.MatzipException;

public class FileUploadException extends MatzipException.ServerErrorException {
    public FileUploadException() {
        super(ErrorType.ServerError.FILE_UPLOAD_FAIL, "Failed to upload files.");
    }
}
