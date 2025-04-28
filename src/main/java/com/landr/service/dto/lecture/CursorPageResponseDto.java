package com.landr.service.dto.lecture;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class CursorPageResponseDto<T> {
    private List<T> data;
    private String nextCursor;
    private LocalDateTime nextCreatedAt;
    private boolean hasNext;
}
