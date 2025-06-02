package com.landr.service.recommend.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class GPTResponse {
    private List<Choice> choices;

    @Getter
    public static class Choice {
        private GPTMessage message;
    }
}
