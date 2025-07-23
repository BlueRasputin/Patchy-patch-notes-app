package com.barrcon.patchy.config;

import com.google.cloud.ai.generativeai.GenerativeModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiConfig {
    @Value("${gemini.api.key}")
    private String apiKey;

    @Bean
    public GenerativeModel geminiModel() {
        return GenerativeModel.builder();
                .model("gemini-1.5-flash")
                .apiKey(apiKey)
                .build();
    }
}
