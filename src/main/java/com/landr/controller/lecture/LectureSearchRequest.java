package com.landr.controller.lecture;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class LectureSearchRequest {

    private String search;
    private Long cursorLectureId;
    private LocalDateTime cursorCreatedAt;
    private Integer offset = 10;
}

