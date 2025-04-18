package com.landr.repository.lecture.dto;

import com.landr.domain.lecture.Lecture;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LectureWithPlanCount {
    private Lecture lecture;
    private Long planCount;
}

