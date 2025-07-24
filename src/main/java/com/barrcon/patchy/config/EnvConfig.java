package com.barrcon.patchy.config;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
@Order(1)
public class EnvConfig {

    @PostConstruct
    public void loadEnv() {
        Dotenv dotenv = Dotenv.configure()

                .filename("app.env")
                .ignoreIfMissing()
                .load();
        System.setProperty("GOOGLE_API_KEY", dotenv.get("GOOGLE_API_KEY"));
    }
}