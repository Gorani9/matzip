package com.matzip.server.global.common.exception;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ErrorResponse {
    @JsonProperty("error_code")
    int errorCode;
    @JsonProperty("error_message")
    String errorMessage;
    @JsonProperty("detail")
    String detail;

    public ErrorResponse(ErrorType errorType, String errorMessage) {
        this.errorCode = errorType.getErrorCode();
        this.errorMessage = errorType.name();
        this.detail = errorMessage;
    }
}
