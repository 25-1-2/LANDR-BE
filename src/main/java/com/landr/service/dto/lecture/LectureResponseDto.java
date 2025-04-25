package com.landr.service.dto.lecture;

import com.landr.domain.lecture.Lecture;
import com.landr.domain.lecture.Platform;
import com.landr.domain.lecture.Subject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class LectureResponseDto {
    @Schema(description = "강의 ID")
    private Long id;

    @Schema(description = "강의 제목")
    private String title;

    @Schema(description = "선생님 이름")
    private String teacher;

    @Schema(description = "플랫폼명")
    private Platform platform;

    @Schema(description = "과목명")
    private Subject subject;

    @Schema(description = "강의 생성일자")
    private LocalDateTime createdAt;

    public static LectureResponseDto from(Lecture lecture, Long planCount) {
        return LectureResponseDto.builder()
                .id(lecture.getId())
                .title(lecture.getTitle())
                .teacher(lecture.getTeacher())
                .platform(lecture.getPlatform())
                .subject(lecture.getSubject())
                .createdAt(lecture.getCreatedAt())
                .build();
    }
}
