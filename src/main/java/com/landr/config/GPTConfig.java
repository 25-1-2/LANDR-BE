package com.landr.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "openai")
@Getter
@Setter
public class GPTConfig {
    private String apiKey;
    private String model = "gpt-3.5-turbo";
    private String baseUrl = "https://api.openai.com/v1";
    private int maxTokens = 800;
    private double temperature = 0.3;
}