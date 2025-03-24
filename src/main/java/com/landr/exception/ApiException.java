package com.landr.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiException extends RuntimeException{

    private final ExceptionType exceptionType;
    private final String errorDescription;

    public ApiException(ExceptionType exceptionType) {
        this.exceptionType = exceptionType;
        this.errorDescription = exceptionType.getDescription();
    }

    public ApiException(ExceptionType exceptionType, Throwable ex) {
        super(ex);
        this.exceptionType = exceptionType;
        this.errorDescription = exceptionType.getDescription();
    }
}
