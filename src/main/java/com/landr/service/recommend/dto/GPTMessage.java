package com.landr.service.recommend.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GPTMessage {
    private String role; // "system", "user"
    private String content;
}
