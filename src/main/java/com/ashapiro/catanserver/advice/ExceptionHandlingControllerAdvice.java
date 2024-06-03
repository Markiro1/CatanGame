package com.ashapiro.catanserver.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.stream.Collectors;

@ControllerAdvice
public class ExceptionHandlingControllerAdvice {

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ProblemDetail> handleBindException(BindException exception) {
        ProblemDetail problemDetail = ProblemDetail
                .forStatusAndDetail(HttpStatus.BAD_REQUEST,
                        "Validation error");
        problemDetail.setProperty("errors",
                exception.getFieldErrors().stream()
                        .collect(Collectors.groupingBy(FieldError::getField,
                                Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList()))));

        return ResponseEntity.badRequest()
                .body(problemDetail);
    }
}
