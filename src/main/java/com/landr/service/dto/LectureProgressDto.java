package com.landr.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LectureProgressDto {

    private Long lectureId;
    private String lectureName;
    private int completedLessons;
    private int totalLessons;
}
