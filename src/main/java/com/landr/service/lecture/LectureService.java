package com.landr.service.lecture;

import com.landr.controller.lecture.LectureSearchRequest;
import com.landr.controller.lecture.dto.LessonDto;
import com.landr.controller.lecture.dto.LessonsResponseDto;
import com.landr.domain.lecture.Lecture;
import com.landr.domain.lecture.Lesson;
import com.landr.repository.lecture.LectureRepository;
import com.landr.repository.lesson.LessonRepository;
import com.landr.service.dto.lecture.CursorPageResponseDto;
import com.landr.service.dto.lecture.LectureResponseDto;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LectureService {

    private final LectureRepository lectureRepository;
    private final LessonRepository lessonRepository;

    /**
     * 최신순 강의 목록 조회 (커서 기반 페이지네이션)
     */
    public CursorPageResponseDto<LectureResponseDto> getLatestLectures(LectureSearchRequest req) {
        List<Lecture> results = lectureRepository.findLatestLecturesWithCursor(req);

        boolean hasNext = results.size() > req.getOffset();
        List<Lecture> page = results.stream().limit(req.getOffset()).toList();

        Long nextCursorId = hasNext ? page.get(page.size() - 1).getId() : null;
        LocalDateTime nextCursorCreatedAt =
            hasNext ? page.get(page.size() - 1).getCreatedAt() : null;

        List<LectureResponseDto> dtos = page.stream()
            .map(l -> LectureResponseDto.from(l, null))
            .toList();

        return CursorPageResponseDto.<LectureResponseDto>builder()
            .data(dtos)
            .nextCursor(nextCursorId)
            .nextCreatedAt(nextCursorCreatedAt)
            .hasNext(hasNext)
            .build();
    }

    /**
     * 검색된 강의 목록 최신순 조회
     */
    public CursorPageResponseDto<LectureResponseDto> searchLatestLectures(
        LectureSearchRequest req) {
        List<Lecture> results = lectureRepository.findLatestLecturesBySearch(req);

        boolean hasNext = results.size() > req.getOffset();
        List<Lecture> page = results.stream().limit(req.getOffset()).toList();

        Long nextCursorId = hasNext ? page.get(page.size() - 1).getId() : null;
        LocalDateTime nextCursorCreatedAt =
            hasNext ? page.get(page.size() - 1).getCreatedAt() : null;

        List<LectureResponseDto> dtos = page.stream()
            .map(l -> LectureResponseDto.from(l, null))
            .toList();

        return CursorPageResponseDto.<LectureResponseDto>builder()
            .data(dtos)
            .nextCursor(nextCursorId)
            .nextCreatedAt(nextCursorCreatedAt)
            .hasNext(hasNext)
            .build();
    }

    @Transactional(readOnly = true)
    public LessonsResponseDto getLessonsByLectureId(Long lectureId) {
        List<Lesson> lessons = lessonRepository.findLessonsByLectureId(lectureId);
        List<LessonDto> lessonDtoList = lessons.stream()
            .map(lesson -> LessonDto.builder()
                .id(lesson.getId())
                .title(lesson.getTitle())
                .build()
            ).toList();

        return LessonsResponseDto.builder()
            .lessons(lessonDtoList)
            .build();
    }
}
