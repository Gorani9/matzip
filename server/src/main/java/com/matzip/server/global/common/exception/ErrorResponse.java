package com.matzip.server.global.common.exception;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ErrorResponse {
    @JsonProperty("error_code")
    int errorCode;
    @JsonProperty("error_message")
    String errorMessage;
    @JsonProperty("detail")
    String detail;

    public ErrorResponse(int errorCode, String errorMessage, String detail) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.detail = detail;
    }

    public ErrorResponse(int errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.detail = "";
    }
}
