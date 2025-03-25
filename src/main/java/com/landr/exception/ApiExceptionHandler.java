package com.landr.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@Order(value = Integer.MIN_VALUE)
public class ApiExceptionHandler {

    // ApiException 클래스로 발생하는 모든 예외는 이 handleApiException 메서드가 처리
    @ExceptionHandler(value = ApiException.class)
    public ResponseEntity<ExceptionResponse> handleApiException(ApiException ex) {
        // 1. 로그찍고
        log.error("", ex);

        // 2. 발생한 예외에서 errorCode 가져와서
        ExceptionType errorCode = ex.getExceptionType();
        String errorDescription = ex.getErrorDescription();
        if (errorDescription == null) {
            errorDescription = errorCode.getDescription();
        }
        // 3. ExceptionResponse 만들기(에러 메시지 내용과 함께)
        ExceptionResponse exRes = ExceptionResponse.builder()
            .errorCode(errorCode.getErrorCode())
            .errorMessage(errorDescription)
            .build();

        return ResponseEntity
            .status(errorCode.getHttpStatusCode())
            .body(exRes);
    }

    /**
     * DTO 등에서 validation 실패 시, 해당 예외 처리하는 핸들러
     * NotNull, NotBlank 등의 애노테이션 검증 실패 시 해당 예외 처리해줌
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleMethodArgumentNotValidException(
        MethodArgumentNotValidException ex) {
        log.error("Method argument not valid exception", ex);

        FieldError fieldError = ex.getBindingResult().getFieldError();
        String errorMessage = (fieldError != null) ? fieldError.getDefaultMessage() : "Validation error";

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ExceptionResponse.builder()
                    .errorCode(40000)
                    .errorMessage(errorMessage)
                    .build()
            );
    }

    // 예상치 못한 예외에 대응하기 위한 Exception handler
    // TODO : 최종 어플리케이션 배포 시, ex.getMessage() 대신 "SERVER ERROR"로 수정
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleException(Exception ex) {
        return ResponseEntity
            .status(ExceptionType.SERVER_ERROR.getHttpStatusCode())
            .body(new ExceptionResponse(500, ex.getMessage()));
    }
}
