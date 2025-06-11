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
    PLAN_ALL_LESSONS_COMPLETED(
        40012, HttpStatus.BAD_REQUEST, "모든 강의가 이미 완료되었습니다."),
    PLAN_END_DATE_PASSED(40013, HttpStatus.BAD_REQUEST, "계획의 종료일이 이미 지났습니다. 종료일을 업데이트한 후 다시 시도해주세요."),


    // 인증 관련(에러 번호 2로 시작)
    AUTHENTICATION_FAILED(40121, HttpStatus.UNAUTHORIZED, "인증에 실패했습니다."),
    INVALID_TOKEN(40122, HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(40123, HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    TOKEN_NOT_FOUND(40124, HttpStatus.UNAUTHORIZED, "토큰이 존재하지 않습니다."),
    TOKEN_NOT_FOUND_USER(40125, HttpStatus.CONFLICT, "토큰에 해당하는 사용자를 찾을 수 없습니다."),
    // 접근 권한 관련
    UNAUTHORIZED_ACCESS(40126, HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

    // LessonSchedule 관련 (에러 번호 3로 시작)
    LESSON_SCHEDULE_NOT_FOUND(40431, HttpStatus.NOT_FOUND, "해당 강의 스케줄을 찾을 수 없습니다."),

    // Lecture 관련 (에러 번호 5로 시작)
    LECTURE_NOT_FOUND(40451, HttpStatus.NOT_FOUND, "해당 Lecture를 찾을 수 없습니다."),

    // Lesson 관련 (에러 번호 6로 시작)
    LESSON_NOT_FOUND(40461, HttpStatus.NOT_FOUND, "해당 Lesson을 찾을 수 없습니다."),

    // User 관련 (에러 번호 7로 시작)
    INVALID_USER_NAME(40471, HttpStatus.BAD_REQUEST, "유효하지 않은 사용자 이름입니다."),
    USER_NOT_FOUND(40472, HttpStatus.BAD_REQUEST, "해당 사용자를 찾을 수 없습니다."),

    // DDay 관련 (에러 번호 8로 시작),
    DDAY_NOT_FOUND(40481, HttpStatus.NOT_FOUND, "해당 D-Day를 찾을 수 없습니다."),
    DDAY_OWNER_NOT_MATCH(40382, HttpStatus.FORBIDDEN, "해당 D-Day의 소유자가 아닙니다."),

    // StudyGroup 관련 (에러 번호 9로 시작)
    STUDY_GROUP_NOT_FOUND(40491, HttpStatus.NOT_FOUND, "해당 스터디 그룹을 찾을 수 없습니다."),
    STUDY_GROUP_ALREADY_EXISTS(40992, HttpStatus.CONFLICT, "이미 해당 계획으로 생성된 스터디 그룹이 있습니다."),
    STUDY_GROUP_NOT_LEADER(40393, HttpStatus.FORBIDDEN, "스터디 그룹의 방장만 수행할 수 있는 작업입니다."),
    STUDY_GROUP_ALREADY_JOINED(40994, HttpStatus.CONFLICT, "이미 해당 스터디 그룹에 가입되어 있습니다."),
    STUDY_GROUP_INVALID_INVITE_CODE(40495, HttpStatus.NOT_FOUND, "유효하지 않은 초대 코드입니다."),

    BAD_REQUEST(400, HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),;


    private final int errorCode;    // errorCode는 5자리로 구성(3자리는 Http status code + 2자리 에러번호)
    private final HttpStatus httpStatusCode;
    private final String description;
}