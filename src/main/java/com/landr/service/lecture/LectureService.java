package com.landr.service.lecture;

import com.landr.controller.lecture.LectureSearchRequest;
import com.landr.domain.lecture.Lecture;
import com.landr.repository.lecture.LectureRepository;
import com.landr.service.dto.lecture.CursorPageResponseDto;
import com.landr.service.dto.lecture.LectureResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LectureService {

    private final LectureRepository lectureRepository;

    /**
     * 최신순 강의 목록 조회 (커서 기반 페이지네이션)
     */
    public CursorPageResponseDto<LectureResponseDto> getLatestLectures(LectureSearchRequest req) {
        List<Lecture> results = lectureRepository.findLatestLecturesWithCursor(req);

        boolean hasNext = results.size() > req.getOffset();
        List<Lecture> page = results.stream().limit(req.getOffset()).toList();

        Long nextCursorId = hasNext ? page.get(page.size() - 1).getId() : null;
        LocalDateTime nextCursorCreatedAt = hasNext ? page.get(page.size() - 1).getCreatedAt() : null;

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
    public CursorPageResponseDto<LectureResponseDto> searchLatestLectures(LectureSearchRequest req) {
        List<Lecture> results = lectureRepository.findLatestLecturesBySearch(req);

        boolean hasNext = results.size() > req.getOffset();
        List<Lecture> page = results.stream().limit(req.getOffset()).toList();

        Long nextCursorId = hasNext ? page.get(page.size() - 1).getId() : null;
        LocalDateTime nextCursorCreatedAt = hasNext ? page.get(page.size() - 1).getCreatedAt() : null;

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
}
