package com.matzip.server.global.common.exception;

public class IOException extends ServerErrorException {
    public IOException() {
        super(ErrorType.SERVER_ERROR, "IO Exception.");
    }
}
