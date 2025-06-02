package com.landr.service.recommend;

import com.landr.controller.dto.LectureRecommendResponse;
import com.landr.domain.lecture.Lecture;
import org.springframework.stereotype.Component;

@Component
public class LectureMapper {

    public LectureRecommendResponse toDto(Lecture lecture) {
        return LectureRecommendResponse.builder()
                .id(lecture.getId())
                .platform(lecture.getPlatform().name())
                .title(lecture.getTitle())
                .teacher(lecture.getTeacher())
                .url(generateLectureUrl(lecture))
                .description(lecture.getTag() != null ? lecture.getTag() : "")
                .build();
    }

    private static String generateLectureUrl(Lecture lecture) {
        switch (lecture.getPlatform()) {
            case MEGA:
                return "https://www.megastudy.net/lecture/" + lecture.getId();
            case ETOOS:
                return "https://www.etoos.com/lecture/" + lecture.getId();
            case EBSI:
                return "https://www.ebs.co.kr/lecture/" + lecture.getId();
            case DAESANG:
                return "https://www.daesangedu.com/lecture/" + lecture.getId();
            default:
                return "https://example.com/lecture/" + lecture.getId();
        }
    }
}