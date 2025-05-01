package com.landr.service.dto.lecture;

import com.landr.domain.lecture.Lecture;
import com.landr.domain.lecture.Platform;
import com.landr.domain.lecture.Subject;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class LectureResponseDto {

    private Long id;
    private String title;
    private String teacher;
    private Platform platform;
    private Subject subject;
    private LocalDateTime createdAt;
    private String tag;
    private int totalLessons;

    public static LectureResponseDto from(Lecture lecture, Long planCount) {
        return LectureResponseDto.builder()
                .id(lecture.getId())
                .title(lecture.getTitle())
                .teacher(lecture.getTeacher())
                .platform(lecture.getPlatform())
                .subject(lecture.getSubject())
                .createdAt(lecture.getCreatedAt())
                .tag(lecture.getTag())
                .totalLessons(lecture.getTotalLessons())
                .build();
    }
}
