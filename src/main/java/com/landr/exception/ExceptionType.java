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

    // 인증 관련(에러 번호 2로 시작)
    AUTHENTICATION_FAILED(40121, HttpStatus.UNAUTHORIZED, "인증에 실패했습니다."),
    INVALID_TOKEN(40122, HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(40123, HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    TOKEN_NOT_FOUND(40124, HttpStatus.UNAUTHORIZED, "토큰이 존재하지 않습니다."),
    TOKEN_NOT_FOUND_USER(40125, HttpStatus.CONFLICT, "토큰에 해당하는 사용자를 찾을 수 없습니다."),
    // 접근 권한 관련
    UNAUTHORIZED_ACCESS(40126, HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

    SERVER_ERROR(50001, HttpStatus.INTERNAL_SERVER_ERROR, "알 수 없는 서버 에러 입니다."),



    // LessonSchedule 관련 (에러 번호 3로 시작)
    LESSON_SCHEDULE_NOT_FOUND(40431, HttpStatus.NOT_FOUND, "해당 강의 스케줄을 찾을 수 없습니다.")
    ;
    private final int errorCode;    // errorCode는 5자리로 구성(3자리는 Http status code + 2자리 에러번호)
    private final HttpStatus httpStatusCode;
    private final String description;
}