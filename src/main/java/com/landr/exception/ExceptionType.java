package com.landr.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ExceptionType {

    // Schedule 관련 (에러 번호 0으로 시작)
    SCHEDULE_NOT_FOUND(40401, HttpStatus.NOT_FOUND, "해당 계획을 찾을 수 없습니다."),

    // Plan 관련 (에러 번호 1로 시작)
    PLAN_NOT_FOUND(40411, HttpStatus.NOT_FOUND, "해당 Plan을 찾을 수 없습니다."),

    SERVER_ERROR(50001, HttpStatus.INTERNAL_SERVER_ERROR, "알 수 없는 서버 에러 입니다.");

    private final int errorCode;    // errorCode는 5자리로 구성(3자리는 Http status code + 2자리 에러번호)
    private final HttpStatus httpStatusCode;
    private final String description;
}
