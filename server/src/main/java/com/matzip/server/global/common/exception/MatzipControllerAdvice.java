package com.matzip.server.global.common.exception;

import com.matzip.server.global.common.dto.ErrorResponse;
import com.matzip.server.global.common.exception.MatzipException.*;
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.validation.ConstraintViolationException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class MatzipControllerAdvice {
    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ErrorResponse> badRequest(MatzipException e) {
        return new ResponseEntity<>(new ErrorResponse(e), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> invalidFields(MethodArgumentNotValidException e) {
        return new ResponseEntity<>(new ErrorResponse(
                ErrorType.BadRequest.INVALID_REQUEST_BODY.getCode(),
                ErrorType.BadRequest.INVALID_REQUEST_BODY.name(),
                e.getFieldErrors().stream().map(it -> it.getField() + " " + it.getDefaultMessage() + ".")
                        .collect(Collectors.joining(" "))
        ), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> bindException(BindException e) {
        return new ResponseEntity<>(new ErrorResponse(
                ErrorType.BadRequest.INVALID_REQUEST_BODY.getCode(),
                ErrorType.BadRequest.INVALID_REQUEST_BODY.name(),
                e.getFieldErrors().stream().map(it -> it.getField() + " " + it.getDefaultMessage() + ".")
                        .collect(Collectors.joining(" "))
        ), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> jsonParseError() {
        return new ResponseEntity<>(new ErrorResponse(
                ErrorType.BadRequest.INVALID_REQUEST_BODY.getCode(),
                "INVALID_FIELD_TYPE",
                "Check your request body fields type."
        ), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> constraintViolation(ConstraintViolationException e) {
        return new ResponseEntity<>(new ErrorResponse(
                ErrorType.BadRequest.INVALID_PARAMETER.getCode(),
                ErrorType.BadRequest.INVALID_PARAMETER.name(),
                e.getConstraintViolations().stream().map(it -> {
                    String[] s = it.getPropertyPath().toString().split("\\.");
                    return s[s.length - 1] + " " + it.getMessage() + ", but the input was '" + it.getInvalidValue() + "'.";
                }).collect(Collectors.joining(" "))
        ), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> argumentTypeMismatch(MethodArgumentTypeMismatchException e) {
        return new ResponseEntity<>(new ErrorResponse(
                ErrorType.BadRequest.INVALID_PARAMETER.getCode(),
                "INVALID_PARAMETER_TYPE",
                "Parameter '" + e.getName() + "' is not in valid type."
        ), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> missingParameter(MissingServletRequestParameterException e) {
        return new ResponseEntity<>(new ErrorResponse(
                ErrorType.BadRequest.INVALID_PARAMETER.getCode(),
                "MISSING_PARAMETER",
                "Parameter '" + e.getParameterName() + "' is missing."
        ), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(FileSizeLimitExceededException.class)
    public ResponseEntity<ErrorResponse> fileSizeLimitExceeded() {
        return new ResponseEntity<>(new ErrorResponse(
                ErrorType.BadRequest.FILE_SIZE_LIMIT_EXCEEDED.getCode(),
                "FILE_SIZE_LIMIT_EXCEEDED",
                "File size limit exceeded. Maximum file size is 10MB."
        ), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> unauthorized(MatzipException e) {
        return new ResponseEntity<>(new ErrorResponse(e), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> forbidden(MatzipException e) {
        return new ResponseEntity<>(new ErrorResponse(e), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> notFound(MatzipException e) {
        return new ResponseEntity<>(new ErrorResponse(e), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> conflict(MatzipException e) {
        return new ResponseEntity<>(new ErrorResponse(e), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ServerErrorException.class)
    public ResponseEntity<ErrorResponse> serverError(MatzipException e) {
        return new ResponseEntity<>(new ErrorResponse(e), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
