package com.barrcon.patchy.config;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EnvConfig {
    @PostConstruct
    public void init() {
        Dotenv dotenv = Dotenv.configure()
                .directory("src")
                .filename("app.env")
                .load();
        System.setProperty("GEMINI_API_KEY", dotenv.get("GEMINI_API_KEY"));
    }
}