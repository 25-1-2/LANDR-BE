package com.landr.repository.lecture;

import com.landr.controller.lecture.dto.LectureSearchRequest;
import com.landr.domain.lecture.Lecture;
import com.landr.repository.lecture.dto.LectureWithPlanCount;

import java.util.List;

public interface LectureRepositoryCustom {
    List<Lecture> findBySearchWithCursor(LectureSearchRequest request);
    List<LectureWithPlanCount> findOrderByPlanCount(LectureSearchRequest request);
}

