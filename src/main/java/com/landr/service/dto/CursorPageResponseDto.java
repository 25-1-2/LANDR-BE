package com.landr.service.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CursorPageResponseDto<T> {
    private List<T> data;
    private Long nextCursor;
    private boolean hasNext;
}
