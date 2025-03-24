package com.landr.service.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProgressDto {

    private List<LectureProgressDto> lectureProgress;
    private int totalCompletedLessons;
    private int totalLessons;
}