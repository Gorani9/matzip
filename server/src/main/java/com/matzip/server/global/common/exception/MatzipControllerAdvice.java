package com.matzip.server.global.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import javax.validation.ConstraintViolationException;

@RestControllerAdvice
public class MatzipControllerAdvice {
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @ExceptionHandler(value=InvalidRequestException.class)
    public ResponseEntity<ErrorResponse> badRequest(MatzipException e) {
        logger.info(e.detail);
        return new ResponseEntity<>(new ErrorResponse(e.errorType, e.detail), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value=ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> constraintViolation(ConstraintViolationException e) {
        logger.info(e.getMessage());
        return new ResponseEntity<>(
                new ErrorResponse(ErrorType.INVALID_REQUEST, e.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> maxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        logger.info(e.getMessage());
        return new ResponseEntity<>(
                new ErrorResponse(ErrorType.FILE_TOO_LARGE, e.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ErrorResponse> multipartException(MultipartException e) {
        logger.info(e.getMessage());
        return new ResponseEntity<>(
                new ErrorResponse(ErrorType.FILE_UPLOAD_FAIL, e.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value=NotAllowedException.class)
    public ResponseEntity<ErrorResponse> notAllowed(MatzipException e) {
        logger.info(e.detail);
        return new ResponseEntity<>(new ErrorResponse(e.errorType, e.detail), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(value=DataNotFoundException.class)
    public ResponseEntity<ErrorResponse> notFound(MatzipException e) {
        logger.info(e.detail);
        return new ResponseEntity<>(new ErrorResponse(e.errorType, e.detail), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value=ConflictException.class)
    public ResponseEntity<ErrorResponse> conflict(MatzipException e) {
        logger.info(e.detail);
        return new ResponseEntity<>(new ErrorResponse(e.errorType, e.detail), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(value=ServerErrorException.class)
    public ResponseEntity<ErrorResponse> serverError(MatzipException e) {
        logger.info(e.detail);
        return new ResponseEntity<>(new ErrorResponse(e.errorType, e.detail), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
