package com.landr.service.lecture;

import com.landr.controller.lecture.dto.LectureSearchRequest;
import com.landr.domain.lecture.Lecture;
import com.landr.repository.lecture.LectureRepository;
import com.landr.repository.lecture.dto.LectureWithPlanCount;
import com.landr.service.dto.CursorPageResponseDto;
import com.landr.service.dto.LectureResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class LectureService {

    private final LectureRepository lectureRepository;

    public CursorPageResponseDto<LectureResponseDto> searchLectures(LectureSearchRequest request) {
        if (Boolean.TRUE.equals(request.getSortByPlan())) {
            return searchByPlanCount(request);
        } else {
            return searchByRecent(request);
        }
    }

    private CursorPageResponseDto<LectureResponseDto> searchByRecent(LectureSearchRequest request) {
        List<Lecture> lectures = lectureRepository.findBySearchWithCursor(request);
        boolean hasNext = lectures.size() > request.getOffset();
        List<Lecture> page = lectures.stream().limit(request.getOffset()).toList();

        List<LectureResponseDto> dtos = page.stream()
                .map(l -> LectureResponseDto.from(l, null))
                .toList();

        Long nextCursor = hasNext ? page.get(page.size() - 1).getId() : null;

        return CursorPageResponseDto.<LectureResponseDto>builder()
                .data(dtos)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .build();
    }

    private CursorPageResponseDto<LectureResponseDto> searchByPlanCount(LectureSearchRequest request) {
        List<LectureWithPlanCount> results = lectureRepository.findOrderByPlanCount(request);
        boolean hasNext = results.size() > request.getOffset();
        List<LectureWithPlanCount> page = results.stream().limit(request.getOffset()).toList();

        List<LectureResponseDto> dtos = page.stream()
                .map(lpc -> LectureResponseDto.from(lpc.getLecture(), lpc.getPlanCount()))
                .toList();

        Long nextCursor = hasNext ? page.get(page.size() - 1).getLecture().getId() : null;

        return CursorPageResponseDto.<LectureResponseDto>builder()
                .data(dtos)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .build();
    }
}

