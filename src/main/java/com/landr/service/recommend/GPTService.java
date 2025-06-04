package com.landr.service.recommend;

import com.landr.config.GPTConfig;
import com.landr.service.recommend.dto.GPTMessage;
import com.landr.service.recommend.dto.GPTRequest;
import com.landr.service.recommend.dto.GPTResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GPTService {

    private final GPTConfig gptConfig;
    private final RestTemplate restTemplate;

    public String getRecommendation(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(gptConfig.getApiKey());

            GPTRequest request = GPTRequest.builder()
                    .model(gptConfig.getModel())
                    .messages(List.of(
                            GPTMessage.builder()
                                    .role("system")
                                    .content("당신은 한국의 교육 전문가입니다. 학생의 정보를 바탕으로 가장 적합한 강의를 추천해주세요.")
                                    .build(),
                            GPTMessage.builder()
                                    .role("user")
                                    .content(prompt)
                                    .build()
                    ))
                    .max_tokens(gptConfig.getMaxTokens())
                    .temperature(gptConfig.getTemperature())
                    .build();

            HttpEntity<GPTRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<GPTResponse> response = restTemplate.postForEntity(
                    gptConfig.getBaseUrl() + "/chat/completions",
                    entity,
                    GPTResponse.class
            );

            return response.getBody().getChoices().get(0).getMessage().getContent();

        } catch (Exception e) {
            log.error("GPT API 호출 실패", e);
            throw new RuntimeException("AI 추천 시스템 오류", e);
        }
    }
}
