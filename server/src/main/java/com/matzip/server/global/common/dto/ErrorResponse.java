package com.matzip.server.global.common.dto;

import com.matzip.server.global.common.exception.MatzipException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ErrorResponse {
    private int errorCode;
    private String errorType;
    private String detail;

    public ErrorResponse(MatzipException matzipException) {
        errorCode = matzipException.getErrorCode();
        errorType = matzipException.getErrorType().toString();
        detail = matzipException.getDetail();
    }
}
