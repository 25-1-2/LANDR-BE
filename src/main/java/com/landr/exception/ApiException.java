package com.landr.exception;

import lombok.Getter;

@Getter
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

    // 인증 관련 예외를 위한 새로운 생성자 추가
    public ApiException(ExceptionType exceptionType, String message) {
        super(message);
        this.exceptionType = exceptionType;
        this.errorDescription = message;
    }
}
