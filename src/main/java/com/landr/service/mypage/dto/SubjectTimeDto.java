package com.landr.service.mypage.dto;

import com.landr.domain.lecture.Subject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectTimeDto {
    private Subject subject;
    private long totalMinutes;
    private double percentage;
}
