package com.barrcon.patchy.config;


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
