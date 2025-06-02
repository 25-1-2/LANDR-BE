package com.landr.repository.lecture;

import com.landr.controller.lecture.LectureSearchRequest;
import com.landr.domain.lecture.Lecture;

import java.util.List;

public interface LectureRepositoryCustom {
    List<Lecture> findLatestLecturesWithCursor(LectureSearchRequest req);
    List<Lecture> findLatestLecturesBySearch(LectureSearchRequest req);
}
