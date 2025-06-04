package com.landr.service.recommend.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GPTRequest {
    private String model;
    private List<GPTMessage> messages;
    private int max_tokens;
    private double temperature;
}
